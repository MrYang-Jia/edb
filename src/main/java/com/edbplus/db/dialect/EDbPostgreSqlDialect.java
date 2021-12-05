/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.dialect;

import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;

/**
 * @ClassName EDbPostgreSqlDialect
 * @Description: postgresql 方言解析器
 * @Author 杨志佳
 * @Date 2021/11/16
 * @Version V1.0
 **/
public class EDbPostgreSqlDialect extends PostgreSqlDialect {

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
        totalRowSqlBuilder.append("select count(1) ct from ( ").append(DialectTool.select_1 + replaceOrderBy(sqlExceptSelect)).append(") as ct_tb_0 ");
        return totalRowSqlBuilder.toString();
    }

    /**
     * 去除 order by 关键词
     * @param sql
     * @return
     */
    public String replaceOrderBy(String sql) {
        try{
            return DialectTool.replaceOrderBy(sql);
        }catch (Throwable e){// 请注意这个是获取父级的方法解析，所以无法单独写到工具类里
            return super.replaceOrderBy(sql); // 报错时使用原来的解析方式，以此兼容
        }
    }
}
