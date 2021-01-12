package com.edbplus.db.jpa.model;

import com.edbplus.db.annotation.EDbRel;

import javax.persistence.*;

import com.edbplus.db.jpa.model.base.BaseCrVehicleTypeModeRel;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: dac
 * @description: CCS车辆类型规格关系表 - 实体
 * @author: MrYang
 * @create:2020-11-14 16:03:22
 **/
@Table(name = "cr_vehicle_type_mode_rel")
public class CrVehicleTypeModeRel extends BaseCrVehicleTypeModeRel {

    // 实现自定义枚举
    public static final String noDel = "noDel";
    public static final String isDel = "isDel";


    // ==========  特殊对象应用 模拟获取同一个对象不同条件下的数据获取，如果是记录不同状态的取数时非常有用 ==============
    // 实现基于该业务表的相关处理方法
    @Setter
    @Getter
    // 关联两个键值 ，并且指定是 IS_DEL = 0 的部分
    @EDbRel(relKey= noDel , relColumn = {"VEHICLE_TYPE_ID"},appendSql = " and IS_DEL = 0 ")
    private CrVehicleType crVehicleType;

    // 实现基于该业务表的相关处理方法
    @Setter
    @Getter
    // 关联两个键值 ，并且指定是 IS_DEL = 1 的部分，已删除的数据
    @EDbRel(relKey= isDel , relColumn = {"VEHICLE_TYPE_ID"},appendSql = " and IS_DEL = 1 ")
    private CrVehicleType delCrVehicleType;

    // ==========  特殊对象应用 ==============


    @Setter
    @Getter
    @EDbRel(appendSql = " and VEHICLE_TYPE_MODE_ID = #(vehicleTypeModeId) and IS_DEL = 0  ")
    private CrVehicleTypeMode crVehicleTypeMode;




    // A to B ==》 获取 A 赋予 B
//    edb.gerRel(dto.getRelObj,dto);

}