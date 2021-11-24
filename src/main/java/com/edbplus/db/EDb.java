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
package com.edbplus.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.query.EDbQuery;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.plugin.activerecord.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @ClassName EDb
 * @Description: jfinal - Db的替代类
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Slf4j
@JsonIgnoreType
// 因为方法名以getxxx开头，如果没有参数的话，会被当作是属性对象返回给前端，所以接下来方法名命名要注意不能以get开头
@JsonIgnoreProperties({"realJpaClass","dbPro", "tableName","columnsMap","relKey","relKeyForFutrue","allRel","allRelForFutrue","countSql"})
public class EDb extends Db{

    // 连接池
    public static final Map<String, ExecutorService> edbFutruePools = new SyncWriteMap<String, ExecutorService>(32, 0.25F);

    // 主要访问的对象 -- 私有可以避免对外暴露信息，尤其是转json时
    private static EDbPro MAIN = null;

    // 主要载体
    private static final Map<String, EDbPro> dbMap = new SyncWriteMap<String, EDbPro>(32, 0.25F);


    /**
     * 初始化 dbPro 对象
     */
    public static void init(){
        if(MAIN == null){
            EDbPro eDbPro = new EDbPro();
            // 加载主对象
            dbMap.put(DbKit.MAIN_CONFIG_NAME,eDbPro);
            // 加载主体对象
            MAIN = eDbPro;
            initPool(DbKit.MAIN_CONFIG_NAME,eDbPro);
        }

    }

    /**
     * 初始化线程池
     * @param configName
     * @param eDbPro
     */
    public static void initPool(String configName, EDbPro eDbPro){
        DruidDataSource dataSource = (DruidDataSource) eDbPro.getConfig().getDataSource();
        // 并发线程控制，预留 0.5 的连接数给予系统应用，避免大量并发导致线程堵塞
        edbFutruePools.put(configName,   Executors.newFixedThreadPool((int) (dataSource.getMaxActive() * 0.5) ));
    }

    /**
     * 初始化线程池
     * @param configName
     */
    public static void init(String configName){
        if(configName==null){
            init();
            return;
        }
        // 如果是默认等于main，则初始化默认对象即可
        if(configName.equals(DbKit.MAIN_CONFIG_NAME)){
            init();
            return;
        }
        EDbPro eDbPro = new EDbPro(configName);
        init(configName,eDbPro);
        log.info("初始化 "+configName+" 的EDbPro数据对象成功");
    }


    /**
     * 初始化 EDbPro
     * @param configName
     * @param EDbPro
     */
    public static void init(String configName, EDbPro EDbPro){
        // 加载主对象
        dbMap.put(configName,EDbPro);
        initPool(configName,EDbPro);
    }



    /**
     * 返回主对象
     * @return
     */
    public static EDbPro use(){
        return MAIN;
    }

    /**
     * 切换对象
     * @param configName
     * @return
     */
    public static EDbPro use(String configName){
        return dbMap.get(configName);
    }

    // ================================== 以下为扩展的jpa主体方法 ===============================

//    /**
//     * 返回一个可操作的 jpa 数据实体
//     * @param t
//     * @param <T>
//     * @return
//     */
//    public static <T> T forUpdate(Class<T> t){
//        T obj = null;
//        T newObj = null;
//        try {
//            obj = t.newInstance();
//            newObj = t.newInstance();
//            String uuid = UUID.randomUUID().toString();
//            Field originalField = JpaAnnotationUtil.getFieldForAnnationClass(t, EDbUuid.class);
//            // 如果有字段用于存储旧jpa的值则赋予
//            if(originalField != null){
//                // 设置原对象的UUID，便于标识
//                ReflectUtil.setFieldValue(obj,originalField,uuid);
//                // 新的对象标识为同一个对象，便于比较变化
//                ReflectUtil.setFieldValue(newObj,originalField,uuid);
//                // 设置对象初始值 -- new 一个新的对象
////                JpaBuilder.setOriginalBean(uuid,newObj);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        return obj;
//    }


