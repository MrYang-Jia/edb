package com.edbplus.db.query.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @ClassName EDbColumnFunc
 * @Description: 用于字段解析
 * @Author 杨志佳
 * @Date 2022/4/2
 * @Version V1.0
 **/
public interface EDbColumnFunc<T, R> extends Function<T, R>, Serializable {

}
