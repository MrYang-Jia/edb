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
package com.edbplus.db.query.lambda;

import com.edbplus.db.dto.FieldAndColumn;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.jfinal.kit.StrKit;

import javax.persistence.Column;
import java.io.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @ClassName EDbLambdaUtil
 * @Description: EDbLambdaUtil
 * @Author 杨志佳
 * @Date 2022/4/15
 * @Version V1.0
 **/
public class EDbLambdaUtil {

    /**
     * 获取字段注解 Column 属性
     * @param func
     * @return
     */
    public static <T> Column getColumn(Class<?> entityClass,EDbColumnFunc<T, ?> func) {
        try {
            SerializedLambda serializedLambda = EDbLambdaUtil.getSerializedLambda(func);
            String getter = serializedLambda.getImplMethodName();
            String fieldName = EDbLambdaUtil.resolveFieldName(getter);
//            Class<?> capturingClass = Class.forName(serializedLambda.getCapturingClass().replace("/", "."));
//            System.out.println(capturingClass.getName()); // 调用类
            if(entityClass == null){
                entityClass = (Class<T>) Class.forName(serializedLambda.getImplClass().replace("/", "."));
            }
//            System.out.println(domainClass.getName()); // 实体类
            // 利用hutool的类反射对象
//            Field field =  ClassUtil.getDeclaredField(domainClass,fieldName); // 通过反射获取字段
//            Column column = EAnnotationUtil.getAnnotation(field, Column.class); // 获取字段上的注解，从而获取到数据库的实际字段名
            // 通过已缓存的对象反射注解池，直接获取到对象的相关信息，只有第一次反射时，才需要消耗一定的资源
            FieldAndColumn fieldAndColumn = JpaAnnotationUtil.getCoumnsMap(entityClass).get(fieldName);
            Column column = fieldAndColumn.getColumn(); // 获取到字段注解
            return column;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> SerializedLambda getSerializedLambda(EDbColumnFunc<T, ?>  fn) {
//        // 从function取出序列化方法
        Method writeReplaceMethod;
        SerializedLambda serializedLambda;
        try {
            writeReplaceMethod = fn.getClass().getDeclaredMethod("writeReplace");
            // 从序列化方法取出序列化的lambda信息
            boolean isAccessible = writeReplaceMethod.isAccessible();
            writeReplaceMethod.setAccessible(true);
            try {
                serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(fn);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            writeReplaceMethod.setAccessible(isAccessible);
        } catch (NoSuchMethodException e) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();

                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(fn);
                oos.flush();
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))
                {
                    @Override
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        Class<?> clazz = super.resolveClass(desc);
                        return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
                    }

                }
                ) {
                    serializedLambda = (SerializedLambda) ois.readObject();
                }
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        return serializedLambda;
    }

//    public String getFieldName(PropertyFunc<T, ?> func) {
//        try {
//            // 通过获取对象方法，判断是否存在该方法
//            Method method = func.getClass().getDeclaredMethod("writeReplace");
//            method.setAccessible(Boolean.TRUE);
//            // 利用jdk的SerializedLambda 解析方法引用
//            java.lang.invoke.SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
//            String getter = serializedLambda.getImplMethodName();
//            String fieldName = resolveFieldName(getter);
////            Class<?> capturingClass = Class.forName(serializedLambda.getCapturingClass().replace("/", "."));
////            System.out.println(capturingClass.getName()); // 调用类
//            Class<?> domainClass = Class.forName(serializedLambda.getImplClass().replace("/", "."));
////            System.out.println(domainClass.getName()); // 实体类
//            // 利用hutool的类反射对象
////            Field field =  ClassUtil.getDeclaredField(domainClass,fieldName); // 通过反射获取字段
////            Column column = EAnnotationUtil.getAnnotation(field, Column.class); // 获取字段上的注解，从而获取到数据库的实际字段名
//            // 通过已缓存的对象反射注解池，直接获取到对象的相关信息，只有第一次反射时，才需要消耗一定的资源
//            FieldAndColumn fieldAndColumn = JpaAnnotationUtil.getCoumnsMap(domainClass).get(fieldName);
//            Column column = fieldAndColumn.getColumn(); // 获取到字段注解
//            return fieldName;
//        } catch (ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static   String resolveFieldName(String getMethodName) {
        if (getMethodName.startsWith("get")) {
            getMethodName = getMethodName.substring(3);
        } else if (getMethodName.startsWith("is")) {
            getMethodName = getMethodName.substring(2);
        }
        // 小写第一个字母
        return firstToLowerCase(getMethodName);
    }

    public static   String firstToLowerCase(String param) {
        if (StrKit.isBlank(param)) {
            return "";
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }
}
