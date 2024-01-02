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

import com.edbplus.db.util.hutool.annotation.EAnnotationUtil;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;

import javax.persistence.Table;
import java.util.*;

/**
 * @ClassName EDbQueryUtil
 * @Description: 单体对象快捷查询封装器 -- 处理逻辑工具类
 * @Author 杨志佳
 * @Date 2020/9/30
 * @Version V1.0
 **/
public class EDbQueryUtil {

    /**
     * 通过jpa过滤器加载生成sql和入参
     * @param eDbFilter
     * @param andSqlStr
     * @param paramsList
     */
    public static void loadFilter(EDbFilter eDbFilter, StringBuffer andSqlStr, List<Object> paramsList){

        if(eDbFilter.getOperator() == EDbFilter.Operator.exists || eDbFilter.getOperator() == EDbFilter.Operator.notExists){

        }else{
            // 驼峰转下划线 -- 如果是规范的驼峰写法，则不会有异常，否则需要去获取jpa对应的字段上的colum注解
            andSqlStr.append(eDbFilter.getProperty());
        }

        // 等于
        if(eDbFilter.getOperator() == EDbFilter.Operator.eq){
            if(eDbFilter.getValue() == null ){
                andSqlStr.append(" is null ");
            }else{
                andSqlStr.append(" = ? ");
                paramsList.add(eDbFilter.getValue());
            }
        }

        // 不等于
        if(eDbFilter.getOperator() == EDbFilter.Operator.ne){
            andSqlStr.append(" <> ? ");
            paramsList.add(eDbFilter.getValue());
        }

        // 大于
        if(eDbFilter.getOperator() == EDbFilter.Operator.gt ){
            andSqlStr.append(" > ? ");
            paramsList.add(eDbFilter.getValue());
        }

        // between 1 and 2
        if(eDbFilter.getOperator() == EDbFilter.Operator.between  ){
            LinkedList<Object> params = (LinkedList<Object>) eDbFilter.getValue();
            andSqlStr.append(" between ? ");
            paramsList.add(params.get(0));
            andSqlStr.append(" and ? ");
            paramsList.add(params.get(1));
        }

        // not between 1 and 2
        if(eDbFilter.getOperator() == EDbFilter.Operator.notBetween  ){
            LinkedList<Object> params = (LinkedList<Object>) eDbFilter.getValue();
            andSqlStr.append(" not between ? ");
            paramsList.add(params.get(0));
            andSqlStr.append(" and ? ");
            paramsList.add(params.get(1));
        }

        // 大于等于
        if(eDbFilter.getOperator() == EDbFilter.Operator.ge  ){
            andSqlStr.append(" >= ? ");
            paramsList.add(eDbFilter.getValue());

        }

        // 小于
        if(eDbFilter.getOperator() == EDbFilter.Operator.lt ){
            andSqlStr.append(" < ? ");
            paramsList.add(eDbFilter.getValue());

        }

        // 小于等于
        if(eDbFilter.getOperator() == EDbFilter.Operator.le ){
            andSqlStr.append(" <= ? ");
            paramsList.add(eDbFilter.getValue());

        }

        // 模糊匹配
        if(eDbFilter.getOperator() == EDbFilter.Operator.like  ){
            andSqlStr.append(" like ? ");
            paramsList.add("%"+eDbFilter.getValue()+"%");
        }

        if(eDbFilter.getOperator() == EDbFilter.Operator.rlk  ){
            andSqlStr.append(" like ? ");
            // 右匹配 ，左侧加 %
            paramsList.add("%"+eDbFilter.getValue());
        }

        if(eDbFilter.getOperator() == EDbFilter.Operator.llk  ){
            andSqlStr.append(" like ? ");
            // 左匹配 ，右侧加 %
            paramsList.add(eDbFilter.getValue()+"%");
        }

        // in
        if(eDbFilter.getOperator() == EDbFilter.Operator.in){
            andSqlStr.append(" in (");
            filterArrayFun(eDbFilter,andSqlStr,paramsList);
            andSqlStr.append(")");
        }

        // exists
        if(eDbFilter.getOperator() == EDbFilter.Operator.exists){
            andSqlStr.append(" exists (");
            andSqlStr.append(eDbFilter.getValue());
            andSqlStr.append(")");
        }

        // not exists
        if(eDbFilter.getOperator() == EDbFilter.Operator.notExists){
            andSqlStr.append(" not exists (");
            andSqlStr.append(eDbFilter.getValue());
            andSqlStr.append(")");
        }

        // not in
        if(eDbFilter.getOperator() == EDbFilter.Operator.notIn){
            andSqlStr.append(" not in (");
            filterArrayFun(eDbFilter,andSqlStr,paramsList);
            andSqlStr.append(")");
        }

        // not like
        if(eDbFilter.getOperator() == EDbFilter.Operator.notLike){
            andSqlStr.append(" not like ? ");
            paramsList.add("%"+eDbFilter.getValue()+"%");
        }

        // not likeLeft
        if(eDbFilter.getOperator() == EDbFilter.Operator.notLlk){
            andSqlStr.append(" not like ? ");
            paramsList.add(eDbFilter.getValue()+"%");
        }

        // not likeRight
        if(eDbFilter.getOperator() == EDbFilter.Operator.notRlk){
            andSqlStr.append(" not like ? ");
            paramsList.add("%"+eDbFilter.getValue());
        }

        // is not null
        if(eDbFilter.getOperator() == EDbFilter.Operator.isNotNull){
            andSqlStr.append(" is not null ");
        }

        // is null
        if(eDbFilter.getOperator() == EDbFilter.Operator.isNull){
            andSqlStr.append(" is null ");
        }
    }

