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
package com.edbplus.db.query.lambda;

import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.dto.FieldAndColumn;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.em.SqlConnectorEnum;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import javax.persistence.Column;
import java.io.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName EDbLambdaQuery
 * @Description: EDbLambdaQuery
 * @Author 杨志佳
 * @Date 2022/4/2
 * @Version V1.0
 **/
public class EDbLambdaQuery<T>  implements LambdaSelectQuery<T>{

    // sql连接操作符 and or


    // 参考1: https://blog.csdn.net/u012503481/article/details/100896507
    // 参考2: https://blog.csdn.net/weixin_38405253/article/details/121847323
    // 参考3：mybatiesPlus LambdaQueryWrapper 源码参考，发现是一个针对sql的封装操作，迭代处理sql
    // 关于native的问题处理解决方案，目前暂时没有比较好的头绪
    // 参考4：https://githubhot.com/repo/oracle/graal/issues/3756

    public EDbQuery eDbQuery = new EDbQuery(); // 基础封装对象

    public SqlConnectorEnum sqlConnector = SqlConnectorEnum.and;

    public List<EDbLambdaQuery<T>> andComs;

    public List<EDbLambdaQuery<T>> orComs;

    private EDbPro eDbPro;


    // class
    private Class<T> entityClass;

    /**
     * 创建查询对象
     * @param entityClass
     * @param <T>
     * @return
     */
    public static <T> EDbLambdaQuery<T> lambdaQuery(Class<T> entityClass){
        return new EDbLambdaQuery<T>(entityClass);
    }

    /**
     * 创建查询对象，并指定数据库
     * @param entityClass
     * @param configName
     * @param <T>
     * @return
     */
    public static <T> EDbLambdaQuery<T> lambdaQuery(Class<T> entityClass,String configName){
        return new EDbLambdaQuery<T>(entityClass,configName);
    }

    /**
     * 创建查询实例对象
     * @param entityClass
     */
    public EDbLambdaQuery(Class<T> entityClass){
        eDbPro = EDb.use();
        this.entityClass = entityClass;
    }

    /**
     * 创建查询实例对象，并指定数据库
     * @param entityClass
     * @param configName
     */
    public EDbLambdaQuery(Class<T> entityClass,String configName){
        eDbPro = EDb.use(configName);
        this.entityClass = entityClass;
    }

    // =============================
    /**
     * 封装 and 和 or 的拼接操作
     * @param sqlConnector
     * @param eDbFilter
     */
    public void doSome(SqlConnectorEnum sqlConnector, EDbFilter eDbFilter){
        if(sqlConnector == SqlConnectorEnum.and){
            eDbQuery.and(eDbFilter);
        }
        if(sqlConnector == SqlConnectorEnum.or){
            eDbQuery.or(eDbFilter);
            this.sqlConnector = SqlConnectorEnum.and; // 切回默认值，因为or的场景比较少，一般只会使用1次，所以切回来and模式
        }
    }


    /**
     * 获取字段注解 Column 属性
     * @param func
     * @return
     */
    public Column getColumn(EDbColumnFunc<T, ?> func) {
        return EDbLambdaUtil.getColumn(entityClass,func);
    }
    // =============================


    /**
     * 设置查询的字段
     * @param funcs
     * @return
     */
    public LambdaQuery<T> select(EDbColumnFunc<T, ?>... funcs){
        Column column = null;
        StringBuilder propertys = new StringBuilder("");
        for(EDbColumnFunc<T, ?> func : funcs){
            column = getColumn(func);
            if(propertys.length()>0){
                propertys.append(",");
            }
            propertys.append(column.name());
        }
        eDbQuery.fields(propertys.toString());
        return this;
    }


    /**
     * 设置查询的字段
     * @param coulumns
     * @return
     */
    public LambdaQuery<T> select(String coulumns){
        eDbQuery.fields(coulumns);
        return this;
    }


