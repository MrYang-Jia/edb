
package com.edbplus.db.dialect;

import com.jfinal.plugin.activerecord.dialect.MysqlDialect;

/**
 * @ClassName EDbMysqlDialect
 * @Description: mysql方言解析器
 * @Author 杨志佳
 * @Date 2021/11/16
 * @Version V1.0
 **/
public class EDbMysqlDialect extends MysqlDialect {
    /**
     * 改写sql统计语句，避免内部包含除了 order By 外未被优化后影响统计性能和结果相关的关键字
     * @param select
     * @param sqlExceptSelect
     * @param ext
     * @return
     */
    public String forPaginateTotalRow(String select, String sqlExceptSelect, Object ext) {
        StringBuilder totalRowSqlBuilder = new StringBuilder(64);
        // 再套1层，避免优化掉 order 排序影响性能的部分，但是内部包含 group ，会变成代码级别的统计模式，但是建议可以改成内外嵌套，内部改为 字段为 1 ，交给数据库统计性能也能非常不错
        totalRowSqlBuilder.append("select count(1) ct from ( select 1 ").append(this.replaceOrderBy(sqlExceptSelect)).append(") as ct_tb_0 ");
        return totalRowSqlBuilder.toString();
    }
}
