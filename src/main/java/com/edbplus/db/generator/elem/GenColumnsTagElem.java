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
 * @ClassName GenColumnsTagElem
 * @Description: 字段类型
 * @Author 杨志佳
 * @Date 2020/3/17
 * @Version V1.0
 **/
public class GenColumnsTagElem {
    // 自增
    public static String auto_increment = "auto_increment";
    // 主键
    public static String PRI = "PRI";
    // 复合唯一索引标识
    public static String UNI = "UNI";
    // 普通索引标识 - 则该列是非唯一索引的第一列，其中允许在列中多次出现给定值
    public static String MUL = "MUL";



}
