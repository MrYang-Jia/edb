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

import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.base.BaseCrVehicleTypeForEnum;
import com.edbplus.db.jpa.model.modelEnum.CreaterEnum;
import com.edbplus.db.jpa.model.modelEnum.DeleteEnumValue;
import com.edbplus.db.util.hutool.reflect.EReflectUtil;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
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
        Method method = EReflectUtil.getMethod(DeleteEnumValue.class, "getValue");
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

    /**
     * 保存枚举字段测试
     */
    @Test
    public void saveTest(){
        BaseCrVehicleTypeForEnum baseCrVehicleTypeForEnum = new BaseCrVehicleTypeForEnum();
        // 赋予创建人为小明的信息
        CreaterEnum createrEnum = CreaterEnum.ChenHong;
        DeleteEnumValue deleteEnumValue = DeleteEnumValue.UNDELETE;
        baseCrVehicleTypeForEnum.setCreator(createrEnum);
        baseCrVehicleTypeForEnum.setVehicleTypeName("测试枚举类型");
        baseCrVehicleTypeForEnum.setIsDel(deleteEnumValue);
        EDb.use().save(baseCrVehicleTypeForEnum);

        System.out.println(baseCrVehicleTypeForEnum.getCreator().getLabel());
    }




    /**
     * 枚举值字段回填后结果测试
     */
    @Test
    public void findTest(){
        List<BaseCrVehicleTypeForEnum> list = EDb.use().find(BaseCrVehicleTypeForEnum.class,"select * from cr_vehicle_type where VEHICLE_TYPE_NAME = '测试枚举类型' ");

        for(BaseCrVehicleTypeForEnum baseCrVehicleTypeForEnum:list){
            if(baseCrVehicleTypeForEnum.getCreator()!=null){
                System.out.println("==>"+baseCrVehicleTypeForEnum.getCreator().getLabel());
            }else{
                System.out.println("枚举回填不存在");
            }
        }

        BaseCrVehicleTypeForEnum update = list.get(0);
        CreaterEnum createrEnum = CreaterEnum.XiaoMing;
        update.setCreator(createrEnum);
        DeleteEnumValue deleteEnumValue = DeleteEnumValue.UNDELETE;
        update.setIsDel(deleteEnumValue);
        EDb.use().update(update);
        System.out.println(update.getVehicleTypeId());


    }


    /**
     * 大数据常用方法 -- insertValues
     */
    @Test
    public void insertValues(){
        //
        EDb.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            long start = System.currentTimeMillis();
            List<BaseCrVehicleTypeForEnum> saveList = new ArrayList<>();
            BaseCrVehicleTypeForEnum vehicleType =null;
            DeleteEnumValue deleteEnumValue = DeleteEnumValue.UNDELETE;
            CreaterEnum createrEnum = CreaterEnum.XiaoMing;
            // 插入数量自己预设
            for(int i=0;i<1000;i++){
                // 数据对象
                vehicleType = new BaseCrVehicleTypeForEnum();
                vehicleType.setVehicleTypeName("车辆类型-"+i);
                vehicleType.setCreator(createrEnum);
                vehicleType.setIsDel(deleteEnumValue);
                saveList.add(vehicleType);
            }
            // insertValues 无id返回值，建议大量数据插入时，可预分配id给数组对象
            // 批量插入 -- 以每批次插入100条数据位例子 ，该模式 id 不会回填，所以只返回操作的插入结果
            int count=EDb.use().insertValues(BaseCrVehicleTypeForEnum.class,saveList,1000);
            System.out.println(count);
            System.out.println("耗时:"+(System.currentTimeMillis()-start));
            return false;
        });
    }


}
