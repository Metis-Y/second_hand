package com.example.dto;

import com.example.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果：商品列表 + 总数，与前端约定 { list, total } 保持一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总条数 */
    private long total;
}