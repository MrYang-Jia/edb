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
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.CrVehicleType;
import com.edbplus.db.jpa.model.CrVehicleTypeMode;
import com.jfinal.plugin.activerecord.Db;
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
        CrVehicleType crVehicleType = CrVehicleType.dao.findById(100);
        System.out.println(crVehicleType); // 打印查询结果
        crVehicleType.setCreator("创建人-100"); // 修改键值
        crVehicleType.update(); // 更新结果集
        crVehicleType.use("pg").save(); //切换数据库保存
        crVehicleType.setCreator("创建人-pg"); // 保存到pg库
        crVehicleType.use("pg").update(); // 指定更新pg库的更新结果集
        crVehicleType = CrVehicleType.dao.use("pg").findById(100); // 查询pg库的对象，会发现已更新成功
        System.out.println("==>"+crVehicleType.getCreator()); // 上一个对象指定查询pg库的数据，所以打印pg库修改后的信息
        crVehicleType = CrVehicleType.dao.findById(100); // 再次查询主库数据
        System.out.println(crVehicleType); // 再次打印数据库查询之后的结果

        CrVehicleType.dao.use("pg").delete(crVehicleType); // 删除
        crVehicleType = CrVehicleType.dao.findById(100); // 查询主库信息
        System.out.println("==>"+crVehicleType.getCreator()); // 切换回主库，并打印信息

        crVehicleType.rel().getCrVehicleTypeModesRel(); // 关联查询,思考 view 和 rel 很容易混淆，相似，但是用途又不一样




//        crVehicleType =  new CrVehicleType(); // 定义新的数据对象
//        crVehicleType.setCreator("小明"); // 保存小明的数据字段
//        crVehicleType.save(); // 插入数据
//        crVehicleType = CrVehicleType.dao.findById(crVehicleType.getVehicleTypeId()); // 查询插入的数据对象
//        System.out.println(crVehicleType); // 再次打印数据库查询之后的结果
//
//        System.out.println(JSONUtil.toJsonStr(CrVehicleType.dao.findById(100)));
//
//        System.out.println(CrVehicleType.dao.use("pg").findById(100));
////
//        System.out.println(CrVehicleType.dao.findById(100));
//        System.out.println(CrVehicleType.dao.use("pg").findById(100));
//        System.out.println(JSONUtil.toJsonStr(Db.use()));
//        System.out.println(CrVehicleType.dao.use().findById(100));

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

//        CrVehicleType crVehicleType = CrVehicleType.dao.template("test.findForLimit",1).findFirst();
//
//        crVehicleType.setCreator("更新用户-100");

//        CrVehicleType.dao.templateByString("select * from cr_vehicle_type ").findFirst();


//        CrVehicleType crVehicleType = EDb.findById(CrVehicleType.class,1L);

//        crVehicleType = CrVehicleType.dao.findById(1L);
//        crVehicleType = crVehicleType.findById(0);
//        crVehicleType = CrVehicleType.dao;
//        crVehicleType = crVehicleType.findById(0); // 如果这么查询，会有风险 -》 crVehicleType 可能是null，导致下次查询无法继续，得用 CrVehicleType.dao 保证每一次都可执行相关操作





//        System.out.println(crVehicleType.getAllRel().getCrVehicleTypeModesRel());



    }

}
