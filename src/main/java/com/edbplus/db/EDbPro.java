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

// 这种对象map非Util不需要扩展
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.CaseInsensitiveMap;

import com.edbplus.db.annotation.EDbSave;
import com.edbplus.db.annotation.EDbUpdate;
import com.edbplus.db.dialect.EDbPostgreSqlDialect;
import com.edbplus.db.druid.EDbSelectUtil;
import com.edbplus.db.dto.EDBListenerResult;
import com.edbplus.db.dto.FieldAndColValue;
import com.edbplus.db.dto.FieldAndColumn;
import com.edbplus.db.em.RunSqlType;
import com.edbplus.db.em.RunStatus;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.jpa.JpaBuilder;
import com.edbplus.db.listener.ConnectListener;
import com.edbplus.db.proxy.EDbRelProxy;
import com.edbplus.db.jpa.util.JpaRelUtil;
import com.edbplus.db.listener.EDbListener;
import com.edbplus.db.proxy.EDbViewProxy;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.EDbQueryUtil;
import com.edbplus.db.util.hutool.annotation.EAnnotationUtil;
import com.edbplus.db.util.hutool.array.EArrayUtil;
import com.edbplus.db.util.hutool.date.EDateUtil;
import com.edbplus.db.util.hutool.json.EJSONUtil;
import com.edbplus.db.util.hutool.number.ENumberUtil;
import com.edbplus.db.util.hutool.reflect.EReflectUtil;
import com.edbplus.db.util.hutool.rul.EReUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import javax.persistence.Table;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * @ClassName EDbPro
 * @Description: DbPro的替代类，并进行常规 jpa 对象 或 dto 对象的转换工具方法延伸
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Slf4j
@JsonIgnoreType
// 因为方法名以getxxx开头，如果没有参数的话，会被当作是属性对象返回给前端，所以接下来方法名命名要注意不能以get开头
@JsonIgnoreProperties({"realJpaClass","dbPro", "tableName","columnsMap","relKey","relKeyForFutrue","allRel","allRelForFutrue","countSql"})
public class EDbPro extends SpringDbPro {

//    protected final Config config;
    // jpa 监听
    @Setter
    private EDbListener eDbListener;

    @Setter// 保存后自动重新根据主键再查询1次
    private boolean saveAndFlush = false;

    // sql 连接监听，用于统计耗时，解析sql处理时使用
    @Setter
    private ConnectListener connectListener;

    public EDbPro(){
        super(DbKit.MAIN_CONFIG_NAME);
    }

    public EDbPro(String  configName){
        super(configName);
    }

