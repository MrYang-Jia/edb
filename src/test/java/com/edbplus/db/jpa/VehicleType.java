package com.edbplus.db.jpa;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.annotation.EDbSave;
import com.edbplus.db.annotation.EDbUpdate;
import com.edbplus.db.dto.FieldAndColumn;

import javax.persistence.*;
import com.edbplus.db.jpa.model.base.BaseVehicleType;

/**
 * @program: dac
 * @description: CCS车辆类型表 - 实体
 * @author: MrYang
 * @create:2020-10-20 21:23:20
 **/
@Table(name = "cr_vehicle_type")
public class VehicleType extends BaseVehicleType{

    /**
     * 保存前
     * @param saveMap -- 准备执行保存的相关字段
     * @param coumns -- 保存对象的所有字段信息
     */
    @EDbSave
    public void beforeSave(Map<String,Object> saveMap,List<FieldAndColumn> coumns){

        // coumns 取出的是jpa对象上的 @Column 集合，用于判断时，建议取统一小写去判断字段，避免研发人员开发时不小心大写或小写定义问题，导致数据更新不一致的情况发生
        if(saveMap.get("CREATE_TIME")== null){
            saveMap.put("CREATE_TIME",new Date());
        }
        // 忽视大小写
        if(saveMap.get("MODIFY_tiME")== null){
            saveMap.put("MODIFY_TIME",new Date());
        }
        System.out.println("执行 @EDbSave 后："+ JSONUtil.toJsonStr(saveMap));
    }

    /**
     * 更新前
     * @param updateMap -- 准备执行更新的相关字段
     * @param coumns
     */
    @EDbUpdate
    public void beforeUpdate(Map<String,Object> updateMap, List<FieldAndColumn> coumns){
        // 忽视大小写
        if(updateMap.get("MODIFY_TIME")==null){
            updateMap.put("MODIFY_TIME", new Date());
        }else
        {
            // + 5秒
            updateMap.put("MODIFY_TIME", DateUtil.offsetSecond((Date) updateMap.get("MODIFY_TIME"),5));
        }
        System.out.println("执行 @EDbUpdate 后："+JSONUtil.toJsonStr(updateMap));
    }


    // 实现自定义枚举


    // 实现基于该业务表的相关处理方法


}
