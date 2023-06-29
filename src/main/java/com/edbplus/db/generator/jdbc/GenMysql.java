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
package com.edbplus.db.generator.jdbc;


/**
 * Mysql常用表语句
 *
 * @author MrYang
 * @date 2019-02-26
 */
public class GenMysql {

    /**
     * 识别用 , 分割的多表或多字段语句，并改写语句
     * @param tableName
     * @return
     */
    public static String getTablesSql(String tableName){
        return splitSqlParams(tableName);
    }

    /**
     * 拆分属性字段，形成sql参数
     * @param tableName
     * @return
     */
    public static String splitSqlParams(String tableName){

        // 如果已经包含了单引号，则不需要再进行一次改写
        if(tableName.contains("'")){
            return tableName;
        }

        String[] tbs = tableName.split(",");
        StringBuilder sql = new StringBuilder("");
        for(String tb : tbs){
            // 前面有字段时，添加逗号即可
            if(sql.length() > 0){
                sql.append(",");
            }
            sql.append("'").append(tb).append("'");
        }
        return sql.toString();
    }

    /**
     * 返回表对象信息Sql
     * @param tableName
     * @return
     */
    public static String getTableInfoSql(String tableName){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" 	table_name tableName ");
        sql.append(" 	,ENGINE ");
        sql.append(" 	,table_comment tableComment ");
        sql.append(" 	,create_time createTime ");
        sql.append(" FROM ");
        sql.append(" 	information_schema. TABLES ");
        sql.append(" WHERE ");
        sql.append(" 	table_schema = (SELECT DATABASE()) ");
        // table表名称
        if(tableName != null && tableName.length() > 0){
            if(tableName.contains(",")){
                sql.append(" AND table_name in (").append(getTablesSql(tableName)).append(") ");
            }else{
                sql.append(" AND table_name = ").append(getTablesSql(tableName)).append(" ");
            }
        }
        sql.append(" order by table_name ");
        return sql.toString();
    }

    /**
     * 获取所有表对象
     * @return
     */
    public static String getAllTableInfoSql(){
        // 获取表对象
        return getTableInfoSql(null);
    }

    /**
     * 返回表字段信息sql
     * @param tableName
     * @return
     */
    public static String getTableColumnsSql(String tableName){

        return getTableColumnsSql(tableName,false);
    }

    /**
     * 根据传入的
     * @param tableName
     * @param isOnlyAutoIncrement -- 是否只获取自增键值的语句
     * @return
     */
    public static String getTableColumnsSql(String tableName,boolean isOnlyAutoIncrement ){
        return getTableColumnsSql(tableName,null,isOnlyAutoIncrement);
    }

    /**
     * 根据传入的表名和字段查询表字段信息
     * @param tableName
     * @param columnName
     * @return
     */
    public static String getTableColumnsSql(String tableName,String columnName ){
        return getTableColumnsSql(tableName,columnName,false);
    }

    /**
     * 根据传入的
     * @param tableName
     * @param columnName -- 传递查询字段
     * @param isOnlyAutoIncrement -- 是否只获取自增键值的语句
     * @return
     */
    public static String getTableColumnsSql(String tableName,String columnName,boolean isOnlyAutoIncrement ){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        // 添加顺序
        sql.append(" ordinal_position ordinalPosition, ");
        // 添加表名字段
        sql.append(" table_name tableName, ");
        sql.append(" 	column_name columnName, ");
        sql.append(" 	COLUMN_DEFAULT columnDefault, ");
        sql.append(" 	data_type dataType, ");
        sql.append(" 	column_comment columnComment, ");
        sql.append(" 	column_key columnKey, ");
        sql.append(" 	extra, ");
        sql.append(" 	( ");
        sql.append(" 		CASE ");
        sql.append(" 		WHEN IS_NULLABLE = 'YES' THEN ");
        sql.append(" 			'1' ");
        sql.append(" 		ELSE ");
        sql.append(" 			'0' ");
        sql.append(" 		END ");
        sql.append(" 	) isN, ");
        sql.append(" 	SUBSTRING( ");
        sql.append(" 		column_type, ");
        sql.append(" 		INSTR(column_type, '(') + 1, ");
        sql.append(" 		INSTR(column_type, ')') - INSTR(column_type, '(') - 1 ");
        sql.append(" 	) maxL ");
        sql.append(",column_type columnType,(locate( 'unsigned',column_type)>0) as signedType "); // 增加 mysql unsigned 的识别
        sql.append(" FROM ");
        sql.append(" 	information_schema. COLUMNS ");
        sql.append(" WHERE ");
        // 如果没有传入表单，则全部查询
        if( tableName!=null && tableName.length()>0 ){
            if(tableName.contains(",")){
                sql.append("  table_name in (").append(getTablesSql(tableName)).append(") ");
            }else{
                sql.append("  table_name = ").append(getTablesSql(tableName)).append(" ");
            }
        }else
        {
            sql.append("  1=1 ");
        }
        // 根据传入的字段过滤掉其他信息
        if(columnName!=null && columnName.length()>0)//column_name
        {
            if(columnName.contains(",")){
                sql.append(" and column_name in (").append(splitSqlParams(columnName)).append(") ");
            }else{
                sql.append(" and column_name = '").append(columnName).append("' ");
            }
        }

        // 查询指定的当前库
        sql.append(" AND table_schema = (SELECT DATABASE()) ");
        // 只获取自增键值的语句
        if(isOnlyAutoIncrement){
            // 区分正则
            sql.append(" and extra = 'auto_increment' ");
        }
        sql.append(" ORDER BY ");
        // 根据表名和字段顺序排序
        sql.append(" 	table_name,ordinal_position ");
        return sql.toString();
    }

    /**
     * 查询所有数据库表结构
     * @return
     */
    public static String getAllTableColumnsSql(){
        return getTableColumnsSql(null);
    }

    /**
     * 返回表索引 - show index
     * @param tableName
     * @return
     */
    public static String getTableIndexSql(String tableName){
        StringBuffer sql = new StringBuffer("");
        sql.append(" show index from ").append(tableName).append(" ");
        return sql.toString();
    }

    /**
     * 返回所有表分布式键 -- 基于唯一值的字段返回
     * @return
     */
    public static String getAllIndexForDistributed(){
        return getIndexForDistributed(null);
    }

    /**
     * 返回表分布式键 -- 基于唯一值的字段返回
     * @param tableName
     * @return
     */
    public static String getIndexForDistributed(String tableName){
        StringBuffer sql = new StringBuffer("");
        sql.append(" select  table_schema tableSchema, ");
        sql.append(" table_name tableName, ");
        sql.append(" non_unique notUnique, ");
        // 根据字段名进行组合，其中重复的字段剔除
        sql.append(" group_concat(distinct column_name) columnName ");

        sql.append(" from INFORMATION_SCHEMA.STATISTICS");

        sql.append(" where 1=1 ");
        // 当前数据库
        sql.append(" AND table_schema = ( SELECT DATABASE ( ) ) ");
        // 只获取唯一值(含主键)
        sql.append(" and non_unique = 0 ");
        // 如果table有值的话
        if( tableName != null ){
            if(tableName.contains(",")){
                sql.append(" and table_name in (").append(getTablesSql(tableName)).append(") ");
            }else{
                sql.append(" and table_name = ").append(getTablesSql(tableName)).append(" ");
            }

        }
        // 根据数据库，表名，非唯一值 统计
        sql.append(" group by table_schema,table_name,non_unique ");
        // 根据表名顺序排序
        sql.append(" order by table_name ");

        return sql.toString();
    }

    /**
     * 返回所有表的所有索引,并将索引字段组合起来
     * @return
     */
    public static String getAllIndex(){
        return getIndex(null);
    }

    /**
     * 返回表的所有索引,并将索引字段组合起来
     * @param tableName
     * @return
     */
    public static String getIndex(String tableName){
        StringBuffer sql = new StringBuffer("");
        sql.append(" select  table_schema tableSchema, ");
        sql.append(" table_name tableName, ");
        sql.append(" non_unique notUnique, ");
        // 索引名称
        sql.append(" index_name indexName, ");
        // 根据字段名进行组合，其中重复的字段剔除
        sql.append(" group_concat(column_name) columnName ");

        sql.append(" from INFORMATION_SCHEMA.STATISTICS");

        sql.append(" where 1=1 ");
        // 当前数据库
        sql.append(" AND table_schema = ( SELECT DATABASE ( ) ) ");
        // 如果table有值的话
        if( tableName != null ){
            if( tableName.contains(",") ){
                sql.append(" and table_name in (").append(getTablesSql(tableName)).append(") ");
            }else
            {
                sql.append(" and table_name = ").append(getTablesSql(tableName)).append(" ");
            }
        }
        // 根据数据库，表名，非唯一值，索引名称 统计
        sql.append(" group by table_schema,table_name,non_unique,index_name ");
        // 根据表名、索引名称(主键优先 -- 因为分布式系统建议唯一键值时，需要优先考虑主键搭配键值建立唯一键值)
        sql.append(" order by table_name,(case when index_name='PRIMARY' then 0 else 1 end) ");

        return sql.toString();
    }


    /**
     * 获取创建表语句sql
     * @param tableName
     * @return
     */
    public static String getCreateTable(String tableName){
        StringBuilder sql =  new StringBuilder("");
        sql.append(" show create table ").append(tableName);
        return sql.toString();
    }

}
