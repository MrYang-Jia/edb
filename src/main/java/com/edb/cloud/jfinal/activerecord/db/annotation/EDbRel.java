package com.edb.cloud.jfinal.activerecord.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库关系对象获取
 */
@Target({ ElementType.METHOD , ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EDbRel {

    // relKey -- 指定 relKey 可用于多对象时取自定义的 relKey 关联对象信息
    String relKey() default "";

    // 指定关联字段
    String[] relColumn() default {};

    // 拼接其他字符串 ，以 and 或 or 开始拼接
    String appendSql() default "";

    // 默认10个，避免关系数量太多，导致获取的列表数据
    int limit() default 10;

    // 有特殊需求可以指定获取的位置
    int offset() default 0;


}
