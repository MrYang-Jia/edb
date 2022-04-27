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
package com.edbplus.db.query.lambda.update;

import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.em.SqlConnectorEnum;
import com.edbplus.db.query.lambda.*;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbLambdaUpdate
 * @Description: EDbLambdaUpdate
 * @Author 杨志佳
 * @Date 2022/4/15
 * @Version V1.0
 **/
public class EDbLambdaUpdate<T> implements LambdaUpdate<T> {

    public EDbQuery eDbQuery = new EDbQuery(); // 基础封装对象

    public SqlConnectorEnum sqlConnector = SqlConnectorEnum.and;

    public List<EDbLambdaQuery<T>> andComs;

    public List<EDbLambdaQuery<T>> orComs;

    private EDbPro eDbPro;

    // 更新字段
    private Map<String,Object> updateMap =  new HashMap<>();


    // class
    private Class<T> entityClass;

    /**
     * 创建查询对象
     * @param entityClass
     * @param <T>
     * @return
     */
    public static <T> EDbLambdaUpdate<T> lambda(Class<T> entityClass){
        return new EDbLambdaUpdate<T>(entityClass);
    }

    /**
     * 创建查询对象，并指定数据库
     * @param entityClass
     * @param configName
     * @param <T>
     * @return
     */
    public static <T> EDbLambdaUpdate<T> lambda(Class<T> entityClass,String configName){
        return new EDbLambdaUpdate<T>(entityClass,configName);
    }

    /**
     * 创建查询实例对象
     * @param entityClass
     */
    public EDbLambdaUpdate(Class<T> entityClass){
        eDbPro = EDb.use();
        this.entityClass = entityClass;
    }

    /**
     * 创建查询实例对象，并指定数据库
     * @param entityClass
     * @param configName
     */
    public EDbLambdaUpdate(Class<T> entityClass,String configName){
        eDbPro = EDb.use(configName);
        this.entityClass = entityClass;
    }

    // =================================================================================

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


    // =================================================================================

    /**
     * and (...)
     * @param func
     * @return
     */
    @Override
    public LambdaUpdate<T> andCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func) {
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
    @Override
    public LambdaUpdate<T> orCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func) {
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

    @Override
    public LambdaUpdate<T> or() {
        sqlConnector = SqlConnectorEnum.or;
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> and() {
        sqlConnector = SqlConnectorEnum.and;
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> lt(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.lt, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> le(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.le, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> eq(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.eq, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> ne(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.ne, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> in(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.in, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> notIn(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notIn, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> gt(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.gt, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> ge(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.ge, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> like(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.like, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> notLike(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notLike, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> likeLeft(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.llk, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notLlk, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> likeRight(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.rlk, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.notRlk, value));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end) {
        Column column = getColumn(func);
        doSome(sqlConnector,EDbFilter.between(column.name(),begin,end));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end) {
        Column column = getColumn(func);
        doSome(sqlConnector,EDbFilter.notBetween(column.name(),begin,end));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> exists(String existsSql) {
        doSome(sqlConnector,EDbFilter.exists(existsSql));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> notExists(String existsSql) {
        doSome(sqlConnector,EDbFilter.notExists(existsSql));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> isNull(EDbColumnFunc<T, ?> func) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.isNull, null));
        return this; // 返回自己本身
    }

    @Override
    public LambdaUpdate<T> isNotNull(EDbColumnFunc<T, ?> func) {
        Column column = getColumn(func);
        doSome(sqlConnector,new EDbFilter(column.name(), EDbFilter.Operator.isNotNull, null));
        return this; // 返回自己本身
    }


    /**
     * 更新字段
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> set(EDbColumnFunc<T, ?> func,Object value) {
        Column column = getColumn(func);
        this.updateMap.put(column.name() ,value);
        return this; // 返回自己本身
    }



    // 执行更新
    public boolean update(){
        return eDbPro.update(entityClass,updateMap,eDbQuery);
    }

    /**
     * 执行删除
     * @return
     */
    public boolean delete(){
        return eDbPro.delete(entityClass,eDbQuery);
    }
}
