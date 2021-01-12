package com.edbplus.db.web.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: coreframework-parent
 * @description: 数据表格分页结果
 * @author: 杨志佳
 * @create: 2021-01-12 11:27
 **/
@Data
public class DataTablePageResult<T> extends ApiResult<T> implements Serializable {

    // 数据集
    private List<T> data;

}
