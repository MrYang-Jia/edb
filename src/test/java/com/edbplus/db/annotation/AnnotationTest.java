package com.edbplus.db.annotation;

import cn.hutool.core.map.CaseInsensitiveMap;
import com.edbplus.db.jpa.model.base.BaseVehicleType;
import com.edbplus.db.util.hutool.json.EJSONUtil;
import com.edbplus.db.util.hutool.reflect.EReflectUtil;
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
        Method[] methods = EReflectUtil.getMethods(baseVehicleType.getClass());
        // 方法
        for(Method method : methods){
            EDbSave eDbSave = method.getAnnotation(EDbSave.class);
            if(eDbSave!=null){
                System.out.println(method.getName());
                // 事先约定map类型
                EReflectUtil.invoke(baseVehicleType, method, dataMap);
                System.out.println("执行后:"+ EJSONUtil.toJsonStr(dataMap));
            }

            EDbUpdate eDbUpdate = method.getAnnotation(EDbUpdate.class);
            if(eDbUpdate!=null){
                System.out.println(method.getName());
                // 事先约定map类型
                EReflectUtil.invoke(baseVehicleType, method, dataMap);
                System.out.println("执行后:"+ EJSONUtil.toJsonStr(dataMap));
            }
        }
    }
}
