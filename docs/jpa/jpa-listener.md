
# JPA 全局或局部 save/update 监听使用案例

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

### EDb自定义注解讲解
- @EDbSave 定义jpa保存前的执行方法
  - 作用域：
    - save(T)
    - batchSaveRid(class<T>,List<T>,int)
    - batchSave(class<T>,List<T>,int)
- @EDbUpdate 定义jpa更新前的执行方法
  - 作用域：
    - update(T)
    - update(class<T>,Map)
    - update(T1,T2)
    - batchUpdate(class<T>,List<T>,int)
    
### EDb审计对象（监听对象）讲解    
- EDbListener.java 定义全局jpa对象保存、更新前的方法，在 @EDbSave @EDbUpdate 定义的方法执行之前触发

**基于Java的相关用例**

**基于全局对象 EDbListener.java 监听类的定义**
- [引用 JpaListener 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/base/JpaListener.java)

```java
package com.edb.cloud.jfinal.activerecord.db.base;

import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColumn;
import com.edb.cloud.jfinal.activerecord.db.listener.EDbListener;

import java.util.List;
import java.util.Map;

public class JpaListener implements EDbListener {

    /**
     * jpa对象统一保存前的实现逻辑
     * @param tableName -- 表名
     * @param saveMap -- 准备执行保存的相关字段
     * @param coumns -- 表字段的相关信息
     */
    @Override
    public void beforeSave(String tableName,Map<String, Object> saveMap, List<FieldAndColumn> coumns) {
        System.out.println("执行保存前的监听");
    }

    /**
     * jpa对象统一更新前的实现逻辑
     * @param tableName -- 表名
     * @param updateMap -- 准备执行更新的相关字段
     * @param coumns -- 表字段的相关信息
     */
    @Override
    public void beforeUpdate(String tableName,Map<String, Object> updateMap, List<FieldAndColumn> coumns) {
        System.out.println("执行更新前的监听");
    }
    
     /**
      * jpa统一对象删除前的操作
      * @param eDbPro -- 数据库操作对象
      * @param jpaClass -- 表对象
      * @param deleteMaps -- 指定需要删除的集合(默认只给予主键对应的id)
      * @param coumns -- 表字段的相关信息
      * @return -- 返回 true 或者 NULL 则继续执行，返回false则拦截不再做直接删除处理
      */
     @Override
     public EDBListenerResult beforeDelete(EDbPro eDbPro,Class jpaClass,List<Map<String,Object>> deleteMaps, List<FieldAndColumn> coumns) {
         System.out.println("=== 进入delete监听 ===");
         EDBListenerResult edbListenerResult = new EDBListenerResult();
         //
         for(FieldAndColumn fieldAndColumn : coumns){
             // 如果包含伪删除 REMOVE_FLAG 字段，则全部统一替换成 update 操作
             if(fieldAndColumn.getColumn().name().toLowerCase().equals("REMOVE_FLAG".toLowerCase())){
                 // 阻止执行 delete 方法
                 edbListenerResult.setNextToDo(false);
                 // 根据情况返回指定的方法结果
                 if(deleteMaps.size() > 1){
                     int updateCt = 0;
                     for(Map<String,Object> delete:deleteMaps){
                         // 统一变更
                         delete.put("REMOVE_FLAG","Y");
                         // 更新该对象
                         eDbPro.update(jpaClass,delete);
                         updateCt++;
                     }
                     // 设置返回结果
                     edbListenerResult.setReturnResult(true);
                     // 设置返回结果影响的条数
                     edbListenerResult.setReturnCt(updateCt);
                     return edbListenerResult;
                 }else{
                     // 单体删除
                     deleteMaps.get(0).put("REMOVE_FLAG","Y");
                     // 设置返回结果
                     edbListenerResult.setReturnResult(eDbPro.update(jpaClass,deleteMaps.get(0)));
                     return edbListenerResult;
                 }
    
             }
         }
         return null;
     }


}


```


- [引用 Vehicle 实体内容](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/VehicleType.java)

**基于单体对象上自定义监听方法的应用 （@EDbSave、@EDbUpdate）**

```java

package com.edb.cloud.jfinal.activerecord.db.jpa;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.edb.cloud.jfinal.activerecord.db.annotation.EDbSave;
import com.edb.cloud.jfinal.activerecord.db.annotation.EDbUpdate;
import com.edb.cloud.jfinal.activerecord.db.dto.FieldAndColumn;
import lombok.Data;
import javax.persistence.*;
import com.edb.cloud.jfinal.activerecord.db.jpa.model.base.BaseVehicleType;

/**
 * @program: xzw-dac
 * @description: CCS车辆类型表 - 实体
 * @author: MrYang
 * @create:2020-10-20 21:23:20
 **/
@Table(name = "cr_vehicle_type")
public class VehicleType extends BaseVehicleType{

    /**
     * 保存前
     * @param saveMap
     * @param coumns -- 保存对象的所有字段信息
     */
    @EDbSave
    public void beforeSave(Map<String,Object> saveMap,List<FieldAndColumn> coumns){

        // coumns 取出的是jpa对象上的 @Column 集合，用于判断时，建议取统一小写去判断字段，避免研发人员开发时不小心大写或小写定义问题，导致数据更新不一致的情况发生
        if(saveMap.get("CREATE_TIME")== null){
            saveMap.put("CREATE_TIME",new Date());
        }
        // 忽视大小写
        if(saveMap.get("MODIFY_tiME")== null){
            saveMap.put("MODIFY_TIME",new Date());
        }
        System.out.println("执行 @EDbSave 后："+ JSONUtil.toJsonStr(saveMap));
    }

    /**
     * 更新前
     * @param updateMap
     * @param coumns
     */
    @EDbUpdate
    public void beforeUpdate(Map<String,Object> updateMap, List<FieldAndColumn> coumns){
        // 忽视大小写
        if(updateMap.get("MODIFY_TIME")==null){
            updateMap.put("MODIFY_TIME", new Date());
        }else
        {
            // + 5秒
            updateMap.put("MODIFY_TIME", DateUtil.offsetSecond((Date) updateMap.get("MODIFY_TIME"),5));
        }
        System.out.println("执行 @EDbUpdate 后："+JSONUtil.toJsonStr(updateMap));
    }

    // 实现自定义枚举

    // 实现基于该业务表的相关处理方法
    
}



```





