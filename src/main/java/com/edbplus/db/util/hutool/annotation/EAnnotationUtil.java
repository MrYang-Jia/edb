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
package com.edbplus.db.util.hutool.annotation;

import cn.hutool.core.annotation.AnnotationUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @ClassName EAnnotationUtil
 * @Description: AnnotationUtil 工具类扩展 -- 原因，避免hutool工具升级，影响到正常服务
 * @Author 杨志佳
 * @Date 2021/6/25
 * @Version V1.0
 **/
public class EAnnotationUtil extends AnnotationUtil {

    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotationEle, Class<A> annotationType) {
        return AnnotationUtil.getAnnotation(annotationEle,annotationType);
    }
}
