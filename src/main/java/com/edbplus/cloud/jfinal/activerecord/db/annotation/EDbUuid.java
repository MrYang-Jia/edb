package com.edbplus.cloud.jfinal.activerecord.db.annotation;


import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标识旧的JPA对象 -- 记录JPA原始值
 */
@Target(FIELD)
@Retention(RUNTIME)
// 暂时弃用 - 原来用途是用来生成 uuid 的，后面去除了
@Deprecated
public @interface EDbUuid {


}
