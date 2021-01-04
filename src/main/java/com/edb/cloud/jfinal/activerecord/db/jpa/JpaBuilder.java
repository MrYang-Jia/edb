package com.edb.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColValue;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColumn;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @ClassName JpaBuilder
 * @Description: Jpa对象转换工具
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
public class JpaBuilder  {

    /**
     * 当前线程 -- 没有主动 remove 内存肯定存在泄露的问题 ， 需要在主方法事务结束时主动去关闭
     */
//    public static final ThreadLocal<ConcurrentHashMap<String,Object>> threadLocal = new ThreadLocal(){
//        @Override
//        protected ConcurrentHashMap<String, Object> initialValue() {
//            return new ConcurrentHashMap<String, Object>();
//        }
//    };


//    /**
//     * 设置对象初始值
//     * @param uuid
//     * @param oldObject
//     */
//    public static void setOriginalBean(String uuid,Object oldObject){
//        threadLocal.get().put(uuid,oldObject);
//    }
//
//    /**
//     * 移除当前线程连接
//     */
//    public static void removeThreadLocalOriginalBeans() {
//        threadLocal.remove();
//    }
//

    /**
     * 构建对象
     * @param beanClass
     * @param rs
     * @param <T>
     * @return
     * @throws SQLException
     * @throws ReflectiveOperationException
     */
    public static  <T> List<T> buildBean(Class<T> beanClass,ResultSet rs,boolean isInTransaction) throws SQLException, ReflectiveOperationException {
        List<T> result = new ArrayList<T>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        String[] labelNames = new String[columnCount + 1];
        int[] types = new int[columnCount + 1];
        buildLabelNamesAndTypes(rsmd, labelNames, types);

//        JpaProxy<T> jpaProxy = null;
        Map<String, Object> attrs = null;
        Object value = null;
        List<FieldAndColumn> columns = null;

        Field originalField = null;
        String uuid = null;

        while (rs.next()) {
            // 对象实例化
            Object ar = beanClass.newInstance();
            //
            attrs = new HashMap<>();
            for (int i=1; i<=columnCount; i++) {
                if (types[i] < Types.BLOB) {
                    value = rs.getObject(i);
                } else {
                    if (types[i] == Types.CLOB) {
                        value = handleClob(rs.getClob(i));
                    } else if (types[i] == Types.NCLOB) {
                        value = handleClob(rs.getNClob(i));
                    } else if (types[i] == Types.BLOB) {
                        value = handleBlob(rs.getBlob(i));
                    } else {
                        value = rs.getObject(i);
                    }
                }
                // 这里需要判断是否有column字段匹配上，有则用column对应的字段填充，适配特殊字段实现的模式
                columns = JpaAnnotationUtil.getCoumns(beanClass);
                if(columns!=null){
                    // 匹配 jpa @Column 指定的数据库字段名，便于将对象的字段别名区分开来
                    for(FieldAndColumn fieldAndColumn : columns){
                        // 如果是匹配上 column 上的字段，则用file的name作为key值
                        if(fieldAndColumn.getColumn().name().trim().equalsIgnoreCase(labelNames[i].trim())){
                            // 用对象字段的字段值作为key值，便于快速转换
                            attrs.put( fieldAndColumn.getField().getName(), value);
                            // 跳出本次循环
                            continue;
                        }
                    }
                }

                // 这里需要转换成驼峰写法，便于copyBean ，先转小写的目的是因为有些db的标准是大写，正常默认应该是统一小写，但为了适应所以统一转小写再驼峰
                attrs.put( StrUtil.toCamelCase(labelNames[i].toLowerCase()), value);
            }
            // 填充bean对象 -- 忽略对象大小写，可以填充aGe age 等语法
            BeanUtil.fillBeanWithMapIgnoreCase(attrs, ar, false);
            //
            Table table = null;
            //
            String keys = null;
            try {
                table = JpaAnnotationUtil.getTableAnnotation(beanClass);
                keys = JpaAnnotationUtil.getPriKeys(beanClass);
            }catch (Throwable e){
                // 如果不是jpa对象，则会抛出异常
            }

            // 由于 cglib 动态代理太消耗内存，所以放弃使用，代码与保留
//            if(table!=null && keys!=null){
//                // 实现jpa代理，查询出来的对象重新赋值时，更新到数据库的字段为变更的对象值
//                jpaProxy = JpaProxy.load(ar);
//                // 返回可操作的jpa代理对象集合
//                result.add(jpaProxy.getJpa());
//            }else{
//                // 返回普通的数据视图对象
//                result.add((T)ar);
//            }

//            Field field = ReflectUtil.getField(beanClass,"oldBean");

            // 2020-10-21 沟通需求后，发现实现协助用户记录数据的初始值意义不大
            // 方案2 通过，jpa继承实现一个固定的属性字段来承载旧的数据对象，便于比较 新 旧 对象，而不使用缓存空间的技术栈来完成相应的工作
//            if(isInTransaction){
//                // 在事务的情况底下才允许使用，便于线程结束时释放内存
//                if(originalField == null){
//                    originalField = JpaAnnotationUtil.getFieldForAnnationClass(beanClass, EDbUuid.class);
//                }
//                // 如果有字段用于存储旧jpa的值则赋予
//                if(originalField != null){
//                    // 每个对象单独一个地址空间标识
//                    uuid = UUID.randomUUID().toString();
//                    Object newAr = beanClass.newInstance();
//                    // 设置原对象的UUID，便于标识
//                    ReflectUtil.setFieldValue(ar,originalField,uuid);
//                    // 拷贝新对象 -- 预留原始值
//                    BeanUtil.copyProperties(ar,newAr,false);
//                    // 设置对象初始值
//                    setOriginalBean(uuid,newAr);
//                }
//            }

            // 返回普通的数据视图对象
            result.add((T)ar);

        }
        return result;
    }


