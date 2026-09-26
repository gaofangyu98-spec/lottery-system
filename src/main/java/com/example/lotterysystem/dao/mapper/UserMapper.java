package com.example.lotterysystem.dao.mapper;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户表 Mapper 接口
 * <p>
 * 提供用户的注册插入、登录查询（邮箱/手机号）、存在性校验和列表查询操作。
 * 手机号字段通过 EncryptTypeHandler 自动进行 AES 加解密。
 */
@Mapper
public interface UserMapper {

    @Select("select count(*) from user where email = #{email}")
    int countByMail(@Param("email") String email);

    @Select("select count(*) from user where phone_number = #{phoneNumber}")
    int countByPhoneNumber(@Param("phoneNumber") Encrypt phoneNumber);

    @Insert("insert into user (user_name, identity, email, phone_number, password) " +
            "values (#{userName}, #{identity}, #{email}, #{phoneNumber}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserDO userDO);

    @Select("select * from user where email = #{loginName}")
    UserDO selectByMail(@Param("loginName") String loginName);

    @Select("select * from user where phone_number = #{loginName}")
    UserDO selectByPhoneNumber(@Param("loginName") Encrypt encrypt);

    @Select("<script> " +
            " select * from user " +
            " <if test=\"identity != null\">" +
            " where identity = #{identity}" +
            " </if>" +
            " order by id desc" +
            " </script>"
    )
    List<UserDO> selectUserList(@Param("identity") String identity);

    @Select("<script>" +
            " select id from user" +
            " where id in" +
            " <foreach item='item' collection='items' open='(' separator=',' close=')'>" +
            " #{item}" +
            " </foreach>" +
            " </script>")
    List<Long> selectExistByIds(@Param("items") List<Long> userIdList);

    @Select("<script>" +
            " select * from user" +
            " where id in" +
            " <foreach item='item' collection='items' open='(' separator=',' close=')'>" +
            " #{item}" +
            " </foreach>" +
            " </script>")
    List<UserDO> selectByIds(List<Long> items);
}
