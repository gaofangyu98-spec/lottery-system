package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.CreatePrizeByActivityParam;
import com.example.lotterysystem.controller.param.CreateUserByActivityParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 活动管理服务实现类
 * <p>
 * 核心职责：
 * 1. 创建活动（活动信息 + 关联奖品 + 关联参与人员，多表事务写入）
 * 2. 活动分页列表查询
 * 3. 活动详情查询（优先 Redis 缓存，未命中查库并回写）
 * 4. 活动缓存手动刷新
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

    /**
     * 活动缓存 key 前缀，为了区分业务
     */
    private final String ACTIVITY_PREFIX = "ACTIVITY_";
    /**
     * 活动缓存过期时间（3 天）
     */
    private final Long ACTIVITY_TIMEOUT = 60 * 60 * 24 * 3L;


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 创建活动：活动基本信息 + 关联奖品 + 关联参与人员，三表事务写入
     * <p>
     * 事务说明：涉及 activity、activity_prize、activity_user 三张表的写入，
     * 任意一步失败则全部回滚。创建完成后将完整活动信息写入 Redis 缓存。
     *
     * @param param 创建活动参数
     * @return 创建结果（含活动 ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateActivityDTO createActivity(CreateActivityParam param) {
        // 校验活动信息是否正确（人员/奖品是否存在、人数是否>=奖品总数等）
        checkActivityInfo(param);

        // 保存活动信息（初始状态为 RUNNING）
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(param.getActivityName());
        activityDO.setDescription(param.getDescription());
        activityDO.setStatus(ActivityStatusEnum.RUNNING.name());
        activityMapper.insert(activityDO);

        // 保存活动关联的奖品信息（初始状态为 INIT）
        List<CreatePrizeByActivityParam> prizeParams = param.getActivityPrizeList();
        List<ActivityPrizeDO> activityPrizeDOList = prizeParams.stream()
                .map(prizeParam -> {
                    ActivityPrizeDO activityPrizeDO = new ActivityPrizeDO();
                    activityPrizeDO.setActivityId(activityDO.getId());
                    activityPrizeDO.setPrizeId(prizeParam.getPrizeId());
                    activityPrizeDO.setPrizeAmount(prizeParam.getPrizeAmount());
                    activityPrizeDO.setPrizeTiers(prizeParam.getPrizeTiers());
                    activityPrizeDO.setStatus(ActivityPrizeStatusEnum.INIT.name());
                    return activityPrizeDO;
                }).collect(Collectors.toList());
        activityPrizeMapper.batchInsert(activityPrizeDOList);

        // 保存活动关联的人员信息（初始状态为 INIT）
        List<CreateUserByActivityParam> userParams = param.getActivityUserList();
        List<ActivityUserDO> activityUserDOList = userParams.stream()
                .map(userParam -> {
                    ActivityUserDO activityUserDO = new ActivityUserDO();
                    activityUserDO.setActivityId(activityDO.getId());
                    activityUserDO.setUserId(userParam.getUserId());
                    activityUserDO.setUserName(userParam.getUserName());
                    activityUserDO.setStatus(ActivityUserStatusEnum.INIT.name());
                    return activityUserDO;
                }).collect(Collectors.toList());
        activityUserMapper.batchInsert(activityUserDOList);

        // 查询奖品基本属性（名称、图片、价格等），用于组装完整活动详情
        List<Long> prizeIds = param.getActivityPrizeList().stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);

        // 整合完整活动信息并写入 Redis 缓存
        ActivityDetailDTO detailDTO = convertToActivityDetailDTO(activityDO, activityUserDOList,
                prizeDOList, activityPrizeDOList);
        cacheActivity(detailDTO);

        // 构造返回
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setActivityId(activityDO.getId());
        return createActivityDTO;
    }

    /**
     * 活动分页列表查询
     *
     * @param param 分页参数
     * @return 分页结果（总量 + 当前页活动列表）
     */
    @Override
    public PageListDTO<ActivityDTO> findActivityList(PageListParam param) {
        int total = activityMapper.count();
        List<ActivityDO> activityDOList = activityMapper.selectActivityList(param.offset(), param.getPageSize());
        List<ActivityDTO> activityDTOList = activityDOList.stream()
                .map(activityDO -> {
                    ActivityDTO activityDTO = new ActivityDTO();
                    activityDTO.setActivityId(activityDO.getId());
                    activityDTO.setActivityName(activityDO.getActivityName());
                    activityDTO.setDescription(activityDO.getDescription());
                    activityDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));
                    return activityDTO;
                }).collect(Collectors.toList());
        return new PageListDTO<>(total, activityDTOList);
    }

    /**
     * 查询活动详情：优先查 Redis 缓存，未命中则查库并回写缓存
     *
     * @param activityId 活动 ID
     * @return 活动完整详情（含奖品列表 + 参与人员列表）
     */
    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        if (null == activityId) {
            logger.warn("查询活动详细信息失败，activityId为空！");
            return null;
        }
        // 查询 redis
        ActivityDetailDTO detailDTO = getActivityFromCache(activityId);
        if (null != detailDTO) {
            logger.info("查询活动详细信息成功！detailDTO={}",
                    JacksonUtil.writeValueAsString(detailDTO));
            return detailDTO;
        }

        // 如果redis不存在，查表
        ActivityDO aDO = activityMapper.selectById(activityId);
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);
        // 奖品表: 先获取要查询的奖品id
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);
        // 整合活动详细信息，存放redis
        detailDTO = convertToActivityDetailDTO(aDO, auDOList, pDOList, apDOList);
        cacheActivity(detailDTO);
        return detailDTO;
    }

    /**
     * 根据活动 ID 重新查询数据库并刷新 Redis 缓存
     * <p>
     * 在活动状态机流转完成后调用，保证缓存中的活动状态是最新的。
     *
     * @param activityId 活动 ID
     */
    @Override
    public void cacheActivity(Long activityId) {
        if (null == activityId) {
            logger.warn("要缓存的活动id为空！");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_IS_EMPTY);
        }

        ActivityDO aDO = activityMapper.selectById(activityId);
        if (null == aDO) {
            logger.error("要缓存的活动id有误！");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_ERROR);
        }
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> pDOList = prizeMapper.batchSelectByIds(prizeIds);
        cacheActivity(
                convertToActivityDetailDTO(aDO, auDOList,
                        pDOList, apDOList));
    }

    /**
     * 将完整活动详情写入 Redis 缓存
     *
     * @param detailDTO 活动详情 DTO
     */
    private void cacheActivity(ActivityDetailDTO detailDTO) {
        // key: ACTIVITY_12
        // value: ActivityDetailDTO(json)
        if (null == detailDTO || null == detailDTO.getActivityId()) {
            logger.warn("要缓存的活动信息不存在!");
            return;
        }

        try {
            redisUtil.set(ACTIVITY_PREFIX + detailDTO.getActivityId(),
                    JacksonUtil.writeValueAsString(detailDTO),
                    ACTIVITY_TIMEOUT);
        } catch (Exception e) {
            logger.error("缓存活动异常，ActivityDetailDTO={}",
                    JacksonUtil.writeValueAsString(detailDTO),
                    e);
        }
    }

    /**
     * 根据活动 ID 从 Redis 缓存中获取活动详情
     *
     * @param activityId 活动 ID
     * @return 活动详情 DTO，缓存未命中或异常时返回 null
     */
    private ActivityDetailDTO getActivityFromCache(Long activityId) {
        if (null == activityId) {
            logger.warn("获取缓存活动数据的activityId为空！");
            return null;
        }
        try {
            String str = redisUtil.get(ACTIVITY_PREFIX + activityId);
            if (!StringUtils.hasText(str)) {
                logger.info("获取的缓存活动数据为空！key={}", ACTIVITY_PREFIX + activityId);
                return null;
            }
            return JacksonUtil.readValue(str, ActivityDetailDTO.class);
        } catch (Exception e) {
            logger.error("从缓存中获取活动信息异常，key={}", ACTIVITY_PREFIX + activityId, e);
            return null;
        }

    }

    /**
     * 将分散的 DO 数据整合为完整的活动详情 DTO
     *
     * @param activityDO          活动基本信息
     * @param activityUserDOList  活动关联的人员列表
     * @param prizeDOList         奖品基本属性列表
     * @param activityPrizeDOList 活动关联的奖品列表
     * @return 活动详情 DTO
     */
    private ActivityDetailDTO convertToActivityDetailDTO(ActivityDO activityDO,
                                                         List<ActivityUserDO> activityUserDOList,
                                                         List<PrizeDO> prizeDOList,
                                                         List<ActivityPrizeDO> activityPrizeDOList) {
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        detailDTO.setActivityId(activityDO.getId());
        detailDTO.setActivityName(activityDO.getActivityName());
        detailDTO.setDesc(activityDO.getDescription());
        detailDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));

        // 将活动关联奖品与奖品基本属性按 prizeId 关联
        List<ActivityDetailDTO.PrizeDTO> prizeDTOList = activityPrizeDOList
                .stream()
                .map(apDO -> {
                    ActivityDetailDTO.PrizeDTO prizeDTO = new ActivityDetailDTO.PrizeDTO();
                    prizeDTO.setPrizeId(apDO.getPrizeId());
                    Optional<PrizeDO> optionalPrizeDO = prizeDOList.stream()
                            .filter(prizeDO -> prizeDO.getId().equals(apDO.getPrizeId()))
                            .findFirst();
                    optionalPrizeDO.ifPresent(prizeDO -> {
                        prizeDTO.setName(prizeDO.getName());
                        prizeDTO.setImageUrl(prizeDO.getImageUrl());
                        prizeDTO.setPrice(prizeDO.getPrice());
                        prizeDTO.setDescription(prizeDO.getDescription());
                    });
                    prizeDTO.setTiers(ActivityPrizeTiersEnum.forName(apDO.getPrizeTiers()));
                    prizeDTO.setPrizeAmount(apDO.getPrizeAmount());
                    prizeDTO.setStatus(ActivityPrizeStatusEnum.forName(apDO.getStatus()));
                    return prizeDTO;
                }).collect(Collectors.toList());
        detailDTO.setPrizeDTOList(prizeDTOList);

        List<ActivityDetailDTO.UserDTO> userDTOList = activityUserDOList.stream()
                .map(auDO -> {
                    ActivityDetailDTO.UserDTO userDTO = new ActivityDetailDTO.UserDTO();
                    userDTO.setUserId(auDO.getUserId());
                    userDTO.setUserName(auDO.getUserName());
                    userDTO.setStatus(ActivityUserStatusEnum.forName(auDO.getStatus()));
                    return userDTO;
                }).collect(Collectors.toList());
        detailDTO.setUserDTOList(userDTOList);
        return detailDTO;
    }

    /**
     * 校验创建活动的参数合法性
     * <p>
     * 校验维度：
     * - 参与人员 ID 是否在用户表中都存在
     * - 奖品 ID 是否在奖品表中都存在
     * - 参与人数 >= 奖品总数量（否则无法保证每人都有奖）
     * - 奖品等级是否合法
     *
     * @param param 创建活动参数
     */
    private void checkActivityInfo(CreateActivityParam param) {
        if (null == param) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_INFO_ERROR);
        }

        // 人员id在人员表中是否存在
        List<Long> userIds = param.getActivityUserList()
                .stream()
                .map(CreateUserByActivityParam::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existUserIds = userMapper.selectExistByIds(userIds);
        if (CollectionUtils.isEmpty(existUserIds)) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
        }
        userIds.forEach(id -> {
            if (!existUserIds.contains(id)) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
            }
        });

        // 奖品id在奖品表中是否存在
        List<Long> prizeIds = param.getActivityPrizeList()
                .stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existPrizeIds = prizeMapper.selectExistByIds(prizeIds);
        if (CollectionUtils.isEmpty(existPrizeIds)) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
        }
        prizeIds.forEach(id -> {
            if (!existPrizeIds.contains(id)) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
            }
        });

        // 人员数量必须大于等于奖品总数量（每人至少有一个奖品可抽）
        int userAmount = param.getActivityUserList().size();
        long prizeAmount = param.getActivityPrizeList()
                .stream()
                .mapToLong(CreatePrizeByActivityParam::getPrizeAmount)
                .sum();
        if (userAmount < prizeAmount) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_PRIZE_ERROR);
        }

        // 校验活动奖品等奖有效性
        param.getActivityPrizeList().forEach(prize -> {
            if (null == ActivityPrizeTiersEnum.forName(prize.getPrizeTiers())) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_TIERS_ERROR);
            }
        });

    }
}
