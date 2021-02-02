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
package com.edbplus.db.generator.elem;

/**
 * @ClassName GenIndexElem
 * @Description: 存储返回的索引字段名，已加工处理返回
 * @Author 杨志佳
 * @Date 2020/3/19
 * @Version V1.0
 **/
public class GenIndexElem {

    // 数据库名称
    public static String tableSchema = "tableSchema";

    // 表名称
    public static String tableName = "tableName";

    // 不是唯一值
    public static String notUnique = "notUnique";

    // 索引名称
    public static String indexName = "indexName";

    // 字段名称
    public static String columnName = "columnName";

}
