package com.edbplus.db.jpa.task;

import cn.hutool.core.util.ReflectUtil;
import com.edbplus.db.EDbPro;
import com.jfinal.plugin.activerecord.SqlPara;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class JpaRelTask implements Callable<Object> {

    private Class<?> jpaClass;
    private SqlPara sqlPara;
    private EDbPro eDbPro;
    private Object oriJpa;
    private Field field;
    private boolean isList = false;

    public JpaRelTask(Object oriJpa,Field oriJpaField,Class<?> jpaClass, SqlPara sqlPara, EDbPro eDbPro,boolean isList){
        this.jpaClass = jpaClass;
        this.sqlPara = sqlPara;
        this.eDbPro = eDbPro;
        this.oriJpa =oriJpa;
        this.field = oriJpaField;
        this.isList = isList;
    }

    @Override
    public Object call() throws Exception {
        Object object = null;
        if(isList){
            object = eDbPro.find(jpaClass, sqlPara);
        }else{
            object = eDbPro.findFirst(jpaClass, sqlPara);
        }
        System.out.println("===运行===");
        // 字段赋值 -- 反射赋值会比较消耗毫秒数
        ReflectUtil.setFieldValue(oriJpa, field, object);

        return object;
    }

}
