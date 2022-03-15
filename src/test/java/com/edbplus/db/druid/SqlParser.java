package com.edbplus.db.druid;

import cn.hutool.json.JSONUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.edbplus.db.druid.sql.parser.SelectParser;
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
    public void test2(){
        String sql = "SELECT\n" +
                "\tvdr.ID,\n" +
                "\tvdr.UC_CONTACT,\n" +
                "\tvdr.UC_TYPE,\n" +
                "\tvdr.source_type,\n" +
                "\tvdr.source_id,\n" +
                "\tvdr.allow_source_types,\n" +
                "\tvdr.UC_VEHICLE_NUMBER,\n" +
                "\tvdr.UC_VEHICLE_TYPE_NAME,\n" +
                "\tvdr.UC_VEHICLE_LENGTH,\n" +
                "\tvdr.UC_VEHICLE_WEIGHT,\n" +
                "\tvdr.UC_COMPANY,\n" +
                "\tvdr.UC_NAME,\n" +
                "\tvdr.START_LC,\n" +
                "\tvdr.END_LC,\n" +
                "\tvdr.CM_LC,\n" +
                "\tvdr.TEL_LC,\n" +
                "\tvdr.CREATE_TIME,\n" +
                "\t( SELECT LABEL_NAME FROM sys_pre_user WHERE USER_NAME = UC_CONTACT LIMIT 1 ) LABEL_NAME,\n" +
                "\t( SELECT USER_ID FROM sys_pre_user WHERE USER_NAME = UC_CONTACT LIMIT 1 ) USER_ID,\n" +
                "\t( SELECT AREA FROM cr_user_last_location WHERE USER_NAME = UC_CONTACT LIMIT 1 ) LOCATION,\n" +
                "\t( SELECT LOCATE_TIME FROM cr_user_last_location WHERE USER_NAME = UC_CONTACT LIMIT 1 ) LOCATE_TIME,\n" +
                "\t( CASE WHEN SOURCE_TYPE = '30' THEN ( SELECT VS_NO FROM tra_vehicle_source WHERE VSID = vdr.SOURCE_ID ) END ) AS sourceNo,\n" +
                "\tvdrjoin.childs AS childs \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tvdrtem.ID,\n" +
                "\t\tCOUNT ( 1 ) AS childs \n" +
                "\tFROM\n" +
                "\t\tVW_DRIVER_ROUTE vdrtem\n" +
                "\t\tLEFT JOIN SYS_PRE_USER spu ON vdrtem.UC_CONTACT = spu.USER_NAME\n" +
                "\t\tLEFT JOIN CR_USER_LAST_LOCATION cull ON vdrtem.UC_CONTACT = cull.USER_NAME \n" +
                "\tWHERE\n" +
                "\t\t1 = 1 \n" +
                "\t\tAND UC_CONTACT IN ( SELECT user_name FROM cr_user_last_location WHERE AREA ~ '云南省,昆明市' ) \n" +
                "\tGROUP BY\n" +
                "\t\tvdrtem.UC_CONTACT,vdrtem.ID\n" +
                "\tORDER BY\n" +
                "\t\tvdrtem.create_time DESC \n" +
                "\t\tLIMIT 10\n" +
                "\t\toffset 0\n" +
                "\t) vdrjoin\n" +
                "\tJOIN vw_driver_route AS vdr ON vdr.ID = vdrjoin.ID\n" +
                "\t";
        System.out.println(JSONUtil.toJsonStr(SelectParser.getSelectNames(sql, DbType.postgresql.name())));
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
