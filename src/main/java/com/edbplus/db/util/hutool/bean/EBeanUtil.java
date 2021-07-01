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
package com.edbplus.db.util.hutool.bean;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

/**
 * @ClassName EBeanUtil
 * @Description: BeanUtil 工具类扩展 -- 原因，避免hutool工具升级，影响到正常服务
 * @Author 杨志佳
 * @Date 2021/6/28
 * @Version V1.0
 **/
public class EBeanUtil extends BeanUtil {

    /**
     * 拷贝对象属性，忽略转换异常，如果字段不一样，则忽略转换该字段
     * @param source
     * @param target
     */
    public static void copyProperties(Object source, Object target) {
        // 忽略转换异常
        copyProperties(source, target, CopyOptions.create().setIgnoreError(true));
    }
}
