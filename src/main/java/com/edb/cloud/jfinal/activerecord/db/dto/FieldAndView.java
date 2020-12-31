package com.edb.cloud.jfinal.activerecord.db.dto;

import com.edb.cloud.jfinal.activerecord.db.annotation.EDbView;
import lombok.Data;

import java.lang.reflect.Field;

/**
 * @ClassName FieldAndColumn
 * @Description: TODO
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Data
public class FieldAndView {
    private Field field;
    // 关系对象
    private EDbView eDbView;
}
