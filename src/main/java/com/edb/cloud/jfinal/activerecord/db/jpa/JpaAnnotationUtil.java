package com.edb.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.SimpleCache;
import cn.hutool.core.util.ReflectUtil;
import com.edb.cloud.jfinal.activerecord.db.annotation.EDbRel;
import com.edb.cloud.jfinal.activerecord.db.annotation.EDbView;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColValue;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColumn;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndRel;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndView;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName JpaAnnotationUtil
 * @Description: jpa注解工具类
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
public class JpaAnnotationUtil {

    // 注解类方法级缓存
    private static final SimpleCache<Class<?>, Map<Class<?>,Method>> METHODS_ANNOTATION_CACHE = new SimpleCache();
    // 字段get属性集
    private static final SimpleCache<Class<?>, Map<Field,Method>> FIELD_METHOD_CACHE = new SimpleCache();

    // 缓存表对象 -- 便于下次直接获取
    private static final SimpleCache<Class<?>, Table> TABLE_CACHE = new SimpleCache();

    // 缓存对象columns
    private static final SimpleCache<Class<?>, List<FieldAndColumn>> COLUMNS_CACHE = new SimpleCache();

    // 缓存关系对象
    private static final SimpleCache<Class<?>, List<FieldAndRel>> REL_CACHE = new SimpleCache();

    // 缓存视图对象
    private static final SimpleCache<Class<?>, List<FieldAndView>> VIEW_CACHE = new SimpleCache();


    /**
     * 返回jpa表对象 -- 默认一个对象只会有一个table对象
     * @param mClass
     * @return
     */
    public static Table getTableAnnotation(Class mClass){
        Table table = TABLE_CACHE.get(mClass);
        // 判断返回结果是否有值，如果没有则加入到缓存里，并将获取的结果返回到对象
        if(table == null){
            // spring工具类 AnnotationUtils.findAnnotation，性能没有 hutool 的工具好
            table = AnnotationUtil.getAnnotation(mClass, Table.class);
            // 需要做判断，避免非jpa对象传入
            if(table == null){
                throw new RuntimeException(" 当前对象必须包含注解 javax.persistence.Table ，指定表名称 ");
            }
            // 放置到缓存
            TABLE_CACHE.put(mClass,table);
        }
        return table;
    }

    /**
     * 根据注解获取指定方法
     * @param mClass
     * @param annotationClass
     * @return
     */
    public static Method getMethod(Class mClass,Class annotationClass){
        // 从缓存里直接获取方法，避免一直循环获取，提高效率
        Map<Class<?>,Method> annotationMethodMap = METHODS_ANNOTATION_CACHE.get(mClass);
        if(annotationMethodMap != null){
            Method method = annotationMethodMap.get(annotationClass);
            if(method!=null){
                // 返回方法
                return method;
            }
        }
        // 本身存在方法级缓存
        Method[] methods = ReflectUtil.getMethods(mClass);
        //
        for(Method method : methods){
            Object annotation = method.getAnnotation(annotationClass);
            if(annotation!=null){
                // 如果是首次则需要重置注解的方法对象
                if(annotationMethodMap == null){
                    // 重置map
                    annotationMethodMap = new HashMap<>();
                }
                // 添加新的key
                annotationMethodMap.put(annotationClass,method);
                // 放到缓存里
                METHODS_ANNOTATION_CACHE.put(mClass,annotationMethodMap);
                return method;
            }
        }
        return null;
    }


    /**
     * 获取对象的所有columns对象
     * @param mClass
     * @return
     */
    public static List<FieldAndColumn> getCoumns(Class mClass){
        // 初始化 columns 对象
        List<FieldAndColumn> columns = COLUMNS_CACHE.get(mClass);
        // 如果获取到结果，则直接返回即可
        if(columns!=null){
            // 便于快速判断时使用
            if(columns.size()==0){
                return null;
            }
            return columns;
        }
        // 没有获取到结果，则初始化话对象集
        columns = new ArrayList<FieldAndColumn>();
        // 获取对象类上的所有字段
        Field[] fields = ReflectUtil.getFields(mClass);

        Column column = null;
        FieldAndColumn fieldAndColumn = null;
        // 字段key填充
        HashSet<String> columnsKey = new HashSet();
        // 关系
        EDbRel eDbRel = null;
        for( Field field : fields ){
            // 获取column对象
            column =  AnnotationUtil.getAnnotation(field, Column.class);
            // 如果有column注解才进行填充，否则不填充
            if(column != null){
                // 如果不存在的字段才添加到返回的字段列表里 -- 存在一个bug，则是相同的字段
                if(!columnsKey.contains(column.name().toLowerCase())){
                    // 初始化字段和注解对象
                    fieldAndColumn = new FieldAndColumn();
                    // 回填字段对象
                    fieldAndColumn.setField(field);
                    // 回填column注解对象
                    fieldAndColumn.setColumn(column);
                    // 判断是否是主键
                    if(AnnotationUtil.getAnnotation(field, Id.class) != null){
                        fieldAndColumn.setIsPriKey(true);
                    }else{
                        fieldAndColumn.setIsPriKey(false);
                    }
                    // 添加对象
                    columns.add(fieldAndColumn);
                    // 添加到key里
                    columnsKey.add(column.name().toLowerCase());
                }
            }
        }
        // 直接回填到对象
        COLUMNS_CACHE.put(mClass,columns);
        // 便于快速判断时使用
        if(columns.size()==0){
            return null;
        }
        return columns;
    }


