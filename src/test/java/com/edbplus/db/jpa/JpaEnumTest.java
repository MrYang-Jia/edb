/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.jpa;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.base.BaseCrVehicleTypeForBoolean;
import com.edbplus.db.jpa.model.base.BaseCrVehicleTypeForEnum;
import com.edbplus.db.jpa.model.modelEnum.CreaterEnum;
import com.edbplus.db.jpa.model.modelEnum.DeleteEnumValue;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName JpaEnumTest
 * @Description: //todo
 * @Author 杨志佳
 * @Date 2021/5/6
 * @Version V1.0
 **/
public class JpaEnumTest extends BaseTest {

    @Test
    public void testValueGetName(){
        DeleteEnumValue deleteEnumValue = DeleteEnumValue.ISDELETE;

        Enum[] var2 = deleteEnumValue.getClass().getEnumConstants();

        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            DeleteEnumValue object = (DeleteEnumValue) var2[var4];
            if (Objects.equals(1, object.getValue())) {
                System.out.println(object) ;
            }
        }


    }

    @Test
    public void testGetValue(){
        DeleteEnumValue deleteEnumValue = DeleteEnumValue.ISDELETE;
//        System.out.println(deleteEnumValue.ordinal());
//        List<Object> types = EnumUtil.getFieldValues(DeleteEnumValue.class, "ISDELETE");
        Method method = ReflectUtil.getMethod(DeleteEnumValue.class, "getValue");
        if(method!=null){
            // 执行方法
            try {
                System.out.println(method.invoke(deleteEnumValue));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

//        System.out.println(DeleteEnumValue.valueOf(DeleteEnumValue.class,deleteEnumValue.name()));

//        DeleteEnum deleteEnum = DeleteEnum.ISDELETE;
//        System.out.println(deleteEnum);

    }

    @Test
    public void saveTest(){
        BaseCrVehicleTypeForEnum baseCrVehicleTypeForEnum = new BaseCrVehicleTypeForEnum();
        // 赋予创建人为小明的信息
        CreaterEnum createrEnum = CreaterEnum.XiaoMing;
        DeleteEnumValue deleteEnumValue = DeleteEnumValue.UNDELETE;
        baseCrVehicleTypeForEnum.setCreator(createrEnum);
        baseCrVehicleTypeForEnum.setVehicleTypeName("测试枚举类型");
        baseCrVehicleTypeForEnum.setIsDel(deleteEnumValue);
        EDb.use().save(baseCrVehicleTypeForEnum);
    }

    @Test
    public void findTest(){
        List<BaseCrVehicleTypeForBoolean> list = EDb.use().find(BaseCrVehicleTypeForBoolean.class,"select * from cr_vehicle_type where VEHICLE_TYPE_NAME = '测试枚举类型' ");

        for(BaseCrVehicleTypeForBoolean baseCrVehicleTypeForEnum:list){
            System.out.println("==>"+baseCrVehicleTypeForEnum.getIsDel());
        }
    }


}
