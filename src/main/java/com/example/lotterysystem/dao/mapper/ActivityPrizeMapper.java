package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 活动-奖品关联表 Mapper 接口
 * <p>
 * 提供活动奖品的批量插入、按条件查询、状态更新和数量统计操作。
 */
@Mapper
public interface ActivityPrizeMapper {
    @Select("select * from activity_prize where" +
            " activity_id = #{activityId} and " +
            " prize_id = #{prizeId}")
    ActivityPrizeDO selectByAPId(Long activityId, Long prizeId);


    @Update("update activity_prize set status = #{status} where activity_id = #{activityId} and prize_id = #{prizeId}")
    void updateStatus(Long activityId, Long prizeId, String status);

    @Select("select count(1) from activity_prize where activity_id = #{activityId}" +
            " and status = #{status}")
    int countPrize(Long activityId, String status);


    @Insert("<script>" +
            " insert into activity_prize (activity_id, prize_id, prize_amount, prize_tiers, status)" +
            " values <foreach collection = 'items' item='item' index='index' separator=','>" +
            " (#{item.activityId}, #{item.prizeId}, #{item.prizeAmount}, #{item.prizeTiers}, #{item.status})" +
            " </foreach>" +
            " </script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int batchInsert(@Param("items") List<ActivityPrizeDO> activityPrizeDOList);


    @Select("select * from activity_prize where activity_id = #{activityId}")
    List<ActivityPrizeDO> selectByActivityId(@Param("activityId") Long activityId);
}
