package com.edbplus.db.generator;

import com.edbplus.db.generator.jdbc.GenPg;
import org.testng.annotations.Test;

public class GeneralSqlTest {

    /**
     * 获取表信息
     */
    @Test
    public void getTableInfoSql(){
        //System.out.println(GenMysql.getTableInfoSql("tra_goods_source"));
        System.out.println(GenPg.getTableInfoSql("tra_goods_source,ah_oauth_user_map"));
        System.out.println(GenPg.getTableNamesSql("xzwpro","public"));
    }

    @Test
    public void getTableColumnsSql(){
//        System.out.println(GenMysql.getTableColumnsSql("sys_account"));
        System.out.println(GenPg.getTableColumnsSql("tra_goods_source,ah_oauth_user_map"));
    }

}
