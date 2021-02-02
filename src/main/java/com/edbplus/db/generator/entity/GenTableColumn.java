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
package com.edbplus.db.generator.entity;

import lombok.Data;

/**
 * 对表象字段工具类
 */
@Data
public class GenTableColumn {
    // 表字段名称
    private String columnName ;

    // java 驼峰式字段名
    private String columnCode ;

    // 初始值
    private String columnDefault ;

    // 数据类型
    private String dataType ;

    // java 数据类型
    private String columnType ;

    // 字段描述
    private String columnComment ;

    // 主键 - PRI 对应唯一主键
    private String columnKey ;

    // 函数 - auto_increment 对应自增id
    private String extra ;

    // 是否允许为null ， 0 -不允许，1-允许
    private String isN ;

    // 最大长度, 数字或NULL ，NULL 一般为时间 或 TEXT
    private String maxL ;

    // 根据长度回填 9
    private String maxValue;

    // 小数位数
    private String decimalDigit;


}
