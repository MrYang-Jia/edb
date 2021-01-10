
# JPA 使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

**基于Java的相关用例**

- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

```java

package com.edbplus.cloud.jfinal.activerecord.db.jpa;
import lombok.Data;
import javax.persistence.*;

/**
 * @program: xzw-bss-crm-basedata
 * @description: 车辆类型基础表 - 实体
 * @author: 杨志佳
 * @create:2020-10-15 18:15:19
 **/
// 引用 JPA 的 Table 注解申明 数据库的实际名称
@Table(name = "cr_vehicle_type")
@Data
public class VehicleType  extends BaseJpa{

   // 引用 JPA 的 Id 注解申明该字段为主键 -- 每个JPA对象里必须至少包含一个 Id 注解
   @Id
   // 引用 JPA 的 Column 注解申明数据库的实际字段，如果没有该注解，该字段变化则不会进行数据添加或者更新等操作
   @Column(name="VEHICLE_TYPE_ID")
   private Long vehicleTypeId;

   /**字段说明:VEHICLE_TYPE_NAME*/
   /**描述说明:车辆类型名称*/
   // 引用 JPA 的 Column 注解申明数据库的实际字段，如果没有该注解，该字段变化则不会进行数据添加或者更新等操作
   @Column(name="VEHICLE_TYPE_NAME")
   private  String vehicleTypeName;

   /**
    * 创建人
    */
   // 引用 JPA 的 Column 注解申明数据库的实际字段，如果没有该注解，该字段变化则不会进行数据添加或者更新等操作
   @Column(name="creator")
   private String creator;

}

```

** 基于JPA对象的简单示例增删改查 **

- [单体jpa的测试案例](src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaEasyTest.java)

```java
package com.edbplus.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;

/**
 * @ClassName JpaEasyTest
 * @Description: jpa的简单案例
 * @Author 杨志佳
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class JpaEasyTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }

    /**
     * JPA 单体对象测试
     * 保存、查询、修改、删除
     */
    @Test
    public void oneTest(){
         // 数据库在远程 -- 所以耗时会偏高，但是相对比较理想，换成spring相关体系的耗时会更好，大家有兴趣的话，可以自己简单地做个对比测试
         // 默认的 jfinal 事务支持
         EDb.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
             long start = System.currentTimeMillis();
             //  === 保存部分 ===
             start = System.currentTimeMillis();
             // 数据对象
             VehicleType vehicleType = new VehicleType();
             vehicleType.setVehicleTypeName("原:小汽车");
             // 如果有多个数据库，可以用 EDb.use("数据库标识1") 指定
             EDb.save(vehicleType);
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             // 打印json字符串
             System.out.println("原: "+JSONUtil.toJsonStr(vehicleType));
             System.out.println("自增的主键id为 " + vehicleType.getVehicleTypeId());
             //  === 查询部分 ===
             VehicleType vehicleTypeFind = EDb.findById(VehicleType.class,vehicleType.getVehicleTypeId());
             System.out.println("保存后查: "+JSONUtil.toJsonStr(vehicleTypeFind));
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             //  === 更新部分 ===
             vehicleTypeFind.setVehicleTypeName("改:大型卡车");
             // 数改保存
             EDb.update(vehicleTypeFind);
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             // 再次查询
             vehicleTypeFind = EDb.findById(VehicleType.class,vehicleType.getVehicleTypeId());
             System.out.println("改后查: "+JSONUtil.toJsonStr(vehicleTypeFind));
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             //  === 删除部分 ===
             EDb.deleteById(vehicleTypeFind);
             vehicleTypeFind = EDb.findById(VehicleType.class,vehicleType.getVehicleTypeId());
             System.out.println("删后查: "+JSONUtil.toJsonStr(vehicleTypeFind));
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             // 由于只是做测试，所以不想直接插入到数据库，直接设置为false即可
             return false;
         }
        );
    }



}


```

```sql
-- 耗时是基于远程单机数据库导致的，对比spring体系的简单jdbc查询来说，都差不多，甚至在对象返回这块的返回耗时，比spring体系优秀
-- 内存消耗的测试没有特意去做，但是从架构和设计角度来说是差不多的，甚至还更省内存，后期可能会补上
Sql: insert into `cr_vehicle_type`(`creator`, `vehicle_type_id`, `vehicle_type_name`) values(?, ?, ?)
耗时:33
原: {"vehicleTypeId":338,"vehicleTypeName":"原:小汽车"}
自增的主键id为 338
Sql: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
保存后查: {"vehicleTypeId":338,"vehicleTypeName":"原:小汽车"}
耗时:27
Sql: update `cr_vehicle_type` set `vehicle_type_name` = ?  where `vehicle_type_id` = ?
耗时:10
Sql: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
改后查: {"vehicleTypeId":338,"vehicleTypeName":"改:大型卡车"}
耗时:11
Sql: delete from `cr_vehicle_type` where `vehicle_type_id` = ?
Sql: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
删后查: null
耗时:16

```



** 单个JPA对象的 更新部分字段 的测试 **

```java

package com.edbplus.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName JpaEasyTest
 * @Description: jpa的简单案例
 * @Author 杨志佳
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class JpaEasyTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }


    /**
     * 单个JPA对象的 更新部分字段 的测试
     */
    @Test
    public void updateForId(){

        EDb.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            // 构建更新对象主体
            VehicleType vehicleType = EDb.forUpdate(VehicleType.class);
            // 必须指定要更新的对象id
            vehicleType.setVehicleTypeId(101L);
            // 需要更新的内容
            vehicleType.setCreator("101忠实的大叔");
            EDb.update(vehicleType);
            VehicleType findNewVehicleType = EDb.findById(VehicleType.class, 101);
            System.out.println(findNewVehicleType.getCreator());
            return false;
        });
    }

}



```


```sql

Sql: update `cr_vehicle_type` set `creator` = ?  where `vehicle_type_id` = ?
Sql: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
101忠实的大叔


```
