package com.edbplus.db.jpa.model;

import javax.persistence.*;

import com.edbplus.db.annotation.EDbRel;
import com.edbplus.db.jpa.model.base.BaseCrVehicleType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @program: dac
 * @description: CCS车辆类型表 - 实体
 * @author: MrYang
 * @create:2020-11-14 16:03:00
 **/
@Table(name = "cr_vehicle_type")
public class CrVehicleType extends BaseCrVehicleType{


    // 实现基于该业务表的相关处理方法
    @Setter
    @Getter
    // 可以设置最多返回20个数据，默认最多返回10个，一般是够用了，但是可以根据实际需求来调整哦
    @EDbRel(relColumn = {"VEHICLE_TYPE_ID"} )
    private List<CrVehicleTypeModeRel> crVehicleTypeModesRel;

}