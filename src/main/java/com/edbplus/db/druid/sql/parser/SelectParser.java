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
package com.edbplus.db.druid.sql.parser;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGTypeCastExpr;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.util.StringUtils;
import com.edbplus.db.util.hutool.str.EStrUtil;
import org.testng.collections.Lists;

import java.util.*;

/**
 * @ClassName SelectParser
 * @Description: select解析器
 * @Author 杨志佳
 * @Date 2022/2/23
 * @Version V1.0
 **/
public class SelectParser {

    /**
     * 获取sql语句中查询字段
     *
     * @param sql
     * @param jdbcType
     * @return
     */
    public static SelectItems getSelectNames(String sql, String jdbcType) { //类型转换
        //Map<String,SelectItemInfo> selectItemInfoMap = new LinkedHashMap<>(); // 属性集合
        SelectItems selectItems =  new SelectItems();
        List<String> aliasNames = Lists.newArrayList();
        //格式化sql语句
//        String sql = SQLUtils.format(sqlOld, jdbcType);
        if (sql.contains("*")) {
            throw new RuntimeException("不支持语句中带 '*' ，必须明确指定查询的列");
        }
        // parser得到AST
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(
                sql, jdbcType);
        //只接受select 语句
        if (!Token.SELECT.equals(parser.getExprParser().getLexer().token())) {
            throw new RuntimeException("不支持 " + parser.getExprParser().getLexer().token() + " 语法,仅支持 SELECT 语法");
        }
        List<SQLStatement> stmtList = parser.parseStatementList();
        if (stmtList.size() > 1) {
            throw new RuntimeException("不支持多条SQL语句,当前是" + stmtList.size() + "条语句");
        }
        //接收查询字段
        List<SQLSelectItem> items = null;
        for (SQLStatement stmt : stmtList) {
            // stmt.accept(visitor);
//            System.out.println("->"+stmt.getClass().getSimpleName());
            if (stmt instanceof SQLSelectStatement) {
                SQLSelectStatement sstmt = (SQLSelectStatement) stmt;
                SQLSelect sqlselect = sstmt.getSelect();
                SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
                items = query.getSelectList(); // 将查询条件赋予

                SQLExprTableSource tableSource = null;
                SQLTableSource sqlTableSource;
                SQLTableSource subSqlTableSource;
                SQLPropertyExpr sqlPropertyExpr = null;
                SQLQueryExpr sqlQueryExpr;
                SQLObject sqlObject;
                SQLSelectQueryBlock sqlSelectQueryBlock;
                SQLSelect subQuerySelect;
                List<SQLSelectItem> selectItem; // 子查询的字段集合，默认只有一个字段
                List<SQLSelectItem> subSelectItem; // 复合查询的字段--最外围
                List<SQLExpr> sqlExprList;

                String columnName = null;
                String tableName = null;
                Map<String,SQLExprTableSource> aliasTables = new HashMap<>(); // 外部表别名集合
                for (SQLSelectItem s : items) {
                    String aliasName = StringUtils.isEmpty(s.getAlias()) ? s.toString() : s.getAlias();
                    //防止字段重复
                    if (!aliasNames.contains(aliasName)) {
                        if(s.getExpr() instanceof SQLPropertyExpr) { // 属性字段 (最外部定义的表)
                            sqlObject = s.getParent();
                            if(sqlObject!=null && sqlObject instanceof SQLSelectQueryBlock) {
                                sqlTableSource = ((SQLSelectQueryBlock) sqlObject).getFrom();
                                loadTableSourceAlias(sqlTableSource,aliasTables); // 加载外部表别名集合
                                break;
                            }
                        }
                    }
                }
                SelectItemInfo selectItemInfo = null;
                // 遍历select字段与表的实际映射关系
                for (SQLSelectItem s : items) {
                    String aliasName = StringUtils.isEmpty(s.getAlias()) ? s.toString() : s.getAlias();
                    //防止字段重复
                    if (!aliasNames.contains(aliasName)) {
                        // 在这里获取所有字段的映射关系，进行sql字段数据库解析
//                        System.out.println("->"+s.getExpr().getClass().getSimpleName());
                        if(s.getExpr() instanceof SQLPropertyExpr){ // 属性字段 (最外部定义的表)
                            sqlPropertyExpr = (SQLPropertyExpr) s.getExpr();
                            sqlObject = s.getParent();
                            if(sqlObject!=null && sqlObject instanceof SQLSelectQueryBlock){
                                sqlTableSource = ((SQLSelectQueryBlock) sqlObject).getFrom();
                                if(sqlTableSource!=null && sqlTableSource instanceof SQLJoinTableSource){
                                    // 获取表对象
                                    tableSource = loadTableSource(sqlTableSource,sqlPropertyExpr);
                                }else{
                                    tableSource = (SQLExprTableSource) sqlTableSource;
                                }
                                tableName =  tableSource.getName().getSimpleName();
                                columnName = sqlPropertyExpr.getName();
                                // 实际表名+表字段
//                                System.out.println("=1=>"+ tableName + "." + columnName +"==>"+aliasName);
                                loadSelectItmes(selectItems,tableName,columnName,aliasName);

                            }
                        }else if(s.getExpr() instanceof SQLQueryExpr){
                            sqlQueryExpr = (SQLQueryExpr) s.getExpr();
                            SQLSelect sqlSelect = sqlQueryExpr.subQuery;
                            if(sqlSelect!=null && sqlSelect.getQuery() instanceof SQLSelectQueryBlock){ // 这是一个内部 select
                                sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelect.getQuery();
                                sqlTableSource = sqlSelectQueryBlock.getFrom();
                                if(sqlTableSource instanceof SQLExprTableSource){
                                    tableSource = (SQLExprTableSource) sqlTableSource;
                                    selectItem = sqlSelectQueryBlock.getSelectList();
                                    SQLExpr sqlExpr =selectItem.get(0).getExpr();

                                    if(sqlExpr instanceof SQLIdentifierExpr){
                                        columnName = ((SQLIdentifierExpr) sqlExpr).getName();
                                    }else if(sqlExpr instanceof SQLMethodInvokeExpr){
                                        sqlExprList = ((SQLMethodInvokeExpr) sqlExpr).getArguments();
                                        for (SQLExpr sqlExpr1:sqlExprList){
                                            if(sqlExpr1 instanceof SQLPropertyExpr){
                                                columnName = ((SQLPropertyExpr) sqlExpr1).getName();
                                                break;
                                            }
                                        }
//                                        System.out.println("columnName->"+columnName);
                                    }else if(sqlExpr instanceof SQLPropertyExpr){
                                        columnName = ((SQLPropertyExpr) sqlExpr).getName();
                                    }else{
                                        System.out.println("===不支持==="+sqlExpr.getClass().getSimpleName());
                                    }
                                    tableName =  tableSource.getName().getSimpleName();
//                                    System.out.println("=21=>"+ tableName + "."+ columnName+"==>"+aliasName);
                                    loadSelectItmes(selectItems,tableName,columnName,aliasName);
                                }else if(sqlTableSource instanceof SQLSubqueryTableSource){ // 复合sql，内部不止定义了一个select
                                    subQuerySelect = ((SQLSubqueryTableSource) sqlTableSource).getSelect();
                                    sqlSelectQueryBlock = (SQLSelectQueryBlock) subQuerySelect.getQuery(); // 内部的一个select查询
                                    subSelectItem = sqlSelectQueryBlock.getSelectList(); // 复合查询对应的字段
                                    subSqlTableSource = sqlSelectQueryBlock.getFrom(); // form 主体对象，可能内部也是复合查询!!!
                                    // 获取表字段，但是如果sql的表别名，子查询的表别名与外部表重叠，将会发生错误信息！！！ 所以目前只处理相对比较简单的模式
                                    tableSource = loadTableSource(subSqlTableSource, (SQLPropertyExpr) subSelectItem.get(0).getExpr());
                                    tableName = tableSource.getTableName();
                                    columnName = ((SQLPropertyExpr) subSelectItem.get(0).getExpr()).getName();
//                                    System.out.println("=22=>"+tableName + "." + columnName+"==>"+aliasName);
                                    loadSelectItmes(selectItems,tableName,columnName,aliasName);
                                }
                            }else{
                                System.out.println("=23=>"+s.getClass().getSimpleName()+"==>"+aliasName);
                            }

//                            System.out.println("=2=>"+s.getAlias() +"->" + s.getClass().getSimpleName());
                        }else if(s.getExpr() instanceof SQLCaseExpr){
                            SQLCaseExpr sqlCaseExpr = (SQLCaseExpr) s.getExpr();
                            List<SQLCaseExpr.Item> sqlCaseExprItems =sqlCaseExpr.getItems(); // 获取 case when then 结果1 else 结果2 end --> 即结果集
                            if(sqlCaseExprItems!=null && sqlCaseExprItems.size() > 0){ // 正常是一定会有这个结果集
                                for(SQLCaseExpr.Item item:sqlCaseExprItems){
                                    // 字段 -- bigint number char 主要是这三类
                                    if(item.getValueExpr().computeDataType()!=null){
//                                        System.out.println("-311->"+item.getValueExpr().computeDataType().getName());
                                        tableName =  "null";
                                        columnName = s.getAlias();
                                        loadSelectItmes(selectItems,tableName,columnName,aliasName, (SQLValuableExpr) item.getValueExpr());
                                        break;
                                    }else{
//                                        System.out.println("-312->"+item.getValueExpr().getClass().getSimpleName());
                                        if(item.getValueExpr() instanceof SQLPropertyExpr){
                                            tableName = aliasTables.get(((SQLPropertyExpr) item.getValueExpr()).getOwner().toString()).getTableName();
                                            columnName = ((SQLPropertyExpr) item.getValueExpr()).getName();
//                                System.out.println("=31=>"+ tableName+"."+columnName+"==>"+aliasName);
                                            loadSelectItmes(selectItems,tableName,columnName,aliasName);
                                            break;
                                        }else{
                                            System.out.println("-313->"+item.getValueExpr().getClass().getSimpleName());
                                        }
                                    }
                                }
                            }

                        // ============== 这部分被注释的代码主要用途在于解析源从哪里来，然后用来做什么判断 ，例如 case (select t1.a from t1 limit 1) when t1.a >1 then xxx else xxx end
//                            SQLExpr sqlExpr = sqlCaseExpr.getValueExpr();
//                            if(sqlExpr instanceof SQLPropertyExpr){
//                                tableName = aliasTables.get(((SQLPropertyExpr) sqlExpr).getOwner().toString()).getTableName();
//                                columnName = ((SQLPropertyExpr) sqlExpr).getName();
////                                System.out.println("=31=>"+ tableName+"."+columnName+"==>"+aliasName);
//                                loadSelectItmes(selectItems,tableName,columnName,aliasName);
//                            }else if(sqlExpr instanceof SQLQueryExpr){ // 子查询
//                                subQuerySelect = ((SQLQueryExpr) sqlExpr).getSubQuery();
//                                sqlSelectQueryBlock = (SQLSelectQueryBlock) subQuerySelect.getQuery();
//                                subSelectItem = sqlSelectQueryBlock.getSelectList();
//                                tableSource = (SQLExprTableSource) sqlSelectQueryBlock.getFrom();
//                                tableName = tableSource.getTableName();
//                                columnName = subSelectItem.get(0).toString();
////                                System.out.println("=32=>"+ tableName+"."+columnName+"==>"+aliasName);
//                                loadSelectItmes(selectItems,tableName,columnName,aliasName);
//                            }
                            // ===============================================================================
//                            System.out.println("=3=>"+ sqlCaseExpr);
                        }else if(s.getExpr() instanceof SQLValuableExpr){ // SQLIntegerExpr 等 SQLValuableExpr 类型
                            // 这个是直接返回数字字段
                            tableName = "null";
                            columnName = s.getAlias();
//                            System.out.println("=4=>"+ tableName+"."+columnName+"==>"+aliasName);
                            loadSelectItmes(selectItems,tableName,columnName,aliasName, (SQLValuableExpr) s.getExpr());
                        }else if(s.getExpr() instanceof PGTypeCastExpr){ // 当字段有属性转换时触发，例如 fieldName :: String 的特殊属性转换用法
                            PGTypeCastExpr pgTypeCastExpr = (PGTypeCastExpr) s.getExpr();
                            SQLExpr sqlExpr = pgTypeCastExpr.getExpr();
                            if(sqlExpr instanceof SQLPropertyExpr){
                                tableName = aliasTables.get(((SQLPropertyExpr) sqlExpr).getOwner().toString()).getTableName();
                                columnName = ((SQLPropertyExpr) sqlExpr).getName();
//                                System.out.println("=51=>"+ tableName+"."+columnName+"==>"+aliasName);
                                loadSelectItmes(selectItems,tableName,columnName,aliasName);
                            }else{
                                System.out.println("=5=>"+s.getExpr().getClass().getSimpleName()+"==>"+aliasName);
                            }
                        }else{
                            System.out.println("=6=>"+s.getExpr().getClass().getSimpleName()+"==>"+aliasName);
                        }

                        aliasNames.add(aliasName);
                        tableSource = null;
                        columnName = null;
                        tableName = null;
                    }
                }
//                SQLTableSource tableSource = query.getFrom();
//                System.out.println(tableSource);
            }
        }
        aliasNames = null;
        return selectItems;
    }

    /**
     * 加载表字段属性集
     * @param selectItems
     * @param tableName
     * @param columnName
     * @param aliasName
     */
    public static void loadSelectItmes(SelectItems selectItems,String tableName,String columnName,String aliasName){
        loadSelectItmes(selectItems,tableName,columnName,aliasName,null);
    }

    /**
     * 加载表字段属性集
     * @param selectItems
     * @param tableName
     * @param columnName
     * @param aliasName
     * @param sqlValuableExpr -- 字段的数据类型
     */
    public static void loadSelectItmes(SelectItems selectItems,String tableName,String columnName,String aliasName,SQLValuableExpr sqlValuableExpr){
        tableName = tableName.toLowerCase(Locale.ROOT); // 全部转小写
        columnName =  columnName.toLowerCase(Locale.ROOT); // 全转小写
        SelectItemInfo selectItemInfo = new SelectItemInfo();
        selectItemInfo.setTableName(tableName).setColumnName(columnName).setAliasName(aliasName);
        String columnType = "";
        // java code name -- 驼峰字段名
        if(aliasName.contains(".")){
            aliasName = aliasName.split("\\.")[1]; // 一般表字段名不会取驼峰命名，而是以下划线命名的方式
            aliasName = aliasName.toLowerCase(Locale.ROOT); // 全转小写
            selectItemInfo.setJavaCodeName(EStrUtil.toCamelCase(aliasName)); // 表别名+字段名
        }else{
            if(EStrUtil.isUpperCase(aliasName) || EStrUtil.isUpperCase(aliasName.substring(0,1))){ // 如果字符串都是大写 或 首字母是大写 --> 即不符合驼峰式写法时触发
                aliasName = aliasName.toLowerCase(Locale.ROOT); // 全转小写
            }
            selectItemInfo.setJavaCodeName(EStrUtil.toCamelCase(aliasName));
        }
        if(sqlValuableExpr!=null){ // 这个如果不是null的话，则可以提前赋予java的属性类型，这种一般是自定义一种固定属性的类型赋予前端
            // number char bigint 主要是这三类
            columnType = sqlValuableExpr.computeDataType().getName();
            if(columnType.equals("number")){
                selectItemInfo.setJavaType("BigDecimal");
            }else
            if(columnType.equals("char")){
                selectItemInfo.setJavaType("String");
                if(sqlValuableExpr instanceof SQLCharExpr){ // 如果是字符串类型，则转换
                    selectItemInfo.setColumnComment(((SQLCharExpr)sqlValuableExpr).getText());
                }
            }else
            if(columnType.equals("bigint")){
                selectItemInfo.setJavaType("Long");
            }else{// 其他情况
                System.out.println("-0->"+tableName+"."+columnName+"->"+columnType);
            }
        }

        Map<String,SelectItemInfo> selectItemInfoMap =  null;
        // 先根据表名
        if(!selectItems.tables.contains(tableName)){
            selectItems.tables.add(tableName);
            selectItemInfoMap = new HashMap<>();
        }else{
            selectItemInfoMap = selectItems.get(tableName);
        }
        selectItemInfoMap.put(columnName,selectItemInfo); // 字段映射
        selectItems.put(tableName,selectItemInfoMap); // 表字段
        selectItems.selectItemInfoList.add(selectItemInfo); // 属性集
        if(!selectItems.columns.contains(columnName)) // 去重(不想去重，可以直接拿字段集合，对应到表名，从而一一对应)
        selectItems.columns.add(columnName); // 表字段集(可能存在重复的)
    }

