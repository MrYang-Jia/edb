package com.edbplus.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * view 视图对象，对应到 jfinal-enjoy 里的 key 信息
 * 结果类型可以是list 也能是 对象
 */
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EDbView {

    // enjoy模板对应的 key 值，通过该key获取sql语句
    String name() default "";


}
