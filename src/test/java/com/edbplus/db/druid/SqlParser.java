package com.edbplus.db.druid;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
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
        String sql = "  select * from tb1 limit 10 ";
        doSql(sql);
    }



    public void doSql(String sql){
        // mysql 装载容器 --  使用Parser解析生成AST
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        // 获取sql实例对象 -- sql语句本身
        SQLStatement sqlStatement = parser.parseStatement();
        // 视图解析器定义
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
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
