package com.edbplus.db.jpa.model;

import javax.persistence.*;

import com.edbplus.db.EDbDao;
import com.edbplus.db.jpa.model.base.BaseCrVehicleTypeMode;

/**
 * @program: dac
 * @description: CCS车辆类型规格表 - 实体
 * @author: MrYang
 * @create:2020-11-14 16:03:22
 **/
@Table(name = "cr_vehicle_type_mode")
public class CrVehicleTypeMode extends BaseCrVehicleTypeMode{

    public static final EDbDao<CrVehicleTypeMode> dao = new EDbDao<CrVehicleTypeMode>(CrVehicleTypeMode.class);
    // 实现自定义枚举

    // 实现基于该业务表的相关处理方法


}