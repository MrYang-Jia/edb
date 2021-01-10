package com.edbplus.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.edbplus.db.query.EDbQuery;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.plugin.activerecord.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

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
public class EDb extends Db{

    // 连接池
    public static final Map<String, ExecutorService> edbFutruePools = new SyncWriteMap<String, ExecutorService>(32, 0.25F);

    // 主要访问的对象
    private static EDbPro MAIN = null;

    // 主要载体
    private static final Map<String, EDbPro> dbMap = new SyncWriteMap<String, EDbPro>(32, 0.25F);


    /**
     * 初始化 dbPro 对象
     */
    public static void init(){
        EDbPro EDbPro = new EDbPro();
        // 加载主对象
        dbMap.put(DbKit.MAIN_CONFIG_NAME,EDbPro);
        // 加载主体对象
        MAIN = EDbPro;
        initPool(DbKit.MAIN_CONFIG_NAME,EDbPro);
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
     * 更新对象 -- 包含null值的变更情况
     * @param mClass
     * @param updateData
     * @param <M>
     * @return
     */
    public static  <M> boolean update(Class<M> mClass,Map<String,Object> updateData){
        return MAIN.update(mClass,updateData);
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
        return MAIN.getRel(t);
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
     * 获取关系对象，控制返回条数
     * @param t
     * @param limit
     * @param offset
     * @param <T>
     * @return
     */
    public static  <T> T getRel(T t,Integer limit,Integer offset){
        return MAIN.getRel(t,limit,offset);
    }

    /**
     * 获取关系对象，控制返回条数和返回的字段
     * @param t
     * @param fields -- 返回的字段字符串
     * @param limit -- 最多返回多少记录 ,null 时，取 @EDbRel里的limit默认值
     * @param offset -- 从第几条记录开始，起始为 0
     * @param <T>
     * @return
     */
    public static <T> T getRel(T t,String fields,Integer limit,Integer offset){
        return MAIN.getRel(t,fields,limit,offset);
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
     * @param limit -- 返回的条数
     * @param offset -- 起始位置
     * @return
     */
    public static List<Future<Object>> getRelForFutrue(Object t,String relKey,Integer limit,Integer offset){
        return MAIN.getRelKeyForFutrue(t,relKey,limit,offset);
    }


    /**
     * 异步获取对象
     * @param t
     * @param relKey
     * @param fields
     * @param limit
     * @param offset
     * @return
     */
    public static List<Future<Object>> getRelForFutrue(Object t,String relKey,String fields,Integer limit,Integer offset){
        return MAIN.getRelKeyForFutrue(t,relKey,fields,limit,offset);
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
    public static <T> T getView(T t){
        return MAIN.getView(t);
    }

}
