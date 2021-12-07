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
package com.edbplus.db.tool;

import cn.hutool.core.thread.ThreadUtil;
import com.edbplus.db.druid.EDbSelectUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @ClassName SqlParserTest
 * @Description: sql语法相关处理的测试
 * @Author 杨志佳
 * @Date 2021/12/1
 * @Version V1.0
 **/
public class SqlParserTest {

    @Test
    public void checkKeyWord(){
        String sql = "select 1 from table";
        String keyWord = "select";
        int idx = sql.indexOf(keyWord); // 传递idx，主要是便于控制获取的是第几个关键字所在位置，避免一个语句包含多个关键字，导致判断混淆
        System.out.println(EDbSelectUtil.checkKeyWordFromSql(sql,keyWord,idx));
    }


    /**
     * 返回 limit 语句之特殊处理
     */
    @Test
    public void returnLimitSqlTest(){
        // 场景1
        String sql = " select 1,(select 1 from tb2 limit 1) from tb limit 9";
        System.out.println("1=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1,(select 1 from tb2 limit 1) from tb  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1,(select 1 from tb2 limit 1) from tb ";
        System.out.println("2=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1,(select 1 from tb2 limit 1) from tb  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb ";
        System.out.println("3=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb limit 10,10"; // 则需要转换成 10,1
        System.out.println("4=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb limit 10,1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb limit 10 offset 7"; // 这个要特殊处理，右侧如果存在 offset 的情况，则必须保留，避免数据结果不一致，尤其是mysql版本
        System.out.println("5=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb  limit 1 offset 7",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb offset 6 limit 9 ";
        System.out.println("6=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb offset 6  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb offset 0\r\nlimit 9 "; // 特殊符号场景
        System.out.println("7=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb offset 0\r\n limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1_limit from tb offset 0 limit 9 ";
        System.out.println("8=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1_limit from tb offset 0  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1  from tb where gs='N' offset 0 \tlimit 9 ";
        System.out.println("9=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1  from tb where gs='N' offset 0 \t limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1_limit -- 换行 \n from tb offset 0 limit 9 ";
        System.out.println("10 => "+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1_limit -- 换行 \n from tb offset 0  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1_limit,'\\n 1' -- 回车 \r\n from tb where and offset 0 limit 9 ";
        System.out.println("11=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1_limit,'\\n 1' -- 回车 \r\n from tb where and offset 0  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

    }


    @Test
    public void returnLimitSqlThredTest(){
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(50);

        for(int i=0;i<20;i++){
           String  sql = " select "+i+" from tb \toffset 0 \tlimit 10 ";
           int j =i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println( j + "==>"+EDbSelectUtil.returnLimitSql(sql,1));
                }
            });
        }

        ThreadUtil.sleep(100);

    }

    /**
     * 移除 order 关键字相关语法测试
     */
    @Test
    public void removeOrderTest(){
        String sql = " select 1 from tb order by id desc";
        System.out.println("1=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb ",EDbSelectUtil.removeOrder(sql));

        sql = " select 1 from tb order by id desc limit 10";
        System.out.println("2=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb  limit 10",EDbSelectUtil.removeOrder(sql));

        sql = " select 1,(select 1 from order by id limit 1) from tb order by id desc limit 10";
        System.out.println("3=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1,(select 1 from order by id limit 1) from tb  limit 10",EDbSelectUtil.removeOrder(sql));

        sql = " select 1,(select 1 from order by id limit 1) from tb order by (case when id =1 then 1 else 0 end)  desc limit 10";
        System.out.println("4=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1,(select 1 from order by id limit 1) from tb  limit 10",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb where id in (select id from tb2 order by id desc ) order by desc limit 10";
        System.out.println("5=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where id in (select id from tb2 order by id desc )  limit 10",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc ) order by desc limit 10";
        System.out.println("6=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc )  limit 10",EDbSelectUtil.removeOrder(sql));



        sql = " select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc ) order by desc ";
        System.out.println("7=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc ) ",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb where gs='N' group by id order by id desc limit 10";
        System.out.println("8=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where gs='N' group by id  limit 10",EDbSelectUtil.removeOrder(sql));

    }




}
