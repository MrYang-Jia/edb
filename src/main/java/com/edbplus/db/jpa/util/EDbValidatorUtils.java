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
package com.edbplus.db.jpa.util;



import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * hibernate-validator校验工具类
 *
 * 参考文档：https://docs.jboss.org/hibernate/validator/7.0/reference/en-US/html_single/
 *
 */
public class EDbValidatorUtils {
    //
    public static Validator validator;

    // 加载类时进行初始化
    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }



    /**
     * 校验对象并返回校验结果
     * @param object
     * @param groups
     * @return
     * @throws RuntimeException
     */
    public static List<FieldError> validated(Object object, Class<?>... groups)
            throws RuntimeException {
        List<FieldError> validateList =  new ArrayList<>();
        // 校验结果集
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        // 判断校验集是否有信息，有则解析
        if (!constraintViolations.isEmpty()) {
            Iterator iterator = constraintViolations.iterator();
            ConstraintViolation<Object> constraint = null;
            FieldError fieldError = null;
            while (iterator.hasNext()){
                constraint = (ConstraintViolation<Object>)iterator.next();
                fieldError = new FieldError(object.getClass().getSimpleName(),constraint.getPropertyPath().toString(),constraint.getMessage());
                validateList.add(fieldError);
            }
        }
        // 返回校验结果
        return validateList;
    }

}
