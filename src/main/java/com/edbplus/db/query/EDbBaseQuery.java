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
     * 移除一个and条件
     * @param EDbFilter
     * @return
     */
    public  EDbBaseQuery andRm(EDbFilter EDbFilter){
        this.andEDbFilters.remove(EDbFilter);
        this.querySize--;
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
     * 删除多个条件表达
     * @param EDbFilter
     * @return
     */
    public  EDbBaseQuery andRm(EDbFilter... EDbFilter){
        this.andEDbFilters.removeAll(Arrays.asList(EDbFilter));
        querySize -= EDbFilter.length;
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
     * or条件移除，必须是同一个 EDbFilter 对象
     * @param EDbFilter
     * @return
     */
    public  EDbBaseQuery orRm(EDbFilter EDbFilter){
        this.orEDbFilters.remove(EDbFilter);
        this.querySize--;
        return this;
    }

    /**
     * or条件批量移除
     * @param EDbFilter
     * @return
     */
    public  EDbBaseQuery orRm(EDbFilter... EDbFilter){
        this.orEDbFilters.removeAll(Arrays.asList(EDbFilter));
        querySize -= EDbFilter.length;
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




}
