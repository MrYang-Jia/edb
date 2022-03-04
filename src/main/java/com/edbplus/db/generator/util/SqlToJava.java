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
package com.edbplus.db.generator.util;

import cn.hutool.core.map.CaseInsensitiveMap;

import java.util.Map;

/**
 * @ClassName MysqlToJava
 * @Description: mysql转java
 * @Author 杨志佳
 * @Date 2022/2/23
 * @Version V1.0
 **/
public class SqlToJava {
    // 使用大小写不敏感的对象封装
    public static Map<String,String> filedTypeMap =  new CaseInsensitiveMap();

    /**
     * 数据库数据类型转java类型初始化
     * key大小写不敏感，所以可以直接拿来用
     */
    static {
        // 转 int4
        filedTypeMap.put("int","Integer");
        // 转 int2
        filedTypeMap.put("tinyint","Integer");
        // 转int8
        filedTypeMap.put("BIGINT","Long");
        // varchar时需要指定大小
        // 一样不变 用text 也不影响性能
        filedTypeMap.put("VARCHAR","String");
        // 默认是一个字符，如果不指定的话
        filedTypeMap.put("CHAR","String");
        // 字段不变
        filedTypeMap.put("TEXT","String");
        // 大文本类型
        filedTypeMap.put("mediumtext","String");
        // long text
        filedTypeMap.put("longtext","String");
        // float -- 可变精度，不靠谱，建议都转变成 decimal
        filedTypeMap.put("FLOAT","BigDecimal");
        // double  -- 可变精度，不靠谱，建议都转变成 decimal
        filedTypeMap.put("DOUBLE","BigDecimal");
        // decimal 精度类型 -- 其实 pg/gp 会被转换成 numeric 类型存储
        filedTypeMap.put("Decimal","BigDecimal");
        // numeric 精度类型，其实与 decimal 一样的效果
        filedTypeMap.put("numeric","BigDecimal");
        // 时间戳
        filedTypeMap.put("timestamp","Date");
        // 时间
        filedTypeMap.put("datetime","Date");
        // time -> time  或 interval
        filedTypeMap.put("time","Date");
        // year
        filedTypeMap.put("year","Integer");
        // 年月日
        filedTypeMap.put("DATE","Date");
        // boolean 类型
        filedTypeMap.put("bit","Boolean");
        // json 类型
        filedTypeMap.put("json","String");
        // blob 文件类型 (数据同步的话，则需要转换为 jdbc 的写入模式)
        filedTypeMap.put("LONGBLOB","File");
        filedTypeMap.put("BLOB","File");
        filedTypeMap.put("tinyblob","File");
        filedTypeMap.put("mediumblob","File");

        filedTypeMap.put("int8","Long");
        filedTypeMap.put("int4","Integer");
        filedTypeMap.put("int2","Integer");
        filedTypeMap.put("float4","BigDecimal");
        filedTypeMap.put("serial8","Long");
        filedTypeMap.put("serial4","Integer");
        filedTypeMap.put("serial2","Integer");
        filedTypeMap.put("jsonb","String");

    }

}
