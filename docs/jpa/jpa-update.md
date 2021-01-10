
# JPA 使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

**基于Java的相关用例**

- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

```java

package com.edbplus.db.jpa;
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

- [基于JPA对象的 update 的相关测试案例](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaUpdate.java)

```java
package com.edbplus.db.jpa;

import cn.hutool.core.bean.BeanUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName JpaUpdate
 * @Description: 更新测试 -- 推荐使用方案3 testUpdateFunThree
 * @Author 杨志佳
 * @Date 2020/10/21
 * @Version V1.0
 **/
public class JpaUpdate  extends BaseTest {


    @Test
    public void testUpdate(){
        // 方案1 -- 非null值的全量更新
        // 数据对象
        VehicleType vehicleType = new VehicleType();
        vehicleType.setVehicleTypeName("原:小汽车");
        vehicleType.setCreator("小陈陈");
        EDb.save(vehicleType);
        // 改
        vehicleType.setVehicleTypeName("改:小汽车");
        // 目前市面上用得最多的更新方式之一 ，全量提交，只提交 非null值的部分
        EDb.update(vehicleType);        //

        // 删掉这个对象
        EDb.deleteById(vehicleType);


    }

    /**
     * 方案2 对比更新，只提交修改后的部分
     */
    @Test
    public void testUpdateFunTwo(){
        // 方案2 -- 对比更新，只提交修改后的部分
        // 数据对象
        VehicleType vehicleType = new VehicleType();
        vehicleType.setVehicleTypeName("原:小汽车");
        vehicleType.setCreator("小陈陈");
        EDb.save(vehicleType);

        // 定义一个新的变量
        VehicleType oldVehicleType = new VehicleType();
        // 记录之前的记录结果集
        BeanUtil.copyProperties(vehicleType,oldVehicleType);

        // 执行相关记录的更新变更
        // 这里由于用了对比更新，所以对象是可以重置为 null 的
        vehicleType.setCreator(null);
        vehicleType.setModifyTime(new Date());
        // 注意：这里需要将原来的对象赋予
        EDb.update(oldVehicleType,vehicleType);
        // 删除
        EDb.deleteById(vehicleType);

    }

    /**
     * 方案3 -- 直接更新数据库字段
     * 相对来说比较推荐该方式，对照下需要更新的字段，然后进行变更
     */
    @Test
    public void testUpdateFunThree(){
        Map<String,Object> updateMap = new HashMap<>();
        // 必须指定数据库的主键
        updateMap.put("VEHICLE_TYPE_ID",1);
        // 需要更新的字段 ，可以允许为null或其他对象类型
        updateMap.put("CREATOR",null);
        updateMap.put("MODIFY_TIME",new Date());
        EDb.update(VehicleType.class,updateMap);
    }




}



```



```sql

方案1:
Sql: insert into `cr_vehicle_type`(`creator`, `vehicle_type_id`, `vehicle_type_name`, `create_time`, `modifier`, `modify_time`) values(?, ?, ?, ?, ?, ?)
Sql: update `cr_vehicle_type` set `creator` = ? , `vehicle_type_name` = ?  where `vehicle_type_id` = ?
Sql: delete from `cr_vehicle_type` where `vehicle_type_id` = ?

方案2:
Sql: insert into `cr_vehicle_type`(`creator`, `vehicle_type_id`, `vehicle_type_name`, `create_time`, `modifier`, `modify_time`) values(?, ?, ?, ?, ?, ?)
Sql: update `cr_vehicle_type` set `creator` = ? , `modify_time` = ?  where `vehicle_type_id` = ?
Sql: delete from `cr_vehicle_type` where `vehicle_type_id` = ?

方案3:
Sql: update `cr_vehicle_type` set `creator` = ? , `modify_time` = ?  where `vehicle_type_id` = ?



```
