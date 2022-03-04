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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SelectItems
 * @Description: select字段信息
 * @Author 杨志佳
 * @Date 2022/2/23
 * @Version V1.0
 **/
public class SelectItems extends HashMap<String,Map<String,SelectItemInfo>> {
    public List<SelectItemInfo> selectItemInfoList; // select字段属性集
    public List<String> tables; // 表名(全小写，避免表名命名不规范，导致判重不好判断)
    public List<String> columns; // 所有表的字段集合(去重，避免重复字段没有携带表名导致存在意义不大)
    public SelectItems(){
        this.selectItemInfoList = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.columns =  new ArrayList<>();
    }

    /**
     * 生成适合 in 的入参格式，例如 'p1','p2'
     * @param params
     * @return
     */
    public static String splitSqlParams(List<String> params){
        StringBuilder paramsStr = new StringBuilder("");
        for (String s:params){
            if(paramsStr.length()>1){
                paramsStr.append(",");
            }
            paramsStr.append("'").append(s).append("'");
        }
        return paramsStr.toString();
    }
}