    /**
     * 获取对象的关系对象集
     * @param mClass
     * @return
     */
    public static List<FieldAndRel> getFieldRels(Class mClass){
        // 初始化 columns 对象
        List<FieldAndRel> fieldRels = REL_CACHE.get(mClass);
        // 如果已缓存则直接返回结果
        if(fieldRels!=null){
            // 便于快速判断时使用
            if(fieldRels.size()==0){
                return null;
            }
            return fieldRels;
        }
        //
        fieldRels = new ArrayList<FieldAndRel>();

        // 获取对象类上的所有字段
        Field[] fields = ReflectUtil.getFields(mClass);

        Column column = null;
        FieldAndRel fieldForRel = null;
        // 字段key填充
        HashSet<String> columnsKey = new HashSet();
        // 关系
        EDbRel eDbRel = null;
        for( Field field : fields ){
            // 获取关联关系的注解对象
            eDbRel =  AnnotationUtil.getAnnotation(field, EDbRel.class);
            if(eDbRel != null){
                // 初始化字段和注解对象
                fieldForRel = new FieldAndRel();
                // 回填字段对象
                fieldForRel.setField(field);
                // 设置关对象
                fieldForRel.setEDbRel(eDbRel);
                // 添加对象
                fieldRels.add(fieldForRel);
            }
        }
        // 放置缓存
        REL_CACHE.put(mClass,fieldRels);
        // 便于快速判断时使用
        if(fieldRels.size()==0){
            return null;
        }
        return fieldRels;
    }

    /**
     * 获取所有 Column 字段列表和属性值
     * @param t
     * @param <T>
     * @return
     */
    public static <T> List<FieldAndColValue> getCoumnValues(T t){
        // 获取对象类上的所有字段
//        Field[] fields = ReflectUtil.getFields(t.getClass());
        List<FieldAndColumn> fieldAndColumns = getCoumns(t.getClass());
        // 初始化 columns 对象
        List<FieldAndColValue> columns = new ArrayList<FieldAndColValue>();
        Column column = null;
        FieldAndColValue fieldAndColValue = null;
        // 字段key填充
        HashSet<String> columnsKey = new HashSet();
        for( FieldAndColumn fieldAndColumn : fieldAndColumns ){
            // 获取column对象
            column =  AnnotationUtil.getAnnotation(fieldAndColumn.getField(), Column.class);
            // 如果有column注解才进行填充，否则不填充
            if(column != null){
                // 如果不存在的字段才添加到返回的字段列表里 -- 存在一个bug，则是相同的字段
                if(!columnsKey.contains(column.name().toLowerCase())){
                    // 初始化字段和注解对象
                    fieldAndColValue = new FieldAndColValue();
                    // 回填字段对象
                    fieldAndColValue.setField(fieldAndColumn.getField());
                    // 回填column注解对象
                    fieldAndColValue.setColumn(column);
                    // 赋予对象值
                    fieldAndColValue.setFieldValue(ReflectUtil.getFieldValue(t,fieldAndColumn.getField()));
                    if(AnnotationUtil.getAnnotation(fieldAndColumn.getField(), Id.class) != null){
                        fieldAndColValue.setIsPriKey(true);
                    }else{
                        fieldAndColValue.setIsPriKey(false);
                    }
                    // 添加对象
                    columns.add(fieldAndColValue);
                    // 添加到key里
                    columnsKey.add(column.name().toLowerCase());
                }
            }
        }
        // 便于快速判断时使用
        if(columns.size()==0){
            return null;
        }
        return columns;
    }



