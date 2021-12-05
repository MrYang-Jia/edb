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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
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
     * 拷贝转换对象
     * @param tClass
     * @param beanList
     * @param <T>
     * @return
     */
    public static <T> List<T> copyBean(Class<T> tClass,List<?> beanList){
        List<T> result = null;
        if(beanList != null){
            result = (List<T>) beanList.stream().map(p ->{
                return JSONUtil.toBean(JSONUtil.toJsonStr(p), tClass);
            }).collect(Collectors.toList());
        }
        return result;
    }

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
                }else if(p instanceof Record){
                    return Convert.convert(
                            // 转换的类型
                            type,
                            // 提取字段
                            ((Record) p).get(fieldName),
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

    /**
     * 重新加载权限树
     * ps:hutool 5.7.2 有bug，退回 5.7.1后正常
     * @param treeList
     * @param config
     */
    public static <T> List<T> reloadTreeList(List<T> treeList, TreeNodeConfig config,Object rootId){
        List<Tree<Object>> build = TreeUtil.build(treeList, rootId, config, (object, tree) -> {
            // 也可以使用 tree.setId(object.getId());等一些默认值
            Field[] fields = ReflectUtil.getFieldsDirectly(object.getClass(), true);
            for (Field field : fields) {
                String fieldName = field.getName();
                Object fieldValue = ReflectUtil.getFieldValue(object, field);
                tree.putExtra(fieldName, fieldValue);
            }
        });
        Class clazz = treeList.get(0).getClass();
        List<T> resultList = new ArrayList<>();
        //
        for(Tree<Object> tree : build){
            resultList.add( (T)BeanUtil.fillBeanWithMap(tree, ReflectUtil.newInstanceIfPossible(clazz), false) );
        }
        return resultList;
    }


    /**
     *
     *@Title:  ListToArray
     *@Description: list列表转换成二维数组
     *@Author: 杨志佳
     *@Since: 2021年10月12日下午7:01:25
     *@param: @param list
     *@param: @param KeyLenght每个map的key数即列数，按最长的计算
     *@param: @return
     *@return Object[][]
     */
    public static Object[][] ListToArray(List<Map<String, Object>> list, int KeyLenght) {
        if (CollectionUtils.isEmpty(list)) {
            return new Object[0][];
        }
        int size = list.size();
        Object[][] array = new Object[size][KeyLenght];
        for (int i = 0; i < size; i++) {//循环遍历所有行
            array[i] = list.get(i).values().toArray();//每行的列数
        }
        return array;
    }



}
