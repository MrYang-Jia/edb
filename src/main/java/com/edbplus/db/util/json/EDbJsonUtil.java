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
package com.edbplus.db.util.json;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.jfinal.kit.StrKit;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @ClassName EDbJsonUtil
 * @Description: json 工具集
 * @Author 杨志佳
 * @Date 2020/10/22
 * @Version V1.0
 **/
public class EDbJsonUtil {

    public static ObjectMapper mapper;


    static {
        if(mapper==null){
            mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;
            mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
            // 如果json中有新增的字段并且是实体类类中不存在的，不报错
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            // 转换为格式化的json
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    /**
     * 转json
     * @param object
     * @return
     */
    public static String toJson(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {

            throw  new RuntimeException("write to json string error",e);
        }
    }

    /**
     * 格式化输出
     * @param object
     * @return
     * @throws
     */
    public static String toJsonForFormat(Object object)  {

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {

            throw  new RuntimeException("write to json string error",e);
        }
    }

    /**
     *
     * @param jsonString
     * @param clazz
     * @param <T>
     * @return
     */
    public static  <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StrKit.isBlank(jsonString)) {
            throw  new RuntimeException("格式JSON不能为空");
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw  new RuntimeException("parse json string error:",e);
        }
    }

    /**
     *
     * @param jsonString
     * @param javaType
     * @param <T>
     * @return
     */
    public static  <T> T fromJson(String jsonString, JavaType javaType) {
        if (StrKit.isBlank(jsonString)) {
            throw  new RuntimeException("格式JSON不能为空");
        }
        try {
            return (T) mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            throw  new RuntimeException("parse json string error:",e);
        }
    }

    public static JavaType contructCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }


    public static JavaType contructMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public void update(String jsonString, Object object){
        try {
            mapper.readerForUpdating(object).readValue(jsonString);
        } catch (JsonProcessingException e) {

            throw  new RuntimeException("update json string:" + jsonString + " to object:" + object + " error.",e);
        } catch (IOException e) {
            throw  new RuntimeException("update json string:" + jsonString + " to object:" + object + " error.",e);
        }
    }


    public static String toJsonP(String functionName, Object object) {
        return toJson(new JSONPObject(functionName, object));
    }

    public static void enableEnumUseToString() {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    /**
     * jsonList字符串转对象数组
     * @param jsonList
     * @param elementClass
     * @param <T>
     * @return
     */
    public static  <T> T parseArray(String jsonList, Class<?> elementClass) {
        JavaType javaType = contructCollectionType(List.class, elementClass);
        return fromJson(jsonList, javaType);
    }


}
