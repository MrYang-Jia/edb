package com.edbplus.cloud.jfinal.activerecord.db.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 对象更新方法标识 -- 作用域 update 相关方法
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface EDbUpdate {


}
