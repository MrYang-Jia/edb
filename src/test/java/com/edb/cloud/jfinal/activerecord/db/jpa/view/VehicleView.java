package com.edb.cloud.jfinal.activerecord.db.jpa.view;

import com.edb.cloud.jfinal.activerecord.db.annotation.EDbView;
import com.edb.cloud.jfinal.activerecord.db.jpa.model.CrVehicleType;
import com.edb.cloud.jfinal.activerecord.db.jpa.model.CrVehicleTypeMode;
import com.edb.cloud.jfinal.activerecord.db.jpa.view.query.WhereKvParams;
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

}
