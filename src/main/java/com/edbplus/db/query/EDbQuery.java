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
import java.util.List;

/**
 * @ClassName EDbQuery
 * @Description: 单体对象快捷查询封装器
 * @Author 杨志佳
 * @Date 2020/10/16
 * @Version V1.0
 **/
public class EDbQuery extends EDbBaseQuery{



    // and
    @Setter
    private EDbBaseQuery andCom;

    /**
     * 添加一个and条件 -> and (  xxxx  )
     * @return 链式调用
     */
    public  EDbBaseQuery andCom(){
        if(andCom ==null){
            andCom = new EDbBaseQuery();
        }
        return andCom;
    }


    /**
     * or组合对象
     */
    @Setter
    private EDbBaseQuery orCom;

    /**
     * 添加一个 or 条件组合 -> or ( xxx )
     * @return 链式调用
     */
    public  EDbBaseQuery orCom(){
        if(orCom == null){
            orCom = new EDbBaseQuery();
        }
        return orCom;
    }






    /**
     * 排序属性
     */
    @Setter
    @Getter
    private List<Order> orders = new ArrayList<>();


    /**
     * sql 头部拼接  -> select ${fieldsSql}(数字库字段,多个用逗号隔开) from
     */
    @Getter
    private String fieldsSql;

    /**
     * 封装的 select 头部快捷拼接方法 -> select ${fieldsSql}(数字库字段,多个用逗号隔开) from
     * @param fields
     */
    public void fields(String fields){
        fieldsSql = fields;
    }


    /**
     * sql order语句之前拼接
     */
    @Getter
    private StringBuffer lastSql = new StringBuffer("");

    /**
     * 尾部sql拼接
     * @param sql
     */
    public void lastSqlAppend(String sql){
        lastSql.append(sql);
    }

    /**
     * 清空尾部sql
     */
    public void cleanLastSql(){
        lastSql.delete( 0, lastSql.length() );
    }


    /**
     * 升序字段
     * @param property 该字段对应变量名
     * @return 链式调用
     */
    public  EDbQuery orderASC(String property){
        this.orders.add(Order.asc(property));
        return this;
    }
    /**
     * 降序字段
     * @param property 该字段对应变量名
     * @return 链式调用
     */
    public  EDbQuery orderDESC(String property){
        this.orders.add(Order.desc(property));
        return this;
    }

    // 返回的分页数量
    @Getter
    private Integer limit ;
    public  EDbQuery limit(int limit){
        this.limit =limit;
        return this;
    }

//    // 返回的随机分页数量 -- 不同数据库不一样，所以不写
//    @Getter
//    private Integer randomLimit ;
//    public  EDbQuery randomLimit(int randomLimit){
//        this.randomLimit =randomLimit;
//        return this;
//    }
}
