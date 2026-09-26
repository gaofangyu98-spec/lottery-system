package com.example.lotterysystem.controller.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求基类参数（当前页 + 每页条数）
 *
 * @author: yibo
 */
@Data
public class PageParam implements Serializable {

    /**
     * 当前页
     */
    private Integer currentPage = 1;

    /**
     * 当前页数量
     */
    private Integer pageSize = 10;

    /**
     * 获取偏移量
     *
     * @return
     */
    public Integer offset() {
        return (currentPage-1) * pageSize;
    }


}
