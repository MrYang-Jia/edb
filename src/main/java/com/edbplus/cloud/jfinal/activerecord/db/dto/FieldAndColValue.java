package com.edbplus.cloud.jfinal.activerecord.db.dto;

import lombok.Data;

/**
 * @ClassName FieldAndColumn
 * @Description: TODO
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Data
public class FieldAndColValue extends FieldAndColumn {
    // 字段值
    private Object fieldValue;
}
