package com.edbplus.db.query.lambda;

import com.jfinal.plugin.activerecord.Page;

import java.util.List;

/**
 * @ClassName LambdaDbQuery
 * @Description: LambdaDbQuery
 * @Author 杨志佳
 * @Date 2022/4/5
 * @Version V1.0
 **/
public interface LambdaOffsetQuery<T> {

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
