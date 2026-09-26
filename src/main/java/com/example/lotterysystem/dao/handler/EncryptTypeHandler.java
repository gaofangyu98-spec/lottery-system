package com.example.lotterysystem.dao.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AES 加密类型处理器
 * <p>
 * MyBatis TypeHandler，用于在数据库读写时自动对 Encrypt 类型的字段进行 AES 加解密：
 * - 写入数据库时（setNonNullParameter）：将 Encrypt.value 明文加密为十六进制密文
 * - 从数据库读取时（getNullableResult）：将密文解密为明文 Encrypt 对象
 * <p>
 * 密钥硬编码为 "123456789abcdefg"（16 字节 AES-128），生产环境应改为从配置中心读取。
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(Encrypt.class)
public class EncryptTypeHandler extends BaseTypeHandler<Encrypt> {

    /**
     * AES 加密密钥（16 字节，对应 AES-128）
     */
    private static final byte[] KEYS = "123456789abcdefg".getBytes(StandardCharsets.UTF_8);

    /**
     * 写入数据库时加密参数
     *
     * @param ps        PreparedStatement
     * @param i         参数索引
     * @param parameter Encrypt 对象（含明文）
     * @param jdbcType  JDBC 类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Encrypt parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.getValue() == null) {
            ps.setString(i, null);
            return;
        }
        AES aes = SecureUtil.aes(KEYS);
        String encrypt = aes.encryptHex(parameter.getValue());
        ps.setString(i, encrypt);
    }

    /**
     * 从 ResultSet 按列名读取时解密
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    /**
     * 从 ResultSet 按列索引读取时解密
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    /**
     * 从 CallableStatement 读取存储过程结果时解密
     */
    @Override
    public Encrypt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    /**
     * AES 解密工具方法
     *
     * @param value 数据库中的密文
     * @return 解密后的 Encrypt 对象，密文为 null 时返回 null
     */
    public Encrypt decrypt(String value) {
        if (value == null) {
            return null;
        }
        return new Encrypt(SecureUtil.aes(KEYS).decryptStr(value));
    }
}
