package com.edbplus.db.annotation;


import com.edbplus.db.em.DataType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标识edb类型，在写入 或 数据获取时进行转换
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface EDbType {
    /**
     * 数据类型 -- 默认是json字符串转换,暂时不想扩展太多类型的转换
     * @return
     */
    DataType type() default DataType.JSONSTRING;
}