    /**
     * 返回表名称
     * @param mClass
     * @param <M>
     * @return
     */
    public static  <M> String getTableName(Class<M> mClass)
    {
        return MAIN.getTableName(mClass);
    }

    /**
     * 返回数据库字段
     * @param m
     * @param ignoreNullValue  - true -屏蔽 null , false - 包含null
     * @param <M>
     * @return
     */
    public static <M> Map<String,Object> getColumnsMap(M m,boolean ignoreNullValue){
        return MAIN.getColumnsMap(m,ignoreNullValue);
    }


    /**
     * 根据主键找对象
     * @param mClass
     * @param idValue
     * @param <M>
     * @return
     */
    public static  <M> M findById(Class<M> mClass, Object idValue ) {
        return MAIN.findById(mClass,idValue);
    }

    /**
     * 根据复合主键找对象
     * @param mClass
     * @param idValues
     * @param <M>
     * @return
     */
    public static  <M> M findByGroupId(Class<M> mClass, Object... idValues ) {
        return MAIN.findByGroupId(mClass,idValues);
    }

    /**
     * 保存并返回int结果，1：成功 2:失败
     * @param m
     * @param <M>
     * @return
     */
    public static <M> int saveReInt(M m){
        return MAIN.saveReInt(m);
    }

    /**
     * 保存jpa对象
     * @param m
     * @param <M>
     * @return
     */
    public static <M> boolean save(M m)
    {
        return MAIN.save(m);
    }

    /**
     * 批量保存记录 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常,并且该返回是没有主键id的回填
     * 缺陷：会多消耗1倍数据对象的内存去执行操作 ，推荐用 insertValues -- 比较省内存
     * @param mClass
     * @param saveList
     * @param batchSize
     * @param <M>
     * @return
     */
    public static  <M> int[] batchSave(Class<M> mClass, List<M> saveList, int batchSize){
        return MAIN.batchSave(mClass,saveList,batchSize);
    }

    /**
     * 批量提交并返回主键
     * 目前只测试了mysql会返回，所以独立方法，避免影响到原 jfinal 的批量提交
     * @param mClass
     * @param saveList
     * @param batchSize
     * @param <M>
     * @return
     */
    public static  <M> int[] batchSaveRid(Class<M> mClass,List<M> saveList,int batchSize)
    {
        return MAIN.batchSaveRid(mClass,saveList,batchSize);
    }

    /**
     * 使用 insert values(...),(...) 的方式批量插入对象
     * @param objs
     * @param batchSize
     * @param <T>
     */
    public static  <T>  int  insertValues(Class<T> tClass,List<T> objs,int batchSize){
        return MAIN.insertValues(tClass,objs,batchSize);
    }

    /**
     * 更新对象
     * @param m
     * @param <M>
     * @return
     */
    public static  <M> boolean update(M m){
        return MAIN.update(m);
    }