    /**
     * 目前只取一层，如果内部再定义一层，则需要通过查询 sqlSelectQueryBlock.getSelectList(); 来对应内部最外层的字段
     * @param sqlTableSource
     * @param sqlPropertyExpr
     * @return
     */
    public static SQLExprTableSource loadTableSource(SQLTableSource sqlTableSource, SQLPropertyExpr sqlPropertyExpr){
        SQLExprTableSource tableSource = null;
        if(sqlTableSource instanceof SQLJoinTableSource){
            SQLTableSource leftTable = ((SQLJoinTableSource) sqlTableSource).getLeft();
            SQLTableSource rightTable = ((SQLJoinTableSource) sqlTableSource).getRight();
            tableSource =  null;
            if(leftTable instanceof SQLJoinTableSource){
                tableSource = loadTableSource(leftTable,sqlPropertyExpr);
            }
            if(rightTable instanceof SQLJoinTableSource){
                tableSource = loadTableSource(rightTable,sqlPropertyExpr);
            }
            if(tableSource!=null){
                return tableSource; // 如果已获取到实际数据则返回
            }
            if(leftTable!=null && sqlPropertyExpr!=null && leftTable.getAlias()!=null && leftTable.getAlias().equals(sqlPropertyExpr.getOwnerName())){
                tableSource = ((SQLExprTableSource)leftTable);
            }
            if(rightTable!=null && sqlPropertyExpr!=null && rightTable.getAlias()!=null && rightTable.getAlias().equals(sqlPropertyExpr.getOwnerName())){
                tableSource = ((SQLExprTableSource)rightTable);
            }
        }else{
            tableSource = (SQLExprTableSource) sqlTableSource;
        }
        return tableSource;
    }

