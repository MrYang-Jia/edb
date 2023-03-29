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
 * 表字段对象描述
 *
 * @author MrYang
 * @date 2019-02-26
 */
public class GenTableColumnsElem {

    // 字段顺序
    public static String ordinalPosition = "ordinalPosition";

    public static String tableName = "tableName";

    // 表字段名称
    public static String columnName = "columnName";

    // 初始值
    public static String columnDefault = "columnDefault";

    // 数据类型
    public static String dataType = "dataType";

    // 数据字段类型全部 -- 包含字段长度等 int(10) unsigned
    public static String columnType = "columnType";
    // 是否是 正整数
    public static String signedType = "signedType";

    // 字段描述
    public static String columnComment = "columnComment";

    // 主键 - PRI 对应唯一主键
    public static String columnKey = "columnKey";

    // 函数 - auto_increment 对应自增id
    public static String extra = "extra";

    // 是否允许为null ， 0 -不允许，1-允许
    public static String isN = "isN";

    // 最大长度, 数字或NULL ，NULL 一般为时间 或 TEXT
    public static String maxL = "maxL";

}
