package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.PrizeDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 奖品表 Mapper 接口
 * <p>
 * 提供奖品的增删查改操作，包括单条插入、分页查询、批量查询和存在性校验。
 */
@Mapper
public interface PrizeMapper {

    @Insert("insert into prize (name, image_url, price, description)" +
            " values (#{name}, #{imageUrl}, #{price}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(PrizeDO prizeDO);

    @Select("select count(*) from prize")
    int count();

    @Select("select * from prize order by id desc limit #{offset}, #{pageSize}")
    List<PrizeDO> selectPrizeList(Integer offset, Integer pageSize);

    @Select("<script>" +
            " select id from prize" +
            " where id in" +
            " <foreach item='item' collection='items' open='(' separator=',' close=')'>" +
            " #{item}" +
            " </foreach>" +
            " </script>")
    List<Long> selectExistByIds(@Param("items") List<Long> prizeIdList);

    @Select("select * from prize where id = #{prizeId}")
    PrizeDO selectById(Long prizeId);


    @Select("<script>" +
            " select * from prize" +
            " where id in" +
            " <foreach item='item' collection='items' open='(' separator=',' close=')'>" +
            " #{item}" +
            " </foreach>" +
            " </script>")
    List<PrizeDO> batchSelectByIds(@Param("items") List<Long> ids);
}
