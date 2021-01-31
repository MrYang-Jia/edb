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
            while (iterator.hasNext()){
                ConstraintViolation<Object> constraint = (ConstraintViolation<Object>)iterator.next();
                FieldError fieldError = new FieldError(object.getClass().getSimpleName(),constraint.getPropertyPath().toString(),constraint.getMessage());
                validateList.add(fieldError);
            }
        }
        // 返回校验结果
        return validateList;
    }

}
