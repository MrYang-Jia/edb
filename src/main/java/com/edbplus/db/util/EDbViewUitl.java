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
package com.edbplus.db.util;

import cn.hutool.core.util.ReflectUtil;
import com.edbplus.db.EDbPro;
import com.edbplus.db.EDbTemplate;
import com.edbplus.db.annotation.EDbView;
import com.edbplus.db.util.bean.EDbBeanUtil;
import com.jfinal.plugin.activerecord.Page;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbViewUitl
 * @Description: EDbView 工具类
 * @Author 杨志佳
 * @Date 2022/2/14
 * @Version V1.0
 **/
public class EDbViewUitl {

    /**
     * 加载模板数据到视图对象
     * @param object
     * @param fieldName
     * @param eDbPro
     * @param pageNo
     * @param pageSize
     */
    public static void  loadView(Object object, String fieldName, EDbPro eDbPro,int pageNo,int pageSize){
        loadView(object,fieldName,eDbPro,pageNo,pageSize,0);
    }

    /**
     * 加载模板数据到视图对象
     * @param object
     * @param fieldName
     * @param eDbPro
     * @param pageNo
     * @param pageSize
     */
    public static void  loadView(Object object, String fieldName, EDbPro eDbPro,int pageNo,int pageSize,long totalRow){
        Object result = null;
        Page jfinalPage = null; // jfinal-page
        Class<?> entityClass = null; // 包装的实体类类型
        Class<?> packingType = null; // 包装类类型
        Class objClass = object.getClass(); // 实体类型
        Field field = ReflectUtil.getField(objClass,fieldName); // 获取属性字段
        if(field == null){
            throw new RuntimeException("fieldName "+fieldName+" 可能不存在，或者是实体对象漏加注解 @RegisterForReflection " );
        }
        EDbView eDbView = field.getAnnotation(EDbView.class); // 获取到 EDbView注解
        if(eDbView!=null){
            Map<String,Object> dataMap = EDbBeanUtil.beanToMap(object);
            EDbTemplate eDbTemplate = eDbPro.template(eDbView.name(),dataMap);  //模板对象获取
            Type fieldType = field.getGenericType(); // 所有字段类型
            if(fieldType instanceof ParameterizedType){ // 字段类型
                entityClass = (Class<?>)((ParameterizedType) fieldType).getActualTypeArguments()[0]; // 包装的实体类类型
                // 包装类 -- 目前只支持 List jfinal-page  2种包装类型
                packingType = (Class<?>) ((ParameterizedType) fieldType).getRawType();
                if(packingType == Page.class){ // 判断包装类是 Jfinal.page 的话
                    if(totalRow > 0){
                        result = eDbTemplate.paginate(entityClass,pageNo,pageSize,totalRow);
                    }else{
                        result = eDbTemplate.paginate(entityClass,pageNo,pageSize);
                    }
                }// 适配spring分页 ，由于quarkus是独立于spring的体系，所以移除该类别的判断
//                else if(packingType == org.springframework.data.domain.Page.class){
//                    if(totalRow > 0){
//                        jfinalPage = eDbTemplate.paginate(entityClass,pageNo,pageSize,totalRow);
//                    }else{
//                        jfinalPage = eDbTemplate.paginate(entityClass,pageNo,pageSize);
//                    }
//                    // 返回对象
//                    result = EDbPageUtil.returnSpringPage(jfinalPage);
//                }
                // 其他类型统一直接当作list返回
                else if (packingType == List.class){
                    //
                    result = eDbTemplate.find(entityClass);
                }else{
                    // 抛错
                    throw new RuntimeException(" view视图只支持单对象或 List 、jfinal-page 2种数组类型的组合 ");
                }
            }else{
                entityClass = field.getType(); // 字段类型
                result = eDbTemplate.findFirst(entityClass); // 这里获取对象的实际类型
            }
            ReflectUtil.setFieldValue(object, field, result); // 反射赋值
        }
    }
}
