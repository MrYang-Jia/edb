package com.edbplus.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.array.TestArrayDemo;
import com.edbplus.db.query.EDbFilter;
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

    /**
     * 验证 arrAll(@>) 与 arrAny(&&) 数组过滤：
     * 整数组参数绑定，由 PG 服务器端推断数组元素类型，避免 text[] 与 varchar[] 类型不匹配报错
     */
    @Test
    public void testArrayFilter(){
        // arrAny: 包含任意一个 -> text_array && ?  (varchar[] && varchar[])
        EDbQuery anyQuery = new EDbQuery();
        anyQuery.and(new EDbFilter("text_array", EDbFilter.Operator.arrAny, List.of("Java", "Python")));
        List<TestArrayDemo> anyList = EDb.use("pg").find(TestArrayDemo.class, anyQuery);
        System.out.println("arrAny 命中条数: " + anyList.size());
        anyList.forEach(d -> System.out.println(JSONUtil.toJsonStr(d)));

        // arrAll: 全部包含 -> text_array @> ?  (varchar[] @> varchar[])
        EDbQuery allQuery = new EDbQuery();
        allQuery.and(new EDbFilter("text_array", EDbFilter.Operator.arrAll, List.of("Java", "Python","Go")));
        List<TestArrayDemo> allList = EDb.use("pg").find(TestArrayDemo.class, allQuery);
        System.out.println("arrAll 命中条数: " + allList.size());
        allList.forEach(d -> System.out.println(JSONUtil.toJsonStr(d)));

        // 断言：arrAny 至少命中 1 条（此前 ARRAY['Java','Python'] 会因类型不匹配直接抛错）
        org.testng.Assert.assertTrue(anyList.size() > 0, "arrAny 应命中至少一条数据");
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
