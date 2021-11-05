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
package com.edbplus.db.jpa.vo;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.CrVehicleType;
import com.edbplus.db.jpa.model.CrVehicleTypeMode;
import com.jfinal.plugin.activerecord.SqlPara;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName EDbModelTest
 * @Description: EDbModel测试类
 * @Author 杨志佳
 * @Date 2021/11/4
 * @Version V1.0
 **/
public class EDbModelTest extends BaseTest {

    @Test
    public void findById(){
//

        // 根据id进行查询
//        CrVehicleType crVehicleType = CrVehicleType.dao.findById(100);
//        System.out.println(crVehicleType); // 打印查询结果
//        crVehicleType.setCreator("创建人-100"); // 修改键值
//        crVehicleType.update(); // 更新结果集
//        crVehicleType = CrVehicleType.dao.findById(100); // 再次查询
//        System.out.println(crVehicleType); // 再次打印数据库查询之后的结果

//        crVehicleType =  new CrVehicleType(); // 定义新的数据对象
//        crVehicleType.setCreator("小明"); // 保存小明的数据字段
//        crVehicleType.save(); // 插入数据
//        crVehicleType = CrVehicleType.dao.findById(crVehicleType.getVehicleTypeId()); // 查询插入的数据对象
//        System.out.println(crVehicleType); // 再次打印数据库查询之后的结果

        System.out.println(JSONUtil.toJsonStr(CrVehicleType.dao.findById(100)));

        System.out.println(CrVehicleType.dao.use("pg").findById(100));
//
        System.out.println(CrVehicleType.dao.findById(100));
        System.out.println(CrVehicleType.dao.use("pg").findById(100));
        System.out.println(CrVehicleType.dao.findById(100));

        // 查询数据列表
//        List<CrVehicleType> crVehicleTypes =  CrVehicleType.dao.find("select * from cr_vehicle_type limit 1");


//        List<CrVehicleType> crVehicleTypes =  CrVehicleType.dao.find("select * from cr_vehicle_type limit 1");
//
//        CrVehicleType crVehicleType = crVehicleTypes.get(0);//CrVehicleType.dao.findById(1);
//
//        crVehicleType.rel().getCrVehicleTypeModesRel();
//
//        List<Object> ids =new ArrayList<>();
//        ids.add(1);
//        ids.add(2);
//        crVehicleTypes =  CrVehicleType.dao.findByIds(ids);

//        CrVehicleTypeMode.dao.findById(1);
//
//        SqlPara sqlPara  = CrVehicleType.dao.getSqlPara("test.findForLimit",1);
//
//
//        System.out.println("==>"+sqlPara.getSql());
//        sqlPara  = CrVehicleType.dao.getSqlParaByString("select * from cr_vehicle_type limit #para(0)",1);
//
//        System.out.println("==>"+sqlPara.getSql());
//
//        CrVehicleType crVehicleType = CrVehicleType.dao.template("test.findForLimit",1).findFirst();
//
//        System.out.println(crVehicleType.rel().getCrVehicleTypeModesRel());



    }

}
