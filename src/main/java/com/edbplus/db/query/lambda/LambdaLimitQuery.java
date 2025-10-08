package com.edbplus.db.query.lambda;

import java.util.List;

/**
 * @ClassName LambdaLimitQuery
 * @Description: LambdaLimitQuery
 * @Author 杨志佳
 * @Date 2022/4/5
 * @Version V1.0
 **/
public interface LambdaLimitQuery<T> {

    /**
     * offset offsetIdx (无条件版本)
     * @param offsetIdx
     * @return
     */
    public LambdaOffsetQuery<T> offset(int offsetIdx);

    /**
     * offset offsetIdx (条件版本)
     * @param condition - 是否应用此设置
     * @param offsetIdx
     * @return
     */
    public LambdaOffsetQuery<T> offset(boolean condition, int offsetIdx);

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

}