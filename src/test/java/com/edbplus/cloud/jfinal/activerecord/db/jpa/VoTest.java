package com.edbplus.cloud.jfinal.activerecord.db.jpa;

import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.cloud.jfinal.activerecord.db.jpa.vo.CrVehicleTypeVo;
import com.jfinal.plugin.activerecord.Page;
import org.testng.annotations.Test;

import java.util.List;

public class VoTest extends BaseTest {

    /**
     * vo对象赋值测试
     */
    @Test
    public void test(){
       List<CrVehicleTypeVo> results = EDb.find(CrVehicleTypeVo.class,"select * from cr_vehicle_type where is_del = 1 limit 2");
       System.out.println(results);
       Page page = EDb.paginate(CrVehicleTypeVo.class,1,2,"select * from cr_vehicle_type ");
       System.out.println(page.getList());

        //CrVehicleTypeVo
    }


}
