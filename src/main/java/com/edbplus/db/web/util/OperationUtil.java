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
package com.edbplus.db.web.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.edbplus.db.util.hutool.bean.EBeanUtil;
import com.edbplus.db.web.shiro.ShiroUser;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @program: coreframework-parent
 * @description: 操作行为工具类
 * @author: 杨志佳
 * @create: 2020-12-17 14:12
 **/
@Slf4j
public class OperationUtil {

    // 定义 逻辑删除 remove 参数
    public final static String removeFlag = "removeFlag";
    // 定义 创建人 creator 参数
    public final static String creator = "creator";
    // 定义 创建时间 createTime 参数
    public final static String createTime = "createTime";
    // 定义 修改人 modifier 参数
    public final static String modifier = "modifier";
    // 定义 修改时间 modifyTime 参数
    public final static String modifyTime = "modifyTime";


    /**
     * 加载伪删除信息
     * @param jpaObj
     * @param shiroUser -- 操作人信息
     */
    public static void loadJpaDeletion(Object jpaObj,ShiroUser shiroUser){
        // 将对象转换成map
        Map<String,Object> dataMap = EBeanUtil.beanToMap(jpaObj);
        // 如果逻辑删除字段未设定，则设定为否
        dataMap.put(removeFlag,"Y");
        // 判断 修改人 是否存在
        if(dataMap.containsKey(modifier)){
            // 判断用户是否有值
            if(shiroUser!=null) {
                dataMap.put(modifier, shiroUser.getUserId());
            }else{
                // 微服务 或者 job 层调用时无法取到用户信息
                dataMap.put(modifier, 0);
            }
        }
        // 判断 修改时间 是否存在
        if(dataMap.containsKey(modifyTime)){
            dataMap.put(modifyTime, DateUtil.date());
        }
        // 重新赋值给对象
        EBeanUtil.fillBeanWithMap(dataMap,jpaObj,true);
    }

    /**
     * 加载jpa校验
     * @param jpaObj
     * @param shiroUser -- 操作人信息
     */
    public static void loadJpa(Object jpaObj, ShiroUser shiroUser){

        // 将对象转换成map
        Map<String,Object> dataMap = EBeanUtil.beanToMap(jpaObj);
        // 判断是否存在 removeFlag 参数
        if(dataMap.containsKey(removeFlag)){
            if(dataMap.get(removeFlag)==null || String.valueOf(dataMap.get(removeFlag)).length()==0){
                // 如果逻辑删除字段未设定，则设定为否
                dataMap.put(removeFlag,"N");
            }
        }
        // 判断 创建人信息是否存在
        if(dataMap.containsKey(creator)){
            if(dataMap.get(creator)==null){
                // 判断用户是否有值
                if(shiroUser!=null){
                    // 如果逻辑删除字段未设定，则设定为否
                    dataMap.put(creator,shiroUser.getUserId());
                }else{
                    // 如果逻辑删除字段未设定，则设定为否
                    dataMap.put(creator,0);
                }

            }
        }

        // 判断 创建时间 是否存在
        if(dataMap.containsKey(createTime)){
            if(dataMap.get(createTime)==null){
                // 如果逻辑删除字段未设定，则设定为否
                dataMap.put(createTime, DateUtil.date());
            }
        }

        // 判断 修改人 是否存在
        if(dataMap.containsKey(modifier)){
            // 判断用户是否有值
            if(shiroUser!=null) {
                dataMap.put(modifier, shiroUser.getUserId());
            }else{
                dataMap.put(modifier, 0);
            }
        }

        // 判断 修改时间 是否存在
        if(dataMap.containsKey(modifyTime)){
           dataMap.put(modifyTime, DateUtil.date());
        }

        // 重新赋值给对象
        EBeanUtil.fillBeanWithMap(dataMap,jpaObj,true);

    }




}
