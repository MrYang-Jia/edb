package com.edb.cloud.jfinal.activerecord.db.vo;

import lombok.Data;

import javax.persistence.Column;
import java.util.Date;


/**
 * @ClassName VehicleTypeVo
 * @Description: 车辆类型vo视图单位
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Data
public class VehicleTypeVo {

    /**
     * 类型名称
     */
    private  String vehicleTypeName;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     * 特殊示例 : 驼峰字段和数据库字段转驼峰不区分大小写还无法匹配的情况下，通过column指定
     */
    @Column(name="CREATE_TIME")
    private Date createDate;



}
