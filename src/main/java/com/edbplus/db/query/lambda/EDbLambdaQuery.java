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
     * 封装 and 和 or 的拼接操作 - 内部使用，带条件判断
     * @param condition - 是否执行此操作
     * @param sqlConnector
     * @param eDbFilter
     */
    public void doSome(boolean condition, SqlConnectorEnum sqlConnector, EDbFilter eDbFilter){
        if (!condition) {
            return; // 如果条件不满足，直接返回
        }
        if(sqlConnector == SqlConnectorEnum.and){
            eDbQuery.and(eDbFilter);
        }
        if(sqlConnector == SqlConnectorEnum.or){
            eDbQuery.or(eDbFilter);
            this.sqlConnector = SqlConnectorEnum.and; // 切回默认值，因为or的场景比较少，一般只会使用1次，所以切回来and模式
        }
    }

    /**
     * 封装 and 和 or 的拼接操作 - 原有方法，保持兼容性
     * @param sqlConnector
     * @param eDbFilter
     */
    public void doSome(SqlConnectorEnum sqlConnector, EDbFilter eDbFilter){
        doSome(true, sqlConnector, eDbFilter); // 默认条件为 true
    }

    // 移除原来的 getColumn 方法
    // public Column getColumn(EDbColumnFunc<T, ?> func) {
    //     return EDbLambdaUtil.getColumn(entityClass,func);
    // }

    // =============================
    /**
     * 设置查询的字段 (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 字段选择器
     * @return
     */
    public LambdaQuery<T> select(boolean condition, EDbColumnFunc<T, ?>... funcs){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(funcs[0]); // 假设所有funcs都属于同一个实体类
        StringBuilder propertys = new StringBuilder("");
        for(EDbColumnFunc<T, ?> func : funcs){
            if(propertys.length()>0){
                propertys.append(",");
            }
            // 使用 EDbFilter.getColumnName 获取列名
            String columnName = EDbFilter.getColumnName(currentEntityClass, func);
            propertys.append(columnName);
        }
        eDbQuery.fields(propertys.toString());
        return this;
    }

    /**
     * 设置查询的字段 (无条件版本 - 保持原有行为)
     * @param funcs
     * @return
     */
    public LambdaQuery<T> select(EDbColumnFunc<T, ?>... funcs){
        return select(true, funcs);
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
     * 连接操作符转变成 or (条件版本)
     * @param condition - 是否切换到 or 模式
     * @return
     */
    public LambdaQuery<T> or(boolean condition){
        if (condition) {
            sqlConnector = SqlConnectorEnum.or;
        }
        return this; // 返回自己本身
    }

    /**
     * 连接操作符转变成 or (无条件版本 - 保持原有行为)
     * @return
     */
    public LambdaQuery<T> or(){
        return or(true); // 默认切换
    }

    /**
     * 连接操作符转变成 and (条件版本)
     * @param condition - 是否切换到 and 模式
     * @return
     */
    public LambdaQuery<T> and(boolean condition){
        if (condition) {
            sqlConnector = SqlConnectorEnum.and;
        }
        return this; // 返回自己本身
    }

    /**
     * 连接操作符转变成 and (无条件版本 - 保持原有行为)
     * @return
     */
    public LambdaQuery<T> and(){
        return and(true); // 默认切换
    }

    /**
     * 小于 < (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> lt(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.lt, value));
        return this; // 返回自己本身
    }

    /**
     * 小于 < (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> lt(EDbColumnFunc<T, ?> func, Object value){
        return lt(true, func, value);
    }

    /**
     * 小于等于 <= (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> le(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.le, value));
        return this; // 返回自己本身
    }

    /**
     * 小于等于 <= (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> le(EDbColumnFunc<T, ?> func, Object value){
        return le(true, func, value);
    }

    /**
     * 等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> eq(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.eq, value));
        return this; // 返回自己本身
    }

    /**
     * 等于 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> eq(EDbColumnFunc<T, ?> func, Object value){
        return eq(true, func, value);
    }

    /**
     * 不等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ne(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.ne, value));
        return this; // 返回自己本身
    }

    /**
     * 不等于 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ne(EDbColumnFunc<T, ?> func, Object value){
        return ne(true, func, value);
    }

    /**
     * 等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> in(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.in, value));
        return this; // 返回自己本身
    }

    /**
     * 等于 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> in(EDbColumnFunc<T, ?> func, Object value){
        return in(true, func, value);
    }

    /**
     * 等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notIn(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.notIn, value));
        return this; // 返回自己本身
    }

    /**
     * 等于 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notIn(EDbColumnFunc<T, ?> func, Object value){
        return notIn(true, func, value);
    }

    /**
     * 大于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> gt(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.gt, value));
        return this; // 返回自己本身
    }

    /**
     * 大于 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> gt(EDbColumnFunc<T, ?> func, Object value){
        return gt(true, func, value);
    }

    /**
     * 大于等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func - 对象字段方法
     * @param value - 赋值
     * @return LambdaQuery<T>
     */
    public LambdaQuery<T> ge(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.ge, value));
        return this; // 返回自己本身
    }

    /**
     * 大于等于 (无条件版本 - 保持原有行为)
     * @param func - 对象字段方法
     * @param value - 赋值
     * @return LambdaQuery<T>
     */
    public LambdaQuery<T> ge(EDbColumnFunc<T, ?> func, Object value){
        return ge(true, func, value);
    }

    /**
     * like %匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> like(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.like, value));
        return this; // 返回自己本身
    }

    /**
     * like %匹配% (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> like(EDbColumnFunc<T, ?> func, Object value){
        return like(true, func, value);
    }

    /**
     * not like %匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLike(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.notLike, value));
        return this; // 返回自己本身
    }

    /**
     * not like %匹配% (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLike(EDbColumnFunc<T, ?> func, Object value){
        return notLike(true, func, value);
    }

    /**
     * like 左匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeLeft(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.llk, value));
        return this; // 返回自己本身
    }

    /**
     * like 左匹配% (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeLeft(EDbColumnFunc<T, ?> func, Object value){
        return likeLeft(true, func, value);
    }

    /**
     * not like 左匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeLeft(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.notLlk, value));
        return this; // 返回自己本身
    }

    /**
     * not like 左匹配% (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value){
        return notLikeLeft(true, func, value);
    }

    /**
     * like %右匹配 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeRight(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.rlk, value));
        return this; // 返回自己本身
    }

    /**
     * like %右匹配 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeRight(EDbColumnFunc<T, ?> func, Object value){
        return likeRight(true, func, value);
    }

    /**
     * like %右匹配 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeRight(boolean condition, EDbColumnFunc<T, ?> func, Object value){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.notRlk, value));
        return this; // 返回自己本身
    }

    /**
     * like %右匹配 (无条件版本 - 保持原有行为)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value){
        return notLikeRight(true, func, value);
    }

    /**
     * 区间 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> between(boolean condition, EDbColumnFunc<T, ?> func, Object begin, Object end){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, EDbFilter.between(columnName, begin, end));
        return this; // 返回自己本身
    }

    /**
     * 区间 (无条件版本 - 保持原有行为)
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end){
        return between(true, func, begin, end);
    }

    /**
     * not 区间 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> notBetween(boolean condition, EDbColumnFunc<T, ?> func, Object begin, Object end){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, EDbFilter.notBetween(columnName, begin, end));
        return this; // 返回自己本身
    }

    /**
     * not 区间 (无条件版本 - 保持原有行为)
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end){
        return notBetween(true, func, begin, end);
    }

    /**
     * exists (条件版本)
     * @param condition - 是否添加此条件
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> exists(boolean condition, String existsSql){
        if (!condition) {
            return this;
        }
        doSome(true, sqlConnector, EDbFilter.exists(existsSql));
        return this; // 返回自己本身
    }

    /**
     * exists (无条件版本 - 保持原有行为)
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> exists(String existsSql){
        return exists(true, existsSql);
    }

    /**
     * not exists (条件版本)
     * @param condition - 是否添加此条件
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> notExists(boolean condition, String existsSql){
        if (!condition) {
            return this;
        }
        doSome(true, sqlConnector, EDbFilter.notExists(existsSql));
        return this; // 返回自己本身
    }

    /**
     * not exists (无条件版本 - 保持原有行为)
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> notExists(String existsSql){
        return notExists(true, existsSql);
    }

    /**
     * is null (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @return
     */
    public LambdaQuery<T> isNull(boolean condition, EDbColumnFunc<T, ?> func){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.isNull, null));
        return this; // 返回自己本身
    }

    /**
     * is null (无条件版本 - 保持原有行为)
     * @param func
     * @return
     */
    public LambdaQuery<T> isNull(EDbColumnFunc<T, ?> func){
        return isNull(true, func);
    }

    /**
     * is not null (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @return
     */
    public LambdaQuery<T> isNotNull(boolean condition, EDbColumnFunc<T, ?> func){
        if (!condition) {
            return this;
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(func);
        String columnName = EDbFilter.getColumnName(currentEntityClass, func);
        doSome(true, sqlConnector, new EDbFilter(columnName, EDbFilter.Operator.isNotNull, null));
        return this; // 返回自己本身
    }

    /**
     * is not null (无条件版本 - 保持原有行为)
     * @param func
     * @return
     */
    public LambdaQuery<T> isNotNull(EDbColumnFunc<T, ?> func){
        return isNotNull(true, func);
    }

    /**
     * groupBy (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 分组字段
     * @return
     */
    public LambdaGroupQuery<T> groupBy(boolean condition, EDbColumnFunc<T, ?>... funcs){
        if (!condition) {
            return this; // 返回 LambdaGroupQuery 类型
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(funcs[0]); // 假设所有funcs都属于同一个实体类
        StringBuilder propertys = new StringBuilder("");
        for(EDbColumnFunc<T, ?> func:funcs){
            if(propertys.length()>0){
                propertys.append(",");
            }
            // 使用 EDbFilter.getColumnName 获取列名
            String columnName = EDbFilter.getColumnName(currentEntityClass, func);
            propertys.append(columnName);
        }
        eDbQuery.groupBy(propertys.toString());;
        return this; // 返回 LambdaGroupQuery 类型
    }

    /**
     * groupBy (无条件版本 - 保持原有行为)
     * @param funcs
     * @return
     */
    public LambdaGroupQuery<T> groupBy(EDbColumnFunc<T, ?>... funcs){
        return groupBy(true, funcs);
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
     * order by column asc (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 排序字段
     * @return
     */
    public  LambdaOrderQuery<T> orderByAsc(boolean condition, EDbColumnFunc<T, ?>... funcs){
        if (!condition) {
            return this; // 返回 LambdaOrderQuery 类型
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(funcs[0]); // 假设所有funcs都属于同一个实体类
        for(EDbColumnFunc<T, ?> func:funcs){
            // 使用 EDbFilter.getColumnName 获取列名
            String columnName = EDbFilter.getColumnName(currentEntityClass, func);
            eDbQuery.orderASC(columnName);
        }
        return this; // 返回 LambdaOrderQuery 类型
    }

    /**
     * order by column asc (无条件版本 - 保持原有行为)
     * @param funcs
     * @return
     */
    public  LambdaOrderQuery<T> orderByAsc(EDbColumnFunc<T, ?>... funcs){
        return orderByAsc(true, funcs);
    }

    /**
     * order by column desc (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 排序字段
     * @return
     */
    public  LambdaOrderQuery<T> orderByDesc(boolean condition, EDbColumnFunc<T, ?>... funcs){
        if (!condition) {
            return this; // 返回 LambdaOrderQuery 类型
        }
        // 使用 EDbFilter 的方法获取列名
        Class<?> currentEntityClass = EDbFilter.getEntityClass(funcs[0]); // 假设所有funcs都属于同一个实体类
        for(EDbColumnFunc<T, ?> func:funcs){
            // 使用 EDbFilter.getColumnName 获取列名
            String columnName = EDbFilter.getColumnName(currentEntityClass, func);
            eDbQuery.orderDESC(columnName);
        }
        return this; // 返回 LambdaOrderQuery 类型
    }

    /**
     * order by column desc (无条件版本 - 保持原有行为)
     * @param funcs
     * @return
     */
    public  LambdaOrderQuery<T> orderByDesc(EDbColumnFunc<T, ?>... funcs){
        return orderByDesc(true, funcs);
    }

    /**
     * limit count (条件版本)
     * @param condition - 是否应用此设置
     * @param limitCount
     * @return
     */
    public  LambdaLimitQuery<T> limit(boolean condition, int limitCount){
        if (!condition) {
            return this; // 返回 LambdaLimitQuery 类型
        }
        eDbQuery.limit(limitCount);
        return this; // 返回 LambdaLimitQuery 类型
    }

    /**
     * limit count (无条件版本 - 保持原有行为)
     * @param limitCount
     * @return
     */
    public  LambdaLimitQuery<T> limit(int limitCount){
        return limit(true, limitCount);
    }

    /**
     * offset offsetIdx (条件版本)
     * @param condition - 是否应用此设置
     * @param offsetIdx
     * @return
     */
    public LambdaOffsetQuery<T> offset(boolean condition, int offsetIdx){
        if (!condition) {
            return this; // 返回 LambdaOffsetQuery 类型
        }
        eDbQuery.offset(offsetIdx);
        return this; // 返回 LambdaOffsetQuery 类型
    }

    /**
     * offset offsetIdx (无条件版本 - 保持原有行为)
     * @param offsetIdx
     * @return
     */
    public LambdaOffsetQuery<T> offset(int offsetIdx){
        return offset(true, offsetIdx);
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