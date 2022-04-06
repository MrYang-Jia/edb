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
package com.edbplus.db.query;

import com.edbplus.db.query.em.SqlConnectorEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName EDbBaseQuery
 * @Description: 单体对象快捷查询封装器 - 基础对象
 * @Author 杨志佳
 * @Date 2020/10/16
 * @Version V1.0
 **/
public class EDbBaseQuery {

    // 操作符
    public SqlConnectorEnum sqlConnector = SqlConnectorEnum.and;
    public EDbBaseQuery(){

    }

    public EDbBaseQuery(SqlConnectorEnum sqlConnector){
        this.sqlConnector = sqlConnector;
    }

    @Setter
    @Getter
    private int querySize = 0;

    /**
     * and条件
     */
    @Setter
    @Getter
    private List<EDbFilter> andEDbFilters = new ArrayList<>();
    /**
     * or条件
     */
    @Setter
    @Getter
    private List<EDbFilter> orEDbFilters = new ArrayList<>();

    @Setter
    @Getter
    private EDbFilter groupByFilter = null;

    @Setter
    @Getter
    private EDbFilter havingFilter = null;


    /**
     * 添加一个and条件
     * @param EDbFilter 该条件
     * @return 链式调用
     */
    public  EDbBaseQuery and(EDbFilter EDbFilter){
        this.andEDbFilters.add(EDbFilter);
        this.querySize++;
        return this;
    }
    /**
     * 添加多个and条件
     * @param EDbFilter 该条件
     * @return 链式调用
     */
    public  EDbBaseQuery and(EDbFilter... EDbFilter){
        this.andEDbFilters.addAll(Arrays.asList(EDbFilter));
        querySize += EDbFilter.length;
        return this;
    }
    /**
     * 添加一个or条件
     * @param EDbFilter 该条件
     * @return 链式调用
     */
    public  EDbBaseQuery or(EDbFilter EDbFilter){
        this.orEDbFilters.add(EDbFilter);
        this.querySize++;
        return this;
    }
    /**
     * 添加多个or条件
     * @param EDbFilter 该条件
     * @return 链式调用
     */
    public  EDbBaseQuery or(EDbFilter... EDbFilter){
        this.orEDbFilters.addAll(Arrays.asList(EDbFilter));
        querySize += EDbFilter.length;
        return this;
    }

    /**
     * groupBy
     * @param propertys
     * @return
     */
    public  EDbBaseQuery groupBy(String propertys){
        this.groupByFilter = EDbFilter.groupBy(propertys);
        return this;
    }

    /**
     * having
     * @param havingSql
     * @return
     */
    public  EDbBaseQuery having(String havingSql){
        this.havingFilter = EDbFilter.having(havingSql);
        return this;
    }

    /**
     * having
     * @param havingSql ->  count(c1) > ? and sum(c1) < ?
     * @param values
     * @return
     */
    public  EDbBaseQuery having(String havingSql,Object... values){
        this.havingFilter = EDbFilter.having(havingSql,values);
        return this;
    }


}
