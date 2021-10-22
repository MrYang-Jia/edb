/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.druid.filter;

import cn.hutool.core.util.ReUtil;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edbplus.db.util.hutool.date.EDateUtil;
import com.edbplus.db.util.hutool.str.EStrUtil;
import com.jfinal.kit.StrKit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * druid - sql日志打印过滤器
 */
@Slf4j
public class EDbDruidSqlLogFilter extends FilterEventAdapter {


    /**
     * 数据库类型 1-mysql,2-pg,3-gp,4-tidb
     */
    @Setter
    private int dbType = 1;


    // 只打印real-sql
    @Setter
    @Getter
    private boolean onlyRealsql = false;


    /**
     * 执行更新前
     * @param statement
     * @param sql
     */
    @Override
    protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
        // 打印sql
        sqlLog(statement);
    }

    /**
     * 执行更新后
     * @param statement
     * @param sql
     * @param updateCount
     */
    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        try {
            // 更新执行完成后触发
            addLastlLog();

        }catch (Throwable e){
            // 报送错误日志
            sendErrLog(e);
        }

    }


    /**
     * 执行查询前
     * @param statement
     * @param sql
     */
    @Override
    protected void statementExecuteQueryBefore(StatementProxy statement, String sql){
        // 打印sql
        sqlLog(statement);
    }

    /**
     * 执行查询完成后
     * @param statement
     * @param sql
     * @param resultSet
     */
    @Override
    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {

        try{
            // 执行查询完成后触发
            addLastlLog();
        }catch (Throwable e){
            // 报送错误日志
            sendErrLog(e);
        }


    }

    /**
     * 更新指令之前触发
     * @param statement
     * @param sql
     */
    @Override
    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        // 打印sql
        sqlLog(statement);
    }


    /**
     * 执行更新指令之后触发
     * @param statement
     * @param sql
     * @param result
     */
    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {
        try {
            // 执行更新指令之后触发
            addLastlLog();

        }catch (Throwable e)
        {
            // 报送错误日志
            sendErrLog(e);
        }

    }

    /**
     * 执行批量命令前
     * @param statement
     */
    @Override
    protected void statementExecuteBatchBefore(StatementProxy statement) {
        // 打印sql
        sqlLog(statement);
    }


    /**
     * 执行批量命令之后
     * @param statement
     * @param result
     */
    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        try {
            // 执行批量命令之后
            addLastlLog();
        }catch (Throwable e)
        {
            // 报送错误日志
            sendErrLog(e);
        }

    }


    /**
     * 执行sql报错之后触发
     * @param statement
     * @param sql
     * @param error
     */
    @Override
    protected void statement_executeErrorAfter(StatementProxy statement, String sql, Throwable error) {
        if(error.getMessage().contains("位置")){// pg异常报错时，只能捕获的方式来提示
            String errStr = error.getMessage();
            // 提取 【位置：81】 里的数字，便于结合sql语句进行精准范围定位
            int idx = ReUtil.getFirstNumber(errStr.substring(errStr.indexOf("位置")));
            int startIdx,endIdx =0;
            if((idx - 20)>0){
                startIdx = idx - 20;
            }else{
                startIdx = 0;
            }
            if((idx + 10)<sql.length()){
                endIdx = idx + 10;
            }else{
                endIdx = sql.length();
            }
            // 弥补pgsql打印时，无法精准输出异常字段的问题
            String errMsg = "sql异常位置:" + sql.substring(startIdx,endIdx) ;
            RuntimeException newErr = new RuntimeException(errMsg,error);
            log.error(errMsg);
            throw newErr;
        }
    }

    /**
     * 统一报送错误，并且删除当前线程里的对象
     * @param e
     */
    public void sendErrLog(Throwable e){
        // 当作是job类型的异常报送 -- 避免真的错误了都不知道处理
        log.error("sql日志拦截类报错（不影响sql的正常执行，但发现请告知）:",e);
    }


    /**
     * 获取执行sql
     * @param statement
     * @return
     */
    public String getSql(StatementProxy statement){
        String sql =  statement.getBatchSql();
        if(sql==null){
            sql = statement.getLastExecuteSql();
        }
        return sql;
    }

    /**
     * sql日志打印
     * PreparedStatementProxyImpl sqlType = 1111 时被强制转为null，导致打印的信息不符
     * @param statement
     */
    public void sqlLog(StatementProxy statement){
        try {
            // 参数对象
            Map<Integer, JdbcParameter> lParameters = statement.getParameters();
            // 获取sql
            String lSql = null;
            try {
                if(dbType==1){ // mysql
                    lSql = SQLUtils.formatMySql(getSql(statement));
                }else{ // 其他
                    lSql = SQLUtils.format(getSql(statement), JdbcConstants.POSTGRESQL);
                }
            }catch (Throwable e){
                // 格式化异常，则获取普通sql即可
                lSql = getSql(statement) ;
            }

            StringBuffer sqlLogStr = new StringBuffer();
            Object lO = null;
            String lS = null;
            // 判空
            if(StrKit.notBlank(lSql)){

                Map<Integer, Object> parameters = new HashMap<>();
                for (Map.Entry<Integer, JdbcParameter> m : lParameters.entrySet()) {
                    parameters.put(m.getKey(),m.getValue().getValue());
                }
                if(!onlyRealsql){
                    sqlLogStr.append("\r\nsql-?: ").append(lSql);
                }
                sqlLogStr.append("\r\nparams: ").append(JSON.toJSONStringWithDateFormat(parameters,"yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect,
                        SerializerFeature.WriteDateUseDateFormat));
                // 循环获取
                for (Map.Entry<Integer,JdbcParameter> lEntry : lParameters.entrySet()){
                    JdbcParameter lValue = lEntry.getValue();
                    if(lValue == null){
                        continue;
                    }
                    lO = lValue.getValue();
                    //
                    if(lO instanceof Date){
                        // 转时间格式
                        lS = EDateUtil.formatDateTime((Date) lO);
                    }else{
                        lS = String.valueOf(lO);
                    }

                    if(lO == null){
//                        continue;
                    }else{
                        if(lO instanceof Number){
                            // 不需要任何处理
                        }else{
                            // 添加单引号
                            lS = "'" + lS +"'";
                        }
                    }

                    // 直接使用 lSql.replaceFirst("\\?",lS) 碰到转义符会报错 或者 $6 的字符串也会报错
                    // 不需要输入转义符去定位sql，避免干扰
                    lSql = replaceFirst(lSql,"?",lS);
                }
//                log.info("edb-sql-real: "+lSql);
                sqlLogStr.append("\r\nsql-real: ").append(lSql);
            }else{
                lSql = statement.getLastExecuteSql();
                // 如果 lSql 不为空的话
                if(StrKit.notBlank(lSql)){
//                    log.info("edb-sql-real: " + lSql);
                }else{
                    lSql = "批量执行sql，暂不完整打印，避免拖沓";
//                    log.info("edb-sql-real: " + lSql);
                }
                sqlLogStr.append("\r\nsql-real: ").append(lSql);
            }

            log.debug(sqlLogStr.toString());
            sqlLogStr = null;

        }catch (Exception e){
            log.error("---sql日志打印出错---",e);
        }
    }


    /**
     * 添加最后的日志信息
     */
    public void addLastlLog(){
//        long start = System.currentTimeMillis();
        // 使用该方式较为节省堆积获取的时间
//        StackTraceElement[] stackTraceElements =  (new Throwable()).getStackTrace();
//        StackTraceElement[] stackTraceElements =   Thread.currentThread().getStackTrace();
        // 定义堆栈对象字符串
//        String steStr = "";
//        for (StackTraceElement steF : stackTraceElements) {
//            steStr = steF.toString();
//
//            //System.out.println("-->"+steStr);
//        }
        //System.out.println("pt-->"+(System.currentTimeMillis()-start));
    }


    /**
       * 字符串替换，左边第一个。
       * @param str 源字符串
       * @param oldStr  目标字符串
       * @param newStr  替换字符串
       * @return  替换后的字符串
       */
    public String replaceFirst(String str, String oldStr, String newStr){
        int i = str.indexOf(oldStr);
        if (i == -1) return str;
        str = str.substring(0, i) + newStr + str.substring(i + oldStr.length());
        return str;
    }


    /**
       * 字符串替换，从头到尾查询一次，替换后的字符串不检查
       * @param str 源字符串
       * @param oldStr  目标字符串
       * @param newStr  替换字符串
       * @return  替换后的字符串
       */
    public String replaceAll(String str, String oldStr, String newStr)
    {
        int i = str.indexOf(oldStr);
        int n = 0;
        while(i != -1)
        {
            str = str.substring(0, i) + newStr + str.substring(i + oldStr.length());
            i = str.indexOf(oldStr, i + newStr.length());
            n++;
        }
        return str;
    }


}
