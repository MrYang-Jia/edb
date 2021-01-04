
# JPA 使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

**基于Java的相关用例**

- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

** 基于JPA对象的insertValues - 大量数据的快速插入 **

- [基于JPA对象的 分页查询 测试案例](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaPaginateTest.java)

```java
package com.edb.cloud.jfinal.activerecord.db.jpa;

import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edb.cloud.jfinal.activerecord.db.query.EDbFilter;
import com.edb.cloud.jfinal.activerecord.db.query.EDbQuery;
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
        EDb.paginate(VehicleType.class, PageRequest.of(1,10),eDbQuery);
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis()-start));

        // 不需要生成统计查询记录数的方法 -- 因为总记录数的方法可以通过 写死 或者写一个 获取缓存记录数的sql方法，原 jfinal 的设计方法不够灵活，所以单独扩展
        start = System.currentTimeMillis();
        EDb.paginate(VehicleType.class, PageRequest.of(1,10),100,eDbQuery);
        System.out.println("无统计语句的耗时:"+(System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        //
        SqlPara sqlPara = EDb.getSqlPara("test.findForId", 200);
        EDb.paginate(VehicleType.class,PageRequest.of(1,10),sqlPara);
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        EDb.paginate(VehicleType.class,PageRequest.of(1,10),200,sqlPara);
        System.out.println("无统计语句的耗时:"+(System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        EDb.paginate(VehicleType.class,PageRequest.of(1,10),"select * from cr_vehicle_type where VEHICLE_TYPE_ID in(100,200) ");
        System.out.println("自动生成统计语句的耗时:"+(System.currentTimeMillis()-start));


    }



}




```

```sql
-- 耗时是基于远程单机数据库导致的，对比spring体系的简单jdbc查询来说，都差不多，甚至在对象返回这块的返回耗时，比spring体系优秀
-- 内存消耗的测试没有特意去做，但是从架构和设计角度来说是差不多的，甚至还更省内存，后期可能会补上
Sql: select count(*) from cr_vehicle_type where   1=1  and CREATOR = ? 
Sql: select  VEHICLE_TYPE_ID,CREATOR   from cr_vehicle_type where   1=1  and CREATOR = ?  limit 0, 10
自动生成统计语句的耗时:48
Sql: select  VEHICLE_TYPE_ID,CREATOR  from cr_vehicle_type where   1=1  and CREATOR = ?  limit 0, 10
无统计语句的耗时:5
Sql: select count(*) from cr_vehicle_type where VEHICLE_TYPE_ID = ? 
Sql: select *  from cr_vehicle_type where VEHICLE_TYPE_ID = ?  limit 0, 10
自动生成统计语句的耗时:29
Sql: select * from cr_vehicle_type where VEHICLE_TYPE_ID = ?  limit 0, 10
无统计语句的耗时:6
Sql: select count(*) from cr_vehicle_type where VEHICLE_TYPE_ID in(100,200) 
Sql: select *  from cr_vehicle_type where VEHICLE_TYPE_ID in(100,200)  limit 0, 10
自动生成统计语句的耗时:68


```
