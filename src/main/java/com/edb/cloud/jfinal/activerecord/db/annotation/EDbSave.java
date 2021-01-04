package com.edb.cloud.jfinal.activerecord.db.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 对象更新方法标识 -- 作用域 save 相关方法
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface EDbSave {


}
