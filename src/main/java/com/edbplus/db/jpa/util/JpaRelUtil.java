package com.edbplus.db.jpa.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ReflectUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.annotation.EDbRel;
import com.edbplus.db.dto.FieldAndColValue;
import com.edbplus.db.dto.FieldAndRel;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.jpa.pip.JpaRelPip;
import com.edbplus.db.proxy.EDbRelProxy;
import com.edbplus.db.jpa.task.JpaRelTask;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.SqlPara;

import javax.persistence.Table;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @ClassName JpaRelUtil
 * @Description: Jpa更新对象
 * @Author 杨志佳
 * @Date 2020/11/14
 * @Version V1.0
 **/
public class JpaRelUtil {

    // 正则匹配 #( ... )
    public static String jpaRelReg = "#\\(([^#\\(]*)\\)";


    /**
     * 获取关系对象返回的结果 或 返回的 sqlPara 对象 (这个设计起始挺不好的，会存在两种可能性的返回值)
     * @param relKey -- 如果存在多个同一对象，用key做区分，否则无法正确赋予对象
     * @param fields -- 字段
     * @param limit -- 最多返回多少条记录
     * @param offset -- 从第几条记录开始返回，起始值为0
     * @param eDbPro -- 数据源
     * @param oriJpa -- 原始jpa对象
     * @param typeName -- class 的全名称 ，便于加载生成 class 对象 或 做类比较
     * @param isReturnResutl true - 返回数据对象结果，false 返回 Map<String, List<SqlPara>>
     * @param isFutrue 是否异步回调, isReturnResutl = true 时有效， 并且返回的结果集变更为 List<Future<Object>>
     * @return
     */
    public static Object getRelObject(String relKey,
                               String fields,
                               Integer limit,
                               Integer offset,
                               EDbPro eDbPro,
                               Object oriJpa,
                               String typeName,
                               Method method,
                               boolean isReturnResutl,
                               boolean isFutrue){
        // 获取所有字段 -- 包含
        List<FieldAndRel> fieldRels = JpaAnnotationUtil.getFieldRels(oriJpa.getClass());

        // 字段的所有查询值
        List<FieldAndColValue> fieldAndColValues =  null;
        //
        EDbRel eDbRel = null;
        Type fieldType = null;
        Class<?> entityClass = null;
        Map<String,Object> tableData = null;
        //
        SqlPara sqlPara = null;
        //
        Map<String, List<SqlPara>> sqlParasMap = null;
        if(!isReturnResutl){
            sqlParasMap = new HashMap<>();
        }

        //
        Object object = null;

        JpaRelTask jpaRelTask = null;
        Future<Object> futureRate = null;
        // 异步结果集
        List<Future<Object>> resultFutrues = null;
        if(isFutrue){
            resultFutrues = new ArrayList<>();
        }


//        System.out.println("返回的方法类型：" + returnType.getTypeName());
        // 有关联的rel注解才进行扩展
        if(fieldRels != null){

            for(FieldAndRel fieldAndRel :fieldRels){
                // -- 判断指向的唯一标识是否有值
                if(!StrKit.isBlank(relKey) ){
                    // 如果 relKey 有传递值，则必须匹配上对应的 relKey 字段才进行数据返回操作，否则跳过
                    if (!relKey.equals(fieldAndRel.getEDbRel().relKey())){
                        continue;
                    }
                }
                // 字段类型
                fieldType = fieldAndRel.getField().getGenericType();
                // 返回对象 和 rel 注解匹配，则返回对应的结果集 或者是有指向唯一标识 relKey
                if(relKey!=null || typeName == null || fieldType.getTypeName().equals(typeName)){

                    if(StrKit.isBlank(relKey) ){
                        //
//                        if(fieldAndRel.getEDbRel().relKey().length() > 0 && typeName!=null){
//                            throw new RuntimeException(" 请使用 edbPro.getRelKey 返回指定对象的结果 或 edbPro.getAllRel() 返回结果集 ");
//                        }
                        if(typeName != null){
                            // 如果方面名和字段名提供的方法名不一致，则不是所命中对象的get方法
                            if(method!=null && !method.getName().equals(JpaAnnotationUtil.getFieldReadMethod(fieldAndRel.getField(),oriJpa.getClass()).getName())){
                                continue;
                            }
                        }

                    }

                    // 有
                    if(fieldAndColValues == null){
                        fieldAndColValues =  JpaAnnotationUtil.getCoumnValues(oriJpa);
                    }
                    // 某个关联字段
                    eDbRel = fieldAndRel.getEDbRel();
                    // 表结构
                    tableData = new HashMap<>();
                    // 判断是否是 ParameterizedType 对象
                    if(fieldType instanceof ParameterizedType){
                        // List
                        entityClass = (Class<?>)((ParameterizedType) fieldType).getActualTypeArguments()[0];
                        // 按默认配置来取条数
                        sqlPara = getSqlPara(fields,limit,offset,eDbPro,fieldAndColValues,entityClass,eDbRel,tableData);
                        // 是否返回结果集
                        if(isReturnResutl){
                            if(isFutrue){
                                jpaRelTask = new JpaRelTask(oriJpa,fieldAndRel.getField(),entityClass,sqlPara,eDbPro,true);
                                //  执行
                                futureRate = EDb.edbFutruePools.get(eDbPro.getConfig().getName()).submit(jpaRelTask);
                                resultFutrues.add(futureRate);
                            }else{
                                object = eDbPro.find(entityClass,sqlPara);
                                // 字段赋值 -- 反射赋值会比较消耗毫秒数
                                ReflectUtil.setFieldValue(oriJpa,fieldAndRel.getField(),object);


                            }

                            if( relKey !=null || typeName != null){
                                // 如果是有异步指定，则必须获得指定结果后再退出循环
                                if(relKey !=null && isFutrue && resultFutrues.size()>0){
                                    return resultFutrues;
                                }else{
                                    return object;
                                }
                            }


                        }else{
                            // 不返回结果集
                            if( sqlParasMap.get(fieldType.getTypeName()) == null){
                                sqlParasMap.put(fieldType.getTypeName(),new ArrayList<SqlPara>());
                            }
                            sqlParasMap.get(fieldType.getTypeName()).add(sqlPara);
                        }
                    }else{
                        // 单体对象
                        try {
                            entityClass = Class.forName(fieldType.getTypeName());
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        // 返回sql对象 -- 参数单独返回，最后统一拼凑
                        sqlPara = getSqlPara(fields,1,0,eDbPro,fieldAndColValues,entityClass,eDbRel,tableData);
                        if(isReturnResutl) {
                            if(isFutrue){
                                jpaRelTask = new JpaRelTask(oriJpa,fieldAndRel.getField(),entityClass,sqlPara,eDbPro,false);
                                //  执行
                                futureRate = EDb.edbFutruePools.get(eDbPro.getConfig().getName()).submit(jpaRelTask);
                                resultFutrues.add(futureRate);
                            }else{
                                object = eDbPro.findFirst(entityClass, sqlPara);
                                // 字段赋值 -- 反射赋值会比较消耗毫秒数
                                ReflectUtil.setFieldValue(oriJpa, fieldAndRel.getField(), object);
                            }
                            if( relKey!=null || typeName != null){
                                // 如果是有异步指定，则必须获得指定结果后再退出循环
                                if(relKey !=null && isFutrue && resultFutrues.size()>0){
                                    return resultFutrues;
                                }else{
                                    return object;
                                }
                            }
                        }else{
                            if( sqlParasMap.get(fieldType.getTypeName()) == null){
                                sqlParasMap.put(fieldType.getTypeName(),new ArrayList<SqlPara>());
                            }
                            sqlParasMap.get(fieldType.getTypeName()).add(sqlPara);
                        }
                    }
                }
            }
        }

        //
        if(sqlParasMap!=null && sqlParasMap.size() > 0){
            return sqlParasMap;
        }

        //
        if(isFutrue && resultFutrues.size()>0){
            return resultFutrues;
        }


        return object;
    }



    /**
     * 获取sql对象 -- 需要开启enjoy-sql模板  sqlTplList.add("/edb/sql/all.sql");
     * @param fields
     * @param limit
     * @param offset
     * @param eDbPro
     * @param fieldAndColValues
     * @param entityClass
     * @param eDbRel
     * @param tableData
     * @param columns
     * @return
     */
    @Deprecated
    public static SqlPara getSqlPara(String fields,
                              Integer limit,
                              Integer offset,
                              EDbPro eDbPro,
                              List<FieldAndColValue> fieldAndColValues,Class<?> entityClass,
                              EDbRel eDbRel,
                              Map<String,Object> tableData,
                              List<String> columns){
        // 获取jpa对象的 table 注解
        Table table = JpaAnnotationUtil.getTableAnnotation(entityClass);
        SqlPara sqlPara = new SqlPara();

        // 自定义查询字段 -- 估计用到的场合少，但是也有特殊情况需要有，甚至可以针对特别字段自己做一些函数表达式等处理
        if(fields != null){
            tableData.put(JpaRelPip.fields,fields);
        }

        // 赋予表名称
        tableData.put(JpaRelPip.tableName,table.name());

        for (String columnName : eDbRel.relColumn()){
            // 匹配
            for(FieldAndColValue fieldAndColValue : fieldAndColValues){
                // 匹配数据库字段
                if(fieldAndColValue.getColumn().name().toLowerCase().equals(columnName.toLowerCase())){
                    // 表字段 -- 暂无用处
                    columns.add(columnName);
                    // 入参赋值
                    sqlPara.addPara(fieldAndColValue.getFieldValue());
                }
            }
        }
        // where条件
        tableData.put(JpaRelPip.params,columns);
        // 拼接的sql
        String appendSql = eDbRel.appendSql();
        //
        List<String> results =  ReUtil.findAll("(#\\(){1}(.*?)(\\){1})",appendSql,0);
        // 字段名称
        String columnName = "";
        //
        for(String regStr:results) {
            // 获取字段 ，将匹配的字段前后部分替换成空字符串
            columnName = regStr.replaceAll(jpaRelReg,"$1").replaceAll(" ","");
            for(FieldAndColValue fieldAndColValue : fieldAndColValues){
                if(fieldAndColValue.getColumn().name().toLowerCase().equals(columnName.toLowerCase())){
                    // 入参赋值
                    sqlPara.addPara(fieldAndColValue.getFieldValue());
                }
            }
        }
        // 统一替换成 ？
        appendSql = appendSql.replaceAll(jpaRelReg,"?");

        tableData.put(JpaRelPip.appendSql,appendSql);

        // 是否有传入每次返回的最大条数
        if(limit == null){
            // 返回的数量
            tableData.put(JpaRelPip.limit,eDbRel.limit());
        }else{
            tableData.put(JpaRelPip.limit,limit);
        }

        // 是否有传入自定义查询起始位
        if(offset == null){
            // 读取数据的位置
            tableData.put(JpaRelPip.offset,eDbRel.offset());
        }else{
            // 读取数据的位置
            tableData.put(JpaRelPip.offset,offset);
        }

        // sql对象 -- 因为还要指定模板加载，对于构建组件来说步骤太麻烦，所以去除
        sqlPara.setSql(eDbPro.getSqlPara(EDbRelProxy.jpaEdbRelKey,tableData).getSql());
        return sqlPara;
    }


    /**
     * 获取sql对象
     * @param fields
     * @param limit
     * @param offset
     * @param eDbPro
     * @param fieldAndColValues
     * @param entityClass
     * @param eDbRel
     * @param tableData
     * @return
     */
    public static SqlPara getSqlPara(String fields,
                                     Integer limit,
                                     Integer offset,
                                     EDbPro eDbPro,
                                     List<FieldAndColValue> fieldAndColValues,Class<?> entityClass,
                                     EDbRel eDbRel,
                                     Map<String,Object> tableData){
        // 获取jpa对象的 table 注解
        Table table = JpaAnnotationUtil.getTableAnnotation(entityClass);
        StringBuffer sqlBuf = new StringBuffer(" select ");
        SqlPara sqlPara = new SqlPara();
        // 自定义查询字段 -- 估计用到的场合少，但是也有特殊情况需要有，甚至可以针对特别字段自己做一些函数表达式等处理
        if(fields != null){
            sqlBuf.append(" fields ");
        }
        else{
            sqlBuf.append(" * ");
        }
        // 赋予表名
        sqlBuf.append(" from ").append(table.name()).append(" where 1=1 ");

        for (String columnName : eDbRel.relColumn()){
            // 匹配
            for(FieldAndColValue fieldAndColValue : fieldAndColValues){
                // 匹配数据库字段
                if(fieldAndColValue.getColumn().name().toLowerCase().equals(columnName.toLowerCase())){
                    // 拼接sql
                    sqlBuf.append(" and ").append(columnName).append(" = ? ");
                    // 入参赋值
                    sqlPara.addPara(fieldAndColValue.getFieldValue());
                }
            }
        }
        // where条件
        // 拼接的sql
        String appendSql = eDbRel.appendSql();
        //
        List<String> results =  ReUtil.findAll("(#\\(){1}(.*?)(\\){1})",appendSql,0);
        // 字段名称
        String fieldColumnName = "";
        //
        for(String regStr:results) {
            // 获取字段 ，将匹配的字段前后部分替换成空字符串
            fieldColumnName = regStr.replaceAll(jpaRelReg,"$1").replaceAll(" ","");
            for(FieldAndColValue fieldAndColValue : fieldAndColValues){
                // 原来是获取到数据库字段名去做替换，但是含义和代表较弱，所以替换成操作对象的key值
//                if(fieldAndColValue.getColumn().name().toLowerCase().equals(columnName.toLowerCase())){
//                    // 入参赋值
//                    sqlPara.addPara(fieldAndColValue.getFieldValue());
//                }
                // 字段名
                if(fieldAndColValue.getField().getName().equals(fieldColumnName)){
                    // 入参赋值
                    sqlPara.addPara(fieldAndColValue.getFieldValue());
                }
            }
        }
        // 统一替换成 ？
        appendSql = appendSql.replaceAll(jpaRelReg,"?");
        //  拼接的sql
        sqlBuf.append(appendSql);
        // 是否有传入每次返回的最大条数
        if(limit == null){
            // 返回的数量
            sqlBuf.append(" limit ").append(eDbRel.limit());
        }else{
            sqlBuf.append(" limit ").append(limit);
        }

        // 是否有传入自定义查询起始位
        if(offset == null){
            // 读取数据的位置
            sqlBuf.append(" offset ").append(eDbRel.offset());
        }else{
            // 读取数据的位置
            sqlBuf.append(" offset ").append(offset);
        }
        // sql对象 -- 因为还要指定模板加载，对于构建组件来说步骤太麻烦，所以去除
        sqlPara.setSql(sqlBuf.toString());
        return sqlPara;
    }



}