    /**
     * 处理 fileter 数组逻辑部分
     * @param EDbFilter
     * @param andSqlStr
     * @param paramsList
     */
    public static void filterArrayFun(EDbFilter EDbFilter, StringBuffer andSqlStr, List<Object> paramsList){
        if (EDbFilter.getValue() instanceof Object[] ||
                EDbFilter.getValue() instanceof String[] ||
                EDbFilter.getValue() instanceof Integer[] ||
                EDbFilter.getValue() instanceof Long[] ||
                EDbFilter.getValue() instanceof List) {
            Object[] values = null;
            if(EDbFilter.getValue() instanceof List){
                values = ((List<Object>) EDbFilter.getValue()).toArray();
            }else{
                values = (Object[]) EDbFilter.getValue();
            }
            for(int j=0; j<values.length; j++) {
                andSqlStr.append("?");
                if(j < values.length - 1) {
                    andSqlStr.append(",");
                }
                paramsList.add(values[j]);
            }
        }else{
            andSqlStr.append("?");
            // 如果不是数组的情况，则直接回填对象即可
            paramsList.add(EDbFilter.getValue());
        }
    }


    /**
     * sql字段拼接
     * @param queryParams
     * @param andSqlStr
     * @param paramsList
     */
    public static void baseQueryFun(EDbBaseQuery queryParams,StringBuffer andSqlStr,List<Object> paramsList){
        EDbFilter eDbFilter = null;
        Boolean firstAnd = false;
        // and 部分
        for(int i = 0; i< queryParams.getAndEDbFilters().size(); i++){
            andSqlStr.append(" and ");
            eDbFilter = queryParams.getAndEDbFilters().get(i);
            // 加载过滤器生成sql部分
            loadFilter(eDbFilter,andSqlStr,paramsList);
            if(!firstAnd){ // 左侧首位 1=1 and的模式必须有才行
                firstAnd = true;
            }
        }

        // or 部分
        for(int i = 0; i< queryParams.getOrEDbFilters().size(); i++){
            if(!firstAnd){ // 1=1 左侧首位必须是and，否则会导致数据有误
                andSqlStr.append(" and ");
                firstAnd = true;
            }else{
                andSqlStr.append(" or ");
            }
            //
            eDbFilter =  queryParams.getOrEDbFilters().get(i);
            // 加载过滤器生成sql部分
            loadFilter(eDbFilter,andSqlStr,paramsList);
        }
        firstAnd = null;
    }

