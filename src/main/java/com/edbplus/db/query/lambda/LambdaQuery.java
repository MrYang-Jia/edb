package com.edbplus.db.query.lambda;

import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * @ClassName LambdaQuery
 * @Description: 对外暴露的接口Api
 * @Author 杨志佳
 * @Date 2022/4/5
 * @Version V1.0
 **/
public interface LambdaQuery<T> extends LambdaBaseQuery<T>,LambdaGroupQuery<T>,LambdaHavingQuery<T>,LambdaOrderQuery<T>,LambdaLimitQuery<T>, LambdaOffsetQuery<T> {

    /**
     * and (...)
     * @param func
     * @return LambdaQuery<T>
     */
    public LambdaQuery<T> andCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func);

    /**
     * or (...)
     * @param func
     * @return
     */
    public LambdaQuery<T> orCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func);

    /**
     * groupBy (无条件版本)
     * @param funcs
     * @return LambdaGroupQuery<T>
     */
    public LambdaGroupQuery<T> groupBy(EDbColumnFunc<T, ?>... funcs);

    /**
     * groupBy (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 分组字段
     * @return LambdaGroupQuery<T>
     */
    public LambdaGroupQuery<T> groupBy(boolean condition, EDbColumnFunc<T, ?>... funcs);

    /**
     * having
     * @param havingSql
     * @return LambdaHavingQuery<T>
     */
    public  LambdaHavingQuery<T> having(String havingSql);

    /**
     * having
     * @param havingSql ->  count(c1) > ? and sum(c1) < ?
     * @param values
     * @return
     */
    public  LambdaHavingQuery<T> having(String havingSql,Object... values);



    // ===========================================================

    /**
     * 连接操作符转变成 or (无条件版本)
     * @return
     */
    public LambdaQuery<T> or();

    /**
     * 连接操作符转变成 or (条件版本)
     * @param condition - 是否切换到 or 模式
     * @return
     */
    public LambdaQuery<T> or(boolean condition);

    /**
     * 连接操作符转变成 and (无条件版本)
     * @return
     */
    public LambdaQuery<T> and();

    /**
     * 连接操作符转变成 and (条件版本)
     * @param condition - 是否切换到 and 模式
     * @return
     */
    public LambdaQuery<T> and(boolean condition);


    /**
     * 小于 < (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> lt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于 < (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> lt(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <= (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> le(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <= (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> le(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于 (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> eq(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> eq(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于 (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ne(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ne(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * IN (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> in(EDbColumnFunc<T, ?> func, Object value);

    /**
     * IN (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> in(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * NOT IN (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notIn(EDbColumnFunc<T, ?> func, Object value);

    /**
     * NOT IN (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notIn(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于 (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> gt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> gt(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于 (无条件版本)
     * @param func - 对象字段方法
     * @param value - 赋值
     * @return LambdaQuery<T>
     */
    public LambdaQuery<T> ge(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return LambdaQuery<T>
     */
    public LambdaQuery<T> ge(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配% (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> like(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> like(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配% (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLike(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLike(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配% (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeLeft(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配% (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配% (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeLeft(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配 (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeRight(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %右匹配 (无条件版本)
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %右匹配 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeRight(boolean condition, EDbColumnFunc<T, ?> func, Object value);


    /**
     * 区间 (无条件版本)
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * 区间 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> between(boolean condition, EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间 (无条件版本)
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间 (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> notBetween(boolean condition, EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * exists (无条件版本)
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> exists(String existsSql);

    /**
     * exists (条件版本)
     * @param condition - 是否添加此条件
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> exists(boolean condition, String existsSql);

    /**
     * not exists (无条件版本)
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> notExists(String existsSql);

    /**
     * not exists (条件版本)
     * @param condition - 是否添加此条件
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> notExists(boolean condition, String existsSql);

    /**
     * is null (无条件版本)
     * @param func
     * @return LambdaBaseQuery<T>
     */
    public LambdaQuery<T> isNull(EDbColumnFunc<T, ?> func);

    /**
     * is null (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @return LambdaBaseQuery<T>
     */
    public LambdaQuery<T> isNull(boolean condition, EDbColumnFunc<T, ?> func);

    /**
     * is not null (无条件版本)
     * @param func
     * @return LambdaBaseQuery<T>
     */
    public LambdaQuery<T> isNotNull(EDbColumnFunc<T, ?> func);

    /**
     * is not null (条件版本)
     * @param condition - 是否添加此条件
     * @param func
     * @return LambdaQuery<T>
     */
    public LambdaQuery<T> isNotNull(boolean condition, EDbColumnFunc<T, ?> func);

    /**
     * 返回查询列表
     * @return List<T>
     */
    public List<T> list();

    /**
     * 获取第一个对象
     * @return T
     */
    public T findFirst();

    /**
     * 获取第一个对象
     * @return T
     */
    public T one();

    /**
     * 返回条数
     * @param limitCount
     * @param offsetIdx
     * @return List<T>
     */
    public List<T> list(int limitCount,int offsetIdx);

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<T> page(int pageNum, int pageSize);

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @param totalSize
     * @return
     */
    public Page<T> page(int pageNum,int pageSize,int totalSize);

    /**
     * 获取查询统计结果
     * @return
     */
    public long count();

    LambdaQuery<T> arrAll(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    LambdaQuery<T> arrAll(EDbColumnFunc<T, ?> func, Object value);

    LambdaQuery<T> arrAny(boolean condition, EDbColumnFunc<T, ?> func, Object value);

    LambdaQuery<T> arrAny(EDbColumnFunc<T, ?> func, Object value);
}