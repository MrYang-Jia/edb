package com.edbplus.cloud.jfinal.activerecord.db.jpa.model.base;

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * @program: xzw-dac
 * @description: CCS车辆类型规格关系表 - 实体
 * @author: MrYang
 * @create:2020-12-07 14:04:30
 **/
@Data
@Table(name = "cr_vehicle_type_mode_rel")
public class BaseCrVehicleTypeModeRel implements Serializable{

   @Id
   @Column(name="VEHICLE_TYPE_MODE_REL_ID")
   private Integer vehicleTypeModeRelId;

   /**字段说明:VEHICLE_TYPE_MODE_ID*/
   /**描述说明:车辆类型规格ID*/
   @Column(name="VEHICLE_TYPE_MODE_ID")
   private  Integer vehicleTypeModeId;

   /**字段说明:VEHICLE_TYPE_ID*/
   /**描述说明:车辆类型ID*/
   @Column(name="VEHICLE_TYPE_ID")
   private  Integer vehicleTypeId;

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

   /**字段说明:IS_DEL*/
   /**描述说明:是否删除*/
   @Column(name="IS_DEL")
   private  Integer isDel;


}