    /**
     * 处理where部分sql
     * @param queryParams
     * @return
     */
    public static String doWhereSql(EDbQuery queryParams,List<Object> paramsList){

        //
        StringBuffer andSqlStr =  new StringBuffer("");
        // 首次拼接前用 1=1 ，以便后续的对象可以 and 拼接
        andSqlStr.append(" 1=1 ");

        baseQueryFun(queryParams,andSqlStr,paramsList);

        if(queryParams.andComs!=null && queryParams.andComs.size()>0){
            for(EDbBaseQuery andCom:queryParams.andComs){
                // and ( filters ) 部分
                if(andCom.getQuerySize() >0 ) {
                    andSqlStr.append(" and ( 1=1 ");
                    baseQueryFun(andCom, andSqlStr, paramsList);
                    andSqlStr.append(" )");
                }
            }
        }

        if(queryParams.orComs!=null && queryParams.orComs.size()>0){
            for(EDbBaseQuery orCom:queryParams.orComs){
                // or ( filters ) 部分
                if(orCom.getQuerySize() >0 ){
                    andSqlStr.append(" or (  1=1 ");
                    baseQueryFun(orCom,andSqlStr,paramsList);
                    andSqlStr.append(" )");
                }
            }
        }

        // 拼接 group By 部分
        if(queryParams.getGroupByFilter()!=null){
            // 添加groupBy
            andSqlStr.append(" group by ").append(queryParams.getGroupByFilter().getProperty());
        }

        // having 的部分处理
        if(queryParams.getHavingFilter() != null ){
            andSqlStr.append(" having ").append(queryParams.getHavingFilter().getProperty());
            if(queryParams.getHavingFilter().getValue() != null){
                if(queryParams.getHavingFilter().getValue() instanceof Object[]){
                    Object[] opts = (Object[]) queryParams.getHavingFilter().getValue();
                    for (Object opt:opts){
                        paramsList.add(opt);
                    }
                }
            }
        }
        // 将 2=2 去掉，是为了保证不会被识别成是注入的代码块，其次是为了移除恒等式，避免不同的数据库恒等式格式不一样，例如 taos 的恒等式为 _c0 > 0 ,不等则为 _c0 = 0
        String andSql =  andSqlStr.toString().replaceAll("1=1  and","");
        // andSql =  andSql.replaceAll("1=1"," "); // 二次处理，最后将只有 1=1 的情况移除，只能外部 where 条件查询时处理才有效，所以这里注释掉，预留是为了避免再次犯错
        return andSql;
    }

    public static SqlPara getSqlParaForJpaQuery(String tableName, EDbQuery queryParams){
        return getSqlParaForJpaQuery(tableName,queryParams,new MysqlDialect());
    }

