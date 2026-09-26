package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.controller.result.CreateActivityResult;
import com.example.lotterysystem.controller.result.FindActivityListResult;
import com.example.lotterysystem.controller.result.GetActivityDetailResult;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 活动管理接口
 * <p>提供创建活动、分页查询活动列表、查询活动详情（含奖品与参与人）能力。</p>
 */
@RestController
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    /**
     * 创建活动
     */
    @RequestMapping("/activity/create")
    public CommonResult<CreateActivityResult> createActivity(@Validated @RequestBody CreateActivityParam param) {
        logger.info("createActivity CreateActivityParam: ", JacksonUtil.writeValueAsString(param));
        CreateActivityDTO createActivityDTO = activityService.createActivity(param);
        return CommonResult.success(convertTOCreateActivityResult(createActivityDTO));
    }

    /**
     * 分页查询活动列表
     */
    @RequestMapping("/activity/find-list")
    public CommonResult<FindActivityListResult> findActivityList(PageListParam param) {
        logger.info("findActivityList param: {}", JacksonUtil.writeValueAsString(param));
        PageListDTO<ActivityDTO> pageListDTO = activityService.findActivityList(param);
        return CommonResult.success(convertToFindActivityListResult(pageListDTO));
    }

    /**
     * 查询活动详情
     */
    @RequestMapping("/activity-detail/find")
    public CommonResult<GetActivityDetailResult> getActivityDetail(Long activityId) {
        logger.info("getActivityDetail activityId:{}", activityId);
        ActivityDetailDTO detailDTO = activityService.getActivityDetail(activityId);
        return CommonResult.success(convertToGetActivityDetailResult(detailDTO));
    }

    private GetActivityDetailResult convertToGetActivityDetailResult(ActivityDetailDTO detailDTO) {
        if (null == detailDTO) {
            throw new ControllerException(ControllerErrorCodeConstants.GET_ACTIVITY_DETAIL_ERROR);
        }

        GetActivityDetailResult result = new GetActivityDetailResult();
        result.setActivityId(detailDTO.getActivityId());
        result.setActivityName(detailDTO.getActivityName());
        result.setDescription(detailDTO.getDesc());
        result.setValid(detailDTO.valid());
        // 抽奖顺序：一等奖、二、三
        result.setPrizes(
                detailDTO.getPrizeDTOList().stream()
                        .sorted(Comparator.comparingInt(prizeDTO -> prizeDTO.getTiers().getCode()))
                        .map(prizeDTO -> {
                            GetActivityDetailResult.Prize prize = new GetActivityDetailResult.Prize();
                            prize.setPrizeId(prizeDTO.getPrizeId());
                            prize.setName(prizeDTO.getName());
                            prize.setImageUrl(prizeDTO.getImageUrl());
                            prize.setPrice(prizeDTO.getPrice());
                            prize.setDescription(prizeDTO.getDescription());
                            prize.setPrizeTierName(prizeDTO.getTiers().getMessage());
                            prize.setPrizeAmount(prizeDTO.getPrizeAmount());
                            prize.setValid(prizeDTO.valid());
                            return prize;
                        }).collect(Collectors.toList())
        );
        result.setUsers(
                detailDTO.getUserDTOList().stream()
                        .map(userDTO -> {
                            GetActivityDetailResult.User user = new GetActivityDetailResult.User();
                            user.setUserId(userDTO.getUserId());
                            user.setUserName(userDTO.getUserName());
                            user.setValid(userDTO.valid());
                            return user;
                        }).collect(Collectors.toList())
        );
        return result;
    }

    private FindActivityListResult convertToFindActivityListResult(PageListDTO<ActivityDTO> activitylist) {
        if (activitylist == null) {
            throw new ControllerException(ControllerErrorCodeConstants.FIND_ACTIVITY_LIST_ERROR);
        }
        FindActivityListResult findActivityListResult = new FindActivityListResult();
        findActivityListResult.setTotal(activitylist.getTotal());
        findActivityListResult.setRecords(
                activitylist.getRecords()
                        .stream()
                        .map(activityDTO -> {
                            FindActivityListResult.ActivityInfo activityInfo = new FindActivityListResult.ActivityInfo();
                            activityInfo.setActivityId(activityDTO.getActivityId());
                            activityInfo.setActivityName(activityDTO.getActivityName());
                            activityInfo.setDescription(activityDTO.getDescription());
                            activityInfo.setValid(activityDTO.valid());
                            return activityInfo;
        }).toList());
        return findActivityListResult;
    }

    private CreateActivityResult convertTOCreateActivityResult(CreateActivityDTO createActivityDTO) {
        if (createActivityDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.CREATE_ACTIVITY_ERROR);
        }
        CreateActivityResult createActivityResult = new CreateActivityResult();
        createActivityResult.setActivityId(createActivityDTO.getActivityId());
        return createActivityResult;
    }
}
