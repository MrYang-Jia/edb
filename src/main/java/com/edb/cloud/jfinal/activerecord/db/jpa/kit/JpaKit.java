package com.edb.cloud.jfinal.activerecord.db.jpa.kit;

import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColumn;
import com.edb.cloud.jfinal.activerecord.db.jpa.JpaAnnotationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaKit {

    /**
     * 是否是列表
     * @param self
     * @return
     */
    public static boolean isList(Object self){
        if(self instanceof java.util.List){
            return true;
        }
        return false;
    }

    /**
     * 是否是map类型
     * @param self
     * @return
     */
    public static boolean isMap(Object self){
        if(self instanceof java.util.Map){
            return true;
        }
        return false;
    }

    /**
     * 根据jpa对象转数据库字段
     * @param mClass
     * @param data -- 从前端获取到驼峰字段数据集合
     * @return
     */
    public static  Map<String,Object> toDbColumnMap(Class<?> mClass, Map<String,Object> data){
        // 获取对象所有字段的列表信息
        List<FieldAndColumn> fieldAndColumns =  JpaAnnotationUtil.getCoumns(mClass);
        Map<String,Object> result =  new HashMap<>();
        // 重置为数据库字段对象
        for(FieldAndColumn fieldAndColumn : fieldAndColumns){
            // 判断是否获取到该驼峰字段信息，如果是则转换成数据库的字段
            if(data.get(fieldAndColumn.getField().getName())!=null){
                result.put(fieldAndColumn.getColumn().name(),data.get(fieldAndColumn.getField().getName()));
            }
        }
        return result;
    }

    /**
     * 获取jpa数据库对照字段集合
     * @param mClass
     * @return
     */
    public static Map<String,String> getJpaDbColumns(Class<?> mClass){
        // 获取对象所有字段的列表信息
        List<FieldAndColumn> fieldAndColumns =  JpaAnnotationUtil.getCoumns(mClass);
        Map<String,String> result =  new HashMap<>();
        // 重置为数据库字段对象
        for(FieldAndColumn fieldAndColumn : fieldAndColumns){
            // key = 驼峰字段 ，value = 数据库字段
            result.put(fieldAndColumn.getField().getName(),fieldAndColumn.getColumn().name());
        }
        return result;
    }


}
