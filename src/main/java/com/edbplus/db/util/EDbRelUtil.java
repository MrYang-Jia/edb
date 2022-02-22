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
import com.edbplus.db.annotation.EDbRel;
import com.edbplus.db.annotation.EDbView;
import com.edbplus.db.dto.FieldAndColValue;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.util.bean.EDbBeanUtil;
import com.edbplus.db.util.hutool.rul.EReUtil;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbRelUtil
 * @Description: EDbRel注解工具类
 * @Author 杨志佳
 * @Date 2022/2/14
 * @Version V1.0
 **/
public class EDbRelUtil {

    /**
     * 加载模板数据到视图对象
     * @param object
     * @param fieldName
     * @param eDbPro
     * @param pageNo
     * @param pageSize
     */
    public static void  loadRel(Object object, String fieldName, EDbPro eDbPro, int pageNo, int pageSize){
        loadRel(object,fieldName,null,eDbPro,pageNo,pageSize,0);
    }

    /**
     * 加载模板数据到视图对象
     * @param object
     * @param fieldName
     * @param eDbPro
     * @param pageNo
     * @param pageSize
     */
    public static void  loadRel(Object object, String fieldName,String fields, EDbPro eDbPro,int pageNo,int pageSize,long totalRow){
        Object result = null;
        Page jfinalPage = null; // jfinal-page
        Class<?> entityClass = null; // 包装的实体类类型
        Class<?> packingType = null; // 包装类类型
        Class objClass = object.getClass(); // 实体类型
        Field field = ReflectUtil.getField(objClass,fieldName); // 获取属性字段
        if(field == null){
            throw new RuntimeException("fieldName "+fieldName+" 可能不存在，或者是实体对象漏加注解 @RegisterForReflection " );
        }
        EDbRel eDbRel = field.getAnnotation(EDbRel.class); // 获取到 EDbView注解
        if(eDbRel!=null){
            Map<String,Object> dataMap = EDbBeanUtil.beanToMap(object); // 当前对象转换成map型数据集合
            Type fieldType = field.getGenericType(); // 该字段的所有字段类型
            if(fieldType instanceof ParameterizedType) { // 复合包装的字段类型
                entityClass = (Class<?>)((ParameterizedType) fieldType).getActualTypeArguments()[0]; // 包装的实体类类型
                // 包装类 -- 目前只支持 List jfinal-page 2种包装类型
                packingType = (Class<?>) ((ParameterizedType) fieldType).getRawType();
            }else{
                entityClass = field.getType(); // 字段类型
            }
            SqlPara sqlPara = new SqlPara(); // 参数集对象
            Map<String,String> columnFieldMap = JpaAnnotationUtil.getColumnFieldMap(object.getClass()); // 获取表对象的字段和驼峰字段映射值
            Table table = JpaAnnotationUtil.getTableAnnotation(entityClass); // 获取表名

            // ===== sql组装 =====
            StringBuffer sqlBuf = new StringBuffer(" select ");
            // 自定义查询字段 -- 估计用到的场合少，但是也有特殊情况需要有，甚至可以针对特别字段自己做一些函数表达式等处理
            if(fields != null){
                sqlBuf.append( fields );
            }
            else{
                sqlBuf.append(" * ");
            }
            // 赋予表名
            sqlBuf.append(" from ").append(table.name()).append(" where 1=1 ");
            for (String columnName : eDbRel.relColumn()){
                sqlBuf.append(" and ").append(columnName).append(" = #(").append(columnFieldMap.get(columnName)).append(") "); // 回填驼峰字段
            }
            // EDbRel 动态sql拼接的部分
            sqlBuf.append(eDbRel.appendSql());
            // ===== sql组装-end =====

            EDbTemplate eDbTemplate = eDbPro.templateByString(sqlBuf.toString(),dataMap);
            if(fieldType instanceof ParameterizedType) {
                if (packingType == Page.class) { // 判断包装类是 Jfinal.page 的话
                    if (totalRow > 0) {
                        result = eDbTemplate.paginate(entityClass, pageNo, pageSize, totalRow);
                    } else {
                        result = eDbTemplate.paginate(entityClass, pageNo, pageSize);
                    }
                }// 适配spring分页 ，由于quarkus是独立于spring的体系，所以移除该类别的判断
                // 其他类型统一直接当作list返回
                else if (packingType == List.class) {
                    //
                    result = eDbTemplate.find(entityClass);
                } else {
                    // 抛错
                    throw new RuntimeException(" view视图只支持单对象或 List 、jfinal-page 2种数组类型的组合 ");
                }
            }else{
                result = eDbTemplate.findFirst(entityClass); // 返回实例结果
            }
            ReflectUtil.setFieldValue(object, field, result); // 反射赋值
        }
    }


}
