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

import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.util.bean.EDbBeanUtil;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.plugin.activerecord.*;

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
public class EDbModel<M extends EDbModel> {
//    public static Map<Class,EDbPro> eDbProMap = new HashMap<>(); // 数据集合对象

    private int upType = 1; // 默认开启允许更新

    private String modelConfigName ; // 多数据源时触发

    private String configName; // 单数据源时指向

    // 额外的开销，如果是多数据源的话，还是得分开，不然一个对象多用途，是会有所影响的
    private static final Map<String, EDbModel> dbMap = new SyncWriteMap<String, EDbModel>(32, 0.25F);

    /**
     * 定义对象组合名 -- 只有使用 use(xxx) 时触发
     * @param modelConfigName
     */
    private void setModelConfigName(String modelConfigName){
        this.modelConfigName = modelConfigName;
    }

    /**
     * 指定数据源
     * @param configName
     */
    private void setConfigName(String configName) {
        this.configName = configName;
    }

    //private EDbPro eDbPro; // 数据对象

    /**
     * 模拟dao层通用常规操作行为
     * @return
     */
    public M dao() {
        this.upType = 0; // 特殊方法不允许直接操作，例如更新或关联查询等操作，从而允许常规通用查询操作
        // 有需要还能定义扩展其他参数，做一些前置项的对象改造，一般是无需调整
        return (M) this;
    }

    /**
     * 模拟dao层通用常规操作行为
     * @return
     */
    public M dao(String configName) {
        this.upType = 0; // 特殊方法不允许直接操作，例如更新或关联查询等操作，从而允许常规通用查询操作
        this.configName = configName; // 指定默认数据源
        // 有需要还能定义扩展其他参数，做一些前置项的对象改造，一般是无需调整
        return (M) this;
    }

    /**
     * 检查更新开关
     */
    public void checkUpType(){
        if(this.upType == 0){
            throw new RuntimeException(" 不允许直接进行保存或更新操作，包括直接调用 rel() or view() ");
        }
    }

    /**
     * 私有方法，避免转json时暴露到外部
     * @return
     */
    private EDbPro getEDbPro() {
        if(configName==null){
            this.configName = DbKit.MAIN_CONFIG_NAME;
        }
        return EDb.use(configName);
    }

    /**
     * 默认初始化当前实体对象
     */
    public M use(){
        if(modelConfigName==null){
            this.modelConfigName = DbKit.MAIN_CONFIG_NAME;
        }
        return use(modelConfigName);
    }

    /**
     * 默认初始化当前实体对象
     */
    public M use(String configName){
        String modelConfigName = configName +":" +this.getClass().getSimpleName();
        if(dbMap.get(modelConfigName) !=null ){
            return (M) dbMap.get(modelConfigName);
        }
        EDbModel<M> eDbModel = null; // 定义新的数据对象
        try {
            eDbModel =  this.getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        eDbModel.setModelConfigName(modelConfigName); // 定义对象
        eDbModel.setConfigName(configName); // 定义数据集对象
        dbMap.put(modelConfigName,eDbModel);
        return (M) eDbModel;
    }



    /**
     * 返回当前表对象名称
     * @return
     */
    public String getTableName() {
        return getEDbPro().getTableName(this.getClass());
    }

    /**
     * 返回数据库字段
     * @param ignoreNullValue - true -屏蔽 null , false - 包含null
     * @param <M>
     * @return
     */
    public <M> Map<String,Object> getColumnsMap(boolean ignoreNullValue){
        return getEDbPro().getColumnsMap(this,ignoreNullValue);
    }

    /**
     * 获取真实的jpa对象实例 -- 如果没有直接指定申明 jpa 对象class 类型，则必须调用该方法，保证类型的准确性
     * @param <M>
     * @return
     */
    public <M> Class<M> getRealJpaClass(){
        return (Class<M>) this.getClass();
    }

    /**
     * 根据对象主键返回实体
     * @param idValues -- 根据字段的顺序进行赋值
     * @param
     * @return
     */
    public M findByGroupId(Object... idValues ) {
        return (M) getEDbPro().findByGroupId(this.getClass(),idValues);
    }

    /**
     * 根据主键找对象
     * @param idValue
     * @return
     */
    public  M findById( Object idValue ){
        return (M) getEDbPro().findById(this.getClass(),idValue);
    }

    /**
     * 保存并返回int结果，1：成功 2:失败
     * @return
     */
    public int saveReInt(){
        checkUpType();
        return getEDbPro().saveReInt(this);
    }

    /**
     * 保存
     * @return
     */
    public boolean save(){
        checkUpType();
        return getEDbPro().save(this);
    }

    /**
     * 批量提交并返回主键
     * 目前只测试了 mysql postgresql 会返回，所以独立方法，避免影响到原 jfinal 的批量提交
     * @param saveList
     * @param batchSize
     * @return
     */
    public int[] batchSaveRid(List<M> saveList, int batchSize){
        checkUpType();
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
        checkUpType();
        Class mClass = saveList.get(0).getClass(); // 如果保存列表是null，直接抛错就好，不然保存插入也是会报错
        return getEDbPro().batchSave(mClass,saveList,batchSize);
    }

    /**
     * 使用 insert values(...),(...) 的方式批量插入对象
     * @param saveList
     * @param batchSize
     */
    public  int  insertValues(List<M> saveList,int batchSize){
        checkUpType();
        Class mClass = saveList.get(0).getClass(); // 如果保存列表是null，直接抛错就好，不然保存插入也是会报错
        return getEDbPro().insertValues(mClass,saveList,batchSize);
    }


    /**
     * 更新对象 -- 剔除null值
     * @param <M>
     * @return
     */
    public <M> boolean update(){
        checkUpType();
        return getEDbPro().update(this);
    }

    /**
     * 更新对象 -- 剔除null值，成功返回1，失败返回0
     * @return
     */
    public  int updateReInt(){
        checkUpType();
        return getEDbPro().updateReInt(this);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public  boolean update(Map<String,Object> updateData,boolean isColumnName){
        checkUpType();
        return getEDbPro().update(this.getClass(),updateData,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @return
     */
    public  int updateReInt(Map<String,Object> updateData){
        checkUpType();
        return getEDbPro().updateReInt(this.getClass(),updateData);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName -- 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public  int updateReInt(Map<String,Object> updateData,boolean isColumnName){
        checkUpType();
        return getEDbPro().updateReInt(this.getClass(),updateData,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @return
     */
    public  boolean update(Map<String,Object> updateData){
        checkUpType();
        return getEDbPro().update(this.getClass(),updateData);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param oldM -- 原始数据对象
     * @param updateM -- 基于原始数据变更后的数据对象
     * @param <M>
     * @return
     */
    public <M> boolean updateCompare(M oldM,M updateM){
        checkUpType();
        return getEDbPro().updateCompare(oldM,updateM);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public <M> int updateReInt(boolean containsNullValue){
        checkUpType();
        return getEDbPro().updateReInt(this,containsNullValue);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public <M> boolean update(boolean containsNullValue){
        checkUpType();
        return getEDbPro().update(this,containsNullValue);
    }



    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param updateList
     * @param batchSize
     * @return
     */
    public   int[] batchUpdate(List<M> updateList, int batchSize){
        checkUpType();
        Class mClass = updateList.get(0).getClass();
        return getEDbPro().batchUpdate(mClass,updateList,batchSize);
    }

    /**
     * 批量更新 -- 推荐使用该方式
     * @param updateList -- 需要更新的实体对象集合
     * @param updateFields -- 指定要更新的字段，必须要有 @column 对应的对象字段名，即 entity.fieldName，否则无法正常匹配
     * @param batchSize -- 每批次批量更新提交的数量
     * @param <M>
     * @return
     */
    public  <M> int[] batchUpdate(List<M> updateList,List<String> updateFields, int batchSize){
        checkUpType();
        Class mClass = updateList.get(0).getClass();
        return getEDbPro().batchUpdate(mClass,updateList,updateFields,batchSize);
    }

    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param updateList
     * @param batchSize
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @return
     */
    public  int[] batchUpdate(List<M> updateList, int batchSize,boolean containsNullValue){
        checkUpType();
        Class mClass = updateList.get(0).getClass();
        return getEDbPro().batchUpdate(mClass,updateList,batchSize,containsNullValue);
    }

    /**
     * 传入一个id组，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param ids
     * @return
     */
    public boolean deleteByGroupIds(Object... ids){
        checkUpType();
        return getEDbPro().deleteByGroupIds(this.getClass(),ids);
    }

    /**
     * 传入一个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param id
     * @return
     */
    public boolean deleteById(Object id){
        checkUpType();
        return getEDbPro().deleteById(this.getClass(),id);
    }

    /**
     * 删除当前对象
     * @return
     */
    public boolean deleteById(){
        checkUpType();
        return getEDbPro().deleteById(this);
    }

    /**
     * 传入多个携带 id 的 jpa对象，以此删除该对象，没有id则职id为null，不做删除数据处理
     * @param jpaList
     * @return
     */
    public  int deleteByJpaList(List<M> jpaList){
        checkUpType();
        return getEDbPro().deleteByIds(jpaList);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param deleteIds
     * @return
     */
    public  int deleteByIds(List<Object> deleteIds){
        checkUpType();
        return getEDbPro().deleteByIds(this.getClass(),deleteIds);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param deleteIds -- 以 , 为分隔符的id串
     * @return
     */
    public  int deleteByIds(String deleteIds) {
        checkUpType();
        return getEDbPro().deleteByIds(this.getClass(),deleteIds);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param deleteIds
     * @param splitStr
     * @return
     */
    public  int deleteByIds(String deleteIds,String splitStr){
        checkUpType();
        return getEDbPro().deleteByIds(this.getClass(),deleteIds,splitStr);
    }


    /**
     * 根据复合主键找对象
     * @param tableName
     * @param primaryKey
     * @param idValues
     * @return
     */
    public  M findByGroupId(String tableName, String primaryKey, Object... idValues){
        return (M) getEDbPro().findByGroupId(this.getClass(),tableName,primaryKey,idValues);
    }

    /**
     * 根据唯一主键找对象
     * @param tableName
     * @param primaryKey
     * @param idValue
     * @return
     */
    public  M findById(String tableName, String primaryKey, Object idValue){
        return (M) getEDbPro().findById(this.getClass(),tableName,primaryKey,idValue);
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param ids
     * @return
     */
    public List<M> findByIds(List<Object> ids){
        return (List<M>) getEDbPro().findByIds(this.getClass(),ids);
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param idsStr
     * @param splitStr
     * @return
     */
    public List<M> findByIds( String idsStr,String splitStr){
        return (List<M>) getEDbPro().findByIds(this.getClass(),idsStr,splitStr);
    }

    /**
     * 通过sql语句返回对象实体
     * @param finalSql
     * @return
     */
    public List<M> find(String finalSql){
        return (List<M>) getEDbPro().find(this.getClass(),finalSql);
    }

    /**
     * 通过对象和sqlpara对象返回查询结果
     * @param sqlPara
     * @return
     */
    public  List<M> find( SqlPara sqlPara){
        return (List<M>) getEDbPro().find(this.getClass(),sqlPara);
    }

    /**
     * 传入对象和sql，返回查询结果
     * @param sql
     * @param paras
     * @return
     */
    public  List<M> find(String sql, Object... paras){
        return (List<M>) getEDbPro().find(this.getClass(),sql,paras);
    }

    /**
     * 根据查询sql进行分页查询
     * @param pageNumber
     * @param pageSize
     * @param findSql
     * @return
     */
    public Page<M> paginate( int pageNumber, int pageSize, String findSql){
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,findSql);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,findSql,paras);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,totalRow,findSql);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,totalRow,findSql,paras);
    }

    /**
     * 根据 预设的数据库总记录数 和 sqlPara查询对象，返回指定的对象分页列表
     * @param pageNumber
     * @param pageSize
     * @param sqlPara
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize,long totalRow, SqlPara sqlPara){
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,totalRow,sqlPara);
    }

    /**
     * 根据 sqlPara查询对象，返回指定的对象分页列表
     * @param pageNumber
     * @param pageSize
     * @param sqlPara
     * @return
     */
    public  Page<M> paginate(int pageNumber, int pageSize, SqlPara sqlPara){
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,sqlPara);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,isGroupBySql,sqlPara);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,select,sqlExceptSelect,paras);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,select,sqlExceptSelect);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,isGroupBySql,select,sqlExceptSelect,paras);
    }

    /**
     * 获取1条记录
     * @param sql
     * @param paras
     * @return
     */
    public M findFirst(String sql, Object... paras){
        return (M) getEDbPro().findFirst(this.getClass(),sql,paras);
    }

    /**
     * 获取1条记录
     * @param sql
     * @return
     */
    public  M findFirst(String sql){
        return (M) getEDbPro().findFirst(this.getClass(),sql);
    }

    /**
     * 获取1条记录
     * @param sqlPara
     * @return
     */
    public M findFirst(SqlPara sqlPara){
        return (M) getEDbPro().findFirst(this.getClass(),sqlPara);
    }

    /**
     * 获取首条记录
     * @param eDbQuery
     * @return
     */
    public  M findFirst(EDbQuery eDbQuery){
        return (M) getEDbPro().findFirst(this.getClass(),eDbQuery);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sqlPara
     * @return
     */
    public  M findOnlyOne(SqlPara sqlPara){
        return (M) getEDbPro().findOnlyOne(this.getClass(),sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param eDbQuery
     * @return
     */
    public M findOnlyOne(EDbQuery eDbQuery){
        return (M) getEDbPro().findOnlyOne(this.getClass(),eDbQuery);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sql
     * @return
     */
    public M findOnlyOne(String sql){
        return (M) getEDbPro().findOnlyOne(this.getClass(),sql);
    }

    /**
     * 根据 EDbQuery 返回查询结果
     * @param eDbQuery
     * @return
     */
    public List<M> find(EDbQuery eDbQuery){
        return (List<M>) getEDbPro().find(this.getClass(),eDbQuery);
    }

    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param eDbQuery
     * @param limit
     * @return
     */
    public List<M> find(EDbQuery eDbQuery,int limit){
        return (List<M>) getEDbPro().find(this.getClass(),eDbQuery,limit);
    }

    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param eDbQuery
     * @param limit
     * @param offset
     * @return
     */
    public List<M> find(EDbQuery eDbQuery,int limit,Integer offset){
        return (List<M>) getEDbPro().find(this.getClass(),eDbQuery,limit,offset);
    }

    /**
     * 根据 EDbQuery 返回分页对象
     * @param pageNumber
     * @param pageSize
     * @param eDbQuery
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize, EDbQuery eDbQuery){
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,eDbQuery);
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
        return (Page<M>) getEDbPro().paginate(this.getClass(),pageNumber,pageSize,totalRow,eDbQuery);
    }

    /**
     * 创建当前对象的关系代理对象
     * @return
     */
    public M rel(){
        checkUpType();
        return (M) getEDbPro().rel(this);
    }

    /**
     * 创建当前对象的关系代理对象，并设置每次查询的分页个数
     * @param pageNo
     * @param pageSize
     * @return
     */
    public M rel(Integer pageNo,Integer pageSize){
        checkUpType();
        return (M) getEDbPro().rel(this,pageNo,pageSize);
    }

    /**
     * 限定字段并返回
     * @param fields -- 可传入自定义的sql字段，并可使用 case when 等语法替代字段的模式
     * @param pageNo
     * @param pageSize
     * @return
     */
    public  M rel(String fields,Integer pageNo,Integer pageSize){
        checkUpType();
        return (M) getEDbPro().rel(this,fields,pageNo,pageSize);
    }

    /**
     * 返回数据对象本身
     * @returnt
     */
    public M getAllRel(){
        checkUpType();
        return (M) getEDbPro().getAllRel(this);
    }

    /**
     * 获取视图对象
     * @return
     */
    public M view(){
        checkUpType();
        return (M) getEDbPro().view(this);
    }


    /**
     * 获取翻页视图对象
     * @param pageNo
     * @param pageSize
     * @return
     */
    public M view(int pageNo,int pageSize){
        checkUpType();
        return (M) getEDbPro().view(this,pageNo,pageSize);
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
    public SqlPara getSqlPara(String key) {
        return this.getSqlPara(key, EDbBeanUtil.beanToMap(this));
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
    public SqlPara getSqlParaByString(String content) {
        return getEDbPro().getSqlParaByString(content, EDbBeanUtil.beanToMap(this));
    }

    public EDbDaoTemplate<M> template(String key, Map data) {
        return new EDbDaoTemplate(this, key, data);
    }

    public EDbDaoTemplate<M> template(String key, Object... paras) {
        return new EDbDaoTemplate(this, key, paras);
    }

    public EDbDaoTemplate<M> template(String key) {
        return this.template(key, EDbBeanUtil.beanToMap(this));
    }

    public EDbDaoTemplate<M> templateByString(String content, Map data) {
        return new EDbDaoTemplate(true, this, content, data);
    }

    public EDbDaoTemplate<M> templateByString(String content, Object... paras) {
        return new EDbDaoTemplate(true, this, content, paras);
    }

    public EDbDaoTemplate<M> templateByString(String content) {
        return this.templateByString(content, EDbBeanUtil.beanToMap(this));
    }


}