    /**
     * and (...)
     * @param func
     * @return
     */
    public LambdaQuery<T> andCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func){
        if(andComs ==null){
            andComs =  new ArrayList<>();
            eDbQuery.andComs = new ArrayList<>();
        }
        EDbLambdaQuery andCom= new EDbLambdaQuery(entityClass);
        eDbQuery.andComs.add(andCom.eDbQuery);
        andComs.add(andCom);
        func.apply(andCom);
        return this;
    }

    /**
     * or (...)
     * @param func
     * @return
     */
    public LambdaQuery<T> orCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func){
        if(orComs ==null){
            orComs =  new ArrayList<>();
            eDbQuery.orComs = new ArrayList<>();
        }
        EDbLambdaQuery orCom = new EDbLambdaQuery(entityClass);
        eDbQuery.orComs.add(orCom.eDbQuery);
        orComs.add(orCom);
        func.apply(orCom);
        return this;
    }



    /**
     * 连接操作符转变成 or , 右侧将是 or xxx or xxx ，除非再调用1次 and 方法，激活操作符转换
     * @return
     */
    public LambdaQuery<T> or(){
        sqlConnector = SqlConnectorEnum.or;
        return this; // 返回自己本身
    }

    /**
     * 连接操作符转变成 and
     * @return
     */
    public LambdaQuery<T> and(){
        sqlConnector = SqlConnectorEnum.and;
        return this; // 返回自己本身
    }



    /**
     * 小于 <
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> lt(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.lt, value));
        return this; // 返回自己本身
    }

    /**
     * 小于等于 <=
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> le(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.le, value));
        return this; // 返回自己本身
    }

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> eq(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.eq, value));
        return this; // 返回自己本身
    }

    /**
     * 不等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ne(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.ne, value));
        return this; // 返回自己本身
    }

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> in(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.in, value));
        return this; // 返回自己本身
    }

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notIn(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notIn, value));
        return this; // 返回自己本身
    }

    /**
     * 大于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> gt(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.gt, value));
        return this; // 返回自己本身
    }

    /**
     * 大于等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ge(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.ge, value));
        return this; // 返回自己本身
    }

    /**
     * like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> like(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.like, value));
        return this; // 返回自己本身
    }

    /**
     * not like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLike(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notLike, value));
        return this; // 返回自己本身
    }

    /**
     * like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeLeft(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.llk, value));
        return this; // 返回自己本身
    }

    /**
     * not like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notLlk, value));
        return this; // 返回自己本身
    }

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeRight(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.rlk, value));
        return this; // 返回自己本身
    }

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notRlk, value));
        return this; // 返回自己本身
    }


    /**
     * 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end){
        Column column = getColumn(func);
        doSome(sqlConnector,EDbFilter.between(column.name(),begin,end));
        return this; // 返回自己本身
    }

    /**
     * not 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end){
        Column column = getColumn(func);
        doSome(sqlConnector,EDbFilter.notBetween(column.name(),begin,end));
        return this; // 返回自己本身
    }

    /**
     * exists
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> exists(String existsSql){
        doSome(sqlConnector,EDbFilter.exists(existsSql));
        return this; // 返回自己本身
    }

    /**`
     * not exists
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> notExists(String existsSql){
        doSome(sqlConnector,EDbFilter.notExists(existsSql));
        return this; // 返回自己本身
    }

    /**
     * is null
     * @param func
     * @return
     */
    public LambdaQuery<T> isNull(EDbColumnFunc<T, ?> func){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.isNull, null));
        return this; // 返回自己本身
    }

    /**
     * is not null
     * @param func
     * @return
     */
    public LambdaQuery<T> isNotNull(EDbColumnFunc<T, ?> func){
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.isNotNull, null));
        return this; // 返回自己本身
    }

    /**
     * groupBy
     * @param funcs
     * @return
     */
    public LambdaGroupQuery<T> groupBy(EDbColumnFunc<T, ?>... funcs){
        Column column = null;
        StringBuilder propertys = new StringBuilder("");
        for(EDbColumnFunc<T, ?> func:funcs){
            column = getColumn(func);
            if(propertys.length()>0){
                propertys.append(",");
            }
            propertys.append(column.name());
        }
        eDbQuery.groupBy(propertys.toString());;
        return this;
    }

    /**
     * having
     * @param havingSql
     * @return
     */
    public  LambdaHavingQuery<T> having(String havingSql){
        eDbQuery.having(havingSql);
        return this;
    }

    /**
     * having
     * @param havingSql ->  count(c1) > ? and sum(c1) < ?
     * @param values
     * @return
     */
    public  LambdaHavingQuery<T> having(String havingSql,Object... values){
        eDbQuery.having(havingSql,values);
        return this;
    }

    /**
     * order by column asc
     * @param funcs
     * @return
     */
    public  LambdaOrderQuery<T> orderByAsc(EDbColumnFunc<T, ?>... funcs){
        Column column = null;
        for(EDbColumnFunc<T, ?> func:funcs){
            column = getColumn(func);
            eDbQuery.orderASC(column.name());
        }
        return this;
    }

    /**
     * order by column desc
     * @param funcs
     * @return
     */
    public  LambdaOrderQuery<T> orderByDesc(EDbColumnFunc<T, ?>... funcs){
        Column column = null;
        for(EDbColumnFunc<T, ?> func:funcs){
            column = getColumn(func);
            eDbQuery.orderDESC(column.name());
        }
        return this;
    }

    /**
     * limit count
     * @param limitCount
     * @return
     */
    public  LambdaLimitQuery<T> limit(int limitCount){
        eDbQuery.limit(limitCount);
        return this;
    }

    /**
     * offset offsetIdx
     * @param offsetIdx
     * @return
     */
    public LambdaOffsetQuery<T> offset(int offsetIdx){
        eDbQuery.offset(offsetIdx);
        return this;
    }

    /**
     * 获取第一个对象
     * @return
     */
    public T findFirst(){
        return eDbPro.findFirst(entityClass,eDbQuery);
    }

    /**
     * 返回查询列表
     * @return
     */
    public List<T> list(){
        return eDbPro.find(entityClass,eDbQuery);
    }

    /**
     * 返回条数
     * @param limitCount
     * @param offsetIdx
     * @return
     */
    public List<T> list(int limitCount,int offsetIdx){
        return eDbPro.find(entityClass,eDbQuery,limitCount,offsetIdx);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<T> page(int pageNum,int pageSize){
        return eDbPro.paginate(entityClass,pageNum,pageSize,eDbQuery);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @param totalSize
     * @return
     */
    public Page<T> page(int pageNum,int pageSize,int totalSize){
        return eDbPro.paginate(entityClass,pageNum,pageSize,totalSize,eDbQuery);
    }

    /**
     * 获取查询统计结果
     * @return
     */
    public long count(){
        return eDbPro.count(entityClass,eDbQuery);
    }





    

   
}
