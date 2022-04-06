package com.edbplus.db.query.lambda;

import com.edbplus.db.query.EDbFilter;

import javax.persistence.Column;

/**
 * @ClassName LambdaBaseQuery
 * @Description: 对外暴露的基础接口Api
 * @Author 杨志佳
 * @Date 2022/4/5
 * @Version V1.0
 **/
public interface LambdaBaseQuery<T> {


    public LambdaBaseQuery<T> or();

    /**
     * 连接操作符转变成 and
     * @return
     */
    public LambdaBaseQuery<T> and();

    /**
     * 小于 <
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> lt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <=
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> le(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> eq(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> ne(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> in(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> notIn(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> gt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> ge(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> like(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> notLike(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> likeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> likeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value);


    /**
     * 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaBaseQuery<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaBaseQuery<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * exists
     * @param existsSql
     * @return
     */
    public LambdaBaseQuery<T> exists(String existsSql);

    /**
     * not exists
     * @param existsSql
     * @return
     */
    public LambdaBaseQuery<T> notExists(String existsSql);

    /**
     * is null
     * @param func
     * @return
     */
    public LambdaBaseQuery<T> isNull(EDbColumnFunc<T, ?> func);

    /**
     * is not null
     * @param func
     * @return
     */
    public LambdaBaseQuery<T> isNotNull(EDbColumnFunc<T, ?> func);
}
