package com.edbplus.db.web.util;

import lombok.extern.slf4j.Slf4j;

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
    // 动态查询参数
    public final static String sEqRemoveFlag ="s_eq_removeFlag";
    // 设置layui成功码
    public final static int layUiSuccessCode = 0;

    // ==================================================
}