    /**
     *
     * @param sqlTableSource
     * @param aliasTables
     */
    public static void loadTableSourceAlias(SQLTableSource sqlTableSource, Map<String,SQLExprTableSource> aliasTables){
        SQLExprTableSource tableSource=null;
        if(sqlTableSource instanceof SQLJoinTableSource){
            SQLTableSource leftTable = ((SQLJoinTableSource) sqlTableSource).getLeft();
            SQLTableSource rightTable = ((SQLJoinTableSource) sqlTableSource).getRight();
            tableSource =  null;
            if(leftTable instanceof SQLJoinTableSource){
                loadTableSourceAlias(leftTable,aliasTables);
            }else{
                tableSource = (SQLExprTableSource) leftTable;
                if(aliasTables.get(tableSource.getAlias())==null){
                    aliasTables.put(tableSource.getAlias(),tableSource);
                }
            }
            if(rightTable instanceof SQLJoinTableSource){
                loadTableSourceAlias(rightTable,aliasTables);
            }else{
                tableSource = (SQLExprTableSource) rightTable;
                if(aliasTables.get(tableSource.getAlias())==null){
                    aliasTables.put(tableSource.getAlias(),tableSource);
                }
            }
        }else{
            tableSource = (SQLExprTableSource) sqlTableSource;
            if(aliasTables.get(tableSource.getAlias())==null){
                aliasTables.put(tableSource.getAlias(),tableSource);
            }
        }

    }
}
