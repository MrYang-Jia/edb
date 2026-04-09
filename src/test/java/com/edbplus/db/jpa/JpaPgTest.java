package com.edbplus.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.array.TestArrayDemo;
import com.edbplus.db.query.EDbQuery;
import org.testng.annotations.Test;

import java.util.List;

public class JpaPgTest extends BaseTest {

    @Test
    public void testArraySelect(){
        EDbQuery eDbQuery = new EDbQuery();
        TestArrayDemo testArrayDemo = EDb.use("pg").findFirst(TestArrayDemo.class,eDbQuery);
        System.out.println(JSONUtil.toJsonStr(testArrayDemo));
        testArrayDemo.setTextArray(testArrayDemo.getTextArray().subList(0,2));
        EDb.use("pg").update(testArrayDemo);
    }

    @Test
    public void testArrayInsert(){
        TestArrayDemo testArrayDemo = new TestArrayDemo();
//        testArrayDemo.setTextArray(
//                new String[]{"Java", "Python", "Go"}
//        );
        testArrayDemo.setTextArray(List.of("Java", "Python", "Go"));
        EDb.use("pg").save(testArrayDemo);
    }
}
