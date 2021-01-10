package com.edbplus.db.jfinal.activerecord.db.base;

import com.edbplus.db.EDbPro;
import com.edbplus.db.dto.EDBListenerResult;
import com.edbplus.db.dto.FieldAndColumn;
import com.edbplus.db.listener.EDbListener;

import java.util.*;

public class JpaListener implements EDbListener {

    /**
     * jpa对象统一保存前的实现逻辑
     * @param jpaClass -- 表对象
     * @param saveMap -- 准备执行保存的相关字段
     * @param coumns -- 表字段的相关信息
     */
    @Override
    public void beforeSave(Class jpaClass,Map<String, Object> saveMap, List<FieldAndColumn> coumns) {
        System.out.println("执行保存前的监听");
    }

    /**
     * jpa对象统一更新前的实现逻辑
     * @param jpaClass -- 表对象
     * @param updateMap -- 准备执行更新的相关字段
     * @param coumns -- 表字段的相关信息
     */
    @Override
    public void beforeUpdate(Class jpaClass,Map<String, Object> updateMap, List<FieldAndColumn> coumns) {
        System.out.println("执行更新前的监听");
    }

    /**
     * jpa统一对象删除前的操作
     * @param eDbPro -- 数据库操作对象
     * @param jpaClass -- 表对象
     * @param deleteMaps -- 指定需要删除的集合(默认只给予主键对应的id)
     * @param coumns -- 表字段的相关信息
     * @return -- 返回 true 或者 NULL 则继续执行，返回false则拦截不再做直接删除处理
     */
    @Override
    public EDBListenerResult beforeDelete(EDbPro eDbPro,Class jpaClass,List<Map<String,Object>> deleteMaps, List<FieldAndColumn> coumns) {
        System.out.println("=== 进入delete监听 ===");
        EDBListenerResult edbListenerResult = new EDBListenerResult();
        //
        for(FieldAndColumn fieldAndColumn : coumns){
            // 如果包含伪删除 REMOVE_FLAG 字段，则全部统一替换成 update 操作
            if(fieldAndColumn.getColumn().name().toLowerCase().equals("REMOVE_FLAG".toLowerCase())){
                // 阻止继续执行 delete 方法
                edbListenerResult.setNextToDo(false);
                // 根据情况返回指定的方法结果
                if(deleteMaps.size() > 1){
                    int updateCt = 0;
                    for(Map<String,Object> delete:deleteMaps){
                        // 统一变更
                        delete.put("REMOVE_FLAG","Y");
                        // 更新该对象
                        eDbPro.update(jpaClass,delete);
                        updateCt++;
                    }
                    // 设置返回结果
                    edbListenerResult.setReturnResult(true);
                    // 设置返回结果影响的条数
                    edbListenerResult.setReturnCt(updateCt);
                    return edbListenerResult;
                }else{
                    // 单体删除
                    deleteMaps.get(0).put("REMOVE_FLAG","Y");
                    // 设置返回结果
                    edbListenerResult.setReturnResult(eDbPro.update(jpaClass,deleteMaps.get(0)));
                    return edbListenerResult;
                }

            }
        }
        return null;
    }


}
