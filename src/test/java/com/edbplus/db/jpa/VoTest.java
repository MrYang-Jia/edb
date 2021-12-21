package com.edbplus.db.jpa;

import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.vo.CrVehicleTypeVo;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoTest extends BaseTest {

    /**
     * vo对象赋值测试
     */
    @Test
    public void test(){
       List<CrVehicleTypeVo> results = EDb.find(CrVehicleTypeVo.class,"select * from cr_vehicle_type where is_del = 1 limit 2");
       System.out.println("==>"+results);
       Page page = EDb.paginate(CrVehicleTypeVo.class,1,2,"select * from cr_vehicle_type ");
       System.out.println(page.getList());

//       System.out.println(EDb.findFirst("select * from cr_vehicle_type where creator='xj' "));

        CrVehicleTypeVo vehicleTypeVo = EDb.use().templateByString("select * from cr_vehicle_type where creator='创建人-1' ").findFirst(CrVehicleTypeVo.class);
        System.out.println("==>"+vehicleTypeVo);

        //CrVehicleTypeVo
    }

    @Test
    public void test2(){
        Arrays.asList("1,2".split(","))
                .forEach(obj -> {
                    System.out.println(obj);
                });

    }

    @Test
    public void test3(){
        int totaolCount = 3;
        int batchSize = 5;
        List<Integer> integers = new ArrayList<>();
        for (int j=0;j<100;j++){
            integers.add(j);
        }
        if(totaolCount>0 && batchSize>0){
            int t = 0;
            int ct = totaolCount/batchSize + 1;
            fj : for(int jt=0;jt<ct;jt++){
                for (int j=0;j<batchSize;j++){
                    if(t>=totaolCount){
                        System.out.println("一批次");
                        break fj;
                    }
                    System.out.println(integers.get(t));
                    t++;
                }
                //
                System.out.println("一批次");
            }
        }
    }

}