    /**
     * 更新对象 -- 剔除null值，成功返回1，失败返回0
     * @param m
     * @param <M>
     * @return
     */
    public static <M> int updateReInt(M m){
        return MAIN.updateReInt(m);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param m
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public static  <M> int updateReInt(M m,boolean containsNullValue){
        return MAIN.updateReInt(m,containsNullValue);
    }

    /**
     * 更新对象
     * @param m
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public static  <M> boolean update(M m,boolean containsNullValue){
        return MAIN.update(m,containsNullValue);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @param <M>
     * @return
     */
    public static  <M> int updateReInt(Class<M> mClass,Map<String,Object> updateData){
        return MAIN.updateReInt(mClass,updateData);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @param isColumnName -- 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @param <M>
     * @return
     */
    public static <M> int updateReInt(Class<M> mClass,Map<String,Object> updateData,boolean isColumnName){
        return MAIN.updateReInt(mClass,updateData,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param mClass
     * @param updateData -- 数据库表字段(非驼峰对象)
     * @param <M>
     * @return
     */
    public static  <M> boolean update(Class<M> mClass,Map<String,Object> updateData){
        return MAIN.update(mClass,updateData);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @param <M> -- 数据集
     * @isColumnName 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public static <M> boolean update(Class<M> mClass,Map<String,Object> updateData,boolean isColumnName){
        return MAIN.update(mClass,updateData,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * 建议前端传递变化前和变化后的数据对象给后端，保存的结果页更贴近实际真实变化
     * @param oldM -- 原始数据对象
     * @param updateM -- 基于原始数据变更后的数据对象
     * @param <M>
     * @return
     */
    public static  <M> boolean updateCompare(M oldM,M updateM){
        return MAIN.updateCompare(oldM,updateM);
    }

    /**
     * 批量更新 -- 推荐使用该方式
     * @param mClass
     * @param updateList
     * @param updateFields -- 指定要更新的字段，必须要有 column 对应，否则会有异常
     * @param batchSize
     * @param <M>
     * @return
     */
    public static <M> int[] batchUpdate(Class<M> mClass,List<M> updateList,List<String> updateFields, int batchSize){
        return MAIN.batchUpdate(mClass,updateList,updateFields,batchSize);
    }


    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param mClass
     * @param updateList
     * @param batchSize
     * @param <M>
     * @return
     */
    public static  <M> int[] batchUpdate(Class<M> mClass,List<M> updateList, int batchSize) {
        return MAIN.batchUpdate(mClass,updateList,batchSize);
    }

    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param mClass
     * @param updateList
     * @param batchSize
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public static <M> int[] batchUpdate(Class<M> mClass,List<M> updateList, int batchSize,boolean containsNullValue)
    {
        return MAIN.batchUpdate(mClass,updateList,batchSize,containsNullValue);
    }


    /**
     * 传入一个id组，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param ids
     * @param <T>
     * @return
     */
    public static  <T> boolean deleteByGroupIds(Class<T> mClass,Object... ids) {
        return MAIN.deleteByGroupIds(mClass,ids);
    }

    /**
     * 传入一个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param id
     * @param <T>
     * @return
     */
    public static  <T> boolean deleteById(Class<T> mClass,Object id) {
        return MAIN.deleteById(mClass,id);
    }


    /**
     * 根据主键字段删除数据
     * @param t
     * @param <T>
     * @return
     */
    public static  <T> boolean deleteById(T t) {
        return MAIN.deleteById(t);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param deleteIds
     * @param <T>
     * @return
     */
    public static  <T> int deleteByIds(Class<T> mClass,List<Object> deleteIds) {
        return MAIN.deleteByIds(mClass,deleteIds);
    }

    /**
     * 传入多个携带 id 的 jpa对象，以此删除该对象，没有id则职id为null，不做删除数据处理
     * @param jpaList
     * @param <T>
     * @return
     */
    public static  <T> int deleteByIds(List<T> jpaList) {
        return MAIN.deleteByIds(jpaList);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param deleteIds -- 以 , 为分隔符的id串
     * @param <T>
     * @return
     */
    public static  <T> int deleteByIds(Class<T> mClass,String deleteIds) {
        return MAIN.deleteByIds(mClass,deleteIds);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param deleteIds
     * @param splitStr
     * @param <T>
     * @return
     */
    public static  <T> int deleteByIds(Class<T> mClass,String deleteIds,String splitStr) {
        return MAIN.deleteByIds(mClass,deleteIds,splitStr);
    }

    /**
     * 根据复合主键找对象
     * @param mClass
     * @param tableName
     * @param primaryKey
     * @param idValues
     * @param <M>
     * @return
     */
    public static  <M> M findByGroupId(Class<M> mClass,String tableName, String primaryKey, Object... idValues) {
        return MAIN.findByGroupId(mClass,tableName,primaryKey,idValues);
    }

    /**
     * 根据唯一主键找对象
     * @param mClass
     * @param tableName
     * @param primaryKey
     * @param idValue
     * @param <M>
     * @return
     */
    public <M> M findById(Class<M> mClass,String tableName, String primaryKey, Object idValue) {
        return MAIN.findById(mClass,tableName,primaryKey,idValue);
    }

    /**
     * 扩展方法体
     * @param sql
     * @param paras
     * @return
     */
    public static List<Record> find(String sql, Object... paras){
        return MAIN.find(sql,paras);
    }

    /**
     * 通过sql语句返回对象实体
     * @param mClass
     * @param finalSql
     * @param <M>
     * @return
     */
    public static  <M> List<M> find(Class<M> mClass,String finalSql)
    {
        return MAIN.find(mClass,finalSql);
    }

    /**
     * 通过对象和sqlpara对象返回查询结果
     * @param mClass
     * @param sqlPara
     * @param <M>
     * @return
     */
    public static  <M> List<M> find(Class<M> mClass, SqlPara sqlPara) {
        return MAIN.find(mClass,sqlPara);
    }


    /**
     * 传入对象和sql，返回查询结果
     * @param mClass
     * @param sql
     * @param paras
     * @param <M>
     * @return
     */
    public static <M> List<M> find(Class<M> mClass,String sql, Object... paras) {
        return MAIN.find(mClass,sql,paras);
    }


    /**
     * 根据查询sql进行分页查询
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param findSql
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass, int pageNumber, int pageSize, String findSql) {
        return MAIN.paginate(mClass,pageNumber,pageSize,findSql);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param findSql
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, String findSql) {
        return MAIN.paginate(mClass,pageRequest,findSql);
    }


    /**
     * 根据查询sql和参数进行分页查询
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String findSql,Object... paras) {
        return MAIN.paginate(mClass,pageNumber,pageSize,findSql,paras);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, String findSql,Object... paras) {
        return MAIN.paginate(mClass,pageRequest,findSql,paras);
    }

    /**
     * 根据设定好的查询总记录数和查询语句返回分页记录集
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param findSql
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, String findSql) {
        return MAIN.paginate(mClass,pageNumber,pageSize,totalRow,findSql);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param totalRow
     * @param findSql
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, String findSql) {
        return MAIN.paginate(mClass,pageRequest,totalRow,findSql);
    }

    /**
     * 根据设定好的查询总记录数和查询语句返回分页记录集
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, String findSql,Object... paras) {
        return MAIN.paginate(mClass,pageNumber,pageSize,totalRow,findSql,paras);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param totalRow
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, String findSql,Object... paras) {
        return MAIN.paginate(mClass,pageRequest,totalRow,findSql,paras);
    }

    /**
     * 根据 预设的数据库总记录数 和 sqlPara查询对象，返回指定的对象分页列表
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param sqlPara
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, SqlPara sqlPara) {
        return MAIN.paginate(mClass,pageNumber,pageSize,totalRow,sqlPara);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param totalRow
     * @param sqlPara
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, SqlPara sqlPara) {
        return MAIN.paginate(mClass,pageRequest,totalRow,sqlPara);
    }


    /**
     * 根据 sqlPara查询对象，返回指定的对象分页列表
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param sqlPara
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, SqlPara sqlPara) {
        return MAIN.paginate(mClass,pageNumber,pageSize,sqlPara);
    }

    /**
     * 根据 sqlPara 查询对象，返回指定的对象分页列表
     * @param mClass
     * @param pageRequest
     * @param sqlPara
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, SqlPara sqlPara){
        return MAIN.paginate(mClass,pageRequest,sqlPara);
    }

    /**
     * 根据 sqlPara查询对象、是否分组sql,返回指定的对象分页列表
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param sqlPara
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql, SqlPara sqlPara) {
        return MAIN.paginate(mClass,pageNumber,pageSize,isGroupBySql,sqlPara);
    }

    /**
     * 返回分页对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param select
     * @param sqlExceptSelect
     * @param paras
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.paginate(mClass,pageNumber,pageSize,select,sqlExceptSelect,paras);
    }

    /**
     * 返回分页对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param select
     * @param sqlExceptSelect
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String select, String sqlExceptSelect) {
        return MAIN.paginate(mClass,pageNumber,pageSize,select,sqlExceptSelect);
    }

    /**
     * 返回分页对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param select
     * @param sqlExceptSelect
     * @param paras
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.paginate(mClass,pageNumber,pageSize,isGroupBySql,select,sqlExceptSelect,paras);
    }

    /**
     * 实现返回对象的分页逻辑实现
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param select
     * @param sqlExceptSelect
     * @param paras
     * @param <M>
     * @return
     */
    protected static  <M> Page<M> doPaginate(Class<M> mClass,int pageNumber, int pageSize, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.doPaginate(mClass,pageNumber,pageSize,isGroupBySql,select,sqlExceptSelect,paras);
    }

    /**
     * 主要是根据 已传入的总记录数 实现分页逻辑
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRow
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    protected static  <M> Page<M> doPaginate(Class<M> mClass,int pageNumber, int pageSize, Boolean isGroupBySql,long totalRow, StringBuilder findSql, Object... paras) {
        return MAIN.doPaginate(mClass,pageNumber,pageSize,isGroupBySql,totalRow,findSql,paras);
    }

    /**
     * 根据分页统计sql、查询sql返回查询对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param totalRowSql
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginateByFullSql(Class<M> mClass,int pageNumber, int pageSize, String totalRowSql, String findSql, Object... paras) {
        return MAIN.paginateByFullSql(mClass,pageNumber,pageSize,totalRowSql,findSql,paras);
    }


    /**
     * 根据 是否分组sql 、 分页统计sql、 查询sql返回查询对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRowSql
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginateByFullSql(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
        return MAIN.paginateByFullSql(mClass,pageNumber,pageSize,isGroupBySql,totalRowSql,findSql,paras);
    }


    /**
     * 根据 是否分组sql、 统计sql 、 查询sql 返回查询对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRowSql
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    protected static  <M> Page<M> doPaginateByFullSql(Class<M> mClass,int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
        return MAIN.doPaginateByFullSql(mClass,pageNumber,pageSize,isGroupBySql,totalRowSql,findSql,paras);
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param mClass
     * @param ids
     * @param <M>
     * @return
     */
    public static  <M> List<M> findByIds(Class<M> mClass, List<Object> ids) {
        return MAIN.findByIds(mClass,ids);
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param mClass
     * @param idsStr
     * @param splitStr
     * @param <M>
     * @return
     */
    public static  <M> List<M> findByIds(Class<M> mClass, String idsStr,String splitStr) {
        return MAIN.findByIds(mClass,idsStr,splitStr);
    }

    /**
     * 获取1条记录
     * @param sqlPara
     * @return
     */
    public static Record findFirst(SqlPara sqlPara){
        return MAIN.findFirst(sqlPara);
    }

    /**
     * 获取第一条记录 (改写原 Db.getFirst sql)
     * @param sql
     * @return
     */
    public static Record findFirst(String sql) {
        return MAIN.findFirst(sql);
    }

    /**
     * 获取1条记录
     * @param tClass
     * @param sql
     * @param <T>
     * @return
     */
    public static <T> T findFirst(Class<T> tClass,String sql) {
        return MAIN.findFirst(tClass,sql);
    }


    /**
     * 获取1条记录
     * @param tClass
     * @param sql
     * @param paras
     * @param <T>
     * @return
     */
    public static <T> T findFirst(Class<T> tClass,String sql, Object... paras){
        return MAIN.findFirst(tClass,sql,paras);
    }

    /**
     * 获取首条记录
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public static  <T> T findFirst(Class<T> tClass,EDbQuery eDbQuery) {
        return MAIN.findFirst(tClass,eDbQuery);
    }

    /**
     * 获取1条记录
     * @param tClass
     * @param sqlPara
     * @param <T>
     * @return
     */
    public static  <T> T findFirst(Class<T> tClass,SqlPara sqlPara) {
        return MAIN.findFirst(tClass,sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param sql
     * @param <T>
     * @return
     */
    public static  <T> T findOnlyOne(Class<T> tClass,String sql) {
        return MAIN.findOnlyOne(tClass,sql);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param sqlPara
     * @param <T>
     * @return
     */
    public static  <T> T findOnlyOne(Class<T> tClass,SqlPara sqlPara) {
        return MAIN.findOnlyOne(tClass,sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public static  <T> T findOnlyOne(Class<T> tClass, EDbQuery eDbQuery) {
        return MAIN.findOnlyOne(tClass,eDbQuery);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sqlPara
     * @return
     */
    public static Record findOnlyOne(SqlPara sqlPara)
    {
        return MAIN.findOnlyOne(sqlPara);
    }
    /**
     * 根据sql返回1条唯一记录,超过则报错
     * @param sql
     * @return
     */
    public static Record findOnlyOne(String sql){
        return MAIN.findOnlyOne(sql);
    }

    /**
     * 根据 EDbQuery 返回查询结果
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public static <T> List<T> find(Class<T> tClass, EDbQuery eDbQuery){
        return MAIN.find(tClass,eDbQuery);
    }

    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param tClass
     * @param eDbQuery
     * @param limit
     * @param <T>
     * @return
     */
    public static <T> List<T> find(Class<T> tClass,EDbQuery eDbQuery,int limit)
    {
        return MAIN.find(tClass,eDbQuery,limit);
    }

    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param tClass
     * @param eDbQuery
     * @param limit
     * @param offset
     * @param <T>
     * @return
     */
    public static <T> List<T> find(Class<T> tClass,EDbQuery eDbQuery,int limit,Integer offset){
        return MAIN.find(tClass,eDbQuery,limit,offset);
    }

    /**
     * 根据 EDbQuery 返回分页对象
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param eDbQuery
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, EDbQuery eDbQuery) {
        return MAIN.paginate(mClass,pageNumber,pageSize,eDbQuery);
    }


    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param eDbQuery
     * @param <M>
     * @return
     */
    public static  <M> Page<M> paginate(Class<M> mClass, PageRequest pageRequest, EDbQuery eDbQuery) {
        return MAIN.paginate(mClass,pageRequest,eDbQuery);
    }


    /**
     * 根据 EDbQuery 返回分页对象（已制定 totalRow的模式）
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param eDbQuery
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, EDbQuery eDbQuery) {
        return MAIN.paginate(mClass,pageNumber,pageSize,totalRow,eDbQuery);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param totalRow
     * @param eDbQuery
     * @param <M>
     * @return
     */
    public static <M> Page<M> paginate(Class<M> mClass, PageRequest pageRequest,long totalRow, EDbQuery eDbQuery){
        return MAIN.paginate(mClass,pageRequest,totalRow,eDbQuery);
    }

    /**
     * 创建当前对象的关系代理对象
     * @param t
     * @param <T>
     * @return
     */
    public static  <T> T getRel(T t){
        return MAIN.rel(t);
    }

    /**
     * 通过relKey直接返回对象
     * @param t
     * @param relKey
     * @param pageNo
     * @param pageSize
     * @return
     */
    public static Object getRelKey(Object t,String relKey,Integer pageNo,Integer pageSize) {
        return MAIN.getRelKey(t,relKey,pageNo,pageSize);
    }

    /**
     * 通过relKey直接返回指定对象
     * @param t
     * @param relKey
     * @return
     */
    public static  Object getRelKey(Object t,String relKey)
    {
        return MAIN.getRelKey(t,relKey);
    }

    /**
     * 获取关系对象，并可控制对象的其实和结束节点，以便控制返回更多的结果
     * @param t
     * @param pageNo
     * @param pageSize
     * @param <T> -- 这类方法一般返回的是list对象，不排除多个里取一个结果集
     * @return
     */
    public static  <T> T getRel(T t,Integer pageNo,Integer pageSize){
        return MAIN.rel(t,pageNo,pageSize);
    }

    /**
     * 获取关系对象，控制返回条数和返回的字段
     * @param t
     * @param fields -- 返回的字段字符串
     * @param pageNo -- 从第几页开始，起始为1
     * @param pageSize -- 最多返回多少记录 ,null 时，取 @EDbRel里的limit默认值
     * @param <T>
     * @return
     */
    public static <T> T getRel(T t,String fields,Integer pageNo,Integer pageSize){
        return MAIN.rel(t,fields,pageNo,pageSize);
    }



    /**
     * 异步获取关系对象
     * @param t
     * @param relKey -- 指定relKey对象
     * @return
     */
    public static List<Future<Object>> getRelForFutrue(Object t,String relKey){
        return MAIN.getRelKeyForFutrue(t,relKey);
    }

    /**
     * 异步获取对象
     * @param t
     * @param relKey -- 指定relKey对象
     * @param pageNo -- 起始页
     * @param pageSize -- 返回页数
     * @return
     */
    public static List<Future<Object>> getRelForFutrue(Object t,String relKey,Integer pageNo,Integer pageSize){
        return MAIN.getRelKeyForFutrue(t,relKey,pageNo,pageSize);
    }


    /**
     * 异步获取对象
     * @param t
     * @param relKey
     * @param fields
     * @param pageNo
     * @param pageSize
     * @return
     */
    public static List<Future<Object>> getRelForFutrue(Object t,String relKey,String fields,Integer pageNo,Integer pageSize){
        return MAIN.getRelKeyForFutrue(t,relKey,fields,pageNo,pageSize);
    }

    /**
     * 返回数据对象本身
     * @param t
     * @param <T>
     * @returnt
     */
    public static  <T> T getAllRel(T t){
        return MAIN.getAllRel(t);
    }

    /**
     * 获取所有数据对象，以异步回调的方式获取，能大量缩短等待时间
     * @param object
     */
    public static List<Future<Object>> getAllRelForFutrue(Object object){
        return MAIN.getAllRelForFutrue(object);
    }


    /**
     * 通过relKey直接返回对象异步列表
     * @param t
     * @param relKey
     * @return
     */
    public static List<Future<Object>> getRelKeyForFutrue(Object t,String relKey){
        return MAIN.getRelKeyForFutrue(t,relKey);
    }

    /**
     * 获取视图对象
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T view(T t){
        return MAIN.view(t);
    }

    /**
     * 获取翻页视图对象
     * @param t
     * @param pageNo
     * @param pageSize
     * @param <T>
     * @return
     */
    public static <T> T view(T t,int pageNo,int pageSize){
        return MAIN.view(t,pageNo,pageSize);
    }

    /**
     * 获取翻页视图对象
     * @param t
     * @param pageNo
     * @param pageSize
     * @param totalRow 总记录数，无需单独执行统计sql (小写的long可以支持int类型的输入，请勿改造成大写，避免已应用的地方导致异常，其次规避null入参的问题)
     * @param <T>
     * @return
     */
    public static <T> T view(T t,int pageNo,int pageSize,long totalRow){
        return MAIN.view(t,pageNo,pageSize,totalRow);
    }

    /**
     * 返回视图的总记录数
     * @param key
     * @param data
     * @return
     */
    public static Long templateForCount(String key,Map data){
        return MAIN.templateForCount(key,data);
    }

    /**
     * 返回sql语句的总记录数
     * @param sql
     * @return
     */
    public static Long sqlForCount(String sql){
        return MAIN.sqlForCount(sql);
    }

    /**
     * 返回sql对应的总记录数
     * @param sqlPara
     * @return
     */
    public static Long sqlForCount(SqlPara sqlPara)
    {
        return MAIN.sqlForCount(sqlPara);
    }


    /**
     * 返回统计语句
     * @param sql
     * @return
     */
    public static String getCountSql(String sql){
        return MAIN.getCountSql(sql);
    }

    public static EDbTemplate template(String key, Map data) {
        return MAIN.template( key, data);
    }

    public static EDbTemplate template(String key, Object... paras) {
        return MAIN.template(key, paras);
    }

    public static EDbTemplate templateByString(String content, Map data) {
        return MAIN.templateByString(content, data);
    }

    public static EDbTemplate templateByString(String content, Object... paras) {
        return MAIN.templateByString(content, paras);
    }

}
