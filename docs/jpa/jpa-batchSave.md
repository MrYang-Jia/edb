
# JPA 使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

**基于Java的相关用例**

- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

** 基于JPA对象的insertValues - 大量数据的快速插入 **

- [基于jpa对象 批量插入 的相关测试案例](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaBatchSaveTest.java)

```java

package com.edb.cloud.jfinal.activerecord.db.jpa;

import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.base.BaseTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName JpaBatchSaveTest
 * @Description: 批量插入的相关测试
 * @Author 杨志佳
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class JpaBatchSaveTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }


/**
     * 基于普通关系型数据库的批量插入操作
     */
    @Test
    public void batchSave(){
        //
        EDb.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            long start = System.currentTimeMillis();
            List<VehicleType> saveList = new ArrayList<>();
            VehicleType vehicleType =null;
            // 插入数量自己预设
            for(int i=1000;i<1100;i++){
                // 数据对象
                vehicleType = new VehicleType();
                // 如果是大量数据的话，建议可以提前初始化，这样子就可以不用数据库的自增
//                vehicleType.setVehicleTypeId(Long.valueOf(i));
                vehicleType.setVehicleTypeName("车辆类型-"+i);
                saveList.add(vehicleType);
            }
            // 批量插入并返回实体id
            // batchSave 无返回id，这里主要测试返回id的Jpa对象方法
            EDb.batchSaveRid(VehicleType.class,saveList,100);
            for(VehicleType v:saveList){
                System.out.println("主键键值:" + v.getVehicleTypeId());
            }

            System.out.println("耗时:"+(System.currentTimeMillis()-start));
            return false;
        });

    }

}



```

```sql
-- 耗时是基于远程单机数据库导致的，对比spring体系的简单jdbc查询来说，都差不多，甚至在对象返回这块的返回耗时，比spring体系优秀
-- 内存消耗的测试没有特意去做，但是从架构和设计角度来说是差不多的，甚至还更省内存，后期可能会补上
Sql: insert into `cr_vehicle_type`(`creator`, `vehicle_type_id`, `vehicle_type_name`) values(?, ?, ?)
主键键值:4122
主键键值:4123
...
...
主键键值:4220
主键键值:4221
-- 比 insertValues 慢点，但是有返回自增主键，在一些特殊场景下还是非常有用的
-- 内网耗时为 175 ms ，性能还不错
耗时:904

```
