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
     * @return
     */
    public LambdaQuery<T> andCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func);

    /**
     * or (...)
     * @param func
     * @return
     */
    public LambdaQuery<T> orCom(EDbColumnFunc<LambdaBaseQuery<T>, ?> func);

    /**
     * groupBy
     * @param funcs
     * @return
     */
    public LambdaGroupQuery<T> groupBy(EDbColumnFunc<T, ?>... funcs);

    /**
     * having
     * @param havingSql
     * @return
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

    public LambdaQuery<T> or();

    /**
     * 连接操作符转变成 and
     * @return
     */
    public LambdaQuery<T> and();


    /**
     * 小于 <
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> lt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 小于等于 <=
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> le(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> eq(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 不等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ne(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> in(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notIn(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> gt(EDbColumnFunc<T, ?> func, Object value);

    /**
     * 大于等于
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> ge(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> like(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like %匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLike(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * not like 左匹配%
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeLeft(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> likeRight(EDbColumnFunc<T, ?> func, Object value);

    /**
     * like %右匹配
     * @param func
     * @param value
     * @return
     */
    public LambdaQuery<T> notLikeRight(EDbColumnFunc<T, ?> func, Object value);


    /**
     * 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> between(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * not 区间
     * @param func
     * @param begin
     * @param end
     * @return
     */
    public LambdaQuery<T> notBetween(EDbColumnFunc<T, ?> func, Object begin, Object end);

    /**
     * exists
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> exists(String existsSql);

    /**
     * not exists
     * @param existsSql
     * @return
     */
    public LambdaQuery<T> notExists(String existsSql);

    /**
     * is null
     * @param func
     * @return
     */
    public LambdaQuery<T> isNull(EDbColumnFunc<T, ?> func);

    /**
     * is not null
     * @param func
     * @return
     */
    public LambdaQuery<T> isNotNull(EDbColumnFunc<T, ?> func);

    /**
     * 返回查询列表
     * @return
     */
    public List<T> list();

    /**
     * 获取第一个对象
     * @return
     */
    public T findFirst();

    /**
     * 返回条数
     * @param limitCount
     * @param offsetIdx
     * @return
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
}
