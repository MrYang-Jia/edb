package com.edbplus.db.jpa;

import cn.hutool.core.util.ReflectUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.EDbTemplate;
import com.edbplus.db.annotation.EDbView;
import com.edbplus.db.dto.FieldAndView;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.view.VehicleView;
import com.edbplus.db.jpa.view.query.WhereKvParams;
import com.edbplus.db.util.EDbPageUtil;
import com.edbplus.db.util.EDbViewUitl;
import com.edbplus.db.util.bean.EDbBeanUtil;
import com.jfinal.plugin.activerecord.Page;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
        System.out.println("==>"+EDb.view(vehicleView,"whereKv").getWhereKv());
        System.out.println("==>"+vehicleView.getWhereKv());
        // 返回查询的对象      ;
        System.out.println(EDb.view(vehicleView,"crVehicleTypeView").getCrVehicleTypeView());
        // 返回查询列表
        System.out.println(EDb.view(vehicleView,"crVehicleTypesView").getCrVehicleTypesView());
        // 返回查询page列表 -- 已兼容springData page返回结果页，便于适配，注意 就jfinal-page 起始页是从1开始
        System.out.println("==>"+EDb.view(vehicleView,"crVehicleTypePageView",1,10).getCrVehicleTypePageView().getPageNumber());

        // 返回查询page列表 -- 已兼容springData page返回结果页，便于适配,注意spring的起始页是从0开始
//        System.out.println("==》"+EDb.view(vehicleView,"crVehicleTypeSpringPageView",1,10,100).getCrVehicleTypeSpringPageView().getPageable().getPageNumber());
    }


    @Test
    public void testEDbViewUitl(){
        EDbPro eDbPro = EDb.use();

        VehicleView vehicleView = new VehicleView();
        WhereKvParams whereKvParams = new WhereKvParams();
        whereKvParams.setVehicleModeName("罐车");
        vehicleView.setWhereKv(whereKvParams);
        vehicleView.setVehicleTypeId(100L);
        String fieldName = "crVehicleTypePageView"; // 直接从标记的 @View 对象的字段名复制过来
        fieldName = "crVehicleTypeView";
        Integer pageNo = 1;
        Integer pageSize = 10;

        EDbViewUitl.loadView(vehicleView,fieldName,eDbPro,pageNo,pageSize); // 加载视图

        System.out.println(vehicleView.getCrVehicleTypeView());
    }

}
