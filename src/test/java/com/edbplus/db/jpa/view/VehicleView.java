package com.edbplus.db.jpa.view;

import com.edbplus.db.annotation.EDbView;
import com.edbplus.db.jpa.model.CrVehicleType;
import com.edbplus.db.jpa.model.CrVehicleTypeMode;
import com.edbplus.db.jpa.view.query.WhereKvParams;
import com.jfinal.plugin.activerecord.Page;
import lombok.Data;

import java.util.List;

/**
 * 车辆视图
 */
@Data
public class VehicleView {

    // 查询可选对象1
    private Long vehicleTypeId;
    // 查询可选对象2
    private CrVehicleTypeMode crVehicleTypeMode;

    private WhereKvParams whereKv;

    // 指定视图 -- 在sql模板里，可以通过配置 对象1 和 查询对象2 进行组合查询，对于入参和出参的定义也能更加清晰直观,同时代码检索也更加容易
    @EDbView(name = "test.EDbViewTest")
    private CrVehicleType crVehicleTypeView;

    @EDbView(name = "test.EDbViewTest")
    private List<CrVehicleType> crVehicleTypesView;

    @EDbView(name = "test.EDbViewTest")
    private Page<CrVehicleType> crVehicleTypePageView;

    @EDbView(name = "test.EDbViewTest")
    private org.springframework.data.domain.Page<CrVehicleType> crVehicleTypeSpringPageView;


}
