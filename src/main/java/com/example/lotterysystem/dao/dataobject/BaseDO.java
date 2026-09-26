package com.example.lotterysystem.dao.dataobject;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据对象基类
 * <p>
 * 所有 DO 的公共父类，包含主键 ID 和公共时间字段。
 * 实现 Serializable 以支持序列化（如 Redis 缓存）。
 */
@Data
public abstract class BaseDO implements Serializable {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;
}
