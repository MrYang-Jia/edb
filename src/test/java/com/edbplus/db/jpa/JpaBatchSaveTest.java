package com.edbplus.db.jpa;

import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
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
     * 大数据常用方法 -- insertValues
     */
    @Test
    public void insertValues(){
        //
        EDb.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            long start = System.currentTimeMillis();
            List<VehicleType> saveList = new ArrayList<>();
            VehicleType vehicleType =null;
            // 插入数量自己预设
            for(int i=0;i<1000;i++){
                // 数据对象
                vehicleType = new VehicleType();
                vehicleType.setVehicleTypeName("车辆类型-"+i);
                vehicleType.setCreatorName("小M-"+i);
                saveList.add(vehicleType);
            }
            // insertValues 无id返回值，建议大量数据插入时，可预分配id给数组对象
            // 批量插入 -- 以每批次插入100条数据位例子 ，该模式 id 不会回填，所以只返回操作的插入结果
            int count=EDb.use().insertValues(VehicleType.class,saveList,1000);
            System.out.println(count);
            System.out.println("耗时:"+(System.currentTimeMillis()-start));
            return false;
        });
    }

    /**
     * 基于普通关系型数据库的批量插入操作
     */
    @Test
    public void batchSave(){
        //
        EDb.txInNewThread(Connection.TRANSACTION_SERIALIZABLE, () -> {
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
            // 原 Db.batchSave 无返回id，这里主要测试返回id的Jpa对象方法
            EDb.batchSaveRid(VehicleType.class,saveList,100);
            //
            for(VehicleType v:saveList){
                System.out.println("主键键值:" + v.getVehicleTypeId());
                System.out.println("主键键值:" + v.getCreateTime());
            }

            System.out.println("耗时:"+(System.currentTimeMillis()-start));
            return false;
        });

        try {
            Thread.sleep(2*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 批量插入 - pg
     */
    @Test
    public void batchSaveForPg(){
        EDbPro pgDbPro =  EDb.use("xzw");

        pgDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
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
            // 原 Db.batchSave 无返回id，这里主要测试返回id的Jpa对象方法
            pgDbPro.batchSaveRid(VehicleType.class,saveList,100);
            //
            for(VehicleType v:saveList){
                System.out.println("主键键值:" + v.getVehicleTypeId());
                System.out.println("主键键值:" + v.getCreateTime());
            }

            System.out.println("耗时:"+(System.currentTimeMillis()-start));
            return false;
        });

        try {
            Thread.sleep(2*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    /**
     * 大数据常用方法 -- insertValues - pg模式测试
     */
    @Test
    public void insertValuesForPg(){
        EDbPro pgDbPro =  EDb.use("xzw");
        //
        pgDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            long start = System.currentTimeMillis();
            List<VehicleType> saveList = new ArrayList<>();
            VehicleType vehicleType =null;
            // 插入数量自己预设
            for(int i=0;i<1000;i++){
                // 数据对象
                vehicleType = new VehicleType();
                vehicleType.setVehicleTypeName("车辆类型-"+i);
                vehicleType.setCreatorName("小M-"+i);
                saveList.add(vehicleType);
            }
            // insertValues 无id返回值，建议大量数据插入时，可预分配id给数组对象
            // 批量插入 -- 以每批次插入100条数据位例子 ，该模式 id 不会回填，所以只返回操作的插入结果
            int count = pgDbPro.insertValues(VehicleType.class,saveList,1000);
            System.out.println(count);
            System.out.println("耗时:"+(System.currentTimeMillis()-start));
            return false;
        });
    }

}
