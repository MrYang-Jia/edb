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
package com.edbplus.db;

import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.util.bean.EDbBeanUtil;
import com.edbplus.db.util.hutool.annotation.EAnnotationUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;

import javax.persistence.Table;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EModel
 * @param <M> 建议引用最外层的对象，这样子的话，就可以通过最外层的对象引用db的通用方法，但是多层的话，有一个缺陷就是互相嵌套的感觉，但是还好与继承实际上不是一个体系，是设计上的一个看似污点的操作
 * @Description: 类似jfinalModel的作用
 * @Author 杨志佳
 * @Date 2021/11/4
 * @Version V1.0
 **/
// https://blog.csdn.net/blwinner/article/details/98532847 fastJson fastJson 注解相关
@JsonIgnoreType // 不允许被fastJson反解析调用，避免默认调用方法级时被反向触发get方法，导致循环调用相应的方法
public class EDbDao<M> {

    private Class<M> mClass; // 表对象 class 类型
    private String configName; // 单数据源时指向
    private String defaultConfigName; // 每次切换完数据库查询完后，当前实例化对象必须进行一次重置，以便指向正确的数据库进行相关操作


    /**
     * 指定默认数据源(有需要的情况下，可以指定默认的操作库)
     * @param configName
     */
    public void mainConfig(String configName) {
        this.configName = configName;
        this.defaultConfigName = configName;
    }

    /**
     * 模拟dao层通用常规操作行为
     * @return
     */
    public EDbDao(Class<M> mClass) {
        this.mClass = mClass;
    }

    /**
     * 模拟dao层通用常规操作行为
     * @return
     */
    public EDbDao(Class<M> mClass,String configName) {
        this.mClass = mClass;
        this.configName = configName; // 指定默认数据源
        this.defaultConfigName = configName; // 指定默认数据源
    }


    /**
     * 私有方法，避免转json时暴露到外部
     * @return
     */
    private EDbPro getEDbPro() {
        if(configName == null){
            this.configName = DbKit.MAIN_CONFIG_NAME; // 默认库
            this.defaultConfigName = this.configName; // 默认数据源指定
        }
        EDbPro eDbPro = EDb.use(configName); // 当前操作的数据库对象
        this.configName = this.defaultConfigName; //切换回默认数据源
        return eDbPro;
    }

    /**
     * 默认初始化当前实体对象
     */
    public EDbDao<M> use(){
        if(configName ==null){
            this.configName = DbKit.MAIN_CONFIG_NAME;
            this.defaultConfigName = this.configName;
        }
        return use(configName);
    }

    /**
     * 默认初始化当前实体对象
     */
    public EDbDao<M> use(String configName){
        if(this.defaultConfigName == null){// 如果默认配置没有，则根据情况进行重置
            if(this.configName == null) {
                this.defaultConfigName = DbKit.MAIN_CONFIG_NAME; // 避免直接使用use时，导致 defaultConfigName 没有指向
            }else{
                this.defaultConfigName = this.configName;
            }
        }
        this.configName = configName;
        return this;
    }



    /**
     * 返回当前表对象名称
     * @return
     */
    public String getTableName() {
        return getEDbPro().getTableName(this.mClass);
    }

    /**
     * 返回数据库字段
     * @param ignoreNullValue - true -屏蔽 null , false - 包含null
     * @return
     */
    public  Map<String,Object> getColumnsMap(boolean ignoreNullValue){
        return getEDbPro().getColumnsMap(this,ignoreNullValue);
    }

    /**
     * 获取真实的jpa对象实例 -- 如果没有直接指定申明 jpa 对象class 类型，则必须调用该方法，保证类型的准确性
     * @return
     */
    public  Class<M> getRealJpaClass(){
        return this.mClass;
    }

    /**
     * 根据对象主键返回实体
     * @param idValues -- 根据字段的顺序进行赋值
     * @param
     * @return
     */
    public M findByGroupId(Object... idValues ) {
        return getEDbPro().findByGroupId(this.mClass,idValues);
    }

    /**
     * 根据主键找对象
     * @param idValue
     * @return
     */
    public  M findById( Object idValue ){
        return getEDbPro().findById(this.mClass,idValue);
    }

    /**
     * 保存并返回int结果，1：成功 2:失败
     * @return
     */
    public int saveReInt(M m){
        return getEDbPro().saveReInt(m);
    }

    /**
     * 保存
     * @return
     */
    public boolean save(M m){
        return getEDbPro().save(m);
    }

    /**
     * 批量提交并返回主键
     * 目前只测试了 mysql postgresql 会返回，所以独立方法，避免影响到原 jfinal 的批量提交
     * @param saveList
     * @param batchSize
     * @return
     */
    public int[] batchSaveRid(List<M> saveList, int batchSize){
        Class mClass = saveList.get(0).getClass(); // 如果保存列表是null，直接抛错就好，不然保存插入也是会报错
        return getEDbPro().batchSaveRid(mClass,saveList,batchSize);
    }

    /**
     * 批量保存记录 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常,并且该返回是没有主键id的回填
     * 缺陷：会多消耗1倍数据对象的内存去执行操作 ，推荐用 insertValues -- 比较省内存
     * @param saveList
     * @param batchSize
     * @param <M>
     * @return
     */
    public <M> int[] batchSave(List<M> saveList,int batchSize){
        Class mClass = saveList.get(0).getClass(); // 如果保存列表是null，直接抛错就好，不然保存插入也是会报错
        return getEDbPro().batchSave(mClass,saveList,batchSize);
    }

    /**
     * 使用 insert values(...),(...) 的方式批量插入对象
     * @param saveList
     * @param batchSize
     */
    public  int  insertValues(List<M> saveList,int batchSize){
        Class mClass = saveList.get(0).getClass(); // 如果保存列表是null，直接抛错就好，不然保存插入也是会报错
        return getEDbPro().insertValues(mClass,saveList,batchSize);
    }


    /**
     * 更新对象 -- 剔除null值
     * @return
     */
    public boolean update(M m){
        return getEDbPro().update(m);
    }

    /**
     * 通过 sqlPara 执行更新语句
     * @return
     */
    public int update(SqlPara sqlPara){
        return getEDbPro().update(sqlPara);
    }

    /**
     * 更新对象 -- 剔除null值，成功返回1，失败返回0
     * @return
     */
    public  int updateReInt(M m){
        return getEDbPro().updateReInt(m);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * ps: 用户必须自己指定主键键值
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public  boolean update(Map<String,Object> updateData,boolean isColumnName){
        return getEDbPro().update(this.mClass,updateData,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * ps: 用户必须自己指定主键键值
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @return
     */
    public  int updateReInt(Map<String,Object> updateData){
        return getEDbPro().updateReInt(this.mClass,updateData);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * ps: 用户必须自己指定主键键值
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName -- 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public  int updateReInt(Map<String,Object> updateData,boolean isColumnName){
        return getEDbPro().updateReInt(this.mClass,updateData,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * ps: 用户必须自己指定主键键值
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @return
     */
    public  boolean update(Map<String,Object> updateData){
        return getEDbPro().update(this.mClass,updateData);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param oldM -- 原始数据对象
     * @param updateM -- 基于原始数据变更后的数据对象
     * @return
     */
    public  boolean updateCompare(M oldM,M updateM){
        return getEDbPro().updateCompare(oldM,updateM);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @return
     */
    public int updateReInt(M m ,boolean containsNullValue){
        return getEDbPro().updateReInt(m,containsNullValue);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @return
     */
    public  boolean update(M m,boolean containsNullValue){
        return getEDbPro().update(m,containsNullValue);
    }



    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param updateList
     * @param batchSize
     * @return
     */
    public   int[] batchUpdate(List<M> updateList, int batchSize){
        return getEDbPro().batchUpdate(this.mClass,updateList,batchSize);
    }

    /**
     * 批量更新 -- 推荐使用该方式
     * @param updateList -- 需要更新的实体对象集合
     * @param updateFields -- 指定要更新的字段，必须要有 @column 对应的对象字段名，即 entity.fieldName，否则无法正常匹配
     * @param batchSize -- 每批次批量更新提交的数量
     * @return
     */
    public  int[] batchUpdate(List<M> updateList,List<String> updateFields, int batchSize){
        return getEDbPro().batchUpdate(this.mClass,updateList,updateFields,batchSize);
    }

    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param updateList
     * @param batchSize
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @return
     */
    public  int[] batchUpdate(List<M> updateList, int batchSize,boolean containsNullValue){
        return getEDbPro().batchUpdate(this.mClass,updateList,batchSize,containsNullValue);
    }

    /**
     * 删除对象
     * @param m
     * @return
     */
    public boolean delete(M m){
        return getEDbPro().delete(m);
    }

    /**
     * 传入一个id组，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param ids
     * @return
     */
    public boolean deleteByGroupIds(Object... ids){
        return getEDbPro().deleteByGroupIds(this.mClass,ids);
    }

    /**
     * 传入一个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param id
     * @return
     */
    public boolean deleteById(Object id){
        Table table = EAnnotationUtil.getAnnotation(id.getClass(), Table.class);
        if(table!=null){ // 如果传入的是jpa对象，则直接操作即可
            return getEDbPro().deleteById(id);
        }
        return getEDbPro().deleteById(this.mClass,id);
    }





    /**
     * 传入多个携带 id 的 jpa对象，以此删除该对象，没有id则职id为null，不做删除数据处理
     * @param jpaList
     * @return
     */
    public  int deleteByJpaList(List<M> jpaList){
        return getEDbPro().deleteByIds(jpaList);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param deleteIds
     * @return
     */
    public  int deleteByIds(List<Object> deleteIds){
        return getEDbPro().deleteByIds(this.mClass,deleteIds);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param deleteIds -- 以 , 为分隔符的id串
     * @return
     */
    public  int deleteByIds(String deleteIds) {
        return getEDbPro().deleteByIds(this.mClass,deleteIds);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param deleteIds
     * @param splitStr
     * @return
     */
    public  int deleteByIds(String deleteIds,String splitStr){
        return getEDbPro().deleteByIds(this.mClass,deleteIds,splitStr);
    }


    /**
     * 根据复合主键找对象
     * @param tableName
     * @param primaryKey
     * @param idValues
     * @return
     */
    public  M findByGroupId(String tableName, String primaryKey, Object... idValues){
        return getEDbPro().findByGroupId(this.mClass,tableName,primaryKey,idValues);
    }

    /**
     * 根据唯一主键找对象
     * @param tableName
     * @param primaryKey
     * @param idValue
     * @return
     */
    public  M findById(String tableName, String primaryKey, Object idValue){
        return  getEDbPro().findById(this.mClass,tableName,primaryKey,idValue);
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param ids
     * @return
     */
    public List<M> findByIds(List<Object> ids){
        return getEDbPro().findByIds(this.mClass,ids);
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param idsStr
     * @param splitStr
     * @return
     */
    public List<M> findByIds( String idsStr,String splitStr){
        return getEDbPro().findByIds(this.mClass,idsStr,splitStr);
    }

    /**
     * 通过sql语句返回对象实体
     * @param finalSql
     * @return
     */
    public List<M> find(String finalSql){
        return getEDbPro().find(this.mClass,finalSql);
    }

    /**
     * 通过对象和sqlpara对象返回查询结果
     * @param sqlPara
     * @return
     */
    public  List<M> find( SqlPara sqlPara){
        return getEDbPro().find(this.mClass,sqlPara);
    }

    /**
     * 传入对象和sql，返回查询结果
     * @param sql
     * @param paras
     * @return
     */
    public  List<M> find(String sql, Object... paras){
        return getEDbPro().find(this.mClass,sql,paras);
    }

    /**
     * 根据查询sql进行分页查询
     * @param pageNumber
     * @param pageSize
     * @param findSql
     * @return
     */
    public Page<M> paginate( int pageNumber, int pageSize, String findSql){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,findSql);
    }

    /**
     * 根据查询sql和参数进行分页查询
     * @param pageNumber
     * @param pageSize
     * @param findSql
     * @param paras
     * @return
     */
    public  Page<M> paginate(int pageNumber, int pageSize, String findSql,Object... paras){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,findSql,paras);
    }

    /**
     * 根据设定好的查询总记录数和查询语句返回分页记录集
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param findSql
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize,long totalRow, String findSql){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,totalRow,findSql);
    }

    /**
     * 根据设定好的查询总记录数和查询语句返回分页记录集
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param findSql
     * @param paras
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize,long totalRow, String findSql,Object... paras){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,totalRow,findSql,paras);
    }

    /**
     * 根据 预设的数据库总记录数 和 sqlPara查询对象，返回指定的对象分页列表
     * @param pageNumber
     * @param pageSize
     * @param sqlPara
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize,long totalRow, SqlPara sqlPara){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,totalRow,sqlPara);
    }

    /**
     * 根据 sqlPara查询对象，返回指定的对象分页列表
     * @param pageNumber
     * @param pageSize
     * @param sqlPara
     * @return
     */
    public  Page<M> paginate(int pageNumber, int pageSize, SqlPara sqlPara){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,sqlPara);
    }

    /**
     * 根据 sqlPara查询对象、是否分组sql,返回指定的对象分页列表
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param sqlPara
     * @return
     */
    public  Page<M> paginate(int pageNumber, int pageSize, boolean isGroupBySql, SqlPara sqlPara){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,isGroupBySql,sqlPara);
    }

    /**
     * 返回分页对象
     * @param pageNumber
     * @param pageSize
     * @param select
     * @param sqlExceptSelect
     * @param paras
     * @return
     */
    public  Page<M> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,select,sqlExceptSelect,paras);
    }

    /**
     * 返回分页对象
     * @param pageNumber
     * @param pageSize
     * @param select
     * @param sqlExceptSelect
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize, String select, String sqlExceptSelect){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,select,sqlExceptSelect);
    }

    /**
     * 返回分页对象（实现返回对象的分页逻辑实现）
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param select
     * @param sqlExceptSelect
     * @param paras
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras){
        return getEDbPro().paginate(this.mClass,pageNumber,pageSize,isGroupBySql,select,sqlExceptSelect,paras);
    }

    /**
     * 获取1条记录
     * @param sql
     * @param paras
     * @return
     */
    public M findFirst(String sql, Object... paras){
        return  getEDbPro().findFirst(this.mClass,sql,paras);
    }

    /**
     * 获取1条记录
     * @param sql
     * @return
     */
    public  M findFirst(String sql){
        return  getEDbPro().findFirst(this.mClass,sql);
    }

    /**
     * 获取1条记录
     * @param sqlPara
     * @return
     */
    public M findFirst(SqlPara sqlPara){
        return  getEDbPro().findFirst(this.mClass,sqlPara);
    }

    /**
     * 获取首条记录
     * @param eDbQuery
     * @return
     */
    public  M findFirst(EDbQuery eDbQuery){
        return (M) getEDbPro().findFirst(this.mClass,eDbQuery);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sqlPara
     * @return
     */
    public  M findOnlyOne(SqlPara sqlPara){
        return  getEDbPro().findOnlyOne(this.mClass,sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param eDbQuery
     * @return
     */
    public M findOnlyOne(EDbQuery eDbQuery){
        return getEDbPro().findOnlyOne(this.mClass,eDbQuery);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sql
     * @return
     */
    public M findOnlyOne(String sql){
        return getEDbPro().findOnlyOne(this.mClass,sql);
    }

    /**
     * 根据 EDbQuery 返回查询结果
     * @param eDbQuery
     * @return
     */
    public List<M> find(EDbQuery eDbQuery){
        return getEDbPro().find(this.mClass,eDbQuery);
    }

    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param eDbQuery
     * @param limit
     * @return
     */
    public List<M> find(EDbQuery eDbQuery,int limit){
        return  getEDbPro().find(this.mClass,eDbQuery,limit);
    }

    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param eDbQuery
     * @param limit
     * @param offset
     * @return
     */
    public List<M> find(EDbQuery eDbQuery,int limit,Integer offset){
        return  getEDbPro().find(this.mClass,eDbQuery,limit,offset);
    }

    /**
     * 根据 EDbQuery 返回分页对象
     * @param pageNumber
     * @param pageSize
     * @param eDbQuery
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize, EDbQuery eDbQuery){
        return  getEDbPro().paginate(this.mClass,pageNumber,pageSize,eDbQuery);
    }

    /**
     * 根据 EDbQuery 返回分页对象（已制定 totalRow的模式）
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param eDbQuery
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize,long totalRow, EDbQuery eDbQuery){
        return  getEDbPro().paginate(this.mClass,pageNumber,pageSize,totalRow,eDbQuery);
    }

    /**
     * 创建当前对象的关系代理对象
     * @param m - jpa 对象
     * @return
     */
    public M rel(M m){
        return getEDbPro().rel(m);
    }

    /**
     * 创建当前对象的关系代理对象，并设置每次查询的分页个数
     * @param m - jpa 对象
     * @param pageNo
     * @param pageSize
     * @return
     */
    public M rel(M m,Integer pageNo,Integer pageSize){
        return getEDbPro().rel(m,pageNo,pageSize);
    }

    /**
     * 限定字段并返回
     * @param fields -- 可传入自定义的sql字段，并可使用 case when 等语法替代字段的模式
     * @param pageNo
     * @param pageSize
     * @return
     */
    public  M rel(M m,String fields,Integer pageNo,Integer pageSize){
        return getEDbPro().rel(m,fields,pageNo,pageSize);
    }

    /**
     * 返回数据对象本身
     * @returnt
     */
    public M getAllRel(M m){
        getEDbPro().getAllRel(m);
        return m;
    }

    /**
     * 获取视图对象
     * @return
     */
    public M view(M m){
        return getEDbPro().view(m);
    }


    /**
     * 获取翻页视图对象
     * @param pageNo
     * @param pageSize
     * @return
     */
    public M view(M m,int pageNo,int pageSize){
        return getEDbPro().view(m,pageNo,pageSize);
    }


    public SqlPara getSqlPara(String key, Map data) {
        return getEDbPro().getConfig().getSqlKit().getSqlPara(key, data);
    }

    public SqlPara getSqlPara(String key, Object... paras) {
        return getEDbPro().getConfig().getSqlKit().getSqlPara(key, paras);
    }

    /**
     * 把当前对象当作参数传递到视图
     * @param key
     * @return
     */
    public SqlPara getSqlPara(String key,M m) {
        return this.getSqlPara(key, EDbBeanUtil.beanToMap(m));
    }

    public SqlPara getSqlParaByString(String content, Map data) {
        return getEDbPro().getConfig().getSqlKit().getSqlParaByString(content, data);
    }

    public SqlPara getSqlParaByString(String content, Object... paras) {
        return getEDbPro().getConfig().getSqlKit().getSqlParaByString(content, paras);
    }

    /**
     * 当前对象传递到视图，并返回 SqlPara 对象
     * @param content
     * @return
     */
    public SqlPara getSqlParaByString(String content,M m) {
        return getEDbPro().getSqlParaByString(content, EDbBeanUtil.beanToMap(m));
    }

    public EDbDaoTemplate<M> template(String key, Map data) {
        return new EDbDaoTemplate(this, key, data);
    }

    public EDbDaoTemplate<M> template(String key, Object... paras) {
        return new EDbDaoTemplate(this, key, paras);
    }

    public EDbDaoTemplate<M> template(String key,M m) {
        return this.template(key, EDbBeanUtil.beanToMap(m));
    }

    public EDbDaoTemplate<M> templateByString(String content, Map data) {
        return new EDbDaoTemplate(true, this, content, data);
    }

    public EDbDaoTemplate<M> templateByString(String content, Object... paras) {
        return new EDbDaoTemplate(true, this, content, paras);
    }

    public EDbDaoTemplate<M> templateByString(String content,M m) {
        return this.templateByString(content, EDbBeanUtil.beanToMap(m));
    }


}
