package com.edbplus.db.jpa;

import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.EDbQueryUtil;
import com.edbplus.db.util.hutool.json.EJSONUtil;
import com.jfinal.plugin.activerecord.SqlPara;
import org.springframework.data.domain.PageRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @ClassName JpaEDbQueryTest
 * @Description: EDbQuery 通用查询的相关测试案例
 * @Author 杨志佳
 * @Date 2020/10/19
 * @Version V1.0
 **/
public class JpaEDbQueryTest extends BaseTest {

    @BeforeTest
    public void initBefor() {
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class, 1);
    }



    @Test
    public void countSqlTest() {
        String sql = "\n" +
                "select a.* from cr_vehicle_type a,cr_vehicle_type b \n" +
                " where 1=1 and b.VEHICLE_TYPE_ID in (1,3,10)  group by a.VEHICLE_TYPE_ID having count(1) > 1 " +
                " order by\n" +
                "(\n" +
                "case when a.VEHICLE_TYPE_ID =100 then 1 else 0 end\n" +
                ")\n" +
                "desc ";
        System.out.println("==>"+  EDb.countSql(sql));

//        System.out.println("==>"+  EDb.use().getFirstSql(sql));



    }

    @Test
    public void testOr(){
        EDbQuery eDbQuery = new EDbQuery();
        // 根据情况设置查询条件
        eDbQuery.or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "1"));
        eDbQuery.or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "2"));
        eDbQuery.andCom().or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "3")).or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "4"));
        eDbQuery.orCom().or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "5")).or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "6"));
        List<VehicleType> vehicleTypes = EDb.use().find(VehicleType.class,eDbQuery);
    }

    /**
     * 单表简单查询案例
     */
    @Test
    public void easyFind(){
        EDbQuery eDbQuery = new EDbQuery();
        // 根据情况设置查询条件
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "1"));
        List<Integer> list =  Arrays.asList(new Integer[]{1,2});
//        list.add(1); // 用 in 替代需要 or 一堆类型的场景，性能会更好
//        list.add(2);
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.in, list));
        eDbQuery.or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "3"));
        eDbQuery.andCom().and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "11"))
                .or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "22"));
        eDbQuery.orCom().and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "3"))
                .or(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, "5"));

        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.notRlk, "1"));

        LinkedList<Object> params = new LinkedList();
        params.add(1);
        params.add(100);
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.notBetween, params));
        eDbQuery.and(new EDbFilter(null, EDbFilter.Operator.exists, " select 1 from cr_vehicle_type c1 where c1.VEHICLE_TYPE_ID = VEHICLE_TYPE_ID "));
        eDbQuery.and(new EDbFilter(null, EDbFilter.Operator.notExists, " select 1 from cr_vehicle_type c1 where c1.VEHICLE_TYPE_ID <> VEHICLE_TYPE_ID "));

        eDbQuery.groupBy("VEHICLE_TYPE_ID");
        eDbQuery.having("count(1) > ?",1);
        // 根据主键id降序排序 -- 跟创建时间基本上是一致的，性能还更好
        eDbQuery.orderDESC("VEHICLE_TYPE_ID");
//        eDbQuery.orderASC("VEHICLE_TYPE_ID");
        // 只获取一条数据
        VehicleType vehicleType = EDb.use().findFirst(VehicleType.class,eDbQuery);
//        // 无法预估范围值时，建议写上
//        eDbQuery.limit(10);
//        // 普通查询
//        List<VehicleType> vehicleTypes =   EDb.find(VehicleType.class,eDbQuery);
    }

    @Test
    public void likeTest(){
        EDbQuery eDbQuery = new EDbQuery();
        // 根据情况设置查询条件
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_NAME", EDbFilter.Operator.llk, "1"));
        VehicleType vehicleType = EDb.use().findFirst(VehicleType.class,eDbQuery);
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
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.in, manyIds));
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
//        EDb.paginate(VehicleType.class, PageRequest.of(1,10),eDbQuery);
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
        System.out.println(EJSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        eDbQuery = new EDbQuery();
        // 基于时间范围的查询
        eDbQuery.and(EDbFilter.like("VEHICLE_TYPE_NAME","车"));
        //eDbQuery.and(new EDbFilter("VEHICLE_TYPE_NAME",EDbFilter.Operator.like,"%车%"));
        start = System.currentTimeMillis();
        System.out.println(EJSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );


        eDbQuery = new EDbQuery();
        // 车辆类型不为null的查询
        eDbQuery.and(EDbFilter.isNotNull("VEHICLE_TYPE_NAME"));
        //eDbQuery.and(new EDbFilter("VEHICLE_TYPE_NAME", EDbFilter.Operator.isNotNull, null));
        start = System.currentTimeMillis();
        System.out.println(EJSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );


    }


    @Test
    public void testPage(){
        System.out.println(EDb.paginate(1,10,"select *"," from cr_vehicle_type where 1=1 ").getList().get(0));

        EDb.paginate(1,10,"select *,(select VEHICLE_TYPE_ID from cr_vehicle_type where 1=1 and VEHICLE_TYPE_ID = ? limit 1 ) "," from cr_vehicle_type where 1=1 and VEHICLE_TYPE_ID = ? and VEHICLE_TYPE_ID = ? ",2,100,100);

        String sql  = " select tb1.* from (select *,(select VEHICLE_TYPE_ID from cr_vehicle_type where 1=1 and VEHICLE_TYPE_ID = ? limit 1 )  from cr_vehicle_type where 1=1 and VEHICLE_TYPE_ID = ? and VEHICLE_TYPE_ID = ?) as tb1 ";
        SqlPara sqlPara = EDb.getSqlParaByString(sql);
        sqlPara.addPara(2);
        sqlPara.addPara(100);
        sqlPara.addPara(100);
        // 以下这种只支持 #para(0) 的写法，不支持 ? 的写法
//        SqlPara sqlPara = EDb.getSqlParaByString(sql,2,100,100);
//        System.out.println("para:"+ EJSONUtil.toJsonStr(sqlPara.getPara()));

        EDb.paginate(1,10,sqlPara);

    }

    @Test
    public void testDateQuery(){
        EDbQuery eDbQuery = new EDbQuery();
        // 基于时间范围的查询
        eDbQuery.and(EDbFilter.le("CREATE_TIME",new Date()));
        System.out.println(EJSONUtil.toJsonStr(EDb.findFirst(VehicleType.class,eDbQuery)));
    }



}
