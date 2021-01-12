package com.edbplus.db.jpa.model.base;

import java.io.Serializable;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;
// 唯一值标记


/**
 * @program: dac
 * @description: CCS车辆类型表 - 实体
 * @author: MrYang
 * @create:2020-10-21 17:49:41
 **/
@Data
@Table(name = "cr_vehicle_type")
public class BaseVehicleType implements Serializable{

   @Id
   @Column(name="VEHICLE_TYPE_ID")
   private Integer vehicleTypeId;

   /**字段说明:VEHICLE_TYPE_NAME*/
   /**描述说明:车辆类型名称*/
   @Column(name="VEHICLE_TYPE_NAME")
   private  String vehicleTypeName;

   /**字段说明:CREATOR*/
   /**描述说明:创建人*/
   @Column(name="CREATOR")
   private  String creator;

   /**字段说明:CREATE_TIME*/
   /**描述说明:创建时间*/
   @Column(name="CREATE_TIME")
   private  Date createTime;

   /**字段说明:MODIFIER*/
   /**描述说明:修改人*/
   @Column(name="MODIFIER")
   private  String modifier;

   /**字段说明:MODIFY_TIME*/
   /**描述说明:修改时间*/
   @Column(name="MODIFY_TIME")
   private  Date modifyTime;


}