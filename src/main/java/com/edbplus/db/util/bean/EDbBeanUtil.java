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
package com.edbplus.db.util.bean;

import cn.hutool.core.bean.BeanUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EDbBeanUtil {

    /**
     * bean对象转换map -- 包括内部不属于java基础对象的对象，也需要全部转换成map
     * @param object
     * @return
     */
    public static Map<String,Object> beanToMap(Object object){
        //
        Map<String,Object> map = BeanUtil.beanToMap(object);
        reloadMapToMap(map);
        return map;
    }

    /**
     * 循环加载非基础类型的对象全部转换成 Map; 正常来说，最多只有2层需要转换
     * @param map
     */
    public static void reloadMapToMap(Map<String,Object> map){
        Map<String,Object> oMap = null;
        for (Map.Entry<String, Object> m : map.entrySet()) {
            if(m.getValue() != null ){
                // 时间搓不转换
                if(m.getValue() instanceof Date){
                    // 不做任何处理
                }else{
                    // 非java基础对象
                    if(
                            // 判断不是 java 工具类对象
                            m.getValue().getClass().getTypeName().indexOf("java") != 0
                            // 判断不是 sun  的工具类对象
                            && m.getValue().getClass().getTypeName().indexOf("sun") != 0
                    ){
                        oMap = BeanUtil.beanToMap(m.getValue());
                        // 替换Map
                        m.setValue(oMap);
                        // 再次执行内循环，如果还存在非java基础对象类型的，则再转换一次
                        reloadMapToMap(oMap);
                    }
                }

            }
        }
    }
}
