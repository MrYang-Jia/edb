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
package com.edbplus.db.druid;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;

/**
 * @ClassName EDbSelectUtil
 * @Description: select 解析器工具类
 * @Author 杨志佳
 * @Date 2021/5/31
 * @Version V1.0
 **/
public class EDbSelectUtil {


    /**
     * 返回limitSql
     * @param sql -- 原语句
     * @param limitCount -- 返回条数
     * @return
     */
    public static String returnLimitSql(String sql,int limitCount){
        // mysql 装载容器 --  使用Parser解析生成AST
        SQLStatementParser parser = new SQLStatementParser(sql);
        // 获取sql实例对象 -- sql语句本身
        SQLSelectStatement sqlStatement = (SQLSelectStatement) parser.parseStatement();
        // 避免解析语句的limit对象为null，则填充
        if(sqlStatement.getSelect().getFirstQueryBlock().getLimit() == null){
            SQLLimit sqlLimit = new SQLLimit();
            sqlStatement.getSelect().getFirstQueryBlock().setLimit(sqlLimit);
        }
        SQLExpr limitExpr = sqlStatement.getSelect().getFirstQueryBlock().getLimit().getRowCount();
        // 修改标志
        Boolean changeType = true;
        if(limitExpr != null){
            if("?".equals(String.valueOf(limitExpr))){
                changeType = false;
            }
        }
        // 设置返回的记录集
        if(changeType){
            sqlStatement.getSelect().getFirstQueryBlock().getLimit().setRowCount(limitCount);
        }
        return sqlStatement.getSelect().toString();
    }

}
