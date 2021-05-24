package com.edbplus.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.EDbQueryUtil;
import com.jfinal.plugin.activerecord.SqlPara;
import org.springframework.data.domain.PageRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName JpaEDbQueryTest
 * @Description: EDbQuery 通用查询的相关测试案例
 * @Author 杨志佳
 * @Date 2020/10/19
 * @Version V1.0
 **/
public class JpaEDbQueryTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }

    @Test
    public void test(){

        // ================== 通用查询自定义组合 开始  ===================
        // 定义多个id
        List<Integer> manyIds = new ArrayList<>();
        manyIds.add(100);
        manyIds.add(200);
        EDbQuery eDbQuery = new EDbQuery();
        // 假如只打印 VEHICLE_TYPE_ID,CREATOR 字段
        eDbQuery.fields(" VEHICLE_TYPE_ID,CREATOR ");
        // select * from xxx where 1 = 1 --> and 拼接1
        // 包含 VEHICLE_TYPE_ID 100 、 200 的数据
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.like, manyIds));
        // 定义id列表
        manyIds = new ArrayList<>();
        manyIds.add(100);
        // andCom 相当于拼接 --> and ( 拼接1   )  ,剔除 VEHICLE_TYPE_ID = 100 的数据
        eDbQuery.andCom().and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.notIn, manyIds));
        // 根据 VEHICLE_TYPE_ID 进行降序布局 -- 可添加多个排序排序规则
        eDbQuery.orderDESC("VEHICLE_TYPE_ID");
        eDbQuery.orderASC("CREATE_TIME");
        // ==================== 通用查询自定义组合 结束  ===================
        // EDbQuery 查询对象解析器，依赖于 注解 @Table 实现，可用于自定义不同表视图切换时使用
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(VehicleType.class,eDbQuery);

        long start = System.currentTimeMillis();
        // 普通查询
        List<VehicleType> vehicleTypes =   EDb.find(VehicleType.class,eDbQuery);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        // 设置返回的最大条数，可以避免查询条件返回的条数过多
        vehicleTypes =   EDb.find(VehicleType.class,eDbQuery,100);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 分页查询
        EDb.paginate(VehicleType.class,1,10,eDbQuery);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // PageRequest 分页查询
        EDb.paginate(VehicleType.class, PageRequest.of(1,10),eDbQuery);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 返回首条记录
        EDb.findFirst(VehicleType.class,eDbQuery);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 返回唯一1条记录
        EDb.findOnlyOne(VehicleType.class,eDbQuery);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );


        //
        eDbQuery = new EDbQuery();
        // 基于时间范围的查询
        eDbQuery.and(EDbFilter.le("CREATE_TIME",new Date()));
//        eDbQuery.and(new EDbFilter("CREATE_TIME",EDbFilter.Operator.le,new Date()));
        start = System.currentTimeMillis();
        System.out.println(JSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        eDbQuery = new EDbQuery();
        // 基于时间范围的查询
        eDbQuery.and(EDbFilter.like("VEHICLE_TYPE_NAME","车"));
        //eDbQuery.and(new EDbFilter("VEHICLE_TYPE_NAME",EDbFilter.Operator.like,"%车%"));
        start = System.currentTimeMillis();
        System.out.println(JSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );


        eDbQuery = new EDbQuery();
        // 车辆类型不为null的查询
        eDbQuery.and(EDbFilter.isNotNull("VEHICLE_TYPE_NAME"));
        //eDbQuery.and(new EDbFilter("VEHICLE_TYPE_NAME", EDbFilter.Operator.isNotNull, null));
        start = System.currentTimeMillis();
        System.out.println(JSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );


    }





}
