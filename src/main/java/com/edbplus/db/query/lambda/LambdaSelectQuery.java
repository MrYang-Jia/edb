package com.edbplus.db.query.lambda;

import javax.persistence.Column;
import java.util.List;

/**
 * @ClassName LambdaSelectQuery
 * @Description: LambdaSelectQuery
 * @Author 杨志佳
 * @Date 2022/4/6
 * @Version V1.0
 **/
public interface LambdaSelectQuery<T> extends LambdaQuery<T>{

    /**
     * 设置查询的字段
     * @param funcs
     * @return
     */
    public LambdaQuery<T> select(EDbColumnFunc<T, ?>... funcs);


    /**
     * 设置查询的字段
     * @param coulumns
     * @return
     */
    public LambdaQuery<T> select(String coulumns);



}
