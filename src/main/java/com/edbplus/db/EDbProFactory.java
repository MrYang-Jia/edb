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
package com.edbplus.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.IDbProFactory;

/**
 * @ClassName EDbProFactory
 * @Description: edb 工厂实现接口类
 * @Author 杨志佳
 * @Date 2021/5/24
 * @Version V1.0
 **/
@JsonIgnoreType
// 因为方法名以getxxx开头，如果没有参数的话，会被当作是属性对象返回给前端，所以接下来方法名命名要注意不能以get开头
@JsonIgnoreProperties({"realJpaClass","dbPro", "tableName","columnsMap","relKey","relKeyForFutrue","allRel","allRelForFutrue","countSql"})
public class EDbProFactory implements IDbProFactory {

    @Override
    public DbPro getDbPro(String configName) {
        EDbPro eDbPro = new EDbPro(configName);
        return eDbPro;
    }
}