    /**
     * 返回表名称
     * @param mClass
     * @param <M>
     * @return
     */
    public <M> String getTableName(Class<M> mClass) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        return table.name();
    }

    /**
     * 根据Jpa对象返回实体
     * @param mClass
     * @param idValues -- 根据字段的顺序进行赋值
     * @param <M>
     * @return
     */
    public <M> M findByGroupId(Class<M> mClass, Object... idValues ) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        // 根据主键返回对象
        M record = this.findByGroupId(mClass,table.name(),keys,idValues);
        return record;
    }

    /**
     * 根据主键找对象
     * @param mClass
     * @param idValue
     * @param <M>
     * @return
     */
    public <M> M findById(Class<M> mClass, Object idValue ) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        // 根据主键返回对象
        M record = this.findById(mClass,table.name(),keys,idValue);
        return record;
    }

    /**
     * 保存并返回int结果，1：成功 2:失败
     * @param m
     * @param <M>
     * @return
     */
    public <M> int saveReInt(M m){
        if(save(m)){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * 保存jpa对象
     * @param m
     * @param <M>
     * @return
     */
    public <M> boolean save(M m){
        if(m instanceof String){
            throw new RuntimeException(" 直接执行sql请使用 EDb.update(sql) ");
        }
        Class<M> mClass = getRealJpaClass(m);
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        // 获取所有字段列表
        List<FieldAndColValue> coumns  = JpaAnnotationUtil.getCoumnValues(m);

        Record record = new Record();
        // 字段赋值
        for(FieldAndColValue fieldAndColumn : coumns){
            // 不剔除null值的话，会导致部分字段数据库定义了默认值，会无效化
            // 但是batchEntity 时，就必须指定了，否则字段长度不一致，是无法提交的
            if(fieldAndColumn.getFieldValue()!= null){
                // 字段赋值 -- 对象全字段赋值
                record.set(fieldAndColumn.getColumn().name().toLowerCase(), fieldAndColumn.getFieldValue());
            }
        }

        // 保存前的监听
        if(eDbListener!=null){
            // 获取所有字段列表
            List<FieldAndColumn> allCoumns  = JpaAnnotationUtil.getCoumns(mClass);
            Map<String,Object> dataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            eDbListener.beforeSave(mClass,dataMap,allCoumns);
            // 替换数据
            record.setColumns(dataMap);
        }

        // 保存前的方法事件
        Method beforeUpdate = JpaAnnotationUtil.getMethod(mClass, EDbSave.class);
        //
        if(beforeUpdate != null){
            // 替换成 忽略 大小写的 map
            Map<String,Object> dataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            EReflectUtil.invoke(m, beforeUpdate, dataMap,coumns);
            // 替换数据
            record.setColumns(dataMap);
        }

        // 判断数据库类型
        if(this.getConfig().getDialect() instanceof PostgreSqlDialect) {
            // 目前只做了mysql 和 pg 版本的兼容
            for (FieldAndColValue fieldAndColumn : coumns) {
                if (fieldAndColumn.getIsPriKey() && fieldAndColumn.getFieldValue() == null) {
                    // 剔除字段
                    record.remove(fieldAndColumn.getColumn().name().toLowerCase());
                }
            }
        }


        // 保存到数据库
        boolean resultStatus = this.save(table.name(),keys,record);
        // 由于结构会返回主键字段，所以可以反向赋值 -- 由于是基于jpa标准，字段可能非正常字段，赋值会引起可能性的错误或不一致
        //BeanUtil.fillBeanWithMap(record.getColumns(),m,false);
        // 获取主键字段
        //coumns = JpaAnnotationUtil.getIdFieldAndColumnValues(m);
        if(saveAndFlush){// 代表保存后一定要刷新下数据，从数据库获取最新的数据变化，可能会因为数据库的默认值字段导致当前字段数据不一致
            String[] pKeys = keys.split(",");
            List<Object> objects = new ArrayList<>();
            for (String keyName:pKeys){
                objects.add(record.get(keyName));
            }
            M m2 = (M) this.findByGroupId(m.getClass(),objects.toArray());
            // 备注： 存在概率性主键回填失败的问题，例如 ID、GSID 等非驼峰式主键字段回填，使用以下方式偶然性主键丢失，所以建议不开启 saveAndFlush 插入保存后再刷新的的功能
            BeanUtil.copyProperties(m2,m); // 必须进行1次属性拷贝，不然无法替换 m 的数值
        }else {
            // 所有字段重新赋值，可能主键或者自定义键值产生变更（通过 beforeSave 方法调整变更的对象）
            for(FieldAndColumn fieldAndColumn : coumns) {
                // 字段赋值
                JpaAnnotationUtil.setFieldValue(m,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
//                BeanUtil.setFieldValue(m,fieldAndColumn.getField().getName(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
            }
        }


        return resultStatus;
    }

    /**
     * 保存操作
     * @param tableName
     * @param primaryKey
     * @param record
     * @return
     */
    public boolean save(String tableName, String primaryKey, Record record) {
        Connection conn = null;
        boolean var5;
        try {
            conn = this.config.getConnection();
            var5 = this.save(this.config, conn, tableName, primaryKey, record);
            // 再次校验 key 是否有值
//            String[] pKeys = primaryKey.split(",");
//            if(pKeys.length==1){ // 如果只有一个键值，则尝试判断主键是否为null,说实话，这样子的兼容性少了标记的可能性，自增键值是哪个，完全依赖于主键标记，复合主键的话，则无法判断了，但这个符合99%的可能性了
//                if(record.get(pKeys[0].trim()) == null){ // 当使用读写分离时，主键键值可能返回为 null 的时候，使用 select LAST_INSERT_ID() 获取当前插入的结果集的主键id的序列值
//                    // mysql 的情况则需要通过 select LAST_INSERT_ID(); 获取结果集 ，当mysql通过 maxscale 的时候会发生可能取到id为null的情况，所以需要重新获取主键键值，通过同一个连接
//                    // 参考文档地址：https://mariadb.com/kb/en/mariadb-maxscale-6-mariadb-maxscale-configuration-guide/
//                    if (this.getConfig().getDialect() instanceof MysqlDialect) {
//                        List<Record> idRecords = this.find(this.config,conn,"select LAST_INSERT_ID() idValue");
//                        //log.error("class : "+this.getConfig().getDialect().getClass().getName()+" table : "+tableName+" key: "+pKeys[0].trim() +" ; this idValue is "+idRecords.get(0).get("idValue") + " oValue:"+ record.get(pKeys[0].trim()));
//                        record.set(pKeys[0].trim(),idRecords.get(0).get("idValue"));
//                    }
//                }
//            }
        } catch (Exception var9) {
            throw new ActiveRecordException(var9);
        } finally {
            this.config.close(conn);
        }
        return var5;
    }


    /**
     * 保存服务
     * @param config
     * @param conn
     * @param tableName
     * @param primaryKey
     * @param record
     * @return
     * @throws SQLException
     */
    protected boolean save(Config config, Connection conn, String tableName, String primaryKey, Record record) throws SQLException {
        Long startTime = System.currentTimeMillis();
        String[] pKeys = primaryKey.split(",");
        List<Object> paras = new ArrayList();
        StringBuilder sql = new StringBuilder();
        config.getDialect().forDbSave(tableName, pKeys, record, sql, paras);
        PreparedStatement pst = config.getDialect().isOracle() ? conn.prepareStatement(sql.toString(), pKeys) : conn.prepareStatement(sql.toString(), 1);
        Throwable var10 = null;

        boolean var12;
        int result = 0;
        try {
            config.getDialect().fillStatement(pst, paras);
            result = pst.executeUpdate();
            if(primaryKey.length()>0){// 支持无主键模式
                config.getDialect().getRecordGeneratedKey(pst, record, pKeys);
            }
            var12 = result >= 1;
        } catch (Throwable var21) {
            var10 = var21;
            if(connectListener != null){
                // 执行结尾增加相应的逻辑处理
                connectListener.loss(this, RunSqlType.save,(System.currentTimeMillis()-startTime),sql.toString(),paras.toArray(),result,RunStatus.FAIL,var21);
            }
            throw var21;
        } finally {
            if (pst != null) {
                if (var10 != null) {
                    try {
                        pst.close();
                    } catch (Throwable var20) {
                        var10.addSuppressed(var20);
                    }
                } else {
                    pst.close();
                }
            }
        }
        if(connectListener != null){
            // 执行结尾增加相应的逻辑处理
            connectListener.loss(this, RunSqlType.save,(System.currentTimeMillis()-startTime),sql.toString(),paras.toArray(),result,RunStatus.SUCCESS);
        }

        return var12;
    }



    /**
     * 批量提交并返回主键
     * 目前只测试了 mysql postgresql 会返回，所以独立方法，避免影响到原 jfinal 的批量提交
     * @param mClass
     * @param saveList
     * @param batchSize
     * @param <M>
     * @return
     */
    public <M> int[] batchSaveRid(Class<M> mClass,List<M> saveList,int batchSize){
        // 获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 主键键值
        List<FieldAndColumn> priCoumns  = JpaAnnotationUtil.getIdFieldAndColumns(mClass);

        Record record = null;
        List<Record> records = null;
        //
        Method beforeSave = null;
        Map<String,Object> dataMap = null;
        // 总记录数
        int totaolCount = saveList.size();
        int t = 0;
        int ct = totaolCount/batchSize + 1;
        M m;
        // 初始化结果数组长度
        int[] resultSize = new int[totaolCount];
        int[] tmpResultSize ;
        int tmpIdx = 0;
        int rt = 0; // 主键回填时的下标记录
        fj : for(int jt=0;jt<ct;jt++) {
            // 初始化
            records = new ArrayList<>();
            for (int j = 0; j < batchSize; j++) {
                if (t >= totaolCount) {
                    if (records.size() > 0) {
                        tmpResultSize = batchSave(mClass,table.name(),records,batchSize);
                        for (int i = 0; i < tmpResultSize.length; i++) {
                            resultSize[tmpIdx] = tmpResultSize[i];
                            tmpIdx++;

                            m = saveList.get(rt);
                            record = records.get(i);
                            for(FieldAndColumn fieldAndColumn : coumns) {
                                // 字段赋值 -- 反向赋予主键的键值
                                JpaAnnotationUtil.setFieldValue(m,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
                            }
                            rt++;
                        }
                        tmpResultSize = null;
                        records = null; // 主动先清空1次内存
                    }
                    break fj;
                }
                m = saveList.get(t);
                record = new Record();
                for (FieldAndColumn fieldAndColumn : coumns) {
                    // 字段赋值 -- 字段全小写
                    record.set(fieldAndColumn.getColumn().name().toLowerCase(), JpaAnnotationUtil.getFieldValue(m, fieldAndColumn.getField()));
                }
                if (beforeSave == null) {
                    // 保存前的方法事件
                    beforeSave = JpaAnnotationUtil.getMethod(mClass, EDbSave.class);
                }

                // 保存前的监听
                if (eDbListener != null) {
                    // 忽略大小写
                    dataMap = new CaseInsensitiveMap(record.getColumns());
                    // 执行对象方法
                    eDbListener.beforeSave(mClass, dataMap, coumns);
                    // 替换数据
                    record.setColumns(dataMap);
                }

                // 保存前的监听 -- 在统一监听之后执行
                if (beforeSave != null) {
                    // 替换成 忽略 大小写的 map
                    dataMap = new CaseInsensitiveMap(record.getColumns());
                    // 执行对象方法
                    EReflectUtil.invoke(m, beforeSave, dataMap, coumns);
                    // 替换数据
                    record.setColumns(dataMap);
                }

                // 区分是否是 postgresql -- 目前注释，正常情况下不会交错id提交，交由id自增，或者自己传入id
                if (this.getConfig().getDialect() instanceof PostgreSqlDialect) {
                    // 主键
                    for (FieldAndColumn fieldAndColumn : priCoumns) {
                        // 如果是主键，主键没有键值，则删除该字段 -- 兼容 pg 模式，mysql本身是不需要剔除
                        if (record.get(fieldAndColumn.getColumn().name().toLowerCase()) == null) {
                            // ? 模式无法添加
                            record.remove(fieldAndColumn.getColumn().name().toLowerCase());
                        }
                    }
                }
                // 添加到保存记录集里
                records.add(record);
                t++; // 指标下钻
            }
            tmpResultSize = batchSave(mClass,table.name(),records,batchSize); // 保存后有返回id
            for (int i = 0; i < tmpResultSize.length; i++) {
                resultSize[tmpIdx] = tmpResultSize[i];
                tmpIdx++;

                m = saveList.get(rt);
                record = records.get(i);
                for(FieldAndColumn fieldAndColumn : coumns) {
                    // 字段赋值 -- 反向赋予主键的键值
                    JpaAnnotationUtil.setFieldValue(m,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
                }
                rt++;
            }
            tmpResultSize = null;
            records = null; // 主动先清空内存
        }

//        int[] resultSize = batchSave(mClass,table.name(),records,batchSize);
        // 获取主键字段
        //coumns = JpaAnnotationUtil.getIdFieldAndColumns(mClass);
//        m = null;
//        // 反向赋予键值
//        for(int i=0;i<saveList.size();i++){
//            m = saveList.get(i);
//            record = records.get(i);
//            for(FieldAndColumn fieldAndColumn : coumns) {
//                // 字段赋值 -- 反向赋予主键的键值
//                JpaAnnotationUtil.setFieldValue(m,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
//            }
//        }


        return resultSize;
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
    public <M> int[] batchSave(Class<M> mClass,List<M> saveList,int batchSize){
        // 获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 主键键值
        List<FieldAndColumn> priCoumns  = JpaAnnotationUtil.getIdFieldAndColumns(mClass);
        Record record = null;
        // 记录集合
        List<Record> records  = null;
        // 开始保存前
        Method beforeSave = null;
        // 对象集合
        Map<String,Object> dataMap = null;
        // 总记录数
        int totaolCount = saveList.size();
        int t = 0;
        int ct = totaolCount/batchSize + 1;
        M m;
        // 初始化结果数组长度
        int[] resultSize = new int[totaolCount];
        int[] tmpResultSize ;
        int tmpIdx = 0;
        fj : for(int jt=0;jt<ct;jt++){
            // 初始化
            records = new ArrayList<>();
            for (int j=0; j<batchSize; j++){
                if(t >= totaolCount){
                    if(records.size()>0){
                        tmpResultSize = batchSave(table.name(),records,batchSize);
                        for (int i=0;i< tmpResultSize.length ;i++){
                            resultSize[tmpIdx] = tmpResultSize[i];
                            tmpIdx++;
                        }
                        tmpResultSize = null;
                        records = null; // 主动先清空1次内存
                    }
                    break fj;
                }
                m = saveList.get(t);
                record = new Record();
                for(FieldAndColumn fieldAndColumn : coumns){
                    // 字段赋值 -- 字段全小写
                    record.set(fieldAndColumn.getColumn().name().toLowerCase(), JpaAnnotationUtil.getFieldValue(m,fieldAndColumn.getField()));
                }
                if(beforeSave == null){
                    // 保存前的方法事件
                    beforeSave = JpaAnnotationUtil.getMethod(mClass, EDbSave.class);
                }

                // 保存前的监听
                if(eDbListener!=null){
                    dataMap =  new CaseInsensitiveMap(record.getColumns());
                    // 执行对象方法
                    eDbListener.beforeSave(mClass,dataMap,coumns);
                    // 替换数据
                    record.setColumns(dataMap);
                }

                // 全局监听之后
                if(beforeSave != null){
                    // 替换成 忽略 大小写的 map
                    dataMap =  new CaseInsensitiveMap(record.getColumns());
                    // 执行对象方法
                    EReflectUtil.invoke(m, beforeSave, dataMap,coumns);
                    // 替换数据
                    record.setColumns(dataMap);
                }

                // 反向赋予键值
                for(int i=0;i<saveList.size();i++){
                    for(FieldAndColumn fieldAndColumn : coumns) {
                        // 字段赋值 -- 反向赋予主键的键值
                        JpaAnnotationUtil.setFieldValue(m,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
                    }
                }

                if(this.getConfig().getDialect() instanceof PostgreSqlDialect) {
                    // 主键 -- 要嘛有主键，要嘛全无主键
                    for (FieldAndColumn fieldAndColumn : priCoumns) {
                        // 如果是主键，主键没有键值，则删除该字段 -- 兼容 pg 模式，mysql本身是不需要剔除
                        if (record.get(fieldAndColumn.getColumn().name().toLowerCase()) == null) {
                            record.remove(fieldAndColumn.getColumn().name().toLowerCase());
                        }
                    }
                }
                // 添加到保存记录集里
                records.add(record);
                t++; // 指标下钻
            }
            // 执行1次
            tmpResultSize = batchSave(table.name(),records,batchSize);
            for (int i=0;i< tmpResultSize.length ;i++){
                resultSize[tmpIdx] = tmpResultSize[i];
                tmpIdx++;
            }
            tmpResultSize = null;
            records = null; // 主动先清空1次内存
        }

//        int[] resultSize = batchSave(table.name(),records,batchSize);

        return resultSize;
    }




    /**
     * 使用 insert values(...),(...) 的方式批量插入对象
     * @param objs
     * @param batchSize
     * @param <T>
     */
    public <T>  int  insertValues(Class<T> tClass,List<T> objs,int batchSize){
        int valueSize = 0;
        String colStr = null; // 字符串转义时复用
        // 如果对象为null不管
        if( objs == null || objs.size() == 0){
            return 0;
        }
        // 获取表注解
        Table table = EAnnotationUtil.getAnnotation(tClass, Table.class);


        // 表头部分
        StringBuffer tableHead = new StringBuffer(100);
        // 拼接sql -- 默认每1000为一批次提交
        StringBuffer inserValues = new StringBuffer(batchSize * 1024);
        // 设置表头名称
        tableHead.append(" insert into ").append(table.name()).append("(");
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(tClass);


        for(FieldAndColumn fieldAndColumn:coumns){
            // 拼接字段
            tableHead.append(fieldAndColumn.getColumn().name()).append(",");
        }
        // 删除最后一个逗号
        tableHead.deleteCharAt(tableHead.length() - 1);
        // 闭环 -- insert into table(...)
        tableHead.append(") values");
        Object value = null;
        T t = null;
        // for 循环
        for(int i=0;i<objs.size();i++){
            // 如果对象有值，并且为一批次时，则进行一次提交
            if(inserValues.length() > 0 && i % batchSize == 0){
                // 裁剪末尾逗号
                inserValues.deleteCharAt(inserValues.length() - 1);
                // 执行语句
                valueSize += this.update(tableHead.toString() + inserValues.toString());
//                System.out.println(tableHead.toString() + inserValues.toString());
                // 主动清空insert对象
                inserValues.delete( 0, inserValues.length() );
            }

            // 赋予对象
            t = objs.get(i);
            // 拼接起始位置
            inserValues.append("(");

            // 赋予数据
            for(FieldAndColumn fieldAndColumn:coumns){
                // 数据项
                value = JpaAnnotationUtil.getFieldValue(t,fieldAndColumn.getField());
                // 只允许对象只有 String Inter Long BigDecimal 这几种常用类型 (便于扩展) ,枚举类型不建议，虽然也能实现，但是存在各种不确定性，请勿玩花样，避免代码太过复杂恶心
                if(value instanceof String){
                    colStr = value.toString();
                    if(this.getConfig().getDialect() instanceof PostgreSqlDialect){
                        // 转义 \ 符号，避免插入数据时缺失字符
                        colStr = colStr.replaceAll("\\\\","\\\\\\\\");
                        // 避免有单引号存在，造成插入异常，需要对单引号全部特殊处理,替换成 '' pg写入转义符号时报错
//                        colStr = colStr.replaceAll("'","''");
                        colStr = colStr.replaceAll("'","\\\\'"); // pg or mysql 都可以使用这种方式转义
                        // 转换; Postgresql对于0x00和\u0000无法支持 -- 所以字符串这里统一进行处理
                        colStr = colStr.replace((char)0x00,' ');
                        // 避免有单引号存在，造成插入异常，需要对单引号全部特殊处理,替换成 \'
                        inserValues.append("E'").append(colStr).append("',");
                    }else if(this.getConfig().getDialect() instanceof MysqlDialect){
                        // 转义 \ 符号，避免插入数据时缺失字符
                        colStr = colStr.replaceAll("\\\\","\\\\\\\\");
                        // 避免有单引号存在，造成插入异常，需要对单引号全部特殊处理,替换成 \'
                        colStr = colStr.replaceAll("'","\\\\'");
                        inserValues.append("'").append(colStr).append("',");
                    }else{
                        inserValues.append("'").append(value).append("',");
                    }
                }else
                if(value instanceof Date){
                    inserValues.append("'").append(EDateUtil.formatDateTime((Date) value)).append("',");
                }
                else{
                    if(value == null && fieldAndColumn.getIsPriKey()) // 当主键为null时的处理方式
                    {
                        if(this.getConfig().getDialect() instanceof PostgreSqlDialect){
                            // 如果是pg的话，则需要引入自增序列的方式进行 id 自增
                            inserValues.append("nextval(pg_get_serial_sequence('").append(table.name().toLowerCase()).append("','").append(fieldAndColumn.getColumn().name().toLowerCase()).append("')),");
                        }else{
                            if(fieldAndColumn.getField().getType().equals(Date.class)){
                                inserValues.append("now").append(","); // tdengine 数据库支持的函数，可以支持到 纳秒
                            }else{
                                inserValues.append(value).append(","); // 其他类型的数据库，实际回填 null 即可
                            }
                        }
                    }else{
                        inserValues.append(value).append(","); // 其他键值
                    }

                }
            }
            // 裁剪末尾逗号
            inserValues.deleteCharAt(inserValues.length() - 1);
            // 拼接末尾
            inserValues.append("),");
        }


        // 如果存在未执行的sql，则需要执行一次
        if(inserValues.length() > 0 ){
            // 裁剪末尾逗号
            inserValues.deleteCharAt(inserValues.length() - 1);
            // 执行语句
            valueSize += this.update(tableHead.toString() + inserValues.toString());
            // 主动清空insert对象
            inserValues.delete( 0, inserValues.length() );
        }
        return valueSize;
    }

    /**
     * 返回数据库字段
     * @param m
     * @param ignoreNullValue - true -屏蔽 null , false - 包含null
     * @param <M>
     * @return
     */
    public <M> Map<String,Object> getColumnsMap(M m,boolean ignoreNullValue){
        if(m instanceof String){
            throw new RuntimeException(" 直接执行sql请使用 EDb.update(sql) ");
        }
        Class<M> mClass = getRealJpaClass(m);
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        if(table==null){
            // 以后再给成英文 -- 中文国际通用 ^_^
            throw new RuntimeException("非数据库对象无法转换");
        }
        return JpaAnnotationUtil.getJpaMap(m,!ignoreNullValue);
    }

    /**
     * 获取真实的jpa对象实例 -- 如果没有直接指定申明 jpa 对象class 类型，则必须调用该方法，保证类型的准确性
     * @param m
     * @param <M>
     * @return
     */
    public <M> Class<M> getRealJpaClass(M m){
//        Class mClass = null;
//        JpaProxy jpaProxy = JpaProxy.getCglibForJpaUpdate(m);
//        if(jpaProxy == null){
//            mClass = m.getClass();
//        }else{
//            mClass = jpaProxy.getJpaClass();
//        }
//        return mClass;
        return (Class<M>) m.getClass();
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @param <M> -- 数据集
     * @isColumnName 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @return
     */
    public <M> boolean update(Class<M> mClass,Map<String,Object> updateData,boolean isColumnName){
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值 -- 小写
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        Record record = new Record();
        // 变更对象 -- 获取到变更的数据库字段值,反向填充到map
        Map<String,Object> dataMap = new HashMap<>();
        // 字段名数据集
        Map<String,FieldAndColumn> fieldNameMap = null;
        // 如果是驼峰字段匹配，才取获取字段数据集
        if(!isColumnName){
            fieldNameMap = JpaAnnotationUtil.getCoumnsMap(mClass);
        }
        // 必须有更新条件，所以不用判断null
        if(updateData.size() > 0){
            for(Map.Entry<String, Object> entry : updateData.entrySet()){
                if(isColumnName){
                    // 由于使用工具类取出来的数据库字段命名是小写，所以统一转小写 ，这点是因为你永远无法知道用户到底是小写还是大写的命名规则决定的
                    dataMap.put(entry.getKey().toLowerCase(),entry.getValue());
                }else{
                    // 驼峰字段赋值
                    if(fieldNameMap.get(entry.getKey())!=null && fieldNameMap.get(entry.getKey()).getColumn() != null){
                        dataMap.put(fieldNameMap.get(entry.getKey()).getColumn().name().toLowerCase(),entry.getValue());
                    }
                }
            }
        }

        // 初始化对象
        record.setColumns(dataMap);
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 更新前的方法事件
        Method beforeSave = JpaAnnotationUtil.getMethod(mClass, EDbUpdate.class);
        Map<String,Object> updateDataMap = null;
        // 保存前的监听
        if(eDbListener!=null){
            updateDataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            eDbListener.beforeUpdate(mClass,updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        //
        if(beforeSave != null){
            Object ojb = null;
            try {
                ojb = mClass.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            // 替换成 忽略 大小写的 map
            updateDataMap =  new CaseInsensitiveMap(dataMap);
            // 执行对象方法
            EReflectUtil.invoke(ojb, beforeSave, updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        // 更新对象
        return this.update(table.name(),keys,record);
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @param <M>
     * @return
     */
    public <M> int updateReInt(Class<M> mClass,Map<String,Object> updateData){
        if(update(mClass,updateData)){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * 更新对象 -- 包含null值的变更情况 ,成功返回1 ，失败返回 0
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @isColumnName -- 是否是数据库字段名称,true-数据库字段名称,false-驼峰字段名称
     * @param <M>
     * @return
     */
    public <M> int updateReInt(Class<M> mClass,Map<String,Object> updateData,boolean isColumnName){
        if(update(mClass,updateData,isColumnName)){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param mClass -- 数据库表对象
     * @param updateData  -- 数据库表字段(非驼峰对象)
     * @param <M>
     * @return
     */
    public <M> boolean update(Class<M> mClass,Map<String,Object> updateData){
        return update(mClass,updateData,true);
    }

    /**
     * 根据外键进行更新
     * @param mClass -- 对象
     * @param updateData -- 更新的数据库字段
     * @param fkData -- 外键数据字段集
     * @param <M>
     * @return
     */
    public <M> boolean updateByFk(Class<M> mClass,Map<String,Object> updateData,Map<String,Object> fkData){
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值 -- 小写
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        Record record = new Record();
        // 变更对象 -- 获取到变更的数据库字段值,反向填充到map
        Map<String,Object> dataMap = new HashMap<>();
        // 必须有更新条件，所以不用判断null
        if(updateData.size() > 0){
            for(Map.Entry<String, Object> entry : updateData.entrySet()){
                // 由于使用工具类取出来的数据库字段命名是小写，所以统一转小写 ，这点是因为你永远无法知道用户到底是小写还是大写的命名规则决定的
                dataMap.put(entry.getKey().toLowerCase(),entry.getValue());
            }
        }
        // 初始化对象
        record.setColumns(dataMap);
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 更新前的方法事件
        Method beforeSave = JpaAnnotationUtil.getMethod(mClass, EDbUpdate.class);
        Map<String,Object> updateDataMap = null;
        // 保存前的监听
        if(eDbListener!=null){
            updateDataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            eDbListener.beforeUpdate(mClass,updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        //
        if(beforeSave != null){
            Object ojb = null;
            try {
                ojb = mClass.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            // 替换成 忽略 大小写的 map
            updateDataMap =  new CaseInsensitiveMap(dataMap);
            // 执行对象方法
            EReflectUtil.invoke(ojb, beforeSave, updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }
         return this.updateFk(table.name(),keys,record,fkData);
    }

    /**
     *
     * @param tableName
     * @param primaryKey
     * @param record
     * @return
     */
    public boolean updateFk(String tableName, String primaryKey, Record record,Map<String,Object> fkData) {
        Connection conn = null;

        boolean var5;
        try {
            conn = this.config.getConnection();
            var5 = this.updateFk(this.config, conn, tableName, primaryKey, record,fkData);
        } catch (Exception var9) {
            throw new ActiveRecordException(var9);
        } finally {
            this.config.close(conn);
        }

        return var5;
    }

    /**
     * 通过外键键值来更新数据
     * @param config
     * @param conn
     * @param tableName
     * @param primaryKey
     * @param record
     * @return
     * @throws SQLException
     */
    protected boolean updateFk(Config config, Connection conn, String tableName, String primaryKey, Record record,Map<String,Object> fkData) throws SQLException {
        String[] pKeys = primaryKey.split(",");

        StringBuilder sql = new StringBuilder();
        List<Object> paras = new ArrayList();
        // 支持外键查询的扩展
        forDbUpdateFk(tableName, pKeys, record, sql, paras,fkData);
        if (paras.size() <= 1) {
            return false;
        } else {
            return this.update(config, conn, sql.toString(), paras.toArray()) >= 1;
        }
    }


    public void forDbUpdateFk(String tableName, String[] pKeys,  Record record, StringBuilder sql, List<Object> paras,Map<String,Object> fkData) {
        tableName = tableName.trim();
        this.config.getDialect().trimPrimaryKeys(pKeys);
        sql.append("update ").append(tableName).append(" set ");
        Iterator var7 = record.getColumns().entrySet().iterator();

        while(var7.hasNext()) {
            Map.Entry<String, Object> e = (Map.Entry)var7.next();
            String colName = (String)e.getKey();
            if (!this.config.getDialect().isPrimaryKey(colName, pKeys)) { // 非主键键值更新防御
                if (paras.size() > 0) {
                    sql.append(", ");
                }
                sql.append(colName).append(" = ? ");
                paras.add(e.getValue());
            }
        }

        sql.append(" where 1=1 ");

//        for(int i = 0; i < pKeys.length; ++i) {
//            if (i > 0) {
//                sql.append(" and ");
//            }
//            sql.append(pKeys[i]).append(" = ?");
//            paras.add(ids[i]);
//        }


        if(fkData!=null){
            if(fkData.size() == 0){
                throw new RuntimeException(" fkData size must be greater than 0 ");
            }
            for (Map.Entry<String, Object> m : fkData.entrySet()) {
                // 前面主键一定有值
                sql.append(" and ");
                sql.append(m.getKey());
                if(m.getValue() instanceof List){
                    List<Object> fkKeys = (List<Object>) m.getValue();
                    sql.append(" in(");
                    for(int i = 0; i < fkKeys.size(); ++i){
                        sql.append("?");
                        if (i+1 < fkKeys.size()) {
                            sql.append(",");
                        }
                        paras.add(fkKeys.get(i));
                    }
                    sql.append(")");
                }else{
                    sql.append(" =?");
                    paras.add(m.getValue());
                }
            }
        }else{
            throw new RuntimeException(" fkData size must be greater than 0 ");
        }

    }

    /**
     * 更新对象 -- 包含null值的变更情况
     * @param oldM -- 原始数据对象
     * @param updateM -- 基于原始数据变更后的数据对象
     * @param <M>
     * @return
     */
    public <M> boolean updateCompare(M oldM,M updateM){
        Map<String,Object> dataMap = null;
        Class mClass = updateM.getClass();
        //
        dataMap = JpaBuilder.contrastObjReturnColumnMap(oldM,updateM);
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
//        System.out.println(updateData);
        // 必须定义record对象，否则无法更新操作
        Record record = new Record();
        // 初始化对象
        record.setColumns(dataMap);

        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);

        // 更新前的方法事件
        Method beforeSave = JpaAnnotationUtil.getMethod(mClass, EDbUpdate.class);

        // 保存前的监听
        if(eDbListener!=null){
            Map<String,Object> updateDataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            eDbListener.beforeUpdate(mClass,updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        //
        if(beforeSave != null){
            // 替换成 忽略 大小写的 map
            Map<String,Object> updateDataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            EReflectUtil.invoke(updateM, beforeSave, updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        // 反向赋予键值
        for(FieldAndColumn fieldAndColumn : coumns) {
            // 字段赋值 -- 反向赋予主键的键值
            JpaAnnotationUtil.setFieldValue(updateM,fieldAndColumn.getField(),record.getColumns().get(fieldAndColumn.getColumn().name().toLowerCase()));
        }

        // 更新对象
        return this.update(table.name(),keys,record);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param m
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public <M> int updateReInt(M m,boolean containsNullValue){
        if(update(m,containsNullValue)){
            return 1;
        }else{
            return 0;
        }
    }



    /**
     * 更新对象 -- 剔除null值
     * @param m
     * @param containsNullValue false-剔除null值，true-保留null值更新
     * @param <M>
     * @return
     */
    public <M> boolean update(M m,boolean containsNullValue){
//
        Class mClass = m.getClass();
        Map<String,Object> dataMap = null;
        // 数据对象
        if(dataMap == null || dataMap.size() ==0 ){
            // 剔除null值
            dataMap = JpaAnnotationUtil.getJpaMap(m,containsNullValue);
        }
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
//        System.out.println(updateData);
        // 必须定义record对象，否则无法更新操作
        Record record = new Record();
        // 初始化对象
        record.setColumns(dataMap);
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 更新前的方法事件
        Method beforeSave = JpaAnnotationUtil.getMethod(mClass, EDbUpdate.class);
        // 保存前的监听
        if(eDbListener!=null){
            Map<String,Object> updateDataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            eDbListener.beforeUpdate(mClass,updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        //
        if(beforeSave != null){
            // 替换成 忽略 大小写的 map
            Map<String,Object> updateDataMap =  new CaseInsensitiveMap(record.getColumns());
            // 执行对象方法
            EReflectUtil.invoke(m, beforeSave, updateDataMap,coumns);
            // 替换数据
            record.setColumns(updateDataMap);
        }

        // 反向赋予键值
        for(FieldAndColumn fieldAndColumn : coumns) {
            // 字段赋值 -- 反向赋予主键的键值
            JpaAnnotationUtil.setFieldValue(m,fieldAndColumn.getField(),record.getColumns().get(fieldAndColumn.getColumn().name().toLowerCase()));
        }

        // 更新对象
        return this.update(table.name(),keys,record);
    }

    /**
     * 更新对象 -- 剔除null值
     * @param m
     * @param <M>
     * @return
     */
    public <M> boolean update(M m){
        // 不允许更新null值的对象
        return update(m,false);
    }

    /**
     * 更新对象 -- 剔除null值，成功返回1，失败返回0
     * @param m
     * @param <M>
     * @return
     */
    public <M> int updateReInt(M m){
        if(update(m)){
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * 批量更新 -- 必须保证每条记录更新的字段数一样多，并且是同样的字段，否则会引发异常
     * @param mClass
     * @param updateList
     * @param batchSize
     * @param <M>
     * @return
     */
    public  <M> int[] batchUpdate(Class<M> mClass,List<M> updateList, int batchSize) {
        return batchUpdate(mClass,updateList,batchSize,false);
    }


    /**
     * 批量更新 -- 推荐使用该方式
     * @param mClass -- 实体对象,包含 @Table @Column 的实体对象
     * @param updateList -- 需要更新的实体对象集合
     * @param updateFields -- 指定要更新的字段，必须要有 @column 对应的对象字段名，即 entity.fieldName，否则无法正常匹配
     * @param batchSize -- 每批次批量更新提交的数量
     * @param <M>
     * @return
     */
    public  <M> int[] batchUpdate(Class<M> mClass,List<M> updateList,List<String> updateFields, int batchSize) {
        if(updateList == null){
            return null;
        }
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
//        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        List<FieldAndColumn> getIdFieldAndColumns = JpaAnnotationUtil.getIdFieldAndColumns(mClass); // 返回主键字段集合
        String keys = JpaAnnotationUtil.getPriKeysByFieldAndColumn(getIdFieldAndColumns); // 返回字段解析结果
        // 匹配主键键值
        for(FieldAndColumn keyField:getIdFieldAndColumns){
            if(!updateFields.contains(keyField.getField().getName())){
                updateFields.add(keyField.getField().getName()); // 不存在主键键值则添加，避免无法匹配
            }
        }

        //JpaProxy jpaProxy = null;
        Record record = null;
        List<Record> records = new ArrayList<>();
        // 变更对象 -- 获取到变更的数据库字段值,反向填充到map
        Map<String,Object> dataMap = null;

        Map<String,Object> updateDataMap = null;
        Method beforeUpdate = null;
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 拼凑 mysql or pg(pg模式下，建表建议全部小写，否则不适用该语法) 通用的 update sql 语句，多语句用 分隔符号 间隔
        for(M obj : updateList){
            record = new Record();
            dataMap = new HashMap<>();
            // 必须有更新条件，所以不用判断null
            dataMap = JpaAnnotationUtil.getJpaMap(obj,updateFields); // 只获取指定的驼峰字段
            // 必须指定2个字段以上才允许更新，否则一点意义都没有，所以直接抛错，避免无效更新！！！
            if(dataMap.size() < 2 ){
                throw new RuntimeException("updateFields size must be than 2,nowSize=> "+ dataMap.size() +" , pkId or other bean fieldName,nowIs ==> " + dataMap);
            }
            // 设置到对象集
            record.setColumns(dataMap);
            if(beforeUpdate == null){
                // 更新前的方法事件
                beforeUpdate = JpaAnnotationUtil.getMethod(mClass, EDbUpdate.class);
            }

            // 保存前的监听
            if(eDbListener!=null){
                updateDataMap =  new CaseInsensitiveMap(record.getColumns());
                // 执行对象方法
                eDbListener.beforeUpdate(mClass , updateDataMap , coumns);
                // 替换数据
                record.setColumns(updateDataMap);
            }

            //
            if(beforeUpdate != null){
                // 替换成 忽略 大小写的 map
                updateDataMap =  new CaseInsensitiveMap(record.getColumns());
                // 执行对象方法
                EReflectUtil.invoke(obj, beforeUpdate, updateDataMap,coumns);
                // 替换数据
                record.setColumns(updateDataMap);
            }

            // 反向赋予键值
            for(FieldAndColumn fieldAndColumn : coumns) {
                // 字段赋值 -- 反向赋予主键的键值
                JpaAnnotationUtil.setFieldValue(obj,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
            }

            records.add(record);
        }


        return  this.batchUpdate(table.name(),keys,records,batchSize);
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
    public  <M> int[] batchUpdate(Class<M> mClass,List<M> updateList, int batchSize,boolean containsNullValue) {
        if(updateList == null){
            return null;
        }
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        //JpaProxy jpaProxy = null;
        Record record = null;
        List<Record> records = new ArrayList<>();
        // 变更对象 -- 获取到变更的数据库字段值,反向填充到map
        Map<String,Object> dataMap = null;
        // 获取一条记录验证是否有值
        dataMap = JpaAnnotationUtil.getJpaMap(updateList.get(0),containsNullValue);
        // 默认 keys 一定有值，所以就不判断了!
        for (String pKey:keys.split(",")){
            if(dataMap.get(pKey)==null){
                throw new RuntimeException(" priKey is null,please set priKey:"+pKey);
            }
        }

        Map<String,Object> updateDataMap = null;
        Method beforeUpdate = null;
        // 获取所有字段列表
        List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
        // 拼凑 mysql or pg(pg模式下，建表建议全部小写，否则不适用该语法) 通用的 update sql 语句，多语句用 分隔符号 间隔
        for(M obj : updateList){
            record = new Record();
            dataMap = new HashMap<>();
            // 必须有更新条件，所以不用判断null
            dataMap = JpaAnnotationUtil.getJpaMap(obj,containsNullValue);
            // 设置到对象集
            record.setColumns(dataMap);
            if(beforeUpdate == null){
                // 更新前的方法事件
                beforeUpdate = JpaAnnotationUtil.getMethod(mClass, EDbUpdate.class);
            }

            // 保存前的监听
            if(eDbListener!=null){
                updateDataMap =  new CaseInsensitiveMap(record.getColumns());
                // 执行对象方法
                eDbListener.beforeUpdate(mClass , updateDataMap , coumns);
                // 替换数据
                record.setColumns(updateDataMap);
            }

            //
            if(beforeUpdate != null){
                // 替换成 忽略 大小写的 map
                updateDataMap =  new CaseInsensitiveMap(record.getColumns());
                // 执行对象方法
                EReflectUtil.invoke(obj, beforeUpdate, updateDataMap,coumns);
                // 替换数据
                record.setColumns(updateDataMap);
            }

            // 反向赋予键值
            for(FieldAndColumn fieldAndColumn : coumns) {
                // 字段赋值 -- 反向赋予主键的键值
                JpaAnnotationUtil.setFieldValue(obj,fieldAndColumn.getField(),record.get(fieldAndColumn.getColumn().name().toLowerCase()));
            }

            records.add(record);
        }


        return  this.batchUpdate(table.name(),keys,records,batchSize);
    }

    /**
     * 传入一个id组，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param ids
     * @param <T>
     * @return
     */
    public <T> boolean deleteByGroupIds(Class<T> mClass,Object... ids) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        // 监听操作
        if(eDbListener != null){
            // 定义删除的对象集 -- 主要是 id 的集合
            CaseInsensitiveMap<String,Object> deleteMap =  new CaseInsensitiveMap();
            List<Map<String,Object>> deleteMaps = new ArrayList<>();
            String[] keyArray =  keys.split(",");
            for(int i=0;i<keyArray.length;i++){
                deleteMap.put(keyArray[i],ids[i]);
            }
            deleteMaps.add(deleteMap);
            // 获取所有字段列表
            List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
            EDBListenerResult filterResult = eDbListener.beforeDelete(this,mClass,deleteMaps,coumns);
            // 如果返回的结果不为null 并且是 false
            if(filterResult!=null && !filterResult.isNextToDo()){
                return filterResult.isReturnResult();
            }
        }
        return this.deleteByIds(table.name(), keys, ids);
    }

    /**
     * 传入一个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param id
     * @param <T>
     * @return
     */
    public <T> boolean deleteById(Class<T> mClass,Object id) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        // 监听操作
        if(eDbListener != null){
            // 获取所有字段列表
            CaseInsensitiveMap<String,Object> deleteMap =  new CaseInsensitiveMap();
            List<Map<String,Object>> deleteMaps = new ArrayList<>();
            deleteMap.put(keys,id);
            deleteMaps.add(deleteMap);
            // 获取所有字段列表
            List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
            EDBListenerResult filterResult = eDbListener.beforeDelete(this,mClass,deleteMaps,coumns);
            // 如果返回的结果不为null 并且是 false
            if(filterResult!=null && !filterResult.isNextToDo()){
                return filterResult.isReturnResult();
            }
        }
        return this.deleteById(table.name(), keys, id);
    }

    /**
     * 根据主键字段删除数据
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean delete(T t)
    {
        return deleteById(t);
    }


    /**
     * 根据主键字段删除数据
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean deleteById(T t) {
        // 获取真实的jpa实例对象类型，避免是获取到代理类，导致异常错误
        Class<T> tClass = getRealJpaClass(t);
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(tClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(tClass);
        // 监听操作
        if(eDbListener != null){
            // 定义map集合
            CaseInsensitiveMap<String,Object> deleteMap =  new CaseInsensitiveMap();
            List<Map<String,Object>> deleteMaps = new ArrayList<>();
            List<Object> results = JpaAnnotationUtil.getPriKeyValues(t);
            if(results.size()==1){
                deleteMap.put(keys,JpaAnnotationUtil.getPriKeyValues(t).get(0));
            }else{
                // 如果是符合主键的话
                String[] keyArray =  keys.split(",");
                for(int i=0;i<results.size();i++){
                    deleteMap.put(keyArray[i],JpaAnnotationUtil.getPriKeyValues(t).get(i));
                }
            }

            deleteMaps.add(deleteMap);
            // 获取所有字段列表
            List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(tClass);
            EDBListenerResult filterResult = eDbListener.beforeDelete(this,tClass,deleteMaps,coumns);
            // 如果返回的结果不为null 并且是 false
            if(filterResult!=null && !filterResult.isNextToDo()){
                return filterResult.isReturnResult();
            }
        }
        return this.deleteByIds(table.name(), keys, JpaAnnotationUtil.getPriKeyValues(t).toArray());
    }

    /**
     * 传入多个携带 id 的 jpa对象，以此删除该对象，没有id则职id为null，不做删除数据处理
     * @param jpaList
     * @param <T>
     * @return
     */
    public <T> int deleteByIds(List<T> jpaList) {
        // 获取真实的jpa实例对象类型，避免是获取到代理类，导致异常错误
        Class<T> tClass = getRealJpaClass(jpaList.get(0));
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(tClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(tClass);
        if(keys.split(",").length > 1){
            throw new RuntimeException(" 只支持单主键的多id传值 ");
        }
        List<Object> idList = new ArrayList<>();
        //
        List<FieldAndColumn> idColumnList = JpaAnnotationUtil.getIdFieldAndColumns(jpaList.get(0).getClass());

        for(T t:jpaList){
            // 赋予id
            idList.add(JpaAnnotationUtil.getFieldValue(t,idColumnList.get(0).getField()));
        }
        return deleteByIds(tClass,idList);
    }

    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param deleteIds
     * @param <T>
     * @return
     */
    public <T> int deleteByIds(Class<T> mClass,List<Object> deleteIds) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        if(keys.split(",").length > 1){
            throw new RuntimeException(" 只支持单主键的多id传值 ");
        }
        //
        if(deleteIds.size() == 0){
            throw new RuntimeException(" 需要指定删除的对象键值，否则不予执行，避免不小心全表删除 ");
        }
        // 监听操作
        if(eDbListener != null){
            // 定义删除的对象集 -- 主要是 id 的集合
            CaseInsensitiveMap<String,Object> deleteMap =  new CaseInsensitiveMap();
            List<Map<String,Object>> deleteMaps = new ArrayList<>();
            //
            for(Object id : deleteIds.toArray()){
                deleteMap =  new CaseInsensitiveMap();
                deleteMap.put(keys,id);
                deleteMaps.add(deleteMap);
            }
            // 获取所有字段列表
            List<FieldAndColumn> coumns  = JpaAnnotationUtil.getCoumns(mClass);
            EDBListenerResult filterResult = eDbListener.beforeDelete(this,mClass,deleteMaps,coumns);
            // 如果返回的结果不为null 并且是 false
            if(filterResult!=null && !filterResult.isNextToDo()){
                // 如果是批量执行的id
                return filterResult.getReturnCt();
            }
        }

        // 删除sql
        StringBuffer deleteSql = new StringBuffer(" delete from ");
        // 表名称
        deleteSql.append( table.name() );
        deleteSql.append(" where 1=1 ");
        deleteSql.append(" and  ").append(keys).append(" in(");
        // 循环拼接字段
        for(int i=0 ; i<deleteIds.size() ; i++){
            deleteSql.append("?,");
        }
        deleteSql.deleteCharAt(deleteSql.length()-1);
        deleteSql.append(")");
        return this.update(deleteSql.toString(),deleteIds.toArray());
    }


    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param deleteIds -- 以 , 为分隔符的id串
     * @param <T>
     * @return
     */
    public <T> int deleteByIds(Class<T> mClass,String deleteIds) {
        // 根据分割符 , 进行数据清理
        return deleteByIds(mClass, deleteIds,",");
    }


    /**
     * 传入多个id，删除指定对象，但是只支持 唯一键值 只有一个的数据结构
     * @param mClass
     * @param deleteIds
     * @param splitStr
     * @param <T>
     * @return
     */
    public <T> int deleteByIds(Class<T> mClass,String deleteIds,String splitStr) {
        String[] ids = deleteIds.split(splitStr);
        return deleteByIds(mClass, Arrays.asList(ids));
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
    public <M> M findByGroupId(Class<M> mClass,String tableName, String primaryKey, Object... idValues) {
        String[] pKeys = primaryKey.split(",");
        if (pKeys.length != idValues.length) {
            throw new IllegalArgumentException("primary key number must equals id value number");
        } else {
            String sql = this.config.getDialect().forDbFindById(tableName, pKeys);
            List<M> result = this.find(mClass,sql, idValues);
            return result.size() > 0 ? (M) result.get(0) : null;
        }
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
        String[] pKeys = primaryKey.split(",");
        if (pKeys.length != 1) {
            throw new IllegalArgumentException("primary key number must equals id value number");
        } else {
            String sql = this.config.getDialect().forDbFindById(tableName, pKeys);
            List<M> result = this.find(mClass,sql, idValue);
            return result.size() > 0 ? (M) result.get(0) : null;
        }
    }

    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param mClass
     * @param ids
     * @param <M>
     * @return
     */
    public <M> List<M> findByIds(Class<M> mClass, List<Object> ids) {
        // 返回表对象 -- 便于获取表名称
        Table table = JpaAnnotationUtil.getTableAnnotation(mClass);
        // 获取主键键值
        String keys = JpaAnnotationUtil.getPriKeys(mClass);
        if(keys.split(",").length > 1){
            throw new RuntimeException(" 只支持单主键的多id传值 ");
        }
        // 基于表结构不区分大小写的写法 如果是 pa 只支持全小写的写法
        StringBuilder sql = (new StringBuilder("select * from ")).append(table.name()).append(" where ").append(keys);
        sql.append(" in(");
        //
        for(int i = 0; i < ids.size(); ++i) {
            sql.append("?,");
        }
        // 去除最后1位的逗号
        sql.deleteCharAt(sql.length()-1);
        sql.append(") ");

        List<Object> newIds =  new ArrayList<>();
        // pg不支持将数字转成字符串查询
        try {
            // 如果是string字符串，则需要转换判断
            if(ids.get(0) instanceof String){
                // 如果是数字的话，则全部转换成数字重新组装成数组
                if( ENumberUtil.isNumber(String.valueOf(ids.get(0)))){
                    for(Object obj:ids){
                        newIds.add(Long.valueOf(String.valueOf(obj)));
                    }
                }
            }else{
                newIds = ids;
            }
        }catch (Throwable e){
            newIds = ids;
        }
        // 执行查询结果
        List<M> result = this.find(mClass,sql.toString(), newIds.toArray());
        return result;
    }


    /**
     * 根据单主键对象传入 ids 串 ，返回数据对象
     * @param mClass
     * @param idsStr
     * @param splitStr
     * @param <M>
     * @return
     */
    public <M> List<M> findByIds(Class<M> mClass, String idsStr,String splitStr) {
        String[] ids = idsStr.split(splitStr);
       return findByIds(mClass,Arrays.asList(ids));
    }

    /**
     * 扩展方法体
     * @param sql
     * @param paras
     * @return
     */
    public List<Record> find(String sql, Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map) {
            SqlPara sqlPara = getSqlParaByString(sql, (Map) paras[0]); // 参数转换
            return this.find(sqlPara);// 重新封装
        }else{
            return super.find(sql,paras);
        }
    }

    /**
     * 通过sql语句返回对象实体
     * @param mClass
     * @param finalSql
     * @param <M>
     * @return
     */
    public <M> List<M> find(Class<M> mClass,String finalSql) {
        // 返回查询结果
        return find(mClass,finalSql,null);
    }

    /**
     * 通过对象和sqlpara对象返回查询结果
     * @param mClass
     * @param sqlPara
     * @param <M>
     * @return
     */
    public <M> List<M> find(Class<M> mClass,SqlPara sqlPara) {
        // 返回查询结果
        return find(mClass,sqlPara.getSql(),sqlPara.getPara());
    }

    /**
     * 传入对象和sql，返回查询结果
     * @param mClass
     * @param sql
     * @param paras
     * @param <M>
     * @return
     */
    public <M> List<M> find(Class<M> mClass,String sql, Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map) {
            SqlPara sqlPara = getSqlParaByString(sql, (Map) paras[0]);
            return this.find(mClass,sqlPara);// 重新封装
        }
        Connection conn = null;
        List var4;
        try {
            conn = this.config.getConnection();
            var4 = this.find(mClass,this.config, conn, sql, paras);
        } catch (Exception var8) {
            throw new ActiveRecordException(var8);
        } finally {
            this.config.close(conn);
        }
        return var4;
    }

    /**
     * 设置 PreparedStatement 的 RowMaxs 属性
     * @param pst
     */
    public void setRowMaxs(PreparedStatement pst){
        try { // 由于postgresql 无法通过jdbcUrl 设置 maxRows 保护系统运行时返回的系统内存，然后使用
            if(this.config.getDialect() instanceof EDbPostgreSqlDialect){
                EDbPostgreSqlDialect eDbPostgreSqlDialect = ((EDbPostgreSqlDialect) this.config.getDialect());
                if(eDbPostgreSqlDialect.maxRows!=null){
                    pst.setMaxRows(eDbPostgreSqlDialect.maxRows);
                }
            }
        } catch (Throwable throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * 执行查询
     * @param config
     * @param conn
     * @param sql
     * @param paras
     * @param <T>
     * @return
     * @throws SQLException
     */
    protected <T> List<T> query(Config config, Connection conn, String sql, Object... paras) throws SQLException {
        List result = new ArrayList();
        Long startTime =  System.currentTimeMillis();
        PreparedStatement pst = conn.prepareStatement(sql);
        setRowMaxs(pst); // 设置返回条数的最大值，可以保护系统运行时避免内存溢出
        Throwable var7 = null;
        List var21;
        try {
            config.getDialect().fillStatement(pst, paras);
            ResultSet rs = pst.executeQuery();
            int colAmount = rs.getMetaData().getColumnCount();
            if (colAmount <= 1) {
                if (colAmount == 1) {
                    while(rs.next()) {
                        result.add(rs.getObject(1));
                    }
                }
            } else {
                while(rs.next()) {
                    Object[] temp = new Object[colAmount];

                    for(int i = 0; i < colAmount; ++i) {
                        temp[i] = rs.getObject(i + 1);
                    }

                    result.add(temp);
                }
            }

            this.close(rs);
            var21 = result;
        } catch (Throwable var19) {
            var7 = var19;
            if(connectListener != null){
                // 执行结尾增加相应的逻辑处理
                connectListener.loss(this, RunSqlType.select,(System.currentTimeMillis()-startTime),sql,paras,0,RunStatus.FAIL,var19);
            }
            throw var19;
        } finally {
            if (pst != null) {
                if (var7 != null) {
                    try {
                        pst.close();
                    } catch (Throwable var18) {
                        var7.addSuppressed(var18);
                    }
                } else {
                    pst.close();
                }
            }

        }
        if(connectListener != null){
            // 执行结尾增加相应的逻辑处理
            connectListener.loss(this, RunSqlType.select,(System.currentTimeMillis()-startTime),sql,paras,result.size(),RunStatus.SUCCESS);
        }
        return var21;
    }

    /**
     * 原jfinal查询，需要改写才能支持一些查询服务的处理
     * @param config
     * @param conn
     * @param sql
     * @param paras
     * @return
     * @throws SQLException
     */
    protected List<Record> find(Config config, Connection conn, String sql, Object... paras) throws SQLException {
        Long startTime =  System.currentTimeMillis();
        PreparedStatement pst = conn.prepareStatement(sql);
        setRowMaxs(pst); // 设置返回条数的最大值，可以保护系统运行时避免内存溢出
        Throwable var6 = null;
        List var9;
        try {
            config.getDialect().fillStatement(pst, paras);
            ResultSet rs = pst.executeQuery();
            List<Record> result = config.getDialect().buildRecordList(config, rs);
            this.close(rs);
            var9 = result;
        } catch (Throwable var18) {
            var6 = var18;
            if(connectListener != null){
                // 执行结尾增加相应的逻辑处理
                connectListener.loss(this, RunSqlType.select,(System.currentTimeMillis()-startTime),sql,paras,0, RunStatus.FAIL,var18);
            }
            throw var18;
        } finally {
            if (pst != null) {
                if (var6 != null) {
                    try {
                        pst.close();
                    } catch (Throwable var17) {
                        var6.addSuppressed(var17);
                    }
                } else {
                    pst.close();
                }
            }

        }

        if(connectListener != null){
            int rowSize=0;
            if(var9!=null){
                rowSize = var9.size();
            }
            // 执行结尾增加相应的逻辑处理
            connectListener.loss(this, RunSqlType.select,(System.currentTimeMillis()-startTime),sql,paras,rowSize,RunStatus.SUCCESS);
        }

        return var9;
    }

    /**
     * 根据对象、数据库连接、查询语句、参数，返回对象列表
     * @param mClass
     * @param config
     * @param conn
     * @param sql
     * @param paras
     * @param <M>
     * @return
     * @throws SQLException
     */
    protected <M> List<M> find(Class<M> mClass,Config config, Connection conn, String sql, Object... paras) throws SQLException {
        // 调整成可调节游标的方式，不然pg会报错，目前只测试了 pg 和 mysql 的游标模式 ; 但是如果是大数据量读取，mysql 应该是使用 TYPE_FORWARD_ONLY 模式
        PreparedStatement pst = null;
        // 注释掉pg的游标模式，正常来说是不需要打开游标的，所以从 JpaBuilder 取数上直接保存最后结果集的统计信息，有需要的地方，直接获取对应的结果集统计结果就行
//        if(this.getConfig().getDialect() instanceof PostgreSqlDialect){
//            // ResultSet.CONCUR_READ_ONLY 不能用结果集更新数据库中的表  --> 默认使用该模式，便于查阅和修改需要提交的数据，再通过反向更新操作提交记录集
//            // ResultSet.CONCUR_UPDATETABLE 能用结果集更新数据库中的表  --> 配合 rs.update 更新记录集的变动
//            // ResultSet.TYPE_FORWARD_ONLY -> 不开启游标，直接往后查询，不能往前设置游标，即 结果集的游标只能向下滚动。
//            // ResultSet.TYPE_SCROLL_INSENSITIVE -> 结果集的游标可以上下移动，当数据库变化时，当前结果集不变 --> 修改后的数据可能无法显示，对数据修改的信息变化不敏感，默认获取缓存中的数据直接展示，
//            // ResultSet.TYPE_SCROLL_SENSITIVE -> 返回可滚动的结果集，当数据库变化时，当前结果集同步改变
//            pst = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY); // 允许pg模式下重置游标，否则无法进行size数计算
//        }else{
//            // https://blog.csdn.net/axman/article/details/3984103 -> 需要参考和做一些测试，避免默认模式和想要实现的结果集查询不一致
//            pst = conn.prepareStatement(sql); // 避免mysql查询数据，查询出不想查询的数据,导致更新异常
//        }
        // 统一打开游标，便于快速返回计算结果
//        pst = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        pst = conn.prepareStatement(sql); // 默认不打开游标，以便提升本身的性能，还有节约内存开销

        Throwable var6 = null;

        List var9;
        try {
            // 有入参才进行调整
            if(paras != null){
                config.getDialect().fillStatement(pst, paras);
            }
            ResultSet rs = pst.executeQuery();
            // 构建 bean 对象
            List<M> result = JpaBuilder.buildBean(mClass, rs,config.isInTransaction());
            // 关闭 ResultSet 对象
            this.close(rs);
            var9 = result;
        } catch (Throwable var18) {
            var6 = var18;
            throw new RuntimeException(var18);
        } finally {
            if (pst != null) {
                if (var6 != null) {
                    try {
                        pst.close();
                    } catch (Throwable var17) {
                        var6.addSuppressed(var17);
                    }
                } else {
                    pst.close();
                }
            }

        }
        return var9;
    }

    /**
     * 关闭 ResultSet 对象
     * @param rs
     * @throws SQLException
     */
    final void close(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }

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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String findSql) {
        String[] sqls = PageSqlKit.parsePageSql(findSql);
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null, sqls[0], sqls[1]);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param findSql
     * @param <M>
     * @return
     */
    public <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, String findSql) {
        String[] sqls = PageSqlKit.parsePageSql(findSql);
        return this.doPaginate(mClass,pageRequest, (Boolean)null, sqls[0], sqls[1]);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String findSql,Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map) {
            SqlPara sqlPara = this.getSqlParaByString(findSql, (Map) paras[0]);
            return this.paginate(mClass,pageNumber,pageSize,sqlPara);
        }

        String[] sqls = PageSqlKit.parsePageSql(findSql);
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null, sqls[0], sqls[1], paras);
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
    public <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, String findSql,Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map) {
            SqlPara sqlPara = this.getSqlParaByString(findSql, (Map) paras[0]);
            return this.paginate(mClass,pageRequest,sqlPara);
        }
        String[] sqls = PageSqlKit.parsePageSql(findSql);
        return this.doPaginate(mClass,pageRequest, (Boolean)null, sqls[0], sqls[1], paras);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, String findSql) {
        //
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null,totalRow, new StringBuilder(findSql),new Object[0]);
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
    public <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, String findSql) {
        return this.doPaginate(mClass,pageRequest, (Boolean)null,totalRow, new StringBuilder(findSql),new Object[0]);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, String findSql,Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map) {
            SqlPara sqlPara = this.getSqlParaByString(findSql, (Map) paras[0]);
            return this.paginate(mClass,pageNumber,pageSize,totalRow,sqlPara);
        }
        //
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null,totalRow, new StringBuilder(findSql),paras);
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
    public <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, String findSql,Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map) {
            SqlPara sqlPara = this.getSqlParaByString(findSql, (Map) paras[0]);
            return this.paginate(mClass,pageRequest,totalRow,sqlPara);
        }
        return this.doPaginate(mClass,pageRequest, (Boolean)null,totalRow, new StringBuilder(findSql),paras);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, SqlPara sqlPara) {
        //
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null,totalRow,new StringBuilder(sqlPara.getSql()), sqlPara.getPara());
    }

    /**
     * 分页查询
     * @param pageNumber -- 当前页
     * @param pageSize -- 分页数量
     * @param totalRow -- 记录总数
     * @param sqlPara -- sql对象
     * @return
     */
    public Page<Record> paginate(int pageNumber, int pageSize,long totalRow, SqlPara sqlPara) {
        //
        return this.doPaginate(pageNumber, pageSize, (Boolean)null,totalRow,new StringBuilder(sqlPara.getSql()), sqlPara.getPara());
    }

    /**
     * 分页查询
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRow
     * @param findSql
     * @param paras
     * @return
     */
    protected Page<Record> doPaginate(int pageNumber, int pageSize, Boolean isGroupBySql,long totalRow, StringBuilder findSql, Object... paras) {
        Connection conn = null;
        Page var10;
        try {
            conn = this.config.getConnection();
            var10 = this.doPaginateByFullSql(this.config, conn, pageNumber, pageSize, isGroupBySql, totalRow, findSql, paras);
        } catch (Exception var14) {
            throw new ActiveRecordException(var14);
        } finally {
            this.config.close(conn);
        }
        return var10;
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
    public <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest,long totalRow, SqlPara sqlPara) {
        //
        return this.doPaginate(mClass,pageRequest, (Boolean)null,totalRow,new StringBuilder(sqlPara.getSql()), sqlPara.getPara());
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, SqlPara sqlPara) {
        String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null, sqls[0], sqls[1], sqlPara.getPara());
    }



    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param sqlPara
     * @param <M>
     * @return
     */
    public <M> Page<M> paginate(Class<M> mClass,PageRequest pageRequest, SqlPara sqlPara) {
        String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
        return this.doPaginate(mClass,pageRequest, (Boolean)null, sqls[0], sqls[1], sqlPara.getPara());
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql, SqlPara sqlPara) {
        String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
        return this.doPaginate(mClass,pageNumber, pageSize, isGroupBySql, sqls[0], sqls[1], sqlPara.getPara());
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String select, String sqlExceptSelect, Object... paras) {
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null, select, sqlExceptSelect, paras);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, String select, String sqlExceptSelect) {
        return this.doPaginate(mClass,pageNumber, pageSize, (Boolean)null, select, sqlExceptSelect, new Object[0]);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        return this.doPaginate(mClass,pageNumber, pageSize, isGroupBySql, select, sqlExceptSelect, paras);
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
    protected <M> Page<M> doPaginate(Class<M> mClass,int pageNumber, int pageSize, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        Connection conn = null;
        Page var10;
        try {
            conn = this.config.getConnection();
//            String totalRowSql = "select count(1) " + this.config.getDialect().replaceOrderBy(sqlExceptSelect);
//            String totalRowSql = "select count(1) from ( select 1 " + this.config.getDialect().replaceOrderBy(sqlExceptSelect) +") as countTb "; // 再套1层，避免优化掉order时内部包含 group ，导致统计错误
            String totalRowSql = this.config.getDialect().forPaginateTotalRow(select, sqlExceptSelect, (Object)null);
            StringBuilder findSql = new StringBuilder();
            findSql.append(select).append(' ').append(sqlExceptSelect);
            var10 = this.doPaginateByFullSql(mClass,this.config, conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
        } catch (Exception var14) {
            throw new ActiveRecordException(var14);
        } finally {
            this.config.close(conn);
        }

        return var10;
    }


    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param isGroupBySql
     * @param select
     * @param sqlExceptSelect
     * @param paras
     * @param <M>
     * @return
     */
    protected <M> Page<M> doPaginate(Class<M> mClass,PageRequest pageRequest, Boolean isGroupBySql, String select, String sqlExceptSelect, Object... paras) {
        // 默认值设定
        if(pageRequest == null){
            // 默认只返回最多10条数据
            pageRequest = PageRequest.of(1,10);
        }
        return doPaginate(mClass,pageRequest.getPageNumber(),pageRequest.getPageSize(),isGroupBySql,select,sqlExceptSelect,paras);
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
    protected <M> Page<M> doPaginate(Class<M> mClass,int pageNumber, int pageSize, Boolean isGroupBySql,long totalRow, StringBuilder findSql, Object... paras) {
        Connection conn = null;
        Page var10;
        try {
            conn = this.config.getConnection();
            var10 = this.doPaginateByFullSql(mClass,this.config, conn, pageNumber, pageSize, isGroupBySql, totalRow, findSql, paras);
        } catch (Exception var14) {
            throw new ActiveRecordException(var14);
        } finally {
            this.config.close(conn);
        }
        return var10;
    }

    /**
     * 直接赋予记录总数的分页查询方法
     * @param mClass
     * @param pageRequest
     * @param isGroupBySql
     * @param totalRow
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     */
    protected <M> Page<M> doPaginate(Class<M> mClass,PageRequest pageRequest, Boolean isGroupBySql,long totalRow, StringBuilder findSql, Object... paras) {
        // 默认值设定
        if(pageRequest == null){
            // 默认只返回最多10条数据
            pageRequest = PageRequest.of(1,10);
        }
        return doPaginate(mClass,pageRequest.getPageNumber(),pageRequest.getPageSize(),isGroupBySql,totalRow,findSql,paras);
    }

    /**
     * 主要是根据 已传入的总记录数 实现分页逻辑
     * -- 就不需要单独在查询计算总记录数(因为数据库可能有大量数据，每次统计查询太耗时，可以默认给一个大小查询翻页即可，节省计算时间
     * -- 用户也不需要每次查询非常靠前的数据，有需要可以到数据仓库里查询)
     * @param mClass
     * @param config
     * @param conn
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRow
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     * @throws SQLException
     */
    protected <M> Page<M> doPaginateByFullSql(Class<M> mClass,Config config, Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, long totalRow, StringBuilder findSql, Object... paras) throws SQLException {
        if (pageNumber >= 1 && pageSize >= 1) {
            int size = 0;
            if (isGroupBySql == null) {
                isGroupBySql = size > 1;
            }
            // 如果数据量为 0
            if (totalRow == 0L) {
                return new Page(new ArrayList<M>(0), pageNumber, pageSize, 0, 0);
            } else {
                // 计算分页逻辑
                // 总页数
                int totalPage = (int)(totalRow / (long)pageSize);
                // 是否+1
                if (totalRow % (long)pageSize != 0L) {
                    ++totalPage;
                }
                // 如果当前页数大于总页数
                if (pageNumber > totalPage) {
                    // 直接归0
                    return new Page(new ArrayList<M>(0), pageNumber, pageSize, totalPage, (int)totalRow);
                } else {
                    // 返回查询的sql语句
                    String sql = config.getDialect().forPaginate(pageNumber, pageSize, findSql);
                    // 返回对象列表
                    List<M> list = this.find(mClass,config, conn, sql, paras);
                    return new Page(list, pageNumber, pageSize, totalPage, (int)totalRow);
                }
            }
        } else {
            throw new ActiveRecordException("pageNumber and pageSize must more than 0");
        }
    }

    /**
     * 全sql语句的分页查询
     * @param config
     * @param conn
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRow -- 替代原来的分页统计语句
     * @param findSql
     * @param paras
     * @return
     * @throws SQLException
     */
    protected Page<Record> doPaginateByFullSql(Config config, Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, long totalRow, StringBuilder findSql, Object... paras) throws SQLException {
        if (pageNumber >= 1 && pageSize >= 1) {
            int size = 0;
            if (isGroupBySql == null) {
                isGroupBySql = size > 1;
            }
            // 如果数据量为 0
            if (totalRow == 0L) {
                return new Page(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
            } else {
                // 计算分页逻辑
                // 总页数
                int totalPage = (int)(totalRow / (long)pageSize);
                // 是否+1
                if (totalRow % (long)pageSize != 0L) {
                    ++totalPage;
                }
                // 如果当前页数大于总页数
                if (pageNumber > totalPage) {
                    // 直接归0
                    return new Page(new ArrayList<Record>(0), pageNumber, pageSize, totalPage, (int)totalRow);
                } else {
                    // 返回查询的sql语句
                    String sql = config.getDialect().forPaginate(pageNumber, pageSize, findSql);
                    // 返回对象列表
                    List<Record> list = this.find(config, conn, sql, paras);
                    return new Page(list, pageNumber, pageSize, totalPage, (int)totalRow);
                }
            }
        } else {
            throw new ActiveRecordException("pageNumber and pageSize must more than 0");
        }
    }


    /**
     * 实现对象的分页逻辑
     * @param mClass
     * @param config
     * @param conn
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRowSql
     * @param findSql
     * @param paras
     * @param <M>
     * @return
     * @throws SQLException
     */
    protected <M> Page<M> doPaginateByFullSql(Class<M> mClass,Config config, Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, StringBuilder findSql, Object... paras) throws SQLException {

        if (pageNumber >= 1 && pageSize >= 1) {
            // 截获 ? 的参数列表
            List<String> totalParsList =  EReUtil.findAll("\\?",totalRowSql, 0, new ArrayList<String>());
            // 计算paraSize 总数 (统计语句里的实际 ? 个数)
            int parasSize = totalParsList.size();
            // 重新生成统计语句入参个数
            Object[] totalParas =  null;
            if(paras.length > 0){
                totalParas = EArrayUtil.sub(paras,paras.length - parasSize,paras.length);
            }else{
                totalParas = new Object[0];
            }
            //
            List pageResult = query(config, conn, totalRowSql, totalParas);
            // 释放内存
            totalParas = null;
            totalParsList = null;
            // 记录数
            int size = pageResult.size(); // 如果是执行 group 的语句则会返回多条统计结果
            // 是否是统计性的语句(如果语句后面出现group , count 时会出现多条)
            if (isGroupBySql == null) {
                isGroupBySql = size > 1;
            }
            // 总记录数
            long totalRow;
            if (isGroupBySql) {
                totalRow = (long)size;
            } else {
                totalRow = size > 0 ? ((Number)pageResult.get(0)).longValue() : 0L;
            }
            // 根据传入的总记录数实现固定分页逻辑
            return doPaginateByFullSql(mClass,config,conn,pageNumber,pageSize,isGroupBySql,totalRow,findSql,paras);
        } else {
            throw new ActiveRecordException("pageNumber and pageSize must more than 0");
        }
    }


    /**
     * 继承改写sql -- 修正了头部可以携带 ？ 参数
     * @param config
     * @param conn
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param totalRowSql
     * @param findSql
     * @param paras
     * @return
     * @throws SQLException
     */
    @Override
    protected Page<Record> doPaginateByFullSql(Config config, Connection conn, int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, StringBuilder findSql, Object... paras) throws SQLException {
        if (pageNumber < 1 || pageSize < 1) {
            throw new ActiveRecordException("pageNumber and pageSize must more than 0");
        }
        if (super.config.getDialect().isTakeOverDbPaginate()) {
            return super.config.getDialect().takeOverDbPaginate(conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
        }
        // 截获 sql语句里 ？ 参有几个 -- 剔除 '?' 直接赋值的场景
        List<String> totalParsList =  EReUtil.findAll("\\?",totalRowSql, 0, new ArrayList<String>());
        // 计算paraSize 总数 (统计语句里的实际 ? 个数)
        int parasSize = totalParsList.size();
        // 改写统计与语句里的入参个数
        Object[] totalParas =  null;
        //
        if(paras.length > 0){
            totalParas = EArrayUtil.sub(paras,paras.length - parasSize,paras.length);
        }else{
            totalParas = new Object[0];
        }
        //
        List result = query(config, conn, totalRowSql, totalParas);
        // 释放对象内存
        totalParas = null;
        totalParsList = null;
        int size = result.size();
        if (isGroupBySql == null) {
            isGroupBySql = size > 1;
        }

        long totalRow;
        if (isGroupBySql) {
            totalRow = size;
        } else {
            totalRow = (size > 0) ? ((Number)result.get(0)).longValue() : 0;
        }
        if (totalRow == 0) {
            return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);
        }

        int totalPage = (int) (totalRow / pageSize);
        if (totalRow % pageSize != 0) {
            totalPage++;
        }

        if (pageNumber > totalPage) {
            return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, totalPage, (int)totalRow);
        }

        // --------
        String sql = super.config.getDialect().forPaginate(pageNumber, pageSize, findSql);
        List<Record> list = find(config, conn, sql, paras);
        return new Page<Record>(list, pageNumber, pageSize, totalPage, (int)totalRow);
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
    public <M> Page<M> paginateByFullSql(Class<M> mClass,int pageNumber, int pageSize, String totalRowSql, String findSql, Object... paras) {
        return this.doPaginateByFullSql(mClass,pageNumber, pageSize, (Boolean)null, totalRowSql, findSql, paras);
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
    public <M> Page<M> paginateByFullSql(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
        return this.doPaginateByFullSql(mClass,pageNumber, pageSize, isGroupBySql, totalRowSql, findSql, paras);
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
    protected <M> Page<M> doPaginateByFullSql(Class<M> mClass,int pageNumber, int pageSize, Boolean isGroupBySql, String totalRowSql, String findSql, Object... paras) {
        Connection conn = null;

        Page var9;
        try {
            conn = this.config.getConnection();
            StringBuilder findSqlBuf = (new StringBuilder()).append(findSql);
            var9 = this.doPaginateByFullSql(mClass,this.config, conn, pageNumber, pageSize, isGroupBySql, totalRowSql, findSqlBuf,0, paras);
        } catch (Exception var13) {
            throw new ActiveRecordException(var13);
        } finally {
            this.config.close(conn);
        }
        return var9;
    }

    /**
     * 获取第一条记录 (改写原 Db.getFirst sql)
     * @param sql
     * @return
     */
    public Record findFirst(String sql) {
        // 由于sql语句，人为的可能会导致拉取大批量的数据，全表的话，会引起不可挽回的内存消耗，所以基于mysql or pg or oracle ，末尾添加 limit 2
        sql = getFirstSql(sql);
        return this.findFirst(sql, new Object[0]);
    }

    /**
     * 改写sql为 limit 1 sql
     * @param sql
     * @return
     */
    public String getFirstSql(String sql){
        // 获取返回limit 1 sql的语句
        return EDbSelectUtil.returnLimitSql(sql,1);
    }

    /**
     * 获取1条记录
     * @param tClass
     * @param sql
     * @param paras
     * @return
     */
    public <T> T findFirst(Class<T> tClass,String sql, Object... paras) {
        if(paras!=null && paras.length == 1 && paras[0] instanceof Map){
            SqlPara sqlPara = getSqlParaByString(sql,(Map) paras[0]);
            return findFirst(tClass,sqlPara);
        }else{
            // 改写sql语句
            sql = getFirstSql(sql);
            // 获取记录集
            List<T> result = find(tClass,sql,paras);
            return result.size() > 0 ? result.get(0) : null;
        }
    }

    /**
     * 获取1条记录
     * @param tClass
     * @param sql
     * @param <T>
     * @return
     */
    public <T> T findFirst(Class<T> tClass,String sql) {
        // 改写sql语句
        sql = getFirstSql(sql);
        // 获取记录集
        List<T> result = find(tClass,sql,new Object[0]);
        return result.size() > 0 ? result.get(0) : null;
    }


    /**
     * 获取1条记录
     * @param sqlPara
     * @return
     */
    public Record findFirst(SqlPara sqlPara) {
        String sql = getFirstSql(sqlPara.getSql());
        return this.findFirst(sql, sqlPara.getPara());
    }

    /**
     * 获取1条记录
     * @param tClass
     * @param sqlPara
     * @param <T>
     * @return
     */
    public <T> T findFirst(Class<T> tClass,SqlPara sqlPara) {
        // 改写sql语句
        String sql = getFirstSql(sqlPara.getSql());
        // 获取记录集
        List<T> result = find(tClass,sql,sqlPara.getPara());
        return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * 获取首条记录
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public <T> T findFirst(Class<T> tClass,EDbQuery eDbQuery) {
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(tClass,eDbQuery);
        return findFirst(tClass,sqlPara);
    }


    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param sqlPara
     * @param <T>
     * @return
     */
    public <T> T findOnlyOne(Class<T> tClass,SqlPara sqlPara) {
        // 改写sql语句 -- 最多返回2条，避免太多
        String newSql = EDbSelectUtil.returnLimitSql(sqlPara.getSql(),2);
        // 获取记录集
        List<T> result = find(tClass,newSql,sqlPara.getPara());
        // 逻辑异常抛出
        if(result.size() > 1){
            // 抛出用户自己编写的语句，避免被改写的语句干扰判断
            throw new RuntimeException("执行中断，记录集超过1条，执行的语句是: "+ sqlPara.getSql() + " 入参：" + EJSONUtil.toJsonStr(sqlPara.getPara()));
        }
        return result.size() > 0 ? result.get(0) : null;
    }


    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public <T> T findOnlyOne(Class<T> tClass, EDbQuery eDbQuery) {
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(tClass,eDbQuery);
        return findOnlyOne(tClass,sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param sql
     * @param <T>
     * @return
     */
    public <T> T findOnlyOne(Class<T> tClass,String sql) {
        // 改写sql语句 -- 最多返回2条
        String newSql = EDbSelectUtil.returnLimitSql(sql,2);
        // 获取记录集
        List<T> result = find(tClass,newSql,new Object[0]);
        // 逻辑异常抛出
        if(result.size() > 1){
            // 抛出用户自己编写的语句，避免被改写的语句干扰判断
            throw new RuntimeException("执行中断，记录集超过1条，执行的语句是: "+ sql);
        }
        return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sqlPara
     * @return
     */
    public Record findOnlyOne(SqlPara sqlPara)
    {
        // 改写sql语句 -- 最多返回2条，避免太多
        String newSql = EDbSelectUtil.returnLimitSql(sqlPara.getSql(),2);
        // 获取记录集
        List<Record> result = find(newSql,sqlPara.getPara());
        // 逻辑异常抛出
        if(result.size() > 1){
            // 抛出用户自己编写的语句，避免被改写的语句干扰判断
            throw new RuntimeException("执行中断，记录集超过1条，执行的语句是: "+ sqlPara.getSql() + " 入参：" + EJSONUtil.toJsonStr(sqlPara.getPara()));
        }
        return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param sql
     * @return
     */
    public Record findOnlyOne(String sql) {
        // 改写sql语句 -- 最多返回2条
        String newSql = EDbSelectUtil.returnLimitSql(sql,2);
        // 获取记录集
        List<Record> result = find(newSql,new Object[0]);
        // 逻辑异常抛出
        if(result.size() > 1){
            // 抛出用户自己编写的语句，避免被改写的语句干扰判断
            throw new RuntimeException("执行中断，记录集超过1条，执行的语句是: "+ sql);
        }
        return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * 根据 EDbQuery 返回查询结果
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public <T> List<T> find(Class<T> tClass,EDbQuery eDbQuery){
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(tClass,eDbQuery);
        return find(tClass,sqlPara);
    }

    /**
     * 根据 EDbQuery 返回统计结果
     * @param tClass
     * @param eDbQuery
     * @param <T>
     * @return
     */
    public <T> long count(Class<T> tClass,EDbQuery eDbQuery){
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(tClass,eDbQuery);
        return sqlForCount(sqlPara);
    }


    /**
     * 根据 EDbquery 返回查询结果，并设置返回的最大条数
     * @param tClass
     * @param eDbQuery
     * @param limit
     * @param <T>
     * @return
     */
    public <T> List<T> find(Class<T> tClass,EDbQuery eDbQuery,int limit){
        return find(tClass,eDbQuery,limit,null);
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
    public <T> List<T> find(Class<T> tClass,EDbQuery eDbQuery,int limit,Integer offset){
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(tClass,eDbQuery);
        if(offset == null){
            sqlPara.setSql(sqlPara.getSql() + " limit " + limit);
        }else{
            // todo:需要根据不同数据库做不同的sql拼接，不然分页会有bug(暂时懒得写，后续改进)
            sqlPara.setSql(sqlPara.getSql() + " limit " + limit + " offset " + offset);
        }

        return find(tClass,sqlPara);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, EDbQuery eDbQuery) {
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(mClass,eDbQuery);
        return paginate(mClass,pageNumber,pageSize,sqlPara);
    }

    /**
     * 分页查询
     * @param mClass
     * @param pageRequest
     * @param eDbQuery
     * @param <M>
     * @return
     */
    public <M> Page<M> paginate(Class<M> mClass, PageRequest pageRequest, EDbQuery eDbQuery) {
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(mClass,eDbQuery);
        return paginate(mClass,pageRequest,sqlPara);
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
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize,long totalRow, EDbQuery eDbQuery) {
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(mClass,eDbQuery);
        return paginate(mClass,pageNumber,pageSize,totalRow,sqlPara);
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
    public <M> Page<M> paginate(Class<M> mClass, PageRequest pageRequest,long totalRow, EDbQuery eDbQuery) {
        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(mClass,eDbQuery);
        return paginate(mClass,pageRequest,totalRow,sqlPara);
    }


    /**
     * Batch save records using the "insert into ..." sql generated by the first record in recordList.
     * Ensure all the record can use the same sql as the first record.
     * @param tableName the table name
     */
    public <M> int[] batchSave(Class<M> mClass,String tableName, List<? extends Record> recordList, int batchSize) {
        if (recordList == null || recordList.size() == 0)
            return new int[0];

        Record record = recordList.get(0);
        Map<String, Object> cols = record.getColumns();
        int index = 0;
        StringBuilder columns = new StringBuilder();
        // the same as the iterator in Dialect.forDbSave() to ensure the order of the columns
        Object value = null;
        for (Map.Entry<String, Object> e : cols.entrySet()) {
            if (config.getDialect().isOracle()) {	// 支持 oracle 自增主键
                value = e.getValue();
                if (value instanceof String && ((String)value).endsWith(".nextval")) {
                    continue ;
                }
            }

            if (index++ > 0) {
                columns.append(',');
            }
            columns.append(e.getKey());
        }

        String[] pKeysNoUse = new String[0];
        StringBuilder sql = new StringBuilder();
        List<Object> parasNoUse = new ArrayList<Object>();
        config.getDialect().forDbSave(tableName, pKeysNoUse, record, sql, parasNoUse);
        return batch(mClass,sql.toString(), columns.toString(), recordList, batchSize);
    }

    public <M> int[] batch(Class<M> mClass,String sql, String columns, List modelOrRecordList, int batchSize) {
        Connection conn = null;
        Boolean autoCommit = null;

        int[] var7;
        try {
            conn = this.config.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            var7 = this.batch(mClass,this.config, conn, sql, columns, modelOrRecordList, batchSize);
        } catch (Exception var16) {
            throw new ActiveRecordException(var16);
        } finally {
            if (autoCommit != null) {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (Exception var15) {
                    LogKit.error(var15.getMessage(), var15);
                }
            }

            this.config.close(conn);
        }

        return var7;
    }


    protected <M> int[] batch(Class<M> mClass,Config config, Connection conn, String sql, String columns, List list, int batchSize) throws SQLException {
        // 获取jpa的主键字段名 -- 必须只有一个自增主键，否则不回填
        List<FieldAndColumn> fieldAndColumns = JpaAnnotationUtil.getIdFieldAndColumns(mClass);
        FieldAndColumn  priKey = null ;
        // 只有单主键时，才赋予该键,默认为自增主键
        if(fieldAndColumns.size() == 1){
            priKey = JpaAnnotationUtil.getIdFieldAndColumns(mClass).get(0);
            // todo 判断字段属性是 数字类型 则赋予，否则应该重置为null
        }
        //
        ResultSet rs = null;
        if (list != null && list.size() != 0) {
            Object element = list.get(0);
            if (!(element instanceof Record) && !(element instanceof Model)) {
                throw new IllegalArgumentException("The element in list must be Model or Record.");
            } else if (batchSize < 1) {
                throw new IllegalArgumentException("The batchSize must more than 0.");
            } else {
                // 不需要model判断
                // boolean isModel = element instanceof Model;
                String[] columnArray = columns.split(",");

                for(int i = 0; i < columnArray.length; ++i) {
                    columnArray[i] = columnArray[i].trim();
                }
                // 是否是在事务中
                boolean isInTransaction =  config.isInTransaction();
                int counter = 0;
                int pointer = 0;
                int size = list.size();
                int[] result = new int[size];
                // 主键键值
                long[] keys = new long[list.size()];
                int keyIdx = 0;
                // 改造成可以根据自增主键键值返回的 id 串,但是数据表需要设置 AUTO_INCREMENT 自增属性
                PreparedStatement pst = null;
                //
                if(priKey != null){
                    pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                }else{
                    pst = conn.prepareStatement(sql);
                }

                Throwable var16 = null;
                Map map = null;
                Object value = null;
                int[] r = null;
                try {
                    for(int i = 0; i < size; ++i) {
                        // 去掉判断 isModel ? ((Model)list.get(i))._getAttrs() :
                        map = ((Record)list.get(i)).getColumns();
                        for(int j = 0; j < columnArray.length; ++j) {
                            value = map.get(columnArray[j]);
                            if (value instanceof Date) {
                                if (value instanceof java.sql.Date) {
                                    pst.setDate(j + 1, (java.sql.Date)value);
                                } else if (value instanceof Timestamp) {
                                    pst.setTimestamp(j + 1, (Timestamp)value);
                                } else {
                                    Date d = (Date)value;
                                    pst.setTimestamp(j + 1, new Timestamp(d.getTime()));
                                }
                            } else {
                                pst.setObject(j + 1, value);
                            }
                        }

                        pst.addBatch();
                        ++counter;
                        if (counter >= batchSize) {
                            counter = 0;
                            r = pst.executeBatch();
                            if (!isInTransaction) {
                                conn.commit();
                            }

                            for(int k = 0; k < r.length; ++k) {
                                result[pointer++] = r[k];
                            }
                            // 判断jpa主键有才执行
                            if(priKey != null){
                                //每批量提交一次,取得自动生成的主键值的结果集
                                rs = pst.getGeneratedKeys() ;
                                // 按提交的顺序回填
                                while(rs.next()){
                                    keys[keyIdx++] = rs.getLong(1);
                                }
                            }
                        }

                    } // 跳出for循环


                    if (counter != 0) {
                        r = pst.executeBatch();
                        if (!isInTransaction) {
                            conn.commit();
                        }

                        for(int k = 0; k < r.length; ++k) {
                            result[pointer++] = r[k];
                        }

                        // 如果传入的对象不为null
                        if(priKey != null){
                            //每批量提交一次,取得自动生成的主键值的结果集
                            rs = pst.getGeneratedKeys() ;
                            // 按提交的顺序回填
                            while(rs.next()){
                                keys[keyIdx++] = rs.getLong(1);
                            }
                        }

                    }

                    r = result;

                    if(priKey != null){
                        for(int i = 0; i < size; ++i) {
                            // map 里的数据库字段，全小写
                            map = ((Record)list.get(i)).getColumns();
                            // 赋予主键键值
                            map.put( priKey.getColumn().name().toLowerCase() ,keys[i]);
                        }
                    }

                    return r;
                } catch (Throwable var29) {
                    var16 = var29;
                    throw var29;
                } finally {
                    if (pst != null) {
                        if (var16 != null) {
                            try {
                                pst.close();
                            } catch (Throwable var28) {
                                var16.addSuppressed(var28);
                            }
                        } else {
                            pst.close();
                        }
                    }

                    if(rs!=null){
                        rs.close();
                    }


                }
            }
        } else {
            return new int[0];
        }
    }



    /**
     * 创建当前对象的关系代理对象
     * @param t
     * @param <T>
     * @return
     */
    public <T> T rel(T t){
        return rel(t,null,1,10);
    }

    /**
     * 获取关系对象，并可控制对象的其实和结束节点，以便控制返回更多的结果
     * @param t
     * @param pageNo
     * @param pageSize
     * @param <T> -- 这类方法一般返回的是list对象，不排除多个里取一个结果集
     * @return
     */
    public <T> T rel(T t,Integer pageNo,Integer pageSize){
        return rel(t,null,pageNo,pageSize);
    }



    /**
     * 限定字段并返回
     * @param t
     * @param fields -- 可传入自定义的sql字段，并可使用 case when 等语法替代字段的模式
     * @param pageNo
     * @param pageSize
     * @param <T>
     * @return
     */
    public <T> T rel(T t,String fields,Integer pageNo,Integer pageSize){
        if(t == null){
            throw new RuntimeException("传入的对象为NULL，无法关联数据，请做判断再做调用");
        }
        // 已被代理则返回自身，不做二次代理
        if (t.getClass().getSimpleName().indexOf("$$Enhancer") > 0){
            // 存在奇葩的代理情况，则暂不做考虑，因为这种情况会比较少，否则直接抛错
            return t;
        }
        EDbRelProxy eDbRelProxy = new EDbRelProxy();
        eDbRelProxy.setFields(fields);
        eDbRelProxy.setPageNo(pageNo);
        eDbRelProxy.setPageSize(pageSize);
        return eDbRelProxy.createProcy(t,this);
    }


    /**
     * 通过relKey直接返回对象
     * @param t
     * @param relKey
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Object getRelKey(Object t,String relKey,Integer pageNo,Integer pageSize){
        return  JpaRelUtil.getRelObject(relKey,null,pageNo,pageSize,this,t,null,null,true,false);
    }

    /**
     * 通过relKey直接返回 object 对象
     * @param t
     * @param relKey
     * @return
     */
    public Object getRelKey(Object t,String relKey){
        return  JpaRelUtil.getRelObject(relKey,null,null,null,this,t,null,null,true,false);
    }


    /**
     * 通过relKey直接返回对象异步列表
     * @param t
     * @param relKey
     * @return
     */
    public List<Future<Object>> getRelKeyForFutrue(Object t,String relKey){
        return getRelKeyForFutrue(t,relKey,null,null,null);
    }



    /**
     * 异步获取对象
     * @param t
     * @param relKey
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<Future<Object>> getRelKeyForFutrue(Object t,String relKey,Integer pageNo,Integer pageSize){
        return getRelKeyForFutrue(t,relKey,null,pageNo,pageSize);
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
    public List<Future<Object>> getRelKeyForFutrue(Object t,String relKey,String fields,Integer pageNo,Integer pageSize){
        return (List<Future<Object>>) JpaRelUtil.getRelObject(relKey,fields,pageNo,pageSize,this,t,null,null,true,true);
    }


    /**
     * 返回数据对象本身
     * @param t
     * @param <T>
     * @returnt
     */
    public <T> T getAllRel(T t){
        // 由于该方法映射的时候，会有一个问题点，就是返回的对象可能是List类型，导致循环匹配时，类型无法正确转换
        JpaRelUtil.getRelObject(null,null,null,null,this,t,null,null,true,false);
        return t;
    }

    /**
     * 获取所有数据对象，以异步回调的方式获取，能大量缩短等待时间
     * @param object
     */
    public List<Future<Object>> getAllRelForFutrue(Object object){
        return (List<Future<Object>>) JpaRelUtil.getRelObject(null,null,null,null,this,object,null,null,true,true);
    }

    /**
     * 获取视图对象
     * @param t
     * @param <T>
     * @return
     */
    public <T> T view(T t){
        if(t == null){
            throw new RuntimeException("传入的对象为NULL，无法关联数据，请做判断再做调用");
        }
        // 已被代理则返回自身，不做二次代理
        if (t.getClass().getSimpleName().indexOf("$$Enhancer") > 0){
            // 存在奇葩的代理情况，则暂不做考虑，因为这种情况会比较少，否则直接抛错
            return t;
        }
        // 生成视图对象
        EDbViewProxy eDbViewProxy = new EDbViewProxy();
        return eDbViewProxy.createProcy(t,this);
    }

    /**
     * 获取翻页视图对象
     * @param t
     * @param pageNo
     * @param pageSize
     * @param <T>
     * @return
     */
    public <T> T view(T t,int pageNo,int pageSize){
        if(t == null){
            throw new RuntimeException("传入的对象为NULL，无法关联数据，请做判断再做调用");
        }
        // 已被代理则返回自身，不做二次代理
        if (t.getClass().getSimpleName().indexOf("$$Enhancer") > 0){
            // 存在奇葩的代理情况，则暂不做考虑，因为这种情况会比较少，否则直接抛错
            return t;
        }
        // 生成视图对象
        EDbViewProxy eDbViewProxy = new EDbViewProxy();
        // 设置翻页参数
        eDbViewProxy.pageOf(pageNo,pageSize);
        return eDbViewProxy.createProcy(t,this);
    }

    /**
     * 获取翻页视图对象
     * @param t
     * @param pageNo
     * @param pageSize
     * @param totalRow 总记录数，无需单独执行统计sql
     * @param <T>
     * @return
     */
    public <T> T view(T t,int pageNo,int pageSize,long totalRow){
        if(t == null){
            throw new RuntimeException("传入的对象为NULL，无法关联数据，请做判断再做调用");
        }
        // 已被代理则返回自身，不做二次代理
        if (t.getClass().getSimpleName().indexOf("$$Enhancer") > 0){
            // 存在奇葩的代理情况，则暂不做考虑，因为这种情况会比较少，否则直接抛错
            return t;
        }
        // 生成视图对象
        EDbViewProxy eDbViewProxy = new EDbViewProxy();
        // 设置翻页参数
        eDbViewProxy.pageOf(pageNo,pageSize,totalRow);
        return eDbViewProxy.createProcy(t,this);
    }


    /**
     * 返回视图的总记录数
     * @param key
     * @param data
     * @return
     */
    public Long templateForCount(String key,Map data){
        //
        SqlPara sqlPara = null;
        if(data != null){
            sqlPara = this.template(key,data).getSqlPara();
        }else{
            sqlPara = this.template(key).getSqlPara();
        }
        // 定义获取的sql
        String totalRowSql = getCountSql(sqlPara.getSql());
        // 定义返回列表
        List<Record> result = null;
        if(data != null){
            // 回填入参信息
            result = this.find(totalRowSql,sqlPara.getPara());
        }else{
            result = this.find(totalRowSql);
        }
        // 返回结果对象
        if(result!=null && result.size()>0){
           return result.get(0).getLong("ct");
        }
        return 0L;
    }

    /**
     * 返回sql对应的总记录数
     * @param sqlPara
     * @return
     */
    public Long sqlForCount(SqlPara sqlPara){
        //
//        String[] sqls = PageSqlKit.parsePageSql(sqlPara.getSql());
        //
        String totalRowSql = getCountSql(sqlPara.getSql());
        List<Record> result = this.find(totalRowSql,sqlPara.getPara());
        if(result!=null && result.size()>0){
            return result.get(0).getLong("ct");
        }
        return 0L;
    }

    /**
     * 返回统计结果
     * @param sqlPara
     * @return
     */
    public long count(SqlPara sqlPara){
        return sqlForCount(sqlPara);
    }

    /**
     * 返回统计结果
     * @param sql
     * @return
     */
    public long count(String sql){
        return sqlForCount(sql);
    }


    /**
     * 返回sql对应的总记录数
     * @param sql
     * @return
     */
    public Long sqlForCount(String sql){
        //
        String totalRowSql = getCountSql(sql);
        List<Record> result = this.find(totalRowSql);
        if(result!=null && result.size()>0){
            return result.get(0).getLong("ct");
        }
        return 0L;
    }

    /**
     * 返回统计sql
     * @param sql
     * @return
     */
    public String getCountSql(String sql){
        String[] sqls = PageSqlKit.parsePageSql(sql);
        String totalRowSql = this.config.getDialect().forPaginateTotalRow(sqls[0], sqls[1], (Object)null);
        sqls = null;
        return totalRowSql;
    }


    public EDbTemplate template(String key, Map data) {
        return new EDbTemplate(this, key, data);
    }

    public EDbTemplate template(String key, Object... paras) {
        return new EDbTemplate(this, key, paras);
    }

    public EDbTemplate templateByString(String content, Map data) {
        return new EDbTemplate(true, this, content, data);
    }

    public EDbTemplate templateByString(String content, Object... paras) {
        return new EDbTemplate(true, this, content, paras);
    }

}
