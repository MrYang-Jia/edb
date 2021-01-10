package com.edbplus.db.annotation;

import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.jpa.model.base.BaseVehicleType;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public class AnnotationTest {


    /**
     * 注解方法测试
     */
    @Test
    public void edbAnnotationTest(){
        // hutool 工具类里不区分大小写的map
        Map<String,Object> dataMap =  new CaseInsensitiveMap();
        dataMap.put("create_time",new Date());
        dataMap.put("MODIFY_TIme",null);
        BaseVehicleType baseVehicleType = new BaseVehicleType();
        // 必须构建对象方法
        Method[] methods = ReflectUtil.getMethods(baseVehicleType.getClass());
        // 方法
        for(Method method : methods){
            EDbSave eDbSave = method.getAnnotation(EDbSave.class);
            if(eDbSave!=null){
                System.out.println(method.getName());
                // 事先约定map类型
                ReflectUtil.invoke(baseVehicleType, method, dataMap);
                System.out.println("执行后:"+ JSONUtil.toJsonStr(dataMap));
            }

            EDbUpdate eDbUpdate = method.getAnnotation(EDbUpdate.class);
            if(eDbUpdate!=null){
                System.out.println(method.getName());
                // 事先约定map类型
                ReflectUtil.invoke(baseVehicleType, method, dataMap);
                System.out.println("执行后:"+ JSONUtil.toJsonStr(dataMap));
            }
        }
    }
}
