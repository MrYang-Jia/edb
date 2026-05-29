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
package com.edbplus.db.jpa;

import com.edbplus.db.EDb;
import com.edbplus.db.jpa.model.MatchedSnippet;
import com.edbplus.db.jpa.model.StrToBeanTest;
import com.edbplus.db.jpa.model.UserInfo;
import com.jfinal.plugin.activerecord.Db;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * @EDbStrToBean 注解测试类
 * <p>
 * 测试将数据库JSON字符串字段自动转换为Java Bean的功能
 * 支持 PostgreSQL 的 text、json、jsonb 类型
 */
public class JpaStrToBeanTest {

    /**
     * PostgreSQL 建表SQL（text 类型版本）
     * <pre>
     * -- 创建测试表（使用text类型存储JSON）
     * CREATE TABLE IF NOT EXISTS t_str_to_bean_test (
     *     id BIGSERIAL PRIMARY KEY,
     *     name VARCHAR(100),
     *     top_matched_snippets TEXT,  -- JSON数组字符串
     *     user_info TEXT,             -- JSON对象字符串
     *     config_json TEXT,           -- JSON对象字符串
     *     user_array TEXT,            -- JSON数组字符串
     *     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     *     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * );
     *
     * -- 插入测试数据
     * INSERT INTO t_str_to_bean_test (name, top_matched_snippets, user_info, config_json, user_array)
     * VALUES (
     *     '测试数据1',
     *     '[{"snippetId": 1, "content": "片段1内容", "score": 0.95, "exactMatch": true},
     *       {"snippetId": 2, "content": "片段2内容", "score": 0.85, "exactMatch": false}]',
     *     '{"userId": 123, "userName": "张三", "email": "zhangsan@example.com", "age": 28}',
     *     '{"key1": "value1", "key2": 123, "key3": true, "nested": {"a": 1, "b": 2}}',
     *     '[{"userId": 1, "userName": "用户1"}, {"userId": 2, "userName": "用户2"}]'
     * );
     *
     * INSERT INTO t_str_to_bean_test (name, top_matched_snippets, user_info, config_json, user_array)
     * VALUES (
     *     '测试数据2',
     *     '[{"snippetId": 3, "content": "片段3内容", "score": 0.75, "exactMatch": true}]',
     *     '{"userId": 456, "userName": "李四", "email": "lisi@example.com", "age": 32}',
     *     '{"category": "test", "level": 2}',
     *     '[{"userId": 3, "userName": "用户3"}]'
     * );
     * </pre>
     *
     * PostgreSQL 建表SQL（jsonb 类型版本）
     * <pre>
     * -- 创建测试表（使用jsonb类型存储JSON）
     * CREATE TABLE IF NOT EXISTS t_str_to_bean_test (
     *     id BIGSERIAL PRIMARY KEY,
     *     name VARCHAR(100),
     *     top_matched_snippets JSONB,  -- JSON数组
     *     user_info JSONB,             -- JSON对象
     *     config_json JSONB,           -- JSON对象
     *     user_array JSONB,            -- JSON数组
     *     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     *     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * );
     *
     * -- 插入测试数据
     * INSERT INTO t_str_to_bean_test (name, top_matched_snippets, user_info, config_json, user_array)
     * VALUES (
     *     '测试数据1',
     *     '[{"snippetId": 1, "content": "片段1内容", "score": 0.95, "exactMatch": true},
     *       {"snippetId": 2, "content": "片段2内容", "score": 0.85, "exactMatch": false}]'::jsonb,
     *     '{"userId": 123, "userName": "张三", "email": "zhangsan@example.com", "age": 28}'::jsonb,
     *     '{"key1": "value1", "key2": 123, "key3": true, "nested": {"a": 1, "b": 2}}'::jsonb,
     *     '[{"userId": 1, "userName": "用户1"}, {"userId": 2, "userName": "用户2"}]'::jsonb
     * );
     *
     * INSERT INTO t_str_to_bean_test (name, top_matched_snippets, user_info, config_json, user_array)
     * VALUES (
     *     '测试数据2',
     *     '[{"snippetId": 3, "content": "片段3内容", "score": 0.75, "exactMatch": true}]'::jsonb,
     *     '{"userId": 456, "userName": "李四", "email": "lisi@example.com", "age": 32}'::jsonb,
     *     '{"category": "test", "level": 2}'::jsonb,
     *     '[{"userId": 3, "userName": "用户3"}]'::jsonb
     * );
     * </pre>
     *
     * 使用 jsonb_agg 聚合查询示例：
     * <pre>
     * -- 使用 jsonb_agg 聚合关联数据
     * SELECT
     *     t.id,
     *     t.name,
     *     COALESCE(
     *         (SELECT jsonb_agg(
     *             jsonb_build_object(
     *                 'snippetId', s.id,
     *                 'content', s.content,
     *                 'score', s.score,
     *                 'exactMatch', s.exact_match
     *             )
     *         )
     *         FROM t_snippet s WHERE s.test_id = t.id),
     *         '[]'::jsonb
     *     ) as top_matched_snippets,
     *     t.user_info,
     *     t.config_json,
     *     t.user_array,
     *     t.create_time,
     *     t.update_time
     * FROM t_str_to_bean_test t
     * WHERE t.id = 1;
     * </pre>
     */

    /**
     * 测试前初始化
     */
    @BeforeClass
    public void setUp() {
        // 假设 EDb 已经配置好数据源
        // 请在实际测试前确保数据库表已创建并插入了测试数据
    }

