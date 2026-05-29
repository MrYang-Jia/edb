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
 * JSON字符串转Bean注解
 * <p>
 * 用于将数据库查询返回的JSON字符串字段自动转换为Java对象
 * 支持转换为以下类型：
 * 1. List&lt;Bean&gt; - JSON数组转对象列表
 * 2. Bean - JSON对象转单个对象
 * 3. Map&lt;String, Object&gt; - JSON对象转Map
 * <p>
 * 使用示例：
 * <pre>
 * public class DemoB {
 *     // 数据库字段 top_matched_snippets 存储的JSON数组字符串转换为 List&lt;MatchedSnippet&gt;
 *     &#64;EDbStrToBean(col = "top_matched_snippets")
 *     List&lt;MatchedSnippet&gt; sps;
 *
 *     // 数据库字段 config_json 存储的JSON对象字符串转换为 Map
 *     &#64;EDbStrToBean(col = "config_json")
 *     Map&lt;String, Object&gt; config;
 *
 *     // 数据库字段 user_info 存储的JSON对象字符串转换为单个对象
 *     &#64;EDbStrToBean(col = "user_info")
 *     UserInfo userInfo;
 * }
 * </pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EDbStrToBean {

    /**
     * 数据库字段名称（小写）
     * <p>
     * 指定要转换的数据库字段名，该字段应存储JSON格式的字符串
     * 支持 PostgreSQL 的 text、json、jsonb 类型
     *
     * @return 数据库字段名称
     */
    String col() default "";

}
