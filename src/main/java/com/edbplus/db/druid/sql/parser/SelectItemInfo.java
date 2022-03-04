/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.druid.sql.parser;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName SelectInfo
 * @Description: select字段信息
 * @Author 杨志佳
 * @Date 2022/2/23
 * @Version V1.0
 **/
@Accessors(chain = true)
@Data
public class SelectItemInfo {
    private String tableName; // 表名
    private String columnName; // 字段名
    private String aliasName; // 表字段别名
    private String columnComment; // 字段注释
    private String javaCodeName; // 对用的java字段驼峰名称
    private String javaType; // 对应的 java 类型
    private Integer javaLength; // java类型字段长度
    private String columnType; // 对应的 数据库字段 类型
    private String maxL; // 最大字段长度 , 例如 11 或 11,2  代表单数值或双精度的数值
}
