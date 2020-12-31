package com.edb.cloud.jfinal.activerecord.db.jpa.vo;

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;
import java.math.BigDecimal;
/**
 * @program: xzw-dac
 * @description: CCS车辆类型表 - Vo实体
 * @author: MrYang
 * @create:2020-12-04 18:18:08
 **/
@Data
public class CrVehicleTypeVo implements Serializable{

   /**字段说明:VEHICLE_TYPE_ID*/
   /**描述说明:主键ID*/
   private  Integer vehicleTypeId;

   /**字段说明:VEHICLE_TYPE_NAME*/
   /**描述说明:车辆类型名称*/
   private  String vehicleTypeName;

   /**字段说明:CREATOR*/
   /**描述说明:创建人*/
   private  String creator;

   /**字段说明:CREATE_TIME*/
   /**描述说明:创建时间*/
   private  Date createTime;

   /**字段说明:MODIFIER*/
   /**描述说明:修改人*/
   private  String modifier;

   /**字段说明:MODIFY_TIME*/
   /**描述说明:修改时间*/
   private  Date modifyTime;

   /**字段说明:IS_DEL*/
   /**描述说明:是否删除*/
   private  Integer isDel;


}