package com.edbplus.db.druid;

import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @ClassName SqlParser
 * @Description: sql解析器
 * @Author 杨志佳
 * @Date 2020/10/17
 * @Version V1.0
 **/
public class SqlParser {

    @Test
    public void test(){
//        String sql = "  select * from tb1 limit 10 ";
//        String sql = " select tb1.* ( select * from tb1 limit 10 ) tb1 limit 88";
        String sql = " select tb1.* ( select * from tb1 limit 10 ) tb1 ";
        doSql(sql);
    }



    public void doSql(String sql){
        // mysql 装载容器 --  使用Parser解析生成AST
        SQLStatementParser parser = new SQLStatementParser(sql);
        // 这时候会执行1次sql解析 -- 第二次获取时，会导致解析失败，所以单独定义
        SQLStatement firslSqlStatement = parser.parseStatement();
        System.out.println(firslSqlStatement.getClass());
        // 获取sql实例对象 -- sql语句本身
        SQLSelectStatement sqlStatement = (SQLSelectStatement) firslSqlStatement;
        // 如果没有limit，直接修改语句则会报错，所以需要提前初始化
        if(sqlStatement.getSelect().getFirstQueryBlock().getLimit()==null){
            SQLLimit sqlLimit = new SQLLimit();
            sqlStatement.getSelect().getFirstQueryBlock().setLimit(sqlLimit);
        }
        sqlStatement.getSelect().getFirstQueryBlock().getLimit().setRowCount(5);
        System.out.println("==>"+sqlStatement.getSelect().getFirstQueryBlock().getLimit().getRowCount());
        System.out.println(sqlStatement.getSelect().toString());

        // 视图解析器定义
        SchemaStatVisitor visitor = new SchemaStatVisitor();
        // 设置sql视图解析器
        sqlStatement.accept(visitor);
        // 定义解析的结果集
        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();



        // 循环分拣
        for(Map.Entry<TableStat.Name, TableStat> tableStatEntry: tableStatMap.entrySet()){

            // 查询语句
            if(tableStatEntry.getValue().getSelectCount()>0){
                System.out.println("操作名：Select");




            }


        }

    }
}
