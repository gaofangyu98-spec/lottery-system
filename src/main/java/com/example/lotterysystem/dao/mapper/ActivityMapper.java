package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 活动表 Mapper 接口
 * <p>
 * 提供活动的插入、分页查询、按 ID 查询和状态更新操作。
 */
@Mapper
public interface ActivityMapper {

    @Insert("insert into activity (activity_name, description, status)" +
            " values (#{activityName}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ActivityDO activityDO);

    @Select("select count(*) from activity")
    int count();

    @Select("select * from activity limit #{offset}, #{pageSize}")
    List<ActivityDO> selectActivityList(Integer offset, Integer pageSize);

    @Select("select * from activity where id = #{activityId}")
    ActivityDO selectById(Long activityId);

    @Update("update activity set status = #{status} where id = #{id}")
    void updateStatus(Long id,String status);
}
