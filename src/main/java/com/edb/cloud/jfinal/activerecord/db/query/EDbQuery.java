package com.edb.cloud.jfinal.activerecord.db.query;

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
     * 添加一个and条件
     * @return 链式调用
     */
    public  EDbBaseQuery andCom(){
        if(andCom ==null){
            andCom = new EDbBaseQuery();
        }
        return andCom;
    }



    // or
    @Setter
    private EDbBaseQuery orCom;

    /**
     * 添加一个 or 条件组合
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
     * sql 头部拼接
     */
    @Getter
    private String fieldsSql;

    /**
     * 封装的 select 头部快捷拼接方法
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

}
