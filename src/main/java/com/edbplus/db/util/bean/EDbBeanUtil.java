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

import com.edbplus.db.util.hutool.bean.EBeanUtil;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EDbBeanUtil {

    /**
     * bean对象转换map -- 包括内部不属于java基础对象的对象，也需要全部转换成map
     * @param object
     * @return
     */
    public static Map<String,Object> beanToMap(Object object){
        //
        Map<String,Object> map = EBeanUtil.beanToMap(object);
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
                        oMap = EBeanUtil.beanToMap(m.getValue());
                        // 替换Map
                        m.setValue(oMap);
                        // 再次执行内循环，如果还存在非java基础对象类型的，则再转换一次
                        reloadMapToMap(oMap);
                    }
                }

            }
        }
    }


    public static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    public static Class<?> getClass(String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("类名不能为空");
        }

        // 先从缓存获取
        return classCache.computeIfAbsent(className, key -> {
            try {
                // 优先使用线程上下文类加载器
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader == null) {
                    // 回退到当前类的类加载器
                    classLoader = EDbBeanUtil.class.getClassLoader();
                }
                return Class.forName(key, false, classLoader);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("无法加载类: " + key, e);
            }
        });
    }

    // 清理缓存的方法（可选）
    public static void clearClassCache() {
        classCache.clear();
    }

    // 移除特定类的缓存（可选）
    public static void removeClassFromCache(String className) {
        classCache.remove(className);
    }
}
