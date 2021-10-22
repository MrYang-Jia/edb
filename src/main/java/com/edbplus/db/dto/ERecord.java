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
package com.edbplus.db.dto;

import com.jfinal.plugin.activerecord.Record;

/**
 * @ClassName ERecord
 * @Description: 继承 Record ，以便进行相关字段的扩展和延申
 * @Author 杨志佳
 * @Date 2021/10/20
 * @Version V1.0
 **/
public class ERecord extends Record {

    private String table; // 表名

    private String pkId; // 主键名






    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }


}
