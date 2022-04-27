package com.edbplus.db.jpa;

import com.edbplus.db.EDbPro;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.EDb;
import com.edbplus.db.util.hutool.json.EJSONUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.*;

/**
 * @ClassName JpaEasyTest
 * @Description: jpa的简单案例
 * @Author 杨志佳
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class JpaEasyTest extends BaseTest {
    // 不能直接赋予对象，需要在beforeTest里执行
    EDbPro eDbPro = null;

    @BeforeTest
    public void initBefor(){
//        eDbPro =  EDb.use();
        eDbPro =  EDb.use("pg");
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        eDbPro.findById(VehicleType.class,1);
    }

    @Test
    public void test2(){
        int ct = eDbPro.findFirst("select count(1) ct from cr_vehicle_type").getInt("ct");
        System.out.println("ct:"+ct);
        int t =ct/3 + 1;
        int offset = 0;
        List<Record> records = null;
        int readCt = 0;
        for(int i=0;i<t;i++){
//            records = eDbPro.find("select VEHICLE_TYPE_ID from cr_vehicle_type limit ?,3",offset);
            records = eDbPro.find("select VEHICLE_TYPE_ID from cr_vehicle_type offset ? limit 3",offset);
            System.out.println(records);
            offset+=3;
            readCt += records.size();
        }
        System.out.println("打印总数:"+readCt);
        System.out.println(ct == readCt);
    }

    @Test
    public void testFirst(){
        VehicleType vehicleType = eDbPro.findFirst(VehicleType.class,"select * from cr_vehicle_type where VEHICLE_TYPE_ID =#para(vehicleTypeId)", Kv.by("vehicleTypeId",100));
        System.out.println(vehicleType);
        vehicleType.setCreateTime(new Date());
        EDb.update(vehicleType);
        Record record = eDbPro.findFirst("select * from cr_vehicle_type where VEHICLE_TYPE_ID = #para(vehicleTypeId)", Kv.by("vehicleTypeId",100));

        System.out.println(record);

//        eDbPro.findFirst("select * from dd where VEHICLE_TYPE_NAME like #para(vehicleTypeId)", Kv.by("vehicleTypeId",100));
    }

    /**
     * JPA 单体对象测试
     * 保存、查询、修改、删除
     */
    @Test
    public void oneTest(){

         // 数据库在远程 -- 所以耗时会偏高，但是相对比较理想，换成spring相关体系的耗时会更好，大家有兴趣的话，可以自己简单地做个对比测试
         // 默认的 jfinal 事务支持
        eDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
             long start = System.currentTimeMillis();
             //  === 保存部分 ===
             start = System.currentTimeMillis();
             // 数据对象
             VehicleType vehicleType = new VehicleType();
             vehicleType.setVehicleTypeName("原:小汽车");
             vehicleType.setCreatorName("小陈陈");
             vehicleType.setCreateTime(new Date());
             // 如果有多个数据库，可以用 EDb.use("数据库标识1") 指定
             eDbPro.save(vehicleType);
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             // 打印json字符串
             System.out.println("原: "+ EJSONUtil.toJsonStr(vehicleType));
             System.out.println("自增的主键id为 " + vehicleType.getVehicleTypeId());
             //  === 查询部分 ===
             VehicleType vehicleTypeFind = eDbPro.findById(VehicleType.class,vehicleType.getVehicleTypeId());
             System.out.println("保存后查: "+EJSONUtil.toJsonStr(vehicleTypeFind));
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             //  === 更新部分 ===
             vehicleTypeFind.setVehicleTypeName("改:大型卡车");
             // 数改保存
             eDbPro.update(vehicleTypeFind);
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             // 再次查询
             vehicleTypeFind = eDbPro.findById(VehicleType.class,vehicleType.getVehicleTypeId());
             System.out.println("改后查: "+EJSONUtil.toJsonStr(vehicleTypeFind));
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             start = System.currentTimeMillis();
             //  === 删除部分 ===
             eDbPro.deleteById(vehicleTypeFind);
             vehicleTypeFind = EDb.findById(VehicleType.class,vehicleType.getVehicleTypeId());
             System.out.println("删后查: "+EJSONUtil.toJsonStr(vehicleTypeFind));
             System.out.println("耗时:"+(System.currentTimeMillis()-start));
             // 由于只是做测试，所以不想直接插入到数据库，直接设置为false即可
             return false;
         }
        );
    }

    /**
     * 单个JPA对象的 更新部分字段 的测试
     */
    @Test
    public void updateForId(){
        // EDb.update 在事务模式下，可以直接保存更新记录变化
        eDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            // 构建更新对象主体 --
            VehicleType vehicleType = new VehicleType();
            // 必须指定要更新的对象id
            vehicleType.setVehicleTypeId(101);
            vehicleType.setModifyTime(new Date());
            // 需要更新的内容
            vehicleType.setCreatorName("101忠实的大叔");
            // 更新
            eDbPro.update(vehicleType);
            VehicleType findNewVehicleType = eDbPro.findById(VehicleType.class, 101);
            //
            System.out.println(findNewVehicleType.getCreatorName());
            System.out.println(EJSONUtil.toJsonStr(findNewVehicleType));
            return false;
        });
    }

    /**
     * 循环更新
     */
    @Test
    public void updateForList(){
        //
        eDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            // 构建更新对象主体
            List<VehicleType> vehicleTypes = eDbPro.find(VehicleType.class,"select * from cr_vehicle_type limit 5");
            for(VehicleType vehicleType : vehicleTypes){
                // 更新时间变更
                vehicleType.setModifyTime(new Date());
                // 需要更新的内容
                vehicleType.setCreatorName("101忠实的大叔");
                eDbPro.update(vehicleType);
            }
            vehicleTypes = eDbPro.find(VehicleType.class,"select * from cr_vehicle_type limit 5");
            System.out.println(EJSONUtil.toJsonStr(vehicleTypes));

            return false;
        });
    }


    /**
     * 根据多个主键键值查询`
     */
    @Test
    public void findByIds(){

        System.out.println("==>"+ EJSONUtil.toJsonStr(eDbPro.findByIds(VehicleType.class, Arrays.asList(100,101,102)).get(0)));

        eDbPro.findByIds(VehicleType.class, "100,101,102",",");
    }


    /**
     * 保存测试
     */
    @Test
    public void testSave(){
        eDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
                    long start = System.currentTimeMillis();
                    // pg 的格式
            if(eDbPro.getConfig().getDialect() instanceof PostgreSqlDialect){
                eDbPro.update("INSERT INTO cr_vehicle_type(VEHICLE_TYPE_NAME, CREATOR, CREATE_TIME, MODIFIER, MODIFY_TIME) VALUES ( '罐车_变更', '小陈', NULL, NULL, NULL);\n");

            }else{
                eDbPro.update("INSERT INTO `tra_log`.`cr_vehicle_type`(`VEHICLE_TYPE_NAME`, `CREATOR`, `CREATE_TIME`, `MODIFIER`, `MODIFY_TIME`) VALUES ( '罐车_变更', '小陈', NULL, NULL, NULL);\n");

            }

                    System.out.println("耗时:"+(System.currentTimeMillis()-start));
                    return false;
                }
        );
    }

    @Test
    public void testFindByPara(){
        List<VehicleType> vehicleTypes = eDbPro.find(VehicleType.class,"select * from cr_vehicle_type where VEHICLE_TYPE_ID= ? ",100);
        System.out.println("1-->"+vehicleTypes);
        vehicleTypes = eDbPro.find(VehicleType.class,"select * from cr_vehicle_type where VEHICLE_TYPE_ID= #para(vehicleTypeId)", Kv.by("vehicleTypeId",100));
        System.out.println("2-->"+vehicleTypes);

    }

//
//    /**
//     * PG 的简单案例
//     */
//    @Test
//    public void testPg(){
//        EDbPro pgDbPro =  EDb.use("pg");
//        VehicleType crVehicleType = pgDbPro.findById(VehicleType.class,100);
//
//        System.out.println(crVehicleType);
//        crVehicleType.setCreateTime(new Date());
//        // 保存对象
//        pgDbPro.update(crVehicleType);
//
//        pgDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
//            crVehicleType.setModifier("小沫沫");
//            pgDbPro.update(crVehicleType);
//            pgDbPro.deleteById(crVehicleType);
//            return false;
//        });
//
//
//    }

}
