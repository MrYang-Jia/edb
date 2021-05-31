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
package com.edbplus.db.util.list;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName EDbListUtil
 * @Description: List对象工具类
 * @Author 杨志佳
 * @Date 2021/5/28
 * @Version V1.0
 **/
public class EDbListUtil {


    /**
     * 将对应字段转换成 List<T> 列表
     * @param type -- 转换的列表参数类型
     * @param list -- 列表对象
     * @param fieldName -- 指定字段
     * @param defaultValue -- 默认值
     * @param <T>
     * @param <M>
     * @return
     */
    public static <T, M> List<T> toConvertList(Class<T> type
            , List<M> list
            , String fieldName
            , T defaultValue
            ){
        List<T> integerList = null;
        //
        //
        if(list!=null && list.size()>0) {
            integerList = list.stream().map(p ->{
                // 如果是map对象的话
                if(p instanceof Map){
                    return Convert.convert(
                            // 转换的类型
                            type,
                            // 提取字段
                            ((Map) p).get(fieldName),
                            // 如果不是数字，则赋予默认值
                            defaultValue
                    );
                }
                // 其他则认为是一个实体对象
                return Convert.convert(
                        // 转换的类型
                        type,
                        // 提取字段
                        ReflectUtil.getFieldValue(p, fieldName),
                        // 如果不是数字，则赋予默认值
                        defaultValue
                );
            }
            ).collect(Collectors.toList());

        } else{
            return null;
        }
        //
        return integerList;
    }



}
