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

    /**
     * 连接操作符转变成 or (无条件版本)
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> or();

    /**
     * 连接操作符转变成 or (条件版本)
     * @param condition - 是否切换到 or 模式
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> or(boolean condition);

    /**
     * 连接操作符转变成 and (无条件版本)
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> and();

    /**
     * 连接操作符转变成 and (条件版本)
     * @param condition - 是否切换到 and 模式
     * @return
     */
    public LambdaBaseQuery<T> and(boolean condition);

    /**
     * 小于 < (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> lt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于 < (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> lt(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <= (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> le(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <= (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> le(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于 (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> eq(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> eq(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于 (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> ne(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> ne(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * IN (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> in(EDbColumnFunc<T, ?> func, Object value);

    /**
     * IN (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> in(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * NOT IN (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notIn(EDbColumnFunc<T, ?> func, Object value);

    /**
     * NOT IN (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notIn(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于 (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> gt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> gt(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于 (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> ge(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> ge(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配% (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> like(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> like(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配% (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> notLike(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> notLike(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配% (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> likeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaBaseQuery<T> likeLeft(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配% (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notLikeLeft(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配 (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> likeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> likeRight(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %右匹配 (无条件版本)
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %右匹配 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notLikeRight(boolean condition, EDbColumnFunc<T, ?> func, Object value);


    /**
     * 区间 (无条件版本)
     * @param func
     * @param begin
     * @param end
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * 区间 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param begin
     * @param end
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> between(boolean condition, EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间 (无条件版本)
     * @param func
     * @param begin
     * @param end
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param begin
     * @param end
     * @return LambdaBaseQuery<T>
     */
    public LambdaBaseQuery<T> notBetween(boolean condition, EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * exists (无条件版本)
     * @param existsSql
     * @return
     */
    public LambdaBaseQuery<T> exists(String existsSql);

    /**
     * exists (条件版本)
     * @param condition - 是否添加此条件
     * @param existsSql
     * @return
     */
    public LambdaBaseQuery<T> exists(boolean condition, String existsSql);

    /**
     * not exists (无条件版本)
     * @param existsSql
     * @return
     */
    public LambdaBaseQuery<T> notExists(String existsSql);

    /**
     * not exists (条件版本)
     * @param condition - 是否添加此条件
     * @param existsSql
     * @return
     */
    public LambdaBaseQuery<T> notExists(boolean condition, String existsSql);

    /**
     * is null (无条件版本)
     * @param func
     * @return
     */
    public LambdaBaseQuery<T> isNull(EDbColumnFunc<T, ?> func);

    /**
     * is null (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @return
     */
    public LambdaBaseQuery<T> isNull(boolean condition, EDbColumnFunc<T, ?> func);

    /**
     * is not null (无条件版本)
     * @param func
     * @return
     */
    public LambdaBaseQuery<T> isNotNull(EDbColumnFunc<T, ?> func);

    /**
     * is not null (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @return
     */
    public LambdaBaseQuery<T> isNotNull(boolean condition, EDbColumnFunc<T, ?> func);
}