    /**
     * 根据 quary 对象动态生成sql语句，并去除 1=1 恒等式
     * @param tableName
     * @param queryParams
     * @return
     */
    public static SqlPara getSqlParaForJpaQuery(String tableName, EDbQuery queryParams, Dialect dialect){


        if(queryParams == null){
            queryParams = new EDbQuery();
        }

        // 处理where部分sql开始
//        //
//        List<Object> paramsList = new ArrayList<>();
//        //
//        StringBuffer andSqlStr =  new StringBuffer("");
//        // 首次拼接前用 1=1 ，以便后续的对象可以 and 拼接
//        andSqlStr.append(" 1=1 ");
//
//        baseQueryFun(queryParams,andSqlStr,paramsList);
//
//        if(queryParams.andComs!=null && queryParams.andComs.size()>0){
//            for(EDbBaseQuery andCom:queryParams.andComs){
//                // and ( filters ) 部分
//                if(andCom.getQuerySize() >0 ) {
//                    andSqlStr.append(" and ( 1=1 ");
//                    baseQueryFun(andCom, andSqlStr, paramsList);
//                    andSqlStr.append(" )");
//                }
//            }
//        }
//
//        if(queryParams.orComs!=null && queryParams.orComs.size()>0){
//            for(EDbBaseQuery orCom:queryParams.orComs){
//                // or ( filters ) 部分
//                if(orCom.getQuerySize() >0 ){
//                    andSqlStr.append(" or (  1=1 ");
//                    baseQueryFun(orCom,andSqlStr,paramsList);
//                    andSqlStr.append(" )");
//                }
//            }
//        }
//
//        // 拼接 group By 部分
//        if(queryParams.getGroupByFilter()!=null){
//            // 添加groupBy
//            andSqlStr.append(" group by ").append(queryParams.getGroupByFilter().getProperty());
//        }
//
//        // having 的部分处理
//        if(queryParams.getHavingFilter() != null ){
//            andSqlStr.append(" having ").append(queryParams.getHavingFilter().getProperty());
//            if(queryParams.getHavingFilter().getValue() != null){
//                if(queryParams.getHavingFilter().getValue() instanceof Object[]){
//                    Object[] opts = (Object[]) queryParams.getHavingFilter().getValue();
//                    for (Object opt:opts){
//                        paramsList.add(opt);
//                    }
//                }
//            }
//        }
//        // 将 2=2 去掉，是为了保证不会被识别成是注入的代码块
//        String andSql =  andSqlStr.toString().replaceAll("1=1  and","");

        List<Object> paramsList = new ArrayList<>();
        String andSql = doWhereSql(queryParams,paramsList);
        // ============ 处理 where 部分结束================

        StringBuffer orderSql =  new StringBuffer("");
        Order order = null;
        // order 部分
        if(queryParams.getOrders().size() > 0){
            orderSql.append(" order by ");
        }
        for(int i=0;i< queryParams.getOrders().size();i++){
            //
            order = (Order) queryParams.getOrders().get(i);

            // order 关键字才处理，否则不携带，避免引发心的问题
            if(order.getProperty().toLowerCase(Locale.ROOT).equals("order")){
                //
                if (dialect instanceof MysqlDialect) {
                    orderSql.append(" `").append(order.getProperty()).append("` ").append(order.getDirection().name());
                }else if (dialect instanceof PostgreSqlDialect){
                    // 先默认全小写，避免 pg 库不区分大小写的时候报错
                    orderSql.append(" \"").append(order.getProperty().toLowerCase(Locale.ROOT)).append("\" ").append(order.getDirection().name());
                }else{
                    orderSql.append(" ").append(order.getProperty()).append(" ").append(order.getDirection().name());
                }
            }else{
                orderSql.append(" ").append(order.getProperty()).append(" ").append(order.getDirection().name());
            }


            //
            if(i < queryParams.getOrders().size() - 1) {
                orderSql.append(",");
            }
        }

        // sqlPara
        SqlPara sqlPara = new SqlPara();
        // select xxx
        if( queryParams.getFieldsSql() != null){
            if(andSql.contains("1=1")){
                andSql =  andSql.replaceAll("1=1"," "); // 处理掉这个 1=1 恒等式
                sqlPara.setSql( "select " + queryParams.getFieldsSql() + " from " + tableName + andSql + queryParams.getLastSql()  + orderSql );
            }else{
                sqlPara.setSql( "select " + queryParams.getFieldsSql() + " from " + tableName + " where  " + andSql + queryParams.getLastSql()  + orderSql );
            }

        }else{
            if(andSql.contains("1=1")){
                andSql =  andSql.replaceAll("1=1"," "); // 处理掉这个 1=1 恒等式
                sqlPara.setSql("select * from "+ tableName + andSql + queryParams.getLastSql()   + orderSql );
            }else{
                //
                sqlPara.setSql("select * from "+ tableName + " where  " + andSql + queryParams.getLastSql()   + orderSql );
            }
        }
        // 传递参数对象
        for(Object para:paramsList){
            sqlPara.addPara(para);
        }

        // 获取条数
        if(queryParams.getLimit() != null){
            sqlPara.setSql( sqlPara.getSql() + " limit ? " );
            sqlPara.addPara( queryParams.getLimit() );
        }

        // 从什么位置开始读取
        if(queryParams.getOffset() != null){
            sqlPara.setSql( sqlPara.getSql() + " offset ? " );
            sqlPara.addPara( queryParams.getOffset() );
        }


        return sqlPara;
    }


    /**
     * 根据jpa单表查询对象返回 jdbcSql 查询对象
     * @param mClass
     * @param queryParams
     * @return
     */
    public static SqlPara getSqlParaForJpaQuery(Class<?> mClass, EDbQuery queryParams){
        return getSqlParaForJpaQuery(mClass,queryParams,new MysqlDialect());
    }

    /**
     * 根据jpa单表查询对象返回 jdbcSql 查询对象
     * @param mClass
     * @param queryParams
     * @return
     */
    public static SqlPara getSqlParaForJpaQuery(Class<?> mClass, EDbQuery queryParams, Dialect dialect){
        // 获取表注解
        Table table = EAnnotationUtil.getAnnotation( mClass , Table.class);
        if(table == null){
            throw new RuntimeException("@Table is not find");
        }
        // 如果有关键字的话，还需要根据数据库类型进行调整
        return getSqlParaForJpaQuery(table.name(),queryParams,dialect);
    }
}
