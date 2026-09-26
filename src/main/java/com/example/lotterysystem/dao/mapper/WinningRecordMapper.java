package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 中奖记录表 Mapper 接口
 * <p>
 * 提供中奖记录的批量插入、按活动/奖品查询、数量统计和删除操作。
 */
@Mapper
public interface WinningRecordMapper {

    @Insert("<script>" +
            " insert into winning_record (activity_id, activity_name," +
            " prize_id, prize_name, prize_tier," +
            " winner_id, winner_name, winner_email, winner_phone_number, winning_time)" +
            " values <foreach collection = 'items' item='item' index='index' separator=','>" +
            " (#{item.activityId}, #{item.activityName}," +
            " #{item.prizeId}, #{item.prizeName}, #{item.prizeTier}," +
            " #{item.winnerId}, #{item.winnerName}, #{item.winnerEmail}, #{item.winnerPhoneNumber}, #{item.winningTime})" +
            " </foreach>" +
            " </script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int batchInsert(@Param("items") List<WinningRecordDO> winningRecordDOList);

    @Select("select * from winning_record where activity_id = #{activityId}")
    List<WinningRecordDO> selectByActivityId(Long activityId);

    @Select("select count(1) from winning_record where activity_id = #{activityId} and prize_id = #{prizeId}")
    int countByAPId(Long activityId, Long prizeId);

    @Delete("<script>" +
            " delete from winning_record" +
            " where activity_id = #{activityId}" +
            " <if test=\"prizeId != null\">" +
            "   and prize_id = #{prizeId}" +
            " </if>" +
            " </script>")
    void deleteRecords(@Param("activityId") Long activityId, @Param("prizeId") Long prizeId);


    @Select("<script>" +
            " select * from winning_record" +
            " where activity_id = #{activityId}" +
            " <if test=\"prizeId != null\">" +
            "   and prize_id = #{prizeId}" +
            " </if>" +
            " </script>")
    List<WinningRecordDO> selectByActivityIdOrPrizeId(@Param("activityId") Long activityId,
                                                      @Param("prizeId")  Long prizeId);
}