    /**
     * 测试 List&lt;Bean&gt; 转换 - 使用 jsonb_agg 聚合查询
     */
    @Test
    public void testFindListBean() {
        // 查询所有数据
        List<StrToBeanTest> list = EDb.findAll(StrToBeanTest.class);

        // 验证结果
        assertNotNull(list);
        assertTrue(list.size() > 0);

        for (StrToBeanTest test : list) {
            // 验证基本字段
            assertNotNull(test.getId());
            assertNotNull(test.getName());

            // 验证 List<MatchedSnippet> 转换
            List<MatchedSnippet> snippets = test.getMatchedSnippets();
            if (snippets != null) {
                assertFalse(snippets.isEmpty());
                for (MatchedSnippet snippet : snippets) {
                    assertNotNull(snippet.getSnippetId());
                    assertNotNull(snippet.getContent());
                    assertNotNull(snippet.getScore());
                    assertNotNull(snippet.getExactMatch());
                    System.out.println("MatchedSnippet: id=" + snippet.getSnippetId() +
                            ", content=" + snippet.getContent() +
                            ", score=" + snippet.getScore());
                }
            }
        }
    }

    /**
     * 测试单个对象转换 - UserInfo
     */
    @Test
    public void testFindSingleBean() {
        // 查询第一条数据
        StrToBeanTest test = EDb.findFirst(StrToBeanTest.class, "select * from t_str_to_bean_test where id = ?", 1);

        // 验证结果
        assertNotNull(test);

        // 验证 UserInfo 对象转换
        UserInfo userInfo = test.getUserInfo();
        assertNotNull(userInfo);
        assertNotNull(userInfo.getUserId());
        assertNotNull(userInfo.getUserName());
        assertNotNull(userInfo.getEmail());
        assertNotNull(userInfo.getAge());

        System.out.println("UserInfo: id=" + userInfo.getUserId() +
                ", name=" + userInfo.getUserName() +
                ", email=" + userInfo.getEmail() +
                ", age=" + userInfo.getAge());
    }

    /**
     * 测试 Map 转换
     */
    @Test
    public void testFindMap() {
        // 查询第一条数据
        StrToBeanTest test = EDb.findFirst(StrToBeanTest.class, "select * from t_str_to_bean_test where id = ?", 1);

        // 验证结果
        assertNotNull(test);

        // 验证 Map 转换
        Map<String, Object> config = test.getConfig();
        assertNotNull(config);
        assertFalse(config.isEmpty());

        System.out.println("Config Map: " + config);

        // 验证嵌套对象
        if (config.containsKey("nested")) {
            Object nested = config.get("nested");
            System.out.println("Nested object: " + nested);
        }
    }

    /**
     * 测试数组转换
     */
    @Test
    public void testFindArray() {
        // 查询第一条数据
        StrToBeanTest test = EDb.findFirst(StrToBeanTest.class, "select * from t_str_to_bean_test where id = ?", 1);

        // 验证结果
        assertNotNull(test);

        // 验证数组转换
        UserInfo[] userArray = test.getUserInfoArray();
        assertNotNull(userArray);
        assertTrue(userArray.length > 0);

        for (UserInfo user : userArray) {
            assertNotNull(user.getUserId());
            assertNotNull(user.getUserName());
            System.out.println("UserInfo Array Item: id=" + user.getUserId() +
                    ", name=" + user.getUserName());
        }
    }

    /**
     * 测试根据ID查询
     */
    @Test
    public void testFindById() {
        // 先插入一条数据
        StrToBeanTest newTest = new StrToBeanTest();
        newTest.setName("测试插入");
        newTest.setCreateTime(new Date());
        newTest.setUpdateTime(new Date());

        boolean saveResult = EDb.save(newTest);
        assertTrue(saveResult);

        // 根据ID查询
        StrToBeanTest found = EDb.findById(StrToBeanTest.class, newTest.getId());
        assertNotNull(found);
        assertEquals(found.getName(), "测试插入");
    }

    /**
     * 测试自定义SQL查询 - 使用 jsonb_agg 聚合
     */
    @Test
    public void testCustomSqlWithJsonbAgg() {
        // 使用 jsonb_agg 的自定义查询示例
        // 假设有关联表 t_snippet 存储片段信息
        String sql = "SELECT " +
                "    t.id, " +
                "    t.name, " +
                "    t.user_info, " +
                "    t.config_json, " +
                "    t.user_array, " +
                "    t.create_time, " +
                "    t.update_time " +
                "FROM t_str_to_bean_test t " +
                "WHERE t.id = ?";

        List<StrToBeanTest> list = EDb.find(StrToBeanTest.class, sql, 1);

        assertNotNull(list);
        assertFalse(list.isEmpty());

        StrToBeanTest test = list.get(0);
        assertNotNull(test.getId());
        System.out.println("Custom SQL Result: id=" + test.getId() + ", name=" + test.getName());
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testPaginate() {
        // 分页查询
        com.jfinal.plugin.activerecord.Page<StrToBeanTest> page =
                EDb.paginate(StrToBeanTest.class, 1, 10, "select * from t_str_to_bean_test");

        assertNotNull(page);
        assertNotNull(page.getList());

        for (StrToBeanTest test : page.getList()) {
            System.out.println("Paginate Result: id=" + test.getId() + ", name=" + test.getName());
        }
    }

}
