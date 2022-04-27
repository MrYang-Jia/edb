package com.edbplus.db.jpa;

import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import org.springframework.data.domain.PageRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @ClassName JpaPaginateTest
 * @Description: 基于jpa对象的分页方法测试案例
 * @Author 杨志佳
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class JpaPaginateTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }

    @Test
    public void paginateTest(){
        EDbQuery eDbQuery = new EDbQuery();
        // 假如只打印 VEHICLE_TYPE_ID,CREATOR 字段
        eDbQuery.fields(" VEHICLE_TYPE_ID,CREATOR ");
        // 只查询创建人 = 小陈 的数据
        eDbQuery.and(new EDbFilter("CREATOR", EDbFilter.Operator.eq, "小陈"));
        long start = System.currentTimeMillis();
        EDb.paginate(VehicleType.class, 1,10,eDbQuery);
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis()-start));

        // 不需要生成统计查询记录数的方法 -- 因为总记录数的方法可以通过 写死 或者写一个 获取缓存记录数的sql方法，原 jfinal 的设计方法不够灵活，所以单独扩展
        start = System.currentTimeMillis();
        EDb.paginate(VehicleType.class, 1,10,eDbQuery);
        System.out.println("无统计语句的耗时:"+(System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        //
        SqlPara sqlPara = EDb.getSqlPara("test.findForId", 200);
        Page page = EDb.paginate(VehicleType.class,1,10,sqlPara);
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis() - start) + "==>"+ page.getTotalRow());

        start = System.currentTimeMillis();
        EDb.paginate(VehicleType.class,1,10,200,sqlPara);
        System.out.println("无统计语句的耗时:"+(System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        page = EDb.paginate(VehicleType.class,1,10,"select * from cr_vehicle_type where VEHICLE_TYPE_ID in(?,200) order by VEHICLE_TYPE_ID desc ","100");
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis()-start) + "==>"+ page.getTotalRow());

        // =============== 传统 jfinal 查询方式 ==============
        start = System.currentTimeMillis();
        page = EDb.paginate(1,10,"select *  ","from cr_vehicle_type where VEHICLE_TYPE_ID in(100,200)");
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis()-start) + "==>"+ page.getTotalRow());


    }



}
