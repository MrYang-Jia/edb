/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.annotation;

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

    /**
     * 自定义数据对象类型，可通过标记直接获取当前对象标记的 relKey 对象并返回结果
     * @return
     */
    String relKey() default "";

    // 指定关联字段，主表和子表必须一致

    /**
     * 当前主对象字段和子对象字段的字段名一致的话，采用该方式指定字段名进行两表关联
     * 例如： a.gid = b.gid ，这时 relColumn = {"gid"} ； 多字段则用逗号隔开，其他情况则使用  appendSql = " and #(字段参数)" 来进行数据关联
     * @return
     */
    String[] relColumn() default {};

    /**
     * 拼接其他字符串 ，以 and 或 or 开始拼接
     * 格式填充: select * from 子表 where 1=1 ${appendSql}
     * 例如： appendSql =  and 子对象的数据库字段 = #(主对象驼峰字段名_1)
     * ps： 不支持 #para() 的写法，默认是将 主对象 bean 当作参数，key-value的模式，目前只支持1层对象的参数解析(有需要的场景后续可以再扩展)
     */
    String appendSql() default "";



}
