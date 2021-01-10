package com.edbplus.db.jfinal.activerecord.db;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jpa.VehicleType;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.EDbQueryUtil;
import com.edbplus.db.generator.jdbc.GenJdbc;
import com.jfinal.plugin.activerecord.SqlPara;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName EDbQueryTest
 * @Description: TODO
 * @Author 杨志佳
 * @Date 2020/10/16
 * @Version V1.0
 **/
public class EDbQueryTest {


    String jdbcUrl = "jdbc:mysql://192.168.1.106:13306/tra_log?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useCompression=true";
    String userName = "root";
    String pwd = "dev-whbj@WHBJ";

    @BeforeTest
    public void init(){

        List<String> sqlTplList = new ArrayList<>();
        // todo: 基于项目的sql模板，你直接添加在这里，就能快速的进行测试
        sqlTplList.add("/sql/all.sql");
        // 共享模板配置
        List<String> shareSqlTplList = new ArrayList<>();
        // 添加共享sql模板
//        shareSqlTplList.add("/sql/sharedfunction/common_function.sql");

        // 初始化
        GenJdbc.initForEnjoy(jdbcUrl,userName,pwd,sqlTplList,shareSqlTplList);

    }


    @Test
    public void test(){
        //
        List<Object> list = new ArrayList<>();
        list.add(100);
        list.add(200);
        EDbQuery eDbQuery = new EDbQuery();
        // 结果集只有200的数据
        System.out.println(JSONUtil.toJsonStr(EDb.paginate(VehicleType.class,1,10,eDbQuery)));

        // 假如只打印 VEHICLE_TYPE_ID,CREATOR 字段
        eDbQuery.fields(" VEHICLE_TYPE_ID,CREATOR ");
        // select * from xxx where 1 = 1 --> and 拼接1
        // 包含 VEHICLE_TYPE_ID 100 、 200 的数据
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.in, list));
        // 相当于拼接 --> and ( 拼接1   )
        list = new ArrayList<>();

        list.add(100);
        // 剔除 VEHICLE_TYPE_ID = 100 的数据
        eDbQuery.andCom().and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.notIn, list));

        // 根据 VEHICLE_TYPE_ID 进行降序布局
        eDbQuery.orderDESC("VEHICLE_TYPE_ID");

        // 解析 sqlpara
        SqlPara sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(VehicleType.class,eDbQuery);

        // 结果集只有200的数据
        System.out.println(JSONUtil.toJsonStr(EDb.find(VehicleType.class,sqlPara)));

        // 假设数据库只有1条 创建人-0 的数据，而它的 VEHICLE_TYPE_ID = 100 ，则会重新添加回来
        eDbQuery.orCom().and(new EDbFilter("CREATOR", EDbFilter.Operator.eq, "创建人-0"));
        //  增加 CREATOR 作为降序要素
        eDbQuery.orderDESC("CREATOR");
        // 重新解析 sqlpara
        sqlPara = EDbQueryUtil.getSqlParaForJpaQuery(VehicleType.class,eDbQuery);

//        System.out.println(sqlPara.getSql());
//        System.out.println(JSONUtil.toJsonStr(sqlPara.getPara()));
        // 结果会发现都包含了100 和 200 的数据
        System.out.println(JSONUtil.toJsonStr(EDb.find(VehicleType.class,sqlPara)));



    }
}