    /**
     * 返回主键列表 -- 含字段值
     * @param t
     * @param <T>
     * @return
     */
    public static <T> List<FieldAndColValue> getIdFieldAndColumnValues(T t){
        List<FieldAndColValue> columns = new ArrayList<FieldAndColValue>();
        // 获取对象类上的所有字段
//        Field[] fields = ReflectUtil.getFields(t.getClass());
        List<FieldAndColumn> fieldAndColumns = getCoumns(t.getClass());

        // 每次只需要找到 几个 ID 主键的注解
        Id id = null;
        Column column = null;
        FieldAndColValue fieldAndColValue = null;
        for( FieldAndColumn fieldAndColumn :fieldAndColumns){
            // 寻找到主键主键
            if(fieldAndColumn.getIsPriKey()){
                // 获取column对象
                column =  AnnotationUtil.getAnnotation(fieldAndColumn.getField(), Column.class);
                // 注解对象判断
                if(column == null){
                    throw new RuntimeException("注解 @Id 必须配合 @Column 对象 ; 请引用包：javax.persistence.* ");
                }
                fieldAndColValue = new FieldAndColValue();
                fieldAndColValue.setField(fieldAndColumn.getField());
                fieldAndColValue.setColumn(column);
                fieldAndColValue.setIsPriKey(true);
                fieldAndColValue.setFieldValue(ReflectUtil.getFieldValue(t,fieldAndColumn.getField()));
                // 添加对象
                columns.add(fieldAndColValue);
            }
        }
        // 如果没有主键必须告知异常
        if(columns.size() == 0){
            throw new RuntimeException("该对象无主键注解 @Id ; 请引用包：javax.persistence.* ");
        }
        return  columns;
    }

    /**
     * 返回主键对象集合
     * @param mClass
     * @return
     */
    public static List<FieldAndColumn> getIdFieldAndColumns(Class mClass){
        List<FieldAndColumn> columns = new ArrayList<FieldAndColumn>();
        // 获取对象类上的所有字段
        Field[] fields = ReflectUtil.getFields(mClass);

        // 每次只需要找到 几个 ID 主键的注解
        Id id = null;
        Column column = null;
        FieldAndColumn fieldAndColumn = null;
        for( Field field :fields){
            id =  AnnotationUtil.getAnnotation(field, Id.class);
            // 寻找到主键主键
            if(id != null){
                // 获取column对象
                column =  AnnotationUtil.getAnnotation(field, Column.class);
                // 注解对象判断
                if(column == null){
                    throw new RuntimeException("注解 @Id 必须配合 @Column 对象 ; 请引用包：javax.persistence.* ");
                }
                fieldAndColumn = new FieldAndColumn();
                fieldAndColumn.setField(field);
                fieldAndColumn.setColumn(column);
                fieldAndColumn.setIsPriKey(true);
                // 添加对象
                columns.add(fieldAndColumn);
            }
        }
        // 如果没有主键必须告知异常
        if(columns.size() == 0){
            throw new RuntimeException("该对象无主键注解 @Id ; 请引用包：javax.persistence.* ");
        }
        return  columns;
    }

    /**
     * 获取返回的主键
     * @param mClass
     * @return
     */
    public static List<Column> getIdCoumns(Class mClass){
        // 获取对象类上的所有字段
        Field[] fields = ReflectUtil.getFields(mClass);
        // 初始化 idColumns 对象
        List<Column> idColumns = new ArrayList<Column>();

        // 每次只需要找到 几个 ID 主键的注解
        Id id = null;

        Column column = null;
        for( Field field :fields){
            id =  AnnotationUtil.getAnnotation(field, Id.class);
            // 寻找到主键主键
            if(id != null){
                // 获取column对象
                column =  AnnotationUtil.getAnnotation(field, Column.class);
                // 注解对象判断
                if(column == null){
                    throw new RuntimeException("注解 @Id 必须配合 @Column 对象 ; 请引用包：javax.persistence.* ");
                }
                // 添加对象
                idColumns.add(column);
            }
        }
        // 如果没有主键必须告知异常
        if(idColumns.size() == 0){
            throw new RuntimeException("该对象无主键注解 @Id ; 请引用包：javax.persistence.* ");
        }

        return idColumns;
    }

    /**
     * 返回主键字段字符串
     * @param mClass
     * @return
     */
    public static String getPriKeys( Class mClass){
        List<Column> idColumns = getIdCoumns(mClass);
        return getPriKeys(idColumns);
    }

