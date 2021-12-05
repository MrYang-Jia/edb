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

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName EDbSelectUtil
 * @Description: select 解析器工具类
 * @Author 杨志佳
 * @Date 2021/5/31
 * @Version V1.0
 **/
public class EDbSelectUtil {


    /**
     * 是否是关键字校验
     * @param sql - sql语句
     * @param keyWord - 关键字
     * @param keyIdx - 关键字位置
     * @return
     */
    public static boolean checkKeyWordFromSql(String sql,String keyWord,int keyIdx){
        String leftIdxStr = null;
        if(keyIdx<=0){
            leftIdxStr = "";
        }else{
            leftIdxStr = sql.substring(keyIdx-1,keyIdx);// 关键字左侧字符串
        }
        String rightIdxStr = sql.substring(keyIdx + keyWord.length(),keyIdx + keyWord.length() +1); // 获取关键字长度+1右侧的字符
        // 判断特殊字符 空格 制表符 换行符 回车 都认为是操作指令前的步骤
        if(checkSpecialCharacters(leftIdxStr,rightIdxStr)){
            return true;
        }
        return false;
    }

    /**
     * 是否是关键字，以左右两侧的首字符做判断
     * @param leftIdxStr
     * @param rightIdxStr
     * @return
     */
    public static boolean  checkSpecialCharacters(String leftIdxStr,String rightIdxStr){
        // 回车 、 换行 、 制表符 、空格
        if(leftIdxStr.equals("") || leftIdxStr.indexOf("\n")>-1 || leftIdxStr.indexOf("\r")>-1 || leftIdxStr.indexOf(" ")>-1 || leftIdxStr.indexOf("\t")>-1) {
            if (rightIdxStr.indexOf("\n") > -1 || leftIdxStr.indexOf("\r") > -1 || rightIdxStr.indexOf(" ") > -1 || rightIdxStr.indexOf("\t") > -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 修改原语句并返回limitSql
     * @param sql -- 原语句
     * @param limitCount -- 返回条数，ps:当用户自己的sql结尾含有 limit xxx 时，以用户自己输入的为准
     * @return
     */
    public static String returnLimitSql(String sql,int limitCount){
        String sqlLower = sql.toLowerCase();
        int lastIdx = sqlLower.lastIndexOf("limit");
        if(lastIdx > -1){ // 可能存在 limit 关键字
            String leftIdxStr = sqlLower.substring(lastIdx-1,lastIdx); // 关键字左侧字符串
            String rightIdxStr = sqlLower.substring(lastIdx + 5,lastIdx + 6); // limit 长度为5,关键字右侧字符串
            // 判断特殊字符 空格 制表符 换行符 回车 都认为是操作指令前的步骤
            if(checkSpecialCharacters(leftIdxStr,rightIdxStr)){ // 确认存在limit关键字
                String lastSql =  sqlLower.substring(lastIdx,sql.length()); // 最后尾部 limit(包含) 右侧的字符串
                // 内部limit函数，则都会在右侧嵌套 ) 以表示结束，所以只要判断这个，就可以在外围加 limit
                if(lastSql.indexOf(")") > -1){
                    return sql + " limit " + limitCount;
                }else{
                    int offsetIdx =  lastSql.indexOf("offset"); // 最后一个 limit 右侧首个 offset 结尾的情况，可能存在换行符等
                    if(offsetIdx > -1){// 可能存在 offset 关键字
                        leftIdxStr = lastSql.substring(offsetIdx-1,offsetIdx);// 关键字左侧字符串
                        rightIdxStr = lastSql.substring(offsetIdx + 6,offsetIdx + 7); // offset 长度为6,关键字右侧字符串
                        // 判断特殊字符 空格 制表符 换行符 回车 都认为是操作指令前的步骤
                        if(checkSpecialCharacters(leftIdxStr,rightIdxStr)){ // 确认是否存在 offset 关键字
                            String offsetSql = lastSql.substring(offsetIdx,lastSql.length());
                            return sql.substring(0,lastIdx) + " limit " + limitCount +" " + offsetSql;
                        }
                    }
                    int limitFilterIdx = lastSql.indexOf(",");//特殊符号，一般是不会有什么特殊的场景，所以直接切割即可
                    if(limitFilterIdx > -1){ // mysql 之 limit 0,10 转为 limit 0,limitCount
                        String limitFilterPreSql = lastSql.substring(0,limitFilterIdx);
                        return sql.substring(0,lastIdx) +  limitFilterPreSql + "," + limitCount;
                    }
                    return sql.substring(0,lastIdx) + " limit " + limitCount;
                }
            }
        }
        return sql + " limit " + limitCount; // 不存在limit则直接拼接
//        if(sql.contains("::")){ // 如果非正常sql语句，包含 :: 特殊符号，则无法正常转换，druid并没有兼容该语法解析
//            if(sql.indexOf("limit") == -1){
//                return sql + " limit " + limitCount;
//            }
//        }else{
//            // 获取sql实例对象 -- sql语句本身
//            SQLSelectStatement sqlStatement = selectStatement(sql);
//            // 避免解析语句的limit对象为null，则填充
//            if(sqlStatement.getSelect().getFirstQueryBlock().getLimit() == null){
//                SQLLimit sqlLimit = new SQLLimit();
//                sqlStatement.getSelect().getFirstQueryBlock().setLimit(sqlLimit);
//            }
//            // sql解析
//            SQLExpr limitExpr = sqlStatement.getSelect().getFirstQueryBlock().getLimit().getRowCount();
//            // 修改标志 -- 用户如果有自己控制limit，则由用户自己控制返回个数
//            Boolean changeType = true;
//            if(limitExpr != null){
//                changeType = false;
//            }
//            // 设置返回的记录集
//            if(changeType){
//                sqlStatement.getSelect().getFirstQueryBlock().getLimit().setRowCount(limitCount);
//            }
//            return sqlStatement.getSelect().toString();
//        }
//        return sql; // 其他情况比较复杂，则不做优化，避免优化过度，导致更多异常问题
    }

    /**
     * select语句实例对象
     * @param sql
     * @return
     */
    public static SQLSelectStatement selectStatement(String sql){
        // sql 装载容器 --  使用Parser解析生成AST
        SQLStatementParser parser = new SQLStatementParser(sql);
        // 获取sql实例对象 -- sql语句本身
        SQLSelectStatement sqlStatement = (SQLSelectStatement) parser.parseStatement();
        return sqlStatement;
    }


    /**
     * 移除 order 关键字相关的语法
     * @param sql
     * @return
     */
    public static String removeOrder(String sql){
        String sqlLower = sql.toLowerCase();
        int lastIdx = sqlLower.lastIndexOf("order");
        if(lastIdx > -1) { // 可能存在 order 关键字
            String leftIdxStr = sqlLower.substring(lastIdx-1,lastIdx); // 关键字左侧字符串
            String rightIdxStr = sqlLower.substring(lastIdx + 5,lastIdx + 6); // limit 长度为5,关键字右侧字符串
            // 判断特殊字符 空格 制表符 换行符 回车 都认为是操作指令前的步骤
            if(checkSpecialCharacters(leftIdxStr,rightIdxStr)){ // 存在关键字 order
                // 由于 order 的场景比较特殊
                String lastSql =  sqlLower.substring(lastIdx,sql.length()); // 最后尾部 limit(包含) 右侧的字符串
                // 内部limit函数，则都会在右侧嵌套 ) 以表示结束，所以只要判断这个，就可以在外围加 limit
                if(lastSql.indexOf(")") > -1){ // 可能存在 order 关键字
                    // in | exist (select xxx from xxx order by xxx) 场景模式需要过滤
                    String leftLastSql = sqlLower.substring(0,lastIdx); // 截取左侧sql
                    int selectIdx = leftLastSql.lastIndexOf("select"); // order 左侧的 "最后一个" select 位置
                    // 校验select是否是关键字
                    leftIdxStr = leftLastSql.substring(selectIdx-1,selectIdx); // 关键字左侧字符串
                    rightIdxStr = leftLastSql.substring(selectIdx + 6,selectIdx + 7);
                    if(checkSpecialCharacters(leftIdxStr,rightIdxStr)) { // 存在关键字 select
                        // 说明是select关键字,接下来判断 select 左侧是否存在 "(" 如果是，则成立场景  in | exist (select xxx from xxx order by xxx)
                        leftLastSql = leftLastSql.substring(0,selectIdx);
                        //
                        if(leftLastSql.indexOf("(") > -1){
                            return sql; // sql 不需要截取
                        }
                    }
                }
                // 其他场景包含 ) ，一般是  order by (case when xxx then xxx else xxx end) limit ?
                int limitIdx = lastSql.lastIndexOf("limit"); // 最右侧的limit位置
                if(limitIdx > -1) {
                    leftIdxStr = lastSql.substring(limitIdx-1,limitIdx); // 关键字左侧字符串
                    rightIdxStr = lastSql.substring(limitIdx + 5,limitIdx + 6);
                    if(checkSpecialCharacters(leftIdxStr,rightIdxStr)) { // 存在关键字 limit
                        lastSql = lastSql.substring(limitIdx,lastSql.length()); // 去除 order ，保留 limit
                        return sqlLower.substring(0,lastIdx) + " " + lastSql;
                    }
                }
                // 说明只需要去除 order 即可
                return sqlLower.substring(0,lastIdx);
            }
        }
        return sql;

//
//        //TimeInterval timerIsRepetition = cn.hutool.core.date.DateUtil.timer();
//        StringBuilder newSql = new StringBuilder("");
//        SQLSelectStatement sqlSelectStatement = selectStatement(sql);
//        SQLSelect sqlSelect = sqlSelectStatement.getSelect();
//        //获取sql查询块
//        SQLSelectQueryBlock sqlSelectQuery = (SQLSelectQueryBlock) sqlSelect.getQuery();
//        StringBuffer out = new StringBuffer();
//        //创建sql解析的标准化输出
//        SQLASTOutputVisitor sqlastOutputVisitor = SQLUtils.createFormatOutputVisitor(out, null,null);
//        out.delete(0, out.length()); // 置空
//        for (SQLSelectItem sqlSelectItem : sqlSelectQuery.getSelectList()) {
//            if (out.length() > 1) {
//                out.append(",");
//            }
//            sqlSelectItem.accept(sqlastOutputVisitor);
//        }
//        newSql.append("SELECT " ).append(out); // 输出 select xxxx
//        out.delete(0, out.length());  // 置空
//        sqlSelectQuery.getFrom().accept(sqlastOutputVisitor); // from xxxx
//        newSql.append(" FROM " ).append(out);
//        out.delete(0, out.length()); // 置空
//        if(sqlSelectQuery.getWhere()!=null){
//            sqlSelectQuery.getWhere().accept(sqlastOutputVisitor);
//            newSql.append(" WHERE ").append(out); // where xxxx
//        }
//        out.delete(0, out.length()); // 置空
//        if(sqlSelectQuery.getGroupBy() != null){
//            sqlSelectQuery.getGroupBy().accept(sqlastOutputVisitor);
//            newSql.append(" ").append( out ); // group by xxxx
//        }
//        sqlSelectStatement = null;
//        //System.out.println("耗时:"+timerIsRepetition.intervalMs());
//        return newSql.toString();
    }




}
