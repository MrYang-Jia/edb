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
package com.edbplus.db.jpa.model;

import com.edbplus.db.annotation.EDbStrToBean;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JSON字符串转Bean测试实体
 * <p>
 * 测试 @EDbStrToBean 注解的功能，支持：
 * 1. List&lt;Bean&gt; - JSON数组转对象列表
 * 2. Bean - JSON对象转单个对象
 * 3. Map&lt;String, Object&gt; - JSON对象转Map
 */
@Data
@Table(name = "t_str_to_bean_test")
public class StrToBeanTest {

    /**
     * 主键ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 名称
     */
    @Column(name = "name")
    private String name;

    /**
     * JSON数组字符串转 List&lt;MatchedSnippet&gt;
     * <p>
     * PostgreSQL 存储类型：text 或 jsonb
     * 示例JSON：
     * <pre>
     * [
     *   {"snippetId": 1, "content": "片段1", "score": 0.95, "exactMatch": true},
     *   {"snippetId": 2, "content": "片段2", "score": 0.85, "exactMatch": false}
     * ]
     * </pre>
     */
    @EDbStrToBean(col = "top_matched_snippets")
    private List<MatchedSnippet> matchedSnippets;

    /**
     * JSON对象字符串转 UserInfo 对象
     * <p>
     * PostgreSQL 存储类型：text 或 jsonb
     * 示例JSON：
     * <pre>
     * {"userId": 123, "userName": "张三", "email": "zhangsan@example.com", "age": 28}
     * </pre>
     */
    @EDbStrToBean(col = "user_info")
    private UserInfo userInfo;

    /**
     * JSON对象字符串转 Map&lt;String, Object&gt;
     * <p>
     * PostgreSQL 存储类型：text 或 jsonb
     * 示例JSON：
     * <pre>
     * {"key1": "value1", "key2": 123, "key3": true, "nested": {"a": 1, "b": 2}}
     * </pre>
     */
    @EDbStrToBean(col = "config_json")
    private Map<String, Object> config;

    /**
     * JSON数组字符串转数组
     * <p>
     * PostgreSQL 存储类型：text 或 jsonb
     * 示例JSON：
     * <pre>
     * [{"userId": 1, "userName": "用户1"}, {"userId": 2, "userName": "用户2"}]
     * </pre>
     */
    @EDbStrToBean(col = "user_array")
    private UserInfo[] userInfoArray;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

}
