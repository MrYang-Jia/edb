package com.edb.cloud.jfinal.activerecord.db.jpa;

import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edb.cloud.jfinal.activerecord.db.jpa.view.VehicleView;
import com.edb.cloud.jfinal.activerecord.db.jpa.model.CrVehicleTypeMode;
import com.edb.cloud.jfinal.activerecord.db.jpa.view.query.WhereKvParams;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

@Slf4j
public class EDbViewTest extends BaseTest {

    /**
     * 视图查询
     */
    @Test
    public void testView(){
     //
        VehicleView vehicleView = new VehicleView();

        WhereKvParams whereKvParams = new WhereKvParams();
        whereKvParams.setVehicleModeName("类型");
        //
        vehicleView.setWhereKv(whereKvParams);
        // 指定id -- 将作为查询参数传递到视图对象里
        vehicleView.setVehicleTypeId(100L);
//        // 设置对象
//        CrVehicleTypeMode crVehicleTypeMode = new CrVehicleTypeMode();
//        crVehicleTypeMode.setIsDel(1);
//        vehicleView.setCrVehicleTypeMode(crVehicleTypeMode);
        log.info("========================================");
        // 返回查询的对象      ;
        System.out.println(EDb.getView(vehicleView).getCrVehicleType());
        // 返回查询列表
        System.out.println(EDb.getView(vehicleView).getCrVehicleTypes());
    }

}
