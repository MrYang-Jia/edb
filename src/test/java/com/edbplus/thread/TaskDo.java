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
package com.edbplus.thread;

import cn.hutool.core.convert.Convert;
import com.edbplus.db.EDb;
import com.edbplus.db.jpa.VehicleType;
import com.edbplus.db.util.list.EDbListUtil;
import com.jfinal.plugin.activerecord.Record;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务测试类
 */
public class TaskDo {
    // static
    public static synchronized void printOut(int finalI){
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 打印数字
        System.out.println(finalI);
    }

    @Test
    public void test(){
        List<Object> result = new ArrayList<>();
        VehicleType vehicleType = new VehicleType();
        vehicleType.setVehicleTypeId(1);
        result.add(vehicleType);
        //
        List<Integer> integerList = EDbListUtil.toConvertList(Integer.class,result,"vehicleTypeId",null);
        // 打印转换的参数对象集
        System.out.println(integerList);
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("idStr","世界是java的");
        result.add(dataMap);
        // 提取 idStr 字段
        List<String> strings = EDbListUtil.toConvertList(String.class,result,"idStr","");
        System.out.println(strings);

        List<Record> records = new ArrayList<>();
        Record record = new Record();
        record.set("idStr","世界是java的");
        records.add(record);
        strings = EDbListUtil.toConvertList(String.class,records,"idStr","");
        System.out.println(strings);

    }

    @Test
    public void test2(){
        // uD83DuDC37
        String strPart = "\uD83D\uDC37";
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        System.out.println(hexString);
    }

}
