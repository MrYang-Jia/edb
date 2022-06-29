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

import com.edbplus.db.query.EDbQuery;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

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

    // 异步连接池 -- 大量使用的话，会影响其他地方的访问请求能力，异步之间的线程数量是恒定的，总数恒定则会相互之间产生影响
    public static final Map<String, ExecutorService> edbFutruePools = new SyncWriteMap<String, ExecutorService>(32, 0.25F);

    // 主要访问的对象 -- 私有可以避免对外暴露信息，尤其是转json时
    private static EDbPro MAIN = null;

    // 主要载体
    private static final Map<String, EDbPro> dbMap = new SyncWriteMap<String, EDbPro>(32, 0.25F);

    /**
     * 删除dbpro
     * @param configName
     */
    public static void removeDbProWithConfig(String configName) {
        if (MAIN != null && MAIN.getConfig().getName().equals(configName)) {
            MAIN = null;
        }
        // 删除配置对象
        edbFutruePools.remove(configName);
        // 删除内部数据操作对象
        dbMap.remove(configName);
    }

    /**
     * 停止db组件服务
     */
    public static void stop() {
        stop(DbKit.MAIN_CONFIG_NAME);
    }

    /**
     * 停止db组件服务
     * @param configName
     */
    public static void stop(String configName) {
        DbKit.removeConfig(configName); // but 自定义的单体对象 ActiveRecordPlugin 则无法直接回收，但是一般也不会用到这层次！！！
        removeDbProWithConfig(configName);
        edbFutruePools.remove(configName);
    }

    /**
     * 初始化 dbPro 对象
     */
    public static void init(){
        if(MAIN != null){
            if(MAIN.getConfig()==null){
                MAIN =null;
            }else{
                return; // 如果已有数据源，不做替换
            }
        }
        EDbPro eDbPro = new EDbPro();
        // 加载主对象
        dbMap.put(DbKit.MAIN_CONFIG_NAME,eDbPro);
        // 加载主体对象
        MAIN = eDbPro;
        initPool(DbKit.MAIN_CONFIG_NAME,eDbPro);
    }

    /**
     * 初始化线程池
     * @param configName
     * @param eDbPro
     */
    public static void initPool(String configName, EDbPro eDbPro){
//        DataSource dataSource = eDbPro.getConfig().getDataSource();
//        if(dataSource instanceof DruidDataSource){ // 某些第三方资源包不支持,待改进
//            // 并发线程控制，预留 1 - 0.3 = 0.7 的连接数给予系统应用，避免大量并发导致线程堵塞
//            edbFutruePools.put(configName,   Executors.newFixedThreadPool((int) (((DruidDataSource)dataSource).getMaxActive() * 0.3) ));
//        }else{
            // 并发线程控制，预留 0.5 的连接数给予系统应用，避免大量并发导致线程堵塞
            edbFutruePools.put(configName,   Executors.newFixedThreadPool( 20 ));
//        }
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
    public static  <M> String tableName(Class<M> mClass)
    {
        return MAIN.tableName(mClass);
    }

    /**
     * 返回数据库字段
     * @param m
     * @param ignoreNullValue  - true -屏蔽 null , false - 包含null
     * @param <M>
     * @return
     */
    public static <M> Map<String,Object> columnsMap(M m, boolean ignoreNullValue){
        return MAIN.columnsMap(m,ignoreNullValue);
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
     * 使用 insert values 的方式插入数据
     * @param m
     * @param <M>
     * @return
     */
    public static <M>  int  insertValue(M m){
        return MAIN.insertValue(m);
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
     * 根据外键进行更新
     * @param mClass -- 对象
     * @param eDbQuery -- 查询字段
     * @param <M>
     * @return
     */
    public static  <M> boolean delete(Class<M> mClass,EDbQuery eDbQuery){
        return MAIN.delete(mClass,eDbQuery);
    }

    /**
     * 根据外键进行更新
     * @param mClass -- 对象
     * @param eDbQuery -- 查询字段
     * @param <M>
     * @return
     */
    public static  <M> int deleteReInt(Class<M> mClass,EDbQuery eDbQuery){
        return MAIN.deleteReInt(mClass,eDbQuery);
    }

    /**
     * 根据外键进行更新
     * @param mClass -- 对象
     * @param updateData -- 更新的数据库字段
     * @param eDbQuery -- 查询字段
     * @param <M>
     * @return
     */
    public static <M> boolean update(Class<M> mClass,Map<String,Object> updateData,EDbQuery eDbQuery){
        return MAIN.update(mClass,updateData,eDbQuery);
    }

    /**
     * 根据外键进行更新
     * @param mClass -- 对象
     * @param updateData -- 更新的数据库字段
     * @param eDbQuery -- 查询字段
     * @param <M>
     * @return
     */
    public static <M> int updateReInt(Class<M> mClass,Map<String,Object> updateData,EDbQuery eDbQuery){
        return MAIN.updateReInt(mClass,updateData,eDbQuery);
    }

    /**
     * 根据外键进行更新
     * @param mClass -- 对象
     * @param updateData -- 更新的数据库字段
     * @param fkData -- 外键数据字段集
     * @param <M>
     * @return
     */
    public static <M> boolean updateByFk(Class<M> mClass,Map<String,Object> updateData,Map<String,Object> fkData){
        return MAIN.updateByFk(mClass,updateData,fkData);
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
     * 查询表所有对象
     * @param mClass
     * @return
     */
    public  static <M> List<M> findAll(Class<M> mClass){
        return MAIN.findAll(mClass);
    }


    /**
     * 返回查询对象和设置返回的条数
     * @param sqlPara
     * @param limit
     * @return
     */
    public static List<Record> find(SqlPara sqlPara,int limit){
        return MAIN.find(sqlPara,limit);
    }

    /**
     * 返回查询对象和设置返回的条数与起始位
     * @param sqlPara
     * @param limit
     * @param offset
     * @return
     */
    public static List<Record> find(SqlPara sqlPara,int limit,int offset){
        return MAIN.find(sqlPara,limit,offset);
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
     * 通过对象和sqlpara对象返回查询结果并设置返回条数
     * @param mClass
     * @param sqlPara
     * @param limit
     * @param <M>
     * @return
     */
    public static  <M> List<M> find(Class<M> mClass, SqlPara sqlPara,int limit) {
        return MAIN.find(mClass,sqlPara,limit);
    }

    /**
     * 通过对象和sqlpara对象返回查询结果并设置返回条数和返回起始位
     * @param mClass
     * @param sqlPara
     * @param limit
     * @param offset
     * @param <M>
     * @return
     */
    public static  <M> List<M> find(Class<M> mClass, SqlPara sqlPara,int limit,int offset) {
        return MAIN.find(mClass,sqlPara,limit,offset);
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
//
//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param findSql
//     * @param <M>
//     * @return
//     */
//    public static  <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, String findSql) {
//        return MAIN.paginate(mClass,pageRequest,findSql);
//    }


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

//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param findSql
//     * @param paras
//     * @param <M>
//     * @return
//     */
//    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, String findSql,Object... paras) {
//        return MAIN.paginate(mClass,pageRequest,findSql,paras);
//    }

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

//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param totalRow
//     * @param findSql
//     * @param <M>
//     * @return
//     */
//    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, String findSql) {
//        return MAIN.paginate(mClass,pageRequest,totalRow,findSql);
//    }

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

//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param totalRow
//     * @param findSql
//     * @param paras
//     * @param <M>
//     * @return
//     */
//    public static  <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, String findSql,Object... paras) {
//        return MAIN.paginate(mClass,pageRequest,totalRow,findSql,paras);
//    }

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

//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param totalRow
//     * @param sqlPara
//     * @param <M>
//     * @return
//     */
//    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, SqlPara sqlPara) {
//        return MAIN.paginate(mClass,pageRequest,totalRow,sqlPara);
//    }


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

//    /**
//     * 根据 sqlPara 查询对象，返回指定的对象分页列表
//     * @param mClass
//     * @param pageRequest
//     * @param sqlPara
//     * @param <M>
//     * @return
//     */
//    public static <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, SqlPara sqlPara){
//        return MAIN.paginate(mClass,pageRequest,sqlPara);
//    }

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


//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param eDbQuery
//     * @param <M>
//     * @return
//     */
//    public static  <M> Page<M> paginate(Class<M> mClass, PageRequest pageRequest, EDbQuery eDbQuery) {
//        return MAIN.paginate(mClass,pageRequest,eDbQuery);
//    }


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

//    /**
//     * 分页查询
//     * @param mClass
//     * @param pageRequest
//     * @param totalRow
//     * @param eDbQuery
//     * @param <M>
//     * @return
//     */
//    public static <M> Page<M> paginate(Class<M> mClass, PageRequest pageRequest,long totalRow, EDbQuery eDbQuery){
//        return MAIN.paginate(mClass,pageRequest,totalRow,eDbQuery);
//    }

    /**
     * 创建当前对象的关系代理对象
     * @param t
     * @param <T>
     * @return
     */
    public static  <T> T rel(T t, String fieldName){
        return MAIN.rel(t,fieldName);
    }

//    /**
//     * 通过relKey直接返回对象
//     * @param t
//     * @param relKey
//     * @param pageNo
//     * @param pageSize
//     * @return
//     */
//    public static Object getRelKey(Object t,String relKey,Integer pageNo,Integer pageSize) {
//        return MAIN.getRelKey(t,relKey,pageNo,pageSize);
//    }
//
//    /**
//     * 通过relKey直接返回指定对象
//     * @param t
//     * @param relKey
//     * @return
//     */
//    public static  Object getRelKey(Object t,String relKey)
//    {
//        return MAIN.getRelKey(t,relKey);
//    }

    /**
     * 获取关系对象，并可控制对象的其实和结束节点，以便控制返回更多的结果
     * @param t
     * @param pageNo
     * @param pageSize
     * @param <T> -- 这类方法一般返回的是list对象，不排除多个里取一个结果集
     * @return
     */
    public static  <T> T rel(T t, String fieldName, Integer pageNo, Integer pageSize){
        return MAIN.rel(t,fieldName,pageNo,pageSize);
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
    public static <T> T rel(T t, String fieldName, String fields, Integer pageNo, Integer pageSize){
        return MAIN.rel(t,fieldName,fields,pageNo,pageSize);
    }

//
//
//    /**
//     * 异步获取关系对象
//     * @param t
//     * @param relKey -- 指定relKey对象
//     * @return
//     */
//    public static List<Future<Object>> getRelForFutrue(Object t,String relKey){
//        return MAIN.getRelKeyForFutrue(t,relKey);
//    }
//
//    /**
//     * 异步获取对象
//     * @param t
//     * @param relKey -- 指定relKey对象
//     * @param pageNo -- 起始页
//     * @param pageSize -- 返回页数
//     * @return
//     */
//    public static List<Future<Object>> getRelForFutrue(Object t,String relKey,Integer pageNo,Integer pageSize){
//        return MAIN.getRelKeyForFutrue(t,relKey,pageNo,pageSize);
//    }
//
//
//    /**
//     * 异步获取对象
//     * @param t
//     * @param relKey
//     * @param fields
//     * @param pageNo
//     * @param pageSize
//     * @return
//     */
//    public static List<Future<Object>> getRelForFutrue(Object t,String relKey,String fields,Integer pageNo,Integer pageSize){
//        return MAIN.getRelKeyForFutrue(t,relKey,fields,pageNo,pageSize);
//    }

//    /**
//     * 返回数据对象本身
//     * @param t
//     * @param <T>
//     * @returnt
//     */
//    public static  <T> T getAllRel(T t){
//        return MAIN.getAllRel(t);
//    }

//    /**
//     * 获取所有数据对象，以异步回调的方式获取，能大量缩短等待时间
//     * @param object
//     */
//    public static List<Future<Object>> getAllRelForFutrue(Object object){
//        return MAIN.getAllRelForFutrue(object);
//    }


//    /**
//     * 通过relKey直接返回对象异步列表
//     * @param t
//     * @param relKey
//     * @return
//     */
//    public static List<Future<Object>> getRelKeyForFutrue(Object t,String relKey){
//        return MAIN.getRelKeyForFutrue(t,relKey);
//    }

    /**
     * 获取视图对象
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T view(T t,String fieldName){
        return MAIN.view(t,fieldName);
    }

    /**
     * 获取翻页视图对象
     * @param t
     * @param pageNo
     * @param pageSize
     * @param <T>
     * @return
     */
    public static <T> T view(T t,String fieldName,int pageNo,int pageSize){
        return MAIN.view(t,fieldName,pageNo,pageSize);
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
    public static <T> T view(T t,String fieldName,int pageNo,int pageSize,long totalRow){
        return MAIN.view(t,fieldName,pageNo,pageSize,totalRow);
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
    public static String countSql(String sql){
        return MAIN.countSql(sql);
    }

    /**
     * 返回统计结果
     * @param sqlPara
     * @return
     */
    public static long count(SqlPara sqlPara){
        return MAIN.count(sqlPara);
    }

    /**
     * 返回统计结果
     * @param sql
     * @return
     */
    public static long count(String sql){
        return MAIN.count(sql);
    }

    /**
     * 根据 EDbQuery 返回统计结果
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public <T> long count(Class<T> tClass,EDbQuery eDbQuery){
        return MAIN.count(tClass,eDbQuery);
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


    static <T> List<T> query(Config config, Connection conn, String sql, Object... paras) throws SQLException {
        return MAIN.query(config, conn, sql, paras);
    }

    public static <T> List<T> query(String sql, Object... paras) {
        return MAIN.query(sql, paras);
    }

    public static <T> List<T> query(String sql) {
        return MAIN.query(sql);
    }

    public static <T> T queryFirst(String sql, Object... paras) {
        return MAIN.queryFirst(sql, paras);
    }

    public static <T> T queryFirst(String sql) {
        return MAIN.queryFirst(sql);
    }

    public static <T> T queryColumn(String sql, Object... paras) {
        return MAIN.queryColumn(sql, paras);
    }

    public static <T> T queryColumn(String sql) {
        return MAIN.queryColumn(sql);
    }

    public static String queryStr(String sql, Object... paras) {
        return MAIN.queryStr(sql, paras);
    }

    public static String queryStr(String sql) {
        return MAIN.queryStr(sql);
    }

    public static Integer queryInt(String sql, Object... paras) {
        return MAIN.queryInt(sql, paras);
    }

    public static Integer queryInt(String sql) {
        return MAIN.queryInt(sql);
    }

    public static Long queryLong(String sql, Object... paras) {
        return MAIN.queryLong(sql, paras);
    }

    public static Long queryLong(String sql) {
        return MAIN.queryLong(sql);
    }

    public static Double queryDouble(String sql, Object... paras) {
        return MAIN.queryDouble(sql, paras);
    }

    public static Double queryDouble(String sql) {
        return MAIN.queryDouble(sql);
    }

    public static Float queryFloat(String sql, Object... paras) {
        return MAIN.queryFloat(sql, paras);
    }

    public static Float queryFloat(String sql) {
        return MAIN.queryFloat(sql);
    }

    public static BigDecimal queryBigDecimal(String sql, Object... paras) {
        return MAIN.queryBigDecimal(sql, paras);
    }

    public static BigDecimal queryBigDecimal(String sql) {
        return MAIN.queryBigDecimal(sql);
    }

    public static BigInteger queryBigInteger(String sql, Object... paras) {
        return MAIN.queryBigInteger(sql, paras);
    }

    public static BigInteger queryBigInteger(String sql) {
        return MAIN.queryBigInteger(sql);
    }

    public static byte[] queryBytes(String sql, Object... paras) {
        return MAIN.queryBytes(sql, paras);
    }

    public static byte[] queryBytes(String sql) {
        return MAIN.queryBytes(sql);
    }

    public static Date queryDate(String sql, Object... paras) {
        return MAIN.queryDate(sql, paras);
    }

    public static Date queryDate(String sql) {
        return MAIN.queryDate(sql);
    }

    public static LocalDateTime queryLocalDateTime(String sql, Object... paras) {
        return MAIN.queryLocalDateTime(sql, paras);
    }

    public static LocalDateTime queryLocalDateTime(String sql) {
        return MAIN.queryLocalDateTime(sql);
    }

    public static Time queryTime(String sql, Object... paras) {
        return MAIN.queryTime(sql, paras);
    }

    public static Time queryTime(String sql) {
        return MAIN.queryTime(sql);
    }

    public static Timestamp queryTimestamp(String sql, Object... paras) {
        return MAIN.queryTimestamp(sql, paras);
    }

    public static Timestamp queryTimestamp(String sql) {
        return MAIN.queryTimestamp(sql);
    }

    public static Boolean queryBoolean(String sql, Object... paras) {
        return MAIN.queryBoolean(sql, paras);
    }

    public static Boolean queryBoolean(String sql) {
        return MAIN.queryBoolean(sql);
    }

    public static Short queryShort(String sql, Object... paras) {
        return MAIN.queryShort(sql, paras);
    }

    public static Short queryShort(String sql) {
        return MAIN.queryShort(sql);
    }

    public static Byte queryByte(String sql, Object... paras) {
        return MAIN.queryByte(sql, paras);
    }

    public static Byte queryByte(String sql) {
        return MAIN.queryByte(sql);
    }

    public static Number queryNumber(String sql, Object... paras) {
        return MAIN.queryNumber(sql, paras);
    }

    public static Number queryNumber(String sql) {
        return MAIN.queryNumber(sql);
    }

    static int update(Config config, Connection conn, String sql, Object... paras) throws SQLException {
        return MAIN.update(config, conn, sql, paras);
    }

    public static int update(String sql, Object... paras) {
        return MAIN.update(sql, paras);
    }

    public static int update(String sql) {
        return MAIN.update(sql);
    }

    static List<Record> find(Config config, Connection conn, String sql, Object... paras) throws SQLException {
        return MAIN.find(config, conn, sql, paras);
    }


    public static List<Record> find(String sql) {
        return MAIN.find(sql);
    }

    public static List<Record> findAll(String tableName) {
        return MAIN.findAll(tableName);
    }

    public static Record findFirst(String sql, Object... paras) {
        return MAIN.findFirst(sql, paras);
    }



    public static Record findById(String tableName, Object idValue) {
        return MAIN.findById(tableName, idValue);
    }

    public static Record findById(String tableName, String primaryKey, Object idValue) {
        return MAIN.findById(tableName, primaryKey, idValue);
    }

    public static Record findByIds(String tableName, String primaryKey, Object... idValues) {
        return MAIN.findByIds(tableName, primaryKey, idValues);
    }

    public static boolean deleteById(String tableName, Object idValue) {
        return MAIN.deleteById(tableName, idValue);
    }

    public static boolean deleteById(String tableName, String primaryKey, Object idValue) {
        return MAIN.deleteById(tableName, primaryKey, idValue);
    }

    public static boolean deleteByIds(String tableName, String primaryKey, Object... idValues) {
        return MAIN.deleteByIds(tableName, primaryKey, idValues);
    }

    public static boolean delete(String tableName, String primaryKey, Record record) {
        return MAIN.delete(tableName, primaryKey, record);
    }

    public static boolean delete(String tableName, Record record) {
        return MAIN.delete(tableName, record);
    }

    public static int delete(String sql, Object... paras) {
        return MAIN.delete(sql, paras);
    }

    public static int delete(String sql) {
        return MAIN.delete(sql);
    }

    static Page<Record> paginate(Config config, Connection conn, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) throws SQLException {
        return MAIN.paginate(config, conn, pageNumber, pageSize, select, sqlExceptSelect, paras);
    }

    public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.paginate(pageNumber, pageSize, select, sqlExceptSelect, paras);
    }

    public static Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.paginate(pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
    }

    public static Page<Record> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect) {
        return MAIN.paginate(pageNumber, pageSize, select, sqlExceptSelect);
    }

    public static Page<Record> paginateByFullSql(int pageNumber, int pageSize, String totalRowSql, String findSql, Object... paras) {
        return MAIN.paginateByFullSql(pageNumber, pageSize, totalRowSql, findSql, paras);
    }

    public static Page<Record> paginateByFullSql(int pageNumber, int pageSize, boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
        return MAIN.paginateByFullSql(pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
    }

    static boolean save(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
        return MAIN.save(config, conn, tableName, primaryKey, record);
    }

    public static boolean save(String tableName, String primaryKey, Record record) {
        return MAIN.save(tableName, primaryKey, record);
    }

    public static boolean save(String tableName, Record record) {
        return MAIN.save(tableName, record);
    }

    static boolean update(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
        return MAIN.update(config, conn, tableName, primaryKey, record);
    }

    public static boolean update(String tableName, String primaryKey, Record record) {
        return MAIN.update(tableName, primaryKey, record);
    }

    public static boolean update(String tableName, Record record) {
        return MAIN.update(tableName, record);
    }

    public static Object execute(ICallback callback) {
        return MAIN.execute(callback);
    }

    static Object execute(Config config, ICallback callback) {
        return MAIN.execute(config, callback);
    }

    static boolean tx(Config config, int transactionLevel, IAtom atom) {
        return MAIN.tx(config, transactionLevel, atom);
    }

    public static boolean tx(IAtom atom) {
        return MAIN.tx(atom);
    }

    public static boolean tx(int transactionLevel, IAtom atom) {
        return MAIN.tx(transactionLevel, atom);
    }

    public static Future<Boolean> txInNewThread(IAtom atom) {
        return MAIN.txInNewThread(atom);
    }

    public static Future<Boolean> txInNewThread(int transactionLevel, IAtom atom) {
        return MAIN.txInNewThread(transactionLevel, atom);
    }

    public static List<Record> findByCache(String cacheName, Object key, String sql, Object... paras) {
        return MAIN.findByCache(cacheName, key, sql, paras);
    }

    public static List<Record> findByCache(String cacheName, Object key, String sql) {
        return MAIN.findByCache(cacheName, key, sql);
    }

    public static Record findFirstByCache(String cacheName, Object key, String sql, Object... paras) {
        return MAIN.findFirstByCache(cacheName, key, sql, paras);
    }

    public static Record findFirstByCache(String cacheName, Object key, String sql) {
        return MAIN.findFirstByCache(cacheName, key, sql);
    }

    public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect, paras);
    }

    public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        return MAIN.paginateByCache(cacheName, key, pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
    }

    public static Page<Record> paginateByCache(String cacheName, Object key, int pageNumber, int pageSize, String select, String sqlExceptSelect) {
        return MAIN.paginateByCache(cacheName, key, pageNumber, pageSize, select, sqlExceptSelect);
    }

    public static int[] batch(String sql, Object[][] paras, int batchSize) {
        return MAIN.batch(sql, paras, batchSize);
    }

    public static int[] batch(String sql, String columns, List modelOrRecordList, int batchSize) {
        return MAIN.batch(sql, columns, modelOrRecordList, batchSize);
    }

    public static int[] batch(List<String> sqlList, int batchSize) {
        return MAIN.batch(sqlList, batchSize);
    }

    public static int[] batchSave(List<? extends Model> modelList, int batchSize) {
        return MAIN.batchSave(modelList, batchSize);
    }

    public static int[] batchSave(String tableName, List<? extends Record> recordList, int batchSize) {
        return MAIN.batchSave(tableName, recordList, batchSize);
    }

    public static int[] batchUpdate(List<? extends Model> modelList, int batchSize) {
        return MAIN.batchUpdate(modelList, batchSize);
    }

    public static int[] batchUpdate(String tableName, String primaryKey, List<? extends Record> recordList, int batchSize) {
        return MAIN.batchUpdate(tableName, primaryKey, recordList, batchSize);
    }

    public static int[] batchUpdate(String tableName, List<? extends Record> recordList, int batchSize) {
        return MAIN.batchUpdate(tableName, recordList, batchSize);
    }

    public static String getSql(String key) {
        return MAIN.getSql(key);
    }

    public static SqlPara getSqlPara(String key, Record record) {
        return MAIN.getSqlPara(key, record);
    }

    public static SqlPara getSqlPara(String key, Model model) {
        return MAIN.getSqlPara(key, model);
    }

    public static SqlPara getSqlPara(String key, Map data) {
        return MAIN.getSqlPara(key, data);
    }

    public static SqlPara getSqlPara(String key, Object... paras) {
        return MAIN.getSqlPara(key, paras);
    }

    public static SqlPara getSqlParaByString(String content, Map data) {
        return MAIN.getSqlParaByString(content, data);
    }

    public static SqlPara getSqlParaByString(String content, Object... paras) {
        return MAIN.getSqlParaByString(content, paras);
    }

    public static List<Record> find(SqlPara sqlPara) {
        return MAIN.find(sqlPara);
    }


    public static int update(SqlPara sqlPara) {
        return MAIN.update(sqlPara);
    }

    public static Page<Record> paginate(int pageNumber, int pageSize, SqlPara sqlPara) {
        return MAIN.paginate(pageNumber, pageSize, sqlPara);
    }

    public static Page<Record> paginate(int pageNumber, int pageSize, boolean isGroupBySql, SqlPara sqlPara) {
        return MAIN.paginate(pageNumber, pageSize, isGroupBySql, sqlPara);
    }

    public static void each(Function<Record, Boolean> func, String sql, Object... paras) {
        MAIN.each(func, sql, paras);
    }




}
