package com.edbplus.db.query.lambda;

import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * @ClassName LambdaGroupQuery
 * @Description: LambdaGroupQuery
 * @Author 杨志佳
 * @Date 2022/4/5
 * @Version V1.0
 **/
public interface LambdaGroupQuery<T> {
    public  LambdaHavingQuery<T> having(String havingSql);
    /**
     * order by column asc
     * @param funcs
     * @return
     */
    public  LambdaOrderQuery<T> orderByAsc(EDbColumnFunc<T, ?>... funcs);

    /**
     * order by column desc
     * @param funcs
     * @return
     */
    public  LambdaOrderQuery<T> orderByDesc(EDbColumnFunc<T, ?>... funcs);

    /**
     * limit count
     * @param limitCount
     * @return
     */
    public  LambdaLimitQuery<T> limit(int limitCount);

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
}
