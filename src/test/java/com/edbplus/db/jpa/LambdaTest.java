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

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.Cat;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.lambda.EDbLambdaQuery;
import com.edbplus.db.query.lambda.LambdaOpt;
import com.edbplus.db.query.lambda.update.EDbLambdaUpdate;
import com.edbplus.db.query.lambda.LambdaSelectQuery;
import com.edbplus.db.query.lambda.update.LambdaUpdate;
import org.testng.annotations.Test;

import java.io.ObjectStreamClass;
import java.util.*;

/**
 * @ClassName LambdaTest
 * @Description: LambdaTest
 * @Author 杨志佳
 * @Date 2022/4/15
 * @Version V1.0
 **/
public class LambdaTest extends BaseTest {



    @Test
    public void eDbLambdaQueryTest(){
        // 建议调用模式，方便工具链路的统一原则
        LambdaSelectQuery<Cat> eDbLambdaQuery = EDb.lambdaQuery(Cat.class);
//        LambdaSelectQuery<Cat> eDbLambdaQuery = EDbLambdaQuery.lambdaQuery(Cat.class);
        // LambdaQueryWrapper<User> lambda3 = Wrappers.<User>lambdaQuery();
// name like '王%' and (age <40 or email in not null)
//        lambda3.likeRight(User::getName, "王").and(
//                qw -> qw.lt(User::getAge, 40).or().isNotNull(User::getEmail)
//        );
        eDbLambdaQuery.ge(Cat::getAge,3)
                // 添加一个andCom条件
                .andCom(p->
                        p.ge(Cat::getAge,4)
                                .ge(Cat::getAge,5)
                )
                .orCom(p->
                        p.ge(Cat::getAge,6)
                                .or().eq(Cat::getAge,8)
                                .ne(Cat::getAge,8)
                                .ge(Cat::getAge,7)
                ).or().isNull(Cat::getAge)
                .groupBy(Cat::getAge)
                .having("count(1)>1").limit(5);
        System.out.println(JSONUtil.toJsonStr(eDbLambdaQuery));
//        System.out.println(JSONUtil.toJsonStr(eDbLambdaQuery.eDbQuery.andComs));
//        System.out.println(JSONUtil.toJsonStr(eDbLambdaQuery.eDbQuery.orComs));
    }

    /**
     * lambdaselect 测试
     */
    @Test
    public void lambdaSelectQueryTest(){
        LambdaSelectQuery<VehicleType> eDbLambdaQuery = LambdaOpt.select.lambdaQuery(VehicleType.class);
        // 多个时用in
        eDbLambdaQuery.in(VehicleType::getVehicleTypeId,List.of(100,300));
        eDbLambdaQuery.likeLeft(VehicleType::getVehicleTypeId,100).findFirst();
    }


    @Test
    public void lambdaUpdateQueryTest(){
//        EDbQuery eDbQuery = new EDbQuery();
//        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.eq, 100));
//        Map<String,Object> update = new HashMap<>();
//        update.put("CREATOR","lambda");
//        EDb.update(VehicleType.class,update,eDbQuery);
//        LambdaUpdate<VehicleType> eDbLambdaQuery = EDbLambdaUpdate.lambda(VehicleType.class);
//        eDbLambdaQuery.set(VehicleType::getCreatorName,"小可2").set(VehicleType::getModifyTime, DateUtil.parse("2022-04-15 11:22:00"))
//        .eq(VehicleType::getVehicleTypeId,100)
//                .update();
        //  LambdaOpt.select.lambdaQuery(VehicleType.class,"pg"); // 指定pg库
        // 查询
//        LambdaSelectQuery<VehicleType> eDbLambdaQuery = LambdaOpt.select.lambdaQuery(VehicleType.class);
        LambdaSelectQuery<VehicleType> eDbLambdaQuery = EDb.lambdaQuery(VehicleType.class);
        VehicleType vehicleType = eDbLambdaQuery.eq(VehicleType::getVehicleTypeId,1378).findFirst();



        LambdaUpdate<VehicleType> delVehicle = EDb.lambdaUpdate(VehicleType.class);
//        LambdaUpdate<VehicleType> delVehicle = LambdaOpt.update.lambda(VehicleType.class);
        // 删除它
        delVehicle.eq(VehicleType::getVehicleTypeId,1378).delete();

        // 再添加回来
        vehicleType.setCreateTime(null);
        vehicleType.setModifyTime(null);
        EDb.save(vehicleType);

    }

    @Test
    public void test2(){
//        Protobuf protobuf =new Protobuf();
    }
}