    public static byte[] handleBlob(Blob blob) throws SQLException {
        if (blob == null) {
            return null;
        } else {
            InputStream is = null;

            Object var4;
            try {
                is = blob.getBinaryStream();
                if (is == null) {
                    Object var17 = null;
                    return (byte[])var17;
                }

                byte[] data = new byte[(int)blob.length()];
                if (data.length != 0) {
                    is.read(data);
                    byte[] var18 = data;
                    return var18;
                }

                var4 = null;
            } catch (IOException var15) {
                throw new RuntimeException(var15);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException var14) {
                        throw new RuntimeException(var14);
                    }
                }

            }

            return (byte[])var4;
        }
    }


    public static String handleClob(Clob clob) throws SQLException {
        if (clob == null) {
            return null;
        } else {
            Reader reader = null;

            String var4;
            try {
                reader = clob.getCharacterStream();
                if (reader == null) {
                    Object var17 = null;
                    return (String)var17;
                }

                char[] buffer = new char[(int)clob.length()];
                if (buffer.length == 0) {
                    var4 = null;
                    return var4;
                }

                reader.read(buffer);
                var4 = new String(buffer);
            } catch (IOException var15) {
                throw new RuntimeException(var15);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException var14) {
                        throw new RuntimeException(var14);
                    }
                }

            }

            return var4;
        }
    }


    public static void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
        for(int i = 1; i < labelNames.length; ++i) {
            labelNames[i] = rsmd.getColumnLabel(i);
            types[i] = rsmd.getColumnType(i);
        }

    }


    /**
     * 比较两个对象，返回实际变更的值
     * @param oldBean
     * @param newBean
     * @return
     */
    public static Map<String,Object> contrastObjReturnColumnMap(Object oldBean, Object newBean) {
        Map<String,Object> updateMap = new HashMap<>();
        // 通过反射获取类的类类型及字段属性
        Field[] fields = ReflectUtil.getFields(oldBean.getClass());
        Object o1 ;
        Object o2 ;
        int i = 1;
        Column column ;
        for (Field field : fields) {
            // 排除序列化属性
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            o1 = ReflectUtil.getFieldValue(oldBean,field);
            o2 = ReflectUtil.getFieldValue(newBean,field);
            //
            if(o1 != o2){
                //
                column =  AnnotationUtil.getAnnotation(field, Column.class);
                if(column != null ){
                    // 赋予新的变更值
                    updateMap.put(column.name().toLowerCase(),o2);
                }
            }
        }
        // 获取新对象上的主键键值 -- 允许动态变更修改键值
        List<FieldAndColValue> priKeys = JpaAnnotationUtil.getIdFieldAndColumnValues(newBean);
        // 获取主键键值，并赋予到
        for(FieldAndColValue fieldAndColValue:priKeys){
            updateMap.put(fieldAndColValue.getColumn().name().toLowerCase(),fieldAndColValue.getFieldValue());
        }

        return updateMap;
    }



}
