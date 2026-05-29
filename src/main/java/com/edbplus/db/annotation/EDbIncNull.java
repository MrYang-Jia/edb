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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 包含Null值更新标识 -- 作用域 update/save 相关方法 (Inc = Include)
 * 用于标记字段在更新时，即使值为null也要包含在更新语句中
 * 使用场景：当 EDb.update(对象, false) 时，被标记的字段即使为null也会被更新
 * 注意：该注解只影响单对象更新方法，不影响批量更新
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface EDbIncNull {

    /**
     * 是否包含null值进行更新
     * 默认为true，表示字段为null时也会更新到数据库
     */
    boolean value() default true;

}