    /**
     * 返回主键字段
     * @param idColumns
     * @return
     */
    public static String getPriKeys( List<Column> idColumns){
        // 这里没有对 idColumns 做判断，是因为有异常的时候会直接抛出
        StringBuffer keys= new StringBuffer();
        for(Column column : idColumns){
            // 统一小写，避免无法识别
            keys.append(column.name().toLowerCase()).append(",");
        }
        // 删除最后一个字符串
        keys.deleteCharAt(keys.length()-1);
        return keys.toString();
    }


    /**
     * 返回主键字段数值
     * @param t
     * @return
     */
    public static <T> List<Object> getPriKeyValues(T t){
        //
        List<FieldAndColValue> idColumns = getIdFieldAndColumnValues(t);
        // 返回字段值
        List<Object> ids = idColumns.stream().map(p -> p.getFieldValue() ).collect(Collectors.toList());
        return ids;
    }


    /**
     * 根据对象类型和注解类型返回对象的字段属性
     * @param mClass
     * @param annotationClass
     * @return
     */
    public static Field getFieldForAnnationClass(Class mClass,Class annotationClass){
        Field[] fields = ReflectUtil.getFields(mClass);
        for(Field field : fields){
            if(field.getAnnotation(annotationClass)!=null){
                return field;
            }
        }
        return null;
    }



    /**
     * 将对象转换成map
     * @param t
     * @param ignoreNullValue
     * @param <T>
     * @return
     */
    public static <T> Map<String,Object> getJpaMap(T t,boolean ignoreNullValue){
        //
        Map<String,Object> dataMap = new HashMap<>();
        // 获取对象类上的所有字段
        List<FieldAndColValue> fields = getCoumnValues(t);
        //
        for(FieldAndColValue fieldAndColValue : fields){
            // 不包含null的情况
            if(!ignoreNullValue){
                if(fieldAndColValue.getFieldValue() != null){
                    // 赋予对象数值
                    dataMap.put(fieldAndColValue.getColumn().name().toLowerCase(),fieldAndColValue.getFieldValue());
                }
            }else{
                // 赋予对象数值
                dataMap.put(fieldAndColValue.getColumn().name().toLowerCase(),fieldAndColValue.getFieldValue());
            }

        }

        return dataMap;

    }


    /**
     * 获取对象的所有columns对象
     * @param mClass
     * @return
     */
    public static List<FieldAndView> getFieldViews(Class mClass){
        // 初始化 columns 对象
        List<FieldAndView> fieldViews = VIEW_CACHE.get(mClass);
        // 有结果则直接返回视图
        if(fieldViews!=null){
            // 便于快速判断时使用
            if(fieldViews.size()==0){
                return null;
            }
            return fieldViews;
        }
        // 初始化
        fieldViews = new ArrayList<FieldAndView>();
        // 获取对象类上的所有字段
        Field[] fields = ReflectUtil.getFields(mClass);
        //
        FieldAndView fieldForRel = null;
        // 关系
        EDbView eDbView = null;
        for( Field field : fields ){
            // 获取关联关系的注解对象
            eDbView =  AnnotationUtil.getAnnotation(field, EDbView.class);
            if(eDbView != null){
                // 初始化字段和注解对象
                fieldForRel = new FieldAndView();
                // 回填字段对象
                fieldForRel.setField(field);
                // 设置关对象
                fieldForRel.setEDbView(eDbView);
                // 添加对象
                fieldViews.add(fieldForRel);
            }
        }
        // 置为缓存
        VIEW_CACHE.put(mClass,fieldViews);
        // 便于快速判断时使用
        if(fieldViews.size()==0){
            return null;
        }
        return fieldViews;
    }


    /**
     * 获取字段的 get 方法
     * @param field
     * @param mClass
     * @return
     */
    public static Method getFieldReadMethod(Field field,Class<?> mClass){
        Map<Field,Method> fieldMethodMap = FIELD_METHOD_CACHE.get(mClass);
        Method getMethod = null;
        if(fieldMethodMap!=null){
            getMethod = fieldMethodMap.get(field);
            if(getMethod!=null){
                return getMethod;
            }
        }else{
            fieldMethodMap =  new HashMap<>();
        }
        // 属性描述器
        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(field.getName(), mClass);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //获得get方法  -- 获得用于读取属性值的方法
        getMethod = pd.getReadMethod();
        // 字段放置入map对象
        fieldMethodMap.put(field,getMethod);
        // 类字段get方法缓存设置
        FIELD_METHOD_CACHE.put(mClass,fieldMethodMap);
        return getMethod;
    }

}
