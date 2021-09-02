### 查询车辆类型
#sql("findForLimit")
select * from cr_vehicle_type limit #para(0)
#end


#sql("findForId")
select * from cr_vehicle_type where VEHICLE_TYPE_ID = #para(0)
#end


#sql("findTf")
select VEHICLE_TYPE_ID tFid from cr_vehicle_type where VEHICLE_TYPE_ID = #para(0)
#end

#sql("updateDemo")
update cr_vehicle_type
set creator = #para(0)
where VEHICLE_TYPE_ID = #para(1)
#end


#sql("EDbViewTest")
select * from cr_vehicle_type
where 1=1
#if(vehicleTypeId)
and VEHICLE_TYPE_ID = #(vehicleTypeId)
#end
#### 假如 crVehicleTypeMode 不为 null,则判断 crVehicleTypeMode.vehicleModeName 是否不为null ，否则不做任何处理
  ### 动态查询
  #@conditionSqlWhere(whereKv,'vehicleModeName','=',' and VEHICLE_TYPE_NAME ')
#if(crVehicleTypeMode ? crVehicleTypeMode.vehicleModeName : false)
and VEHICLE_TYPE_NAME = '#(crVehicleTypeMode.vehicleModeName)'
#end
#### 假如 crVehicleTypeMode 不为 null,则判断 crVehicleTypeMode.isDel 是否不为null ，否则不做任何处理
#if(crVehicleTypeMode ? crVehicleTypeMode.isDel : false)
and is_del = '#(crVehicleTypeMode.isDel)'
#end

#end



########################################################################
#define conditionSqlWhere(condMap,filterKey,operator,queryField)
  #if(condMap)
      #if(condMap[filterKey])
            #if(operator == 'LIKE')
              #(queryField) #(operator) #para(condMap.get(filterKey)+"%")
            #else if(operator == 'ALLLIKE')
              #set(operator = 'LIKE')
              #(queryField) #(operator) #para("%" + condMap.get(filterKey).trim()+"%")
            #else
              #(queryField) #(operator) #para(condMap.get(filterKey))
            #end
      #end
  #end
#end