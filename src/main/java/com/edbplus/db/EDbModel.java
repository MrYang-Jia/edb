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
import com.edbplus.db.util.hutool.map.CaseInsensitiveMap;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.kit.SyncWriteMap;
import com.jfinal.plugin.activerecord.*;

import java.util.ArrayList;
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
public class EDbModel<M extends EDbModel> {

    private String configName; // 单数据源时指向,多数据源时，可通过 use 切换到另外一个数据库进行操作,一体多用

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
     * 私有方法，避免转json时暴露到外部
     * @return
     */
    private EDbPro getEDbPro() {
        if(configName==null){
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
    public M use(){
        if(configName==null){
            this.configName = DbKit.MAIN_CONFIG_NAME;
            this.defaultConfigName = this.configName;
        }
        return use(configName);
    }

    /**
     * 默认初始化当前实体对象
     */
    public M use(String configName){
        this.configName = configName;
        if(defaultConfigName == null){
            this.defaultConfigName = DbKit.MAIN_CONFIG_NAME; // 避免直接使用use时，导致 defaultConfigName 没有指向
        }
        return (M) this;
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
     * 保存并返回int结果，1：成功 2:失败
     * @return
     */
    public int saveReInt(){
        return getEDbPro().saveReInt(this);
    }

    /**
     * 保存
     * @return
     */
    public boolean save(){
        return getEDbPro().save(this);
    }


    /**
     * 更新对象 -- 剔除null值
     * @return
     */
    public boolean update(){
        return getEDbPro().update(this);
    }

    /**
     * 更新对象 -- 剔除null值，成功返回1，失败返回0
     * @return
     */
    public  int updateReInt(){
        return getEDbPro().updateReInt(this);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public  boolean update(Map<String,Object> updateData,boolean isColumnName){
        Map<String,Object> priKeyMapAndAll = null;
        if(isColumnName){
            priKeyMapAndAll = JpaAnnotationUtil.getPriKeyColumnMap(this); // 获取数据库主键字段，同时会转成忽略大小写的map对象
        }else{
            priKeyMapAndAll = JpaAnnotationUtil.getPriKeyMap(this); // 获取驼峰式数据库主键字段
        }
        priKeyMapAndAll.putAll(updateData);
        // 赋予其他需要更新的字段 --> 如果对方有主键键值，则会根据实际情况进行改写
        return getEDbPro().update(this.getClass(),priKeyMapAndAll,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @return
     */
    public  int updateReInt(Map<String,Object> updateData){
        CaseInsensitiveMap<String,Object> priKeyMapAndAll = JpaAnnotationUtil.getPriKeyColumnMap(this);
        priKeyMapAndAll.putAll(updateData); // 赋予其他需要更新的字段 --> 如果对方有主键键值，则会根据实际情况进行改写
        return getEDbPro().updateReInt(this.getClass(),priKeyMapAndAll);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName -- 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public  int updateReInt(Map<String,Object> updateData,boolean isColumnName){
        Map<String,Object> priKeyMapAndAll = null;
        if(isColumnName){
            priKeyMapAndAll = JpaAnnotationUtil.getPriKeyColumnMap(this); // 获取数据库主键字段，同时会转成忽略大小写的map对象
        }else{
            priKeyMapAndAll = JpaAnnotationUtil.getPriKeyMap(this); // 获取驼峰式数据库主键字段
        }
        priKeyMapAndAll.putAll(updateData); // 赋予其他需要更新的字段 --> 如果对方有主键键值，则会根据实际情况进行改写
        return getEDbPro().updateReInt(this.getClass(),priKeyMapAndAll,isColumnName);
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @return
     */
    public  boolean update(Map<String,Object> updateData){
        CaseInsensitiveMap<String,Object> priKeyMapAndAll = JpaAnnotationUtil.getPriKeyColumnMap(this);
        priKeyMapAndAll.putAll(updateData); // 赋予其他需要更新的字段 --> 如果对方有主键键值，则会根据实际情况进行改写
        return getEDbPro().update(this.getClass(),priKeyMapAndAll);
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
    public  int updateReInt(boolean containsNullValue){
        return getEDbPro().updateReInt(this,containsNullValue);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @return
     */
    public  boolean update(boolean containsNullValue){
        return getEDbPro().update(this,containsNullValue);
    }


    /**
     * 删除当前对象
     * @return
     */
    public boolean deleteById(){
        return getEDbPro().deleteById(this);
    }




    /**
     * 创建当前对象的关系代理对象
     * @return
     */
    public M rel(){
        return (M) getEDbPro().rel(this);
    }

    /**
     * 创建当前对象的关系代理对象，并设置每次查询的分页个数
     * @param pageNo
     * @param pageSize
     * @return
     */
    public M rel(Integer pageNo,Integer pageSize){
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
        return (M) getEDbPro().rel(this,fields,pageNo,pageSize);
    }

    /**
     * 返回数据对象本身
     * @returnt
     */
    public M getAllRel(){
        getEDbPro().getAllRel(this);
        return (M) this;
    }

    /**
     * 获取视图对象
     * @return
     */
    public M view(){
        return (M) getEDbPro().view(this);
    }


    /**
     * 获取翻页视图对象
     * @param pageNo
     * @param pageSize
     * @return
     */
    public M view(int pageNo,int pageSize){
        return (M) getEDbPro().view(this,pageNo,pageSize);
    }


}
