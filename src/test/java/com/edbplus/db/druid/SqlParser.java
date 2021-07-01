package com.edbplus.db.druid;

import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.edbplus.db.util.hutool.exception.EExceptionUtil;
import com.edbplus.db.util.hutool.str.EStrUtil;
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

    @Test
    public void minSql(){
        String sql = "select tb1.* from (\n" +
                "  SELECT\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.AMM_ID,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_TITLE,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_AUTHOR,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_RELEASE_TIME,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_ABSTRACT,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_OSS_URL,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_COVER_STYLE,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_STATUS,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ATICLE_APPROVAL_STATUS,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ATICLE_SOURCE_TYPE,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.ATICLE_APPROVAL_TIME,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.REMOVE_FLAG,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.CREATOR,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.CREATE_TIME,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.MODIFIER,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.MODIFY_TIME,\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN.INSIDE_ARTICLE,\n" +
                "\t(select AMM_ID from CMS_ARTICLE_CLASS_RELATION where ACL_ID in(3) and AMM_ID = CMS_ARTICLE_MANAGEMENT_MAIN.AMM_ID  limit 1)\n" +
                "\tCLASS_AMM_ID ,\n" +
                "\t(select AMM_ID from CMS_ARTICLE_TOP_RELATION where 1=1\n" +
                "             AND TOP_END_TIME  >= '2021-05-31 16:28:03'\n" +
                "             AND TOP_START_TIME  <= '2021-05-31 16:28:03'\n" +
                "             AND TOP_TYPE  = 10 \n" +
                "\t\t\t\t\t\t and AMM_ID = CMS_ARTICLE_MANAGEMENT_MAIN.AMM_ID \n" +
                "\t\t\t\t\t\t limit 1 ) TOP_AMM_ID\n" +
                "  FROM\n" +
                "  CMS_ARTICLE_MANAGEMENT_MAIN\n" +
                "  WHERE 1=1\n" +
                "  AND CMS_ARTICLE_MANAGEMENT_MAIN.REMOVE_FLAG = 'N'\n" +
                "             AND CMS_ARTICLE_MANAGEMENT_MAIN.ARTICLE_STATUS  = 'RELEASE_SUCCESS'\n" +
                "             AND CMS_ARTICLE_MANAGEMENT_MAIN.INSIDE_ARTICLE  = 'N'\n" +
                "    AND CMS_ARTICLE_MANAGEMENT_MAIN.AMM_ID  NOT IN(\n" +
                "    SELECT cf.AMM_ID FROM CMS_ARTICLE_MANAGEMENT_MAIN cf\n" +
                "    WHERE cf.ARTICLE_RELEASE_TIME > '2021-05-31 16:28:03'\n" +
                "  ) \n" +
                "\t\t) as tb1\n" +
                "\t\twhere  -- 属于该分类的文章\n" +
                "  1=1 AND tb1.CLASS_AMM_ID IS NOT NULL\n" +
                "\t\tAND TOP_AMM_ID IS NULL\n" +
                "\t  ORDER BY\n" +
                "\t  tb1.ARTICLE_RELEASE_TIME DESC\t\t\n" +
                "\tLIMIT 0,14;";
        sql = EStrUtil.removeAllLineBreaks(sql);

        System.out.println(sql);
    }


    @Test
    public void errTest(){

        try {
            int i=1/0;
        }catch (Throwable e){

            System.out.println(EExceptionUtil.stacktraceToString(e,2000));
        }

    }

}
