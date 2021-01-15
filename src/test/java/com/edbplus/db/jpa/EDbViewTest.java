package com.edbplus.db.jpa;

import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.view.VehicleView;
import com.edbplus.db.jpa.view.query.WhereKvParams;
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
        whereKvParams.setVehicleModeName("罐车");
        //
        vehicleView.setWhereKv(whereKvParams);
        // 指定id -- 将作为查询参数传递到视图对象里
        vehicleView.setVehicleTypeId(100L);
//        // 设置对象
//        CrVehicleTypeMode crVehicleTypeMode = new CrVehicleTypeMode();
//        crVehicleTypeMode.setIsDel(1);
//        vehicleView.setCrVehicleTypeMode(crVehicleTypeMode);
        log.info("========================================");
        System.out.println("==>"+EDb.view(vehicleView).getWhereKv());
        System.out.println("==>"+vehicleView.getWhereKv());
        // 返回查询的对象      ;
        System.out.println(EDb.view(vehicleView).getCrVehicleTypeView());
        // 返回查询列表
        System.out.println(EDb.view(vehicleView).getCrVehicleTypesView());
        // 返回查询page列表 -- 已兼容springData page返回结果页，便于适配，注意 就jfinal-page 起始页是从1开始
        System.out.println("==>"+EDb.view(vehicleView,1,10).getCrVehicleTypePageView().getPageNumber());

        // 返回查询page列表 -- 已兼容springData page返回结果页，便于适配,注意spring的起始页是从0开始
        System.out.println("==》"+EDb.view(vehicleView,1,10).getCrVehicleTypeSpringPageView().getPageable().getPageNumber());
    }

}
