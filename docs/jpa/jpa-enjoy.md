
# JPA 使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

**基于Java的相关用例**

- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

** enjoySql模板 **

- [ Enjoy SQL 模板介绍 ](../../docs/jpa/enjoy.md)

- [替代 mybatis or mybatis-plus 的相关测试案例](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaEnjoyTest.java)

> sql模板内容

- [ 示例sql模板 ](../../src/test/resources/sql/logInfo.sql)


```sql

#sql("findForId")
select * from cr_vehicle_type where VEHICLE_TYPE_ID = #para(0)
#end

```

> java 示例代码

```java

package com.edbplus.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.cloud.jfinal.activerecord.db.vo.VehicleTypeVo;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @ClassName JpaEnjoyTest
 * @Description: JpaEjoySql 查询方案，替代 mybatis 查询方案
 * @Author 杨志佳
 * @Date 2020/10/19
 * @Version V1.0
 **/
public class JpaEnjoyTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }

    /**
     * 通过enjoySql模板功能替代 mybaties 的sql模板功能
     */
    @Test
    public void sqlParaTest(){
        long start = System.currentTimeMillis();
        // 模板的指定 可以看 BaseTest.java 类里的 init() 方法
        // 通过enjoySql 模板的方式
        SqlPara sqlPara = EDb.getSqlPara("test.findForId", 101);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        System.out.println("打印sql语句：" + sqlPara.getSql());
        System.out.println("打印参数：" + JSONUtil.toJsonStr(sqlPara.getPara()));

        start = System.currentTimeMillis();
        // 根据sql语句返回对象查询列表 -- 对象可以是jpa对象也可以是普通的vo对象
        List<VehicleType> vehicleTypeList = EDb.find(VehicleType.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 返回首个对象
        VehicleType vehicleType = EDb.findFirst(VehicleType.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 返回一个分页对象
        Page<VehicleType> page = EDb.paginate(VehicleType.class,1,10,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 只返回一个唯一对象，超过1个对象则会抛错，告知业务逻辑错误，对标 jpa 的 getOne 方法
        VehicleType onlyOneVehicleType = EDb.findOnlyOne(VehicleType.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 匹配普通的 vo 表 和 特殊字段的转义匹配,其中 createDate 是查询出来的数据里不包含的字段
        VehicleTypeVo vehicleTypeVo = EDb.findFirst(VehicleTypeVo.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        System.out.println("内容:"+ JSONUtil.toJsonStr(vehicleTypeVo));

    }


    /**
    * 更新测试 
    */
    @Test
    public void testUpdate(){
        SqlPara sqlPara = EDb.getSqlPara("test.updateDemo", "小陈",1);
        // 更新
        EDb.update(sqlPara);
    }


}



```

```sql
-- 内网环境，非本机，网络是通过wifi模式连接的相关测试，比远程主机的相关测试速度回快点，实际上云上服务器的响应更快
耗时:0
打印sql语句：select * from cr_vehicle_type where VEHICLE_TYPE_ID = ? 
打印参数：[101]
Sql: select * from cr_vehicle_type where VEHICLE_TYPE_ID = ? 
耗时:48
Sql:  select * from (select * from cr_vehicle_type where VEHICLE_TYPE_ID = ? ) as edb_findFirst_tb limit 2
耗时:9
Sql: select count(*) from cr_vehicle_type where VEHICLE_TYPE_ID = ? 
Sql: select *  from cr_vehicle_type where VEHICLE_TYPE_ID = ?  limit 0, 10
耗时:11
Sql:  select * from (select * from cr_vehicle_type where VEHICLE_TYPE_ID = ? ) as edb_findFirst_tb limit 2
耗时:5
Sql:  select * from (select * from cr_vehicle_type where VEHICLE_TYPE_ID = ? ) as edb_findFirst_tb limit 2
耗时:10
内容:{"creator":"101忠实的大叔","vehicleTypeName":"不锈钢罐车","createDate":1603078965000}


更新测试
Sql: update cr_vehicle_type set creator = ? where VEHICLE_TYPE_ID = ? 


```
