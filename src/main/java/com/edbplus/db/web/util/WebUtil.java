package com.edbplus.db.web.util;

import com.edbplus.db.query.EDbFilterKit;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.web.api.ApiReturnCode;
import com.edbplus.db.web.api.DataTablePageResult;
import com.jfinal.plugin.activerecord.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @program: coreframework-parent
 * @description: web层工具类
 * @author: 杨志佳
 * @create: 2020-12-17 17:12
 **/
@Slf4j
public class WebUtil {
    // =====  page的limit参数返回等级定义 ==========
    // 最多返回条数 -- 等级1
    public final static int maxLimitLv_1 = 30;
    // excel 导出的最大条数设置
    public final static int maxLimitLv_2 = 50;

    public final static int maxLimitLv_3 = 100;

    public final static int maxLimitLv_4 = 1000;

    public final static int maxLimitLv_5 = 2000;

    public final static int maxLimitLv_6 = 5000;

    public final static int maxLimitLv_7 = 10000;

    public final static int maxLimitLv_8 = 20000;

    public final static int maxLimitLv_9 = 50000;

    // ============= 动态查询参数定义===================
    // 动态查询排序参数指定字段
    public final static String field = "field";
    // 动态查询排序参数指定排序方式
    public final static String order = "order";
    // 动态查询排序参数指定页数
    public final static String page = "page";
    // 动态查询排序参数指定返回条数
    public final static String limit = "limit";
    // 动态查询参数 -- 可根据各自系统的情况自定义
    public static String sEqRemoveFlag ="s_eq_removeFlag";
    // 默认排序条件
    public static String defaultOrerStr = "desc";
    // 设置自定义系统逻辑删除默认值 -- 可修改
    public static String sEqRemoveFlagDefaultValue = "0";
    // 设置layui成功码
    public static int layUiSuccessCode = 0;
    // 可根据自己的系统设置最大统一返回条数
    public static int maxLimit = maxLimitLv_1;
    // 可自定义超出最大 maxLimit 的返回条数时，统一返回的条数配置
    public static int overFlowLimit = 5;

    // ==================================================


    /**
     * 统一过滤查询条件，避免有非法的查询的信息带入
     * @param className
     * @param idName
     * @param orderStr
     * @param whereMap
     */
    public static void filterWhereMap(Class className,Object idName,String orderStr, Map<String,Object> whereMap){
        // 去除空指针对象 -- 前端传递到后台的，默认都会携带空指针，导致查询数据的时候会携带该信息，所以统一去除
        EDbFilterKit.removeNullValue(whereMap);
        // 如果没有默认排序字段，则赋予主键字段进行排序
        if(!whereMap.containsKey(WebUtil.field)){
            whereMap.put(WebUtil.field,idName);
        }
        // 排序方式
        if(orderStr!=null){
            whereMap.put(WebUtil.order,orderStr);
        }else{
            whereMap.put(WebUtil.order,WebUtil.defaultOrerStr);
        }
        int pageCt = 1;
        // 判断是否有分页对象
        if(whereMap.containsKey(WebUtil.page)){
            pageCt = Integer.valueOf((String) whereMap.get(WebUtil.page));
        }
        // 重置基础变量的数据类型，前端可能是字符串
        whereMap.put(WebUtil.page,pageCt);
        int limitCt = 10;
        // 判断是否有返回条数设置
        if(whereMap.containsKey(WebUtil.limit)) {
            limitCt = Integer.valueOf((String) whereMap.get(WebUtil.limit));
        }
        // 如果超过最大等级，则直接返回5条信息。
        if(limitCt > WebUtil.maxLimit){
            // 可自定义静态变量调整统一返回参数
            limitCt = WebUtil.overFlowLimit;
        }
        // 重置基础变量的数据类型，前端可能是字符串
        whereMap.put(WebUtil.limit,limitCt);

        // 设置只查询未删除的部分 -- 自定义系统自带属性，可自定义，null的话，则不需要标记
        if(WebUtil.sEqRemoveFlag!=null){
            whereMap.put(WebUtil.sEqRemoveFlag,WebUtil.sEqRemoveFlagDefaultValue);
        }
    }

    /**
     * 加载返回前端的分页结果
     * @param page
     * @param <T>
     * @return
     */
    public static<T> DataTablePageResult<T> loadDataTablePageResult(Page page){
        // 数据对象封装 -- 适配LayUI
        DataTablePageResult<T> dataTablePageResult = new DataTablePageResult<T>();
        // 设置返回的数据对象
        dataTablePageResult.setData(page.getList());
        // 设置成功的返回码 -- 适配LayUI
        dataTablePageResult.setCode(ApiReturnCode.SUCCESS.getCode());
        // 设置返回总条数
        dataTablePageResult.setCount(page.getTotalRow());
        return dataTablePageResult;
    }

}
