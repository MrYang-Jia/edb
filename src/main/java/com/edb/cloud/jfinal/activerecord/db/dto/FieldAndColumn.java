package com.edb.cloud.jfinal.activerecord.db.dto;

import com.edb.cloud.jfinal.activerecord.db.annotation.EDbRel;
import lombok.Data;

import javax.persistence.Column;
import java.lang.reflect.Field;

/**
 * @ClassName FieldAndColumn
 * @Description: 对象字段+数据库字段的组合对象
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Data
public class FieldAndColumn {
    private Field field;
    private Column column;
    private Boolean isPriKey;
}
