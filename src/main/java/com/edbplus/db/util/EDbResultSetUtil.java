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
package com.edbplus.db.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName EDbResultSetUtil
 * @Description: ResultSet工具类
 * @Author 杨志佳
 * @Date 2021/7/1
 * @Version V1.0
 **/
public class EDbResultSetUtil {

    /**
     * 返回记录集
     * @param rs
     * @return
     */
    public static Map<String,Object> returnMap(ResultSet rs){
        Map<String,Object> data = new HashMap<>();
        try {
            ResultSetMetaData resultSetMetaData =  rs.getMetaData();
            // 定义字段名
            String coulumnName = null;
            // 遍历字段 ， 字段下标从1开始
            for(int i=1 ; i < resultSetMetaData.getColumnCount()+1 ;i++){
                // 获取字段名
                coulumnName = resultSetMetaData.getColumnName(i);
                // 通过字段名获取对象
                data.put(coulumnName,rs.getObject(coulumnName));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error traversing collection",e);
        }

        return data;
    }

}
