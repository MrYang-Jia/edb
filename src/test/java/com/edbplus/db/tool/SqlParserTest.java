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
package com.edbplus.db.tool;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import com.edbplus.db.druid.EDbSelectUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @ClassName SqlParserTest
 * @Description: sql语法相关处理的测试
 * @Author 杨志佳
 * @Date 2021/12/1
 * @Version V1.0
 **/
public class SqlParserTest {

    @Test
    public void checkKeyWord(){
        String sql = "select 1 from table";
        String keyWord = "select";
        int idx = sql.indexOf(keyWord); // 传递idx，主要是便于控制获取的是第几个关键字所在位置，避免一个语句包含多个关键字，导致判断混淆
        System.out.println(EDbSelectUtil.checkKeyWordFromSql(sql,keyWord,idx));


        System.out.println(CharsetUtil.defaultCharset());
    }


    /**
     * 返回 limit 语句之特殊处理
     */
    @Test
    public void returnLimitSqlTest(){
        // 场景1
        String sql = " select 1,(select 1 from tb2 limit 1) from tb limit 9";
        System.out.println("1=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1,(select 1 from tb2 limit 1) from tb  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1,(select 1 from tb2 limit 1) from tb ";
        System.out.println("2=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1,(select 1 from tb2 limit 1) from tb  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb ";
        System.out.println("3=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb limit 10,10"; // 则需要转换成 10,1
        System.out.println("4=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb limit 10,1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb limit 10 offset 7"; // 这个要特殊处理，右侧如果存在 offset 的情况，则必须保留，避免数据结果不一致，尤其是mysql版本
        System.out.println("5=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb  limit 1 offset 7",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb offset 6 limit 9 ";
        System.out.println("6=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb offset 6  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1 from tb offset 0\r\nlimit 9 "; // 特殊符号场景
        System.out.println("7=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1 from tb offset 0\r\n limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1_limit from tb offset 0 limit 9 ";
        System.out.println("8=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1_limit from tb offset 0  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1  from tb where gs='N' offset 0 \tlimit 9 ";
        System.out.println("9=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1  from tb where gs='N' offset 0 \t limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1_limit -- 换行 \n from tb offset 0 limit 9 ";
        System.out.println("10 => "+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1_limit -- 换行 \n from tb offset 0  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

        sql = " select 1_limit,'\\n 1' -- 回车 \r\n from tb where and offset 0 limit 9 ";
        System.out.println("11=>"+EDbSelectUtil.returnLimitSql(sql,1));
        Assert.assertEquals(" select 1_limit,'\\n 1' -- 回车 \r\n from tb where and offset 0  limit 1",EDbSelectUtil.returnLimitSql(sql,1));

    }


    @Test
    public void returnLimitSqlThredTest(){
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(50);

        for(int i=0;i<20;i++){
           String  sql = " select "+i+" from tb \toffset 0 \tlimit 10 ";
           int j =i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println( j + "==>"+EDbSelectUtil.returnLimitSql(sql,1));
                }
            });
        }

        ThreadUtil.sleep(100);

    }


    @Test
    public void test2(){
        String sql= "select\n" +
                "tvs.vsid,\n" +
                "tvs.id,\n" +
                "tvs.vs_no,\n" +
                "tvs.vehicle_id,\n" +
                "tvs.vehicle_type_code,\n" +
                "\n" +
                "tvs.vehicle_model_code,\n" +
                "tvs.vehicle_model,\n" +
                "tvs.vehicle_length,\n" +
                "tvs.vehicle_load_weight,\n" +
                "tvs.vehicle_volume,\n" +
                "tvs.vs_set_out_time,\n" +
                "tvs.vs_insurance,\n" +
                "tvs.vs_destination,\n" +
                "tvs.destination_province,\n" +
                "tvs.destination_city,\n" +
                "tvs.destination_district,\n" +
                "tvs.vs_origin,\n" +
                "tvs.orgin_province,\n" +
                "tvs.orgin_city,\n" +
                "tvs.orgin_district,\n" +
                "tvs.vs_contact,\n" +
                "tvs.vs_cellphone,\n" +
                "tvs.release_time,\n" +
                "tvs.vs_status,\n" +
                "tvs.vs_remark,\n" +
                "tvs.createtime,\n" +
                "tvs.createby,\n" +
                "tvs.updatetime,\n" +
                "tvs.updateby,\n" +
                "tvs.enabled,\n" +
                "tvs.account_id,\n" +
                "tvs.user_id_int,\n" +
                "tvs.vs_type_code,\n" +
                "tvs.vid,\n" +
                "tvs.c_vehicle_type_id,\n" +
                "tvs.source_type,\n" +
                "tvs.business_status,\n" +
                "tvs.vs_start_set_out_time,\n" +
                "tvs.vs_end_set_out_time,\n" +
                "tvs.vs_reward_ammount,\n" +
                "tvs.vs_reward_status_id,\n" +
                "tvs.reward_business_status,\n" +
                "tvs.tank_volume,\n" +
                "tvs.suitable_source,\n" +
                "tvs.tank_function,\n" +
                "tvs.cargo_name,\n" +
                "? as recommend,\n" +
                "( case when tvs.vehicle_number ='未填写' then '' else tvs.vehicle_number end) vehicle_number,\n" +
                "( case when tvs.vehicle_type ='未填写' then '' else tvs.vehicle_type end) vehicle_type,\n" +
                "(select los_name from lom_order_status where los_id = tvs.vs_reward_status_id) AS losName,\n" +
                "(select los_code from lom_order_status where los_id = tvs.vs_reward_status_id) AS losCode,\n" +
                "( select ifnull(tra_gvs_price.min_price,0.00) from tra_gvs_price where tra_gvs_price.biz_id = tvs.vsid and tra_gvs_price.biz_type = 20) as minPrice,\n" +
                "( select ifnull(tra_gvs_price.top_price,0.00) from tra_gvs_price where tra_gvs_price.biz_id = tvs.vsid and tra_gvs_price.biz_type = 20) as topPrice,\n" +
                "( select ifnull(tra_gvs_price.sin_price,0.00) from tra_gvs_price where tra_gvs_price.biz_id = tvs.vsid and tra_gvs_price.biz_type = 20) as sinPrice,\n" +
                "( select base_number from tra_gvs_detail_num where tra_gvs_detail_num.biz_id = tvs.vsid and tra_gvs_detail_num.biz_type = 20 limit 1) as baseNumber,\n" +
                "( select detail_number from tra_gvs_detail_num where tra_gvs_detail_num.biz_id = tvs.vsid and tra_gvs_detail_num.biz_type = 20 limit 1) as detailNumber,\n" +
                "( select vehicle_type from mot_vehicle where mot_vehicle.vid = tvs.vid) AS vsTypeName,\n" +
                "( select c_vehicle_type_id from mot_vehicle where mot_vehicle.vid = tvs.vid) AS vsTypeId,\n" +
                "( select auth_status from sys_account where sys_account.user_id_int = tvs.user_id_int) AS authStatus,\n" +
                "( select true_name from sys_user where id = (select development_user_id from sys_account where user_id_int = tvs.user_id_int)) AS developmentName,\n" +
                "( select account_type from sys_account where sys_account.user_id_int = tvs.user_id_int) AS accountType,\n" +
                "( select sys_attachment.attachment_dest_name from sys_attachment where sys_attachment.id = (select attachment_id from sys_account_attachment where sys_account_attachment.user_id_int = tvs.user_id_int and sys_account_attachment.attachment_type = 6)) as attDestName,\n" +
                "( select sys_attachment.attachment_path from sys_attachment where sys_attachment.id = (select attachment_id from sys_account_attachment where sys_account_attachment.user_id_int = tvs.user_id_int and sys_account_attachment.attachment_type = 6)) as attPath,\n" +
                "( select lord_trade_status from lom_order_reward_detail where lord_vsid = tvs.vsid and remove_flag = 'n' and lord_trade_status = 0 limit 1) as waitPaySubOrderStatus,\n" +
                "( select group_concat(type_name) from mot_vehicle_receive where mot_vehicle_receive.vid = tvs.vid) as vehicleReceives,\n" +
                "( select distance from cr_routes_source where cr_routes_source.route_id = tvv.route_id) as distance,\n" +
                "(select pro.vip_icon from oc_third_vip_user tvu left join oc_third_vip_product pro on pro.product_id = tvu.product_id where tvu.usid = tvs.user_id_int and tvu.status = 10 and tvu.remove_flag = 'n' and tvu.serve_start_time <= now() and tvu.serve_end_time > now()) as vipIcon,\n" +
                "(select pro.personal_icon from oc_third_vip_user tvu left join oc_third_vip_product pro on pro.product_id = tvu.product_id where tvu.usid = tvs.user_id_int and tvu.status = 10 and tvu.remove_flag = 'n' and tvu.serve_start_time <= now() and tvu.serve_end_time > now()) as personalVipIcon,\n" +
                "(select pro.vehicle_icon from oc_third_vip_user tvu left join oc_third_vip_product pro on pro.product_id = tvu.product_id where tvu.usid = tvs.user_id_int and tvu.status = 10 and tvu.remove_flag = 'n' and tvu.serve_start_time <= now() and tvu.serve_end_time > now()) as vsVipIcon,\n" +
                "(select img_path from pt_uc_head_img where img_use_type = 1 and remove_flag = 'n' and  pt_uc_head_img.user_id = tvs.user_id_int limit 1) as ossHeadUrl,\n" +
                "(select behavior.call_count from tra_vehicle_user_behavior behavior where behavior.vsid = tvv.vsid and behavior.usid = ? limit 1) as call_count,\n" +
                "(select behavior.view_count from tra_vehicle_user_behavior behavior where behavior.vsid = tvv.vsid and behavior.usid = ? limit 1) as view_count,\n" +
                "case when tvv.top_end_time > now() then 1 else 0 end as top_flag,\n" +
                "(select define_code from sys_dict_define lsc where lsc.define_value = tvs.suitable_source and lsc.type_code = 'SUITABLE_SOURCE' ) as suitableSourceName,\n" +
                "(select define_code from sys_dict_define lsc where lsc.define_value = tvs.tank_function and lsc.type_code = 'TANK_FUNCTION' ) as tankFunctionName,\n" +
                "(\n" +
                "SELECT\n" +
                "\t group_concat( distinct mrtl.tag_name ) tagNam\n" +
                "FROM\n" +
                "\tmkt_report_main mrm\n" +
                "\tLEFT JOIN mkt_report_main_tag_type_relation mrmttr ON mrm.rm_id = mrmttr.rm_id\n" +
                "\tLEFT JOIN mkt_report_tag_type_relation mrttr ON mrttr.rttr_id = mrmttr.rttr_id\n" +
                "\tLEFT JOIN mkt_report_tag_label mrtl ON mrtl.rtl_id = mrttr.rtl_id\n" +
                "WHERE\n" +
                "\tmrm.remove_flag = 'N'\n" +
                "\tAND mrtl.remove_flag = 'N'\n" +
                "\tAND mrtl.tag_name IS NOT NULL\n" +
                "    AND mrm.report_user_id = ?\n" +
                "\tand mrm.reported_user_id = tvs.user_id_int\n" +
                "\tAND mrttr.tag_type = 'PEOPLE'\n" +
                "\tAND mrttr.tag_feedback_type != '30'\n" +
                ") as label_names\n" +
                "FROM\n" +
                "  tra_vehicle_view tvv\n" +
                "LEFT JOIN tra_vehicle_source tvs on tvv.vsid = tvs.vsid\n" +
                "\n" +
                "WHERE 1=1\n" +
                "    AND tvv.status = 'N'\n" +
                "    AND tvs.enabled = 1\n" +
                "    and tvs.BUSINESS_STATUS = 10\n" +
                "    and tvs.reward_business_status = 10\n" +
                "    and  not EXISTS (\n" +
                "        select vsid from tra_vehicle_source where enabled = 1 and user_id_int = ? and source_type = 6\n" +
                "        and  vsid = tvv.vsid\n" +
                "    )\n" +
                "    and not EXISTS (\n" +
                "        select target_user_id from uc_user_blacklist where remove_flag = 'N' and user_id = ?\n" +
                "        and tvs.user_id_int = target_user_id\n" +
                "    )\n" +
                "        AND tvv.set_out_time >= ?\n" +
                "        AND tvv.route_from_id >= ?\n" +
                "        AND tvv.route_from_id <= ?\n" +
                "        AND tvv.route_to_id >= ?\n" +
                "        AND tvv.route_to_id <= ?\n" +
                "        AND tvv.vs_start_set_out_time <= ?\n" +
                "        AND tvv.vs_end_set_out_time >= ?\n" +
                "\n" +
                "\n" +
                "ORDER BY top_flag DESC , tvv.release_time DESC\n" +
                "\n" +
                "\n";
        sql = EDbSelectUtil.returnLimitSql(sql,10);
        sql = EDbSelectUtil.returnOffsetSql(sql,1);

        System.out.println(sql);
//        System.out.println("1=>"+EDbSelectUtil.returnLimitSql(sql,10));
    }


    /**
     * 移除 order 关键字相关语法测试
     */
    @Test
    public void removeOrderTest(){
        // 第一个语句搞一个比较特殊的，就是 order 作为字段名来排序的情况
        String sql = " select 1 from tb ";
        System.out.println("-1=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb ",EDbSelectUtil.removeOrder(sql));

        sql = " select 1 from tb order by `order_by` desc";
        System.out.println("0=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb ",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb order by id desc";
        System.out.println("1=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb ",EDbSelectUtil.removeOrder(sql));

        sql = " select 1 from tb order by id desc limit 10";
        System.out.println("2=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb  limit 10",EDbSelectUtil.removeOrder(sql));

        sql = " select 1,(select 1 from order by id limit 1) from tb order by id desc limit 10";
        System.out.println("3=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1,(select 1 from order by id limit 1) from tb  limit 10",EDbSelectUtil.removeOrder(sql));

        sql = " select 1,(select 1 from order by id limit 1) from tb order by (case when id =1 then 1 else 0 end)  desc limit 10";
        System.out.println("4=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1,(select 1 from order by id limit 1) from tb  limit 10",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb where id in (select id from tb2 order by id desc ) order by desc limit 10";
        System.out.println("5=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where id in (select id from tb2 order by id desc )  limit 10",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc ) order by desc limit 10";
        System.out.println("6=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc )  limit 10",EDbSelectUtil.removeOrder(sql));



        sql = " select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc ) order by desc ";
        System.out.println("7=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where exist (select 1 from tb2 where tb.id=tb2.id order by id desc ) ",EDbSelectUtil.removeOrder(sql));


        sql = " select 1 from tb where gs='N' group by id order by id desc limit 10";
        System.out.println("8=>"+EDbSelectUtil.removeOrder(sql));
        Assert.assertEquals(" select 1 from tb where gs='N' group by id  limit 10",EDbSelectUtil.removeOrder(sql));

    }

    @Test
    public void returnOffsetTest(){
        // 场景1
        String sql = " select 1,(select 1 from tb2 limit 1) from tb limit 9";
        System.out.println("1=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1,(select 1 from tb2 limit 1) from tb limit 9 offset 1",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1,(select 1 from tb2 offset 1 limit 1) from tb "; // 注意，mysql 的 offset 只能放 limit 之后
        System.out.println("2=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1,(select 1 from tb2 offset 1 limit 1) from tb  offset 1",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1 from tb ";
        System.out.println("3=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1 from tb  offset 1",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1 from tb limit 10,10"; // 则需要转换成 1,10
        System.out.println("4=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1 from tb  limit 1,10",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1 from tb limit 10 offset 1"; // 这个要特殊处理，右侧如果存在 offset 的情况，则必须保留，避免数据结果不一致，尤其是mysql版本
        System.out.println("5=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1 from tb limit 10  offset 1",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1 from tb offset 6 limit 9 ";
        System.out.println("6=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1 from tb  offset 1 limit 9 ",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1 from tb offset 0\r\nlimit 9 "; // 特殊符号场景
        System.out.println("7=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1 from tb  offset 1 limit 9 ",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1_offset from tb offset 0 limit 9 ";
        System.out.println("8=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1_offset from tb  offset 1 limit 9 ",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1  from tb where gs='N' offset 0 \tlimit 9 ";
        System.out.println("9=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1  from tb where gs='N'  offset 1 limit 9 ",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1_offset -- 换行 \n from tb offset 0 limit 9 ";
        System.out.println("10 => "+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1_offset -- 换行 \n from tb  offset 1 limit 9 ",EDbSelectUtil.returnOffsetSql(sql,1));

        sql = " select 1_offset,'\\n 1' -- 回车 \r\n from tb where and offset 0 limit 9 ";
        System.out.println("11=>"+EDbSelectUtil.returnOffsetSql(sql,1));
        Assert.assertEquals(" select 1_offset,'\\n 1' -- 回车 \r\n from tb where and  offset 1 limit 9 ",EDbSelectUtil.returnOffsetSql(sql,1));
    }

    @Test
    public void test333(){
        List<Map<String,Integer>> list = new ArrayList<>();
        Map<String,Integer> dataMap = new HashMap<>();
        dataMap.put("age",3);
        list.add(dataMap);
        dataMap = new HashMap<>();
        dataMap.put("age",2);
        list.add(dataMap);
//        list.sort(Comparator.comparing((Map<String, Integer> key) -> {
//            return key.get("age");
//        }));
//        Collections.sort(list, new Comparator<Map<String, Integer>>() {
//            public int compare(Map<String, Integer> o1, Map<String, Integer> o2) {
//                return o1.get("age").compareTo(o2.get("age"));
//            }
//        });
        System.out.println(list);
    }




}
