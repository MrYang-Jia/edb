package com.edbplus.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.core.bean.BeanUtil;
import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

        System.out.println(vehicleType.getCreateTime());

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
        EDb.updateCompare(oldVehicleType,vehicleType);
        System.out.println(vehicleType.getCreateTime());
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


    /**
     * batchUpdate 相关测试
     */
    @Test
    public void batchUpdate(){
        List<VehicleType> result = EDb.use().find(VehicleType.class," select * from cr_vehicle_type limit 3 ");
        // 返回Jpa对象
        System.out.println(result);

        // 批量修改
        for(int i=0;i<result.size();i++){
            result.get(i).setCreator("创建人-"+i);
            // jpa对象的逐一更新
            //   EDb.use().update(result.get(i));
        }

        // jpa模式的批量更新，必须保证每条记录变更字段个数必须一致，否则会导致更新异常
        EDb.use().batchUpdate(VehicleType.class,result,1000);


    }



}
