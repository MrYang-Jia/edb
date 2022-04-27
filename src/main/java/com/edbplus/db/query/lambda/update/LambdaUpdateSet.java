package com.edbplus.db.query.lambda.update;

import com.edbplus.db.query.lambda.EDbColumnFunc;
import com.edbplus.db.query.lambda.LambdaBaseQuery;

/**
 * @ClassName LambdaUpdateSet
 * @Description: LambdaUpdateSet
 * @Author 杨志佳
 * @Date 2022/4/15
 * @Version V1.0
 **/
public interface LambdaUpdateSet<T> {
    /**
     * and (...)
     * @param func
     * @return
     */
    public LambdaUpdate<T> andCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func);

    /**
     * or (...)
     * @param func
     * @return
     */
    public LambdaUpdate<T> orCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func);


    // ===========================================================

    public LambdaUpdate<T> or();

    /**
     * 连接操作符转变成 and
     * @return
     */
    public LambdaUpdate<T> and();


    /**
     * 小于 <
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> lt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <=
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> le(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> eq(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> ne(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> in(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> notIn(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> gt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> ge(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> like(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> notLike(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> likeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> likeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdate<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value);


    /**
     * 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaUpdate<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaUpdate<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * exists
     * @param existsSql
     * @return
     */
    public LambdaUpdate<T> exists(String existsSql);

    /**
     * not exists
     * @param existsSql
     * @return
     */
    public LambdaUpdate<T> notExists(String existsSql);

    /**
     * is null
     * @param func
     * @return
     */
    public LambdaUpdate<T> isNull(EDbColumnFunc<T, ?> func);

    /**
     * is not null
     * @param func
     * @return
     */
    public LambdaUpdate<T> isNotNull(EDbColumnFunc<T, ?> func);


    /**
     * 更新字段
     * @param func
     * @param value
     * @return
     */
    public LambdaUpdateSet<T> set(EDbColumnFunc<T, ?> func, Object value); // 通过返回当前对象规避掉直接执行 update 的问题
}
