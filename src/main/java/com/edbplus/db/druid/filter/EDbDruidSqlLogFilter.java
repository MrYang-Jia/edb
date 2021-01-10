package com.edbplus.db.druid.filter;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.edbplus.db.util.json.EDbJsonUtil;
import com.jfinal.kit.StrKit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * druid - sql日志打印过滤器
 */
@Slf4j
public class EDbDruidSqlLogFilter extends FilterEventAdapter {


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
        try{

        }catch (Throwable e)
        {
            // 报送错误日志
            sendErrLog(e);
        }



    }

    /**
     * 统一报送错误，并且删除当前线程里的对象
     * @param e
     */
    public void sendErrLog(Throwable e){
        // 主动删除当前线程的存储对象，避免线程池复用！
//        t1.remove();
        // 当作是job类型的异常报送 -- 避免真的错误了都不知道处理
        log.error("sql日志拦截类报错（不影响sql的正常执行，但发现请告知）:",e);
//        LogSender.getInstance().sendJobError(e);
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
            String lSql = statement.getBatchSql();
            // 判空
            if(StrKit.notBlank(lSql)){
                if(!onlyRealsql){
                    Map<Integer, Object> parameters = new HashMap<>();
                    for (Map.Entry<Integer, JdbcParameter> m : lParameters.entrySet()) {
                        parameters.put(m.getKey(),m.getValue().getValue());
                    }
                    log.debug("edb-sql-?: "+lSql);
                    log.debug("edb-sql-params: "+ EDbJsonUtil.toJsonForFormat(parameters));
                }
                // 循环获取
                for (Map.Entry<Integer,JdbcParameter> lEntry : lParameters.entrySet()){
                    JdbcParameter lValue = lEntry.getValue();
                    if(lValue == null){
                        continue;
                    }
                    Object lO = lValue.getValue();

                    String lS = String.valueOf(lO);
                    if(lO == null){
//                        continue;
                    }else{
                        // 添加单引号
                        lS = "'" + lS +"'";
                    }

                    // 直接使用 lSql.replaceFirst("\\?",lS) 碰到转义符会报错 或者 $6 的字符串也会报错
                    // 不需要输入转义符去定位sql，避免干扰
                    lSql = replaceFirst(lSql,"?",lS);
                }
                log.debug("edb-sql-real: "+lSql);
            }else{
                lSql = statement.getLastExecuteSql();
                // 如果 lSql 不为空的话
                if(StrKit.notBlank(lSql)){
                    log.debug("edb-sql-real: " + lSql);
                }else{
                    lSql = "批量执行sql，暂不完整打印，避免拖沓";
                    log.debug("edb-sql-real: " + lSql);
                }
            }

        }catch (Exception e){
            log.error("---sql日志打印出错---",e);
        }
    }


    /**
     * 添加最后的日志信息
     */
    public void addLastlLog(){

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
