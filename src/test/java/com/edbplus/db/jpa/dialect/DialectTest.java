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
package com.edbplus.db.jpa.dialect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DialectTest
 * @Description: 方言测试
 * @Author 杨志佳
 * @Date 2021/6/3
 * @Version V1.0
 **/
public class DialectTest {


    @Test
    public void test(){
        String selectHeadSql = " select count(*)  from cr_vehicle_type where 1=1 and VEHICLE_TYPE_ID = ?";
        List<String> totalParsList =  ReUtil.findAll("\\?",selectHeadSql, 0, new ArrayList<String>());
        System.out.println(totalParsList.size());

        String whereSql = "from cr_vehicle_type where   1=1  and VEHICLE_TYPE_ID like ?  and (  VEHICLE_TYPE_ID not in (?) ) order by VEHICLE_TYPE_ID desc,CREATE_TIME asc";
        totalParsList =  ReUtil.findAll("\\?",selectHeadSql, 0, new ArrayList<String>());
        System.out.println(totalParsList.size());

    }

    @Test
    public void testFilter(){
        Integer[] a = {1,2,3,4,5,6};
        System.out.println(JSONUtil.toJsonStr(ArrayUtil.sub(a,3,a.length)));
        System.out.println(JSONUtil.toJsonStr(a));
    }

}
