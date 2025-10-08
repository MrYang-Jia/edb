package com.edbplus.db.query.lambda;

import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * @ClassName LambdaHavingQuery
 * @Description: LambdaHavingQuery
 * @Author 杨志佳
 * @Date 2022/4/5
 * @Version V1.0
 **/
public interface LambdaHavingQuery<T> {
    /**
     * order by column asc (无条件版本)
     * @param funcs - 对象字段方法
     * @return LambdaOrderQuery<T>
     */
    public  LambdaOrderQuery<T> orderByAsc(EDbColumnFunc<T, ?>... funcs);

    /**
     * order by column asc (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 排序字段
     * @return LambdaOrderQuery<T>
     */
    public  LambdaOrderQuery<T> orderByAsc(boolean condition, EDbColumnFunc<T, ?>... funcs);

    /**
     * order by column desc (无条件版本)
     * @param funcs
     * @return LambdaOrderQuery<T>
     */
    public  LambdaOrderQuery<T> orderByDesc(EDbColumnFunc<T, ?>... funcs);

    /**
     * order by column desc (条件版本)
     * @param condition - 是否应用此设置
     * @param funcs - 排序字段
     * @return LambdaOrderQuery<T>
     */
    public  LambdaOrderQuery<T> orderByDesc(boolean condition, EDbColumnFunc<T, ?>... funcs);

    /**
     * limit count (无条件版本)
     * @param limitCount -- 总条数
     * @return LambdaLimitQuery<T>
     */
    public  LambdaLimitQuery<T> limit(int limitCount);

    /**
     * limit count (条件版本)
     * @param condition - 是否应用此设置
     * @param limitCount
     * @return mbdaLimitQuery<T>
     */
    public  LambdaLimitQuery<T> limit(boolean condition, int limitCount);

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
}