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
    /**
     * and条件
     */
    @Setter
    @Getter
    private List<EDbFilter> andEDbFilters = new ArrayList<>();
//    /**
//     * or条件
//     */
//    @Setter
//    @Getter
//    private List<EDbFilter> orEDbFilters = new ArrayList<>();


    /**
     * 添加一个and条件
     * @param EDbFilter 该条件
     * @return 链式调用
     */
    public  EDbBaseQuery and(EDbFilter EDbFilter){
        this.andEDbFilters.add(EDbFilter);
        return this;
    }
    /**
     * 添加多个and条件
     * @param EDbFilter 该条件
     * @return 链式调用
     */
    public  EDbBaseQuery and(EDbFilter... EDbFilter){
        this.andEDbFilters.addAll(Arrays.asList(EDbFilter));
        return this;
    }
//    /**
//     * 添加一个or条件
//     * @param EDbFilter 该条件
//     * @return 链式调用
//     */
//    public  EDbBaseQuery or(EDbFilter EDbFilter){
//        this.orEDbFilters.add(EDbFilter);
//        return this;
//    }
//    /**
//     * 添加多个or条件
//     * @param EDbFilter 该条件
//     * @return 链式调用
//     */
//    public  EDbBaseQuery or(EDbFilter... EDbFilter){
//        this.orEDbFilters.addAll(Arrays.asList(EDbFilter));
//        return this;
//    }


}
