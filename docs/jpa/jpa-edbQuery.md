
# JPA 使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

**基于Java的相关用例**

- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

** EDbQuery 查询规则说明 **
> 设计思路是针对对象上的 @Table 注解，也就是单体表做一个通用查询封装的工具类
> 实现类似的sql

```sql
select EDbQuery.fields(需要显示的字段名) from @TABLE.name 
where 1=1
-- and 自定义组合 ，对应 EDbQuery.andCom().and(EDbFilter) or  EDbQuery.andCom().or(EDbFilter)
and ( 1=1 and xx1 and  xx2 or xx3 )
-- or 自定义组合 ，对应 EDbQuery.orCom().and(EDbFilter)  or EDbQuery.orCom().or(EDbFilter) 
or ( 1=1 and xx1 and  xx2 or xx3 )
-- 自定义 and 、自定义 or 添加，对应 EDbQuery.and(EDbFilter) 和 EDbQuery.or(EDbFilter)
and xx1 and xx2 or xx3
--  对应 eDbQuery.orderDESC("x1");   eDbQuery.orderASC("x2");
order by  x1 desc,x2 asc

```

** EDbFilter 规则说明 **

- `EDbFilter.eq`   填充sql表达式里 "=" 右侧的数值

- `EDbFilter.ne`   填充sql表达式里 "!=" 或 "<>" 右侧的数值

- `EDbFilter.gt`   填充sql表达式里 ">"  右侧的数值(可以是时间，会自动转换)

- `EDbFilter.ge`   填充sql表达式里 ">="  右侧的数值(可以是时间，会自动转换)

- `EDbFilter.lt`   填充sql表达式里 "<"  右侧的数值(可以是时间，会自动转换)

- `EDbFilter.le`   填充sql表达式里 "<="  右侧的数值(可以是时间，会自动转换)

- `EDbFilter.like`  填充sql表达式里 "like"  右侧的数值(全匹配时，需要自己填写标识符 %全匹配表达% 、 %左匹配 、 右匹配% )

- `EDbFilter.in`    填充sql表达式里 "in"  右侧的数值

- `EDbFilter.notIn`     填充sql表达式里 "not in"  右侧的数值

- `EDbFilter.isNotNull` 填充sql表达式里字段表达 is not null 的方式 

- `EDbFilter.isNull`    填充sql表达式里字段表达 is null 的方式 

```javascript

// 解析 EDbQuery 对象，使用以下方式
SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(T.class,eDbQuery);

```

- [实体对象 通用查询 的测试案例](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaEDbQueryTest.java)

> java 示例代码

```java

package com.edbplus.cloud.jfinal.activerecord.db.jpa;

import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.cloud.jfinal.activerecord.db.query.EDbFilter;
import com.edbplus.cloud.jfinal.activerecord.db.query.EDbQuery;
import org.springframework.data.domain.PageRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
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
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.in, manyIds));
        // 相当于拼接 --> and ( 拼接1   )
        manyIds = new ArrayList<>();
        manyIds.add(100);
        // 剔除 VEHICLE_TYPE_ID = 100 的数据
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

    }



}

```

```sql

-- 内网环境，非本机，网络是通过wifi模式连接的相关测试，比远程主机的相关测试速度回快点，实际上云上服务器的响应更快
Sql: select  VEHICLE_TYPE_ID,CREATOR  from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) order by VEHICLE_TYPE_ID desc,CREATE_TIME asc
耗时:36
Sql: select count(*) from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) 
Sql: select  VEHICLE_TYPE_ID,CREATOR   from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) order by VEHICLE_TYPE_ID desc,CREATE_TIME asc limit 0, 10
耗时:10
Sql: select count(*) from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) 
Sql: select  VEHICLE_TYPE_ID,CREATOR   from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) order by VEHICLE_TYPE_ID desc,CREATE_TIME asc limit 0, 10
耗时:12
Sql:  select * from (select  VEHICLE_TYPE_ID,CREATOR  from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) order by VEHICLE_TYPE_ID desc,CREATE_TIME asc) as edb_findFirst_tb limit 2
耗时:4
Sql:  select * from (select  VEHICLE_TYPE_ID,CREATOR  from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID in (?,?) and ( 1=1  and VEHICLE_TYPE_ID not in (?) ) order by VEHICLE_TYPE_ID desc,CREATE_TIME asc) as edb_findFirst_tb limit 2
耗时:3


```