** 基于JPA对象的简单示例增删改查(监听的审计对象添加后) **

- [单体jpa的测试案例](../../src/test/java/com/edb/cloud/jfinal/activerecord/db/jpa/JpaEasyTest.java)

```java
package com.edb.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edb.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edb.cloud.jfinal.activerecord.db.EDb;
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
执行保存前的监听
执行 @EDbSave 后：["creator=小陈陈","vehicle_type_id=null","vehicle_type_name=原:小汽车","create_time=Fri Oct 30 16:41:32 CST 2020","modifier=null","modify_time=Fri Oct 30 16:41:32 CST 2020"]
2020-10-30 16:41:32.583 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,206] - edb-sql-?: insert into `cr_vehicle_type`(`creator`, `vehicle_type_id`, `vehicle_type_name`, `create_time`, `modifier`, `modify_time`) values(?, ?, ?, ?, ?, ?)
2020-10-30 16:41:32.602 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,207] - edb-sql-params: {
  "0" : "小陈陈",
  "1" : null,
  "2" : "原:小汽车",
  "3" : 1604047292573,
  "4" : null,
  "5" : 1604047292573
}
2020-10-30 16:41:32.602 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,229] - edb-sql-real: insert into `cr_vehicle_type`(`creator`, `vehicle_type_id`, `vehicle_type_name`, `create_time`, `modifier`, `modify_time`) values('小陈陈', null, '原:小汽车', 'Fri Oct 30 16:41:32 CST 2020', null, 'Fri Oct 30 16:41:32 CST 2020')
耗时:50
原: {"vehicleTypeId":20538,"creator":"小陈陈","modifyTime":1604047292573,"createTime":1604047292573,"vehicleTypeName":"原:小汽车"}
自增的主键id为 20538
2020-10-30 16:41:32.623 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,206] - edb-sql-?: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
2020-10-30 16:41:32.623 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,207] - edb-sql-params: {
  "0" : 20538
}
2020-10-30 16:41:32.624 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,229] - edb-sql-real: select * from `cr_vehicle_type` where `vehicle_type_id` = '20538'
保存后查: {"vehicleTypeId":20538,"creator":"小陈陈","modifyTime":1604047292000,"createTime":1604047292000,"vehicleTypeName":"原:小汽车"}
耗时:14
执行更新前的监听
执行 @EDbUpdate 后：["creator=小陈陈","vehicle_type_id=20538","vehicle_type_name=改:大型卡车","create_time=2020-10-30 16:41:32.0","modify_time=2020-10-30 16:41:37"]
2020-10-30 16:41:32.634 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,206] - edb-sql-?: update `cr_vehicle_type` set `creator` = ? , `vehicle_type_name` = ? , `create_time` = ? , `modify_time` = ?  where `vehicle_type_id` = ?
2020-10-30 16:41:32.635 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,207] - edb-sql-params: {
  "0" : "小陈陈",
  "1" : "改:大型卡车",
  "2" : 1604047292000,
  "3" : null,
  "4" : 20538
}
2020-10-30 16:41:32.635 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,229] - edb-sql-real: update `cr_vehicle_type` set `creator` = '小陈陈' , `vehicle_type_name` = '改:大型卡车' , `create_time` = '2020-10-30 16:41:32.0' , `modify_time` = null  where `vehicle_type_id` = '20538'
耗时:7
2020-10-30 16:41:32.638 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,206] - edb-sql-?: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
2020-10-30 16:41:32.638 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,207] - edb-sql-params: {
  "0" : 20538
}
2020-10-30 16:41:32.638 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,229] - edb-sql-real: select * from `cr_vehicle_type` where `vehicle_type_id` = '20538'
改后查: {"vehicleTypeId":20538,"creator":"小陈陈","modifyTime":1604047297000,"createTime":1604047292000,"vehicleTypeName":"改:大型卡车"}
耗时:3
2020-10-30 16:41:32.642 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,206] - edb-sql-?: delete from `cr_vehicle_type` where `vehicle_type_id` = ?
2020-10-30 16:41:32.642 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,207] - edb-sql-params: {
  "0" : 20538
}
2020-10-30 16:41:32.642 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,229] - edb-sql-real: delete from `cr_vehicle_type` where `vehicle_type_id` = '20538'
2020-10-30 16:41:32.644 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,206] - edb-sql-?: select * from `cr_vehicle_type` where `vehicle_type_id` = ?
2020-10-30 16:41:32.644 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,207] - edb-sql-params: {
  "0" : 20538
}
2020-10-30 16:41:32.644 [,] [main] DEBUG c.e.c.j.a.d.d.f.EDbDruidSqlLogFilter - [sqlLog,229] - edb-sql-real: select * from `cr_vehicle_type` where `vehicle_type_id` = '20538'
删后查: null
耗时:6




```

