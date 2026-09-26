package com.example.lotterysystem.service.dto;
import lombok.Data;
import java.util.List;

/**
 * 通用分页结果 DTO
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageListDTO<T> {

    /**
     * 数据总量
     */
    private Integer total;

    /**
     * 当前页数据列表
     */
    private List<T> records;


    public PageListDTO(Integer total, List<T> records) {
        this.total = total;
        this.records = records;
    }


}
