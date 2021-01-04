package com.edb.cloud.jfinal.activerecord.db.jpa;

import cn.hutool.json.JSONUtil;
import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edb.cloud.jfinal.activerecord.db.jpa.pip.JpaRelPip;
import com.edb.cloud.jfinal.activerecord.db.proxy.EDbRelProxy;
import com.edb.cloud.jfinal.activerecord.db.vo.VehicleTypeVo;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName JpaEnjoyTest
 * @Description: JpaEjoySql 查询方案，替代 mybatis 查询方案
 * @Author 杨志佳
 * @Date 2020/10/19
 * @Version V1.0
 **/
public class JpaEnjoyTest extends BaseTest {

    @BeforeTest
    public void initBefor(){
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        EDb.findById(VehicleType.class,1);
    }

    /**
     * 通过enjoySql模板功能替代 mybaties 的sql模板功能
     */
    @Test
    public void sqlParaTest(){
        long start = System.currentTimeMillis();
        // 模板的指定 可以看 BaseTest.java 类里的 init() 方法
        // 通过enjoySql 模板的方式
        SqlPara sqlPara = EDb.getSqlPara("test.findForId", 101);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        System.out.println("打印sql语句：" + sqlPara.getSql());
        System.out.println("打印参数：" + JSONUtil.toJsonStr(sqlPara.getPara()));

        start = System.currentTimeMillis();
        // 根据sql语句返回对象查询列表 -- 对象可以是jpa对象也可以是普通的vo对象
        List<VehicleType> vehicleTypeList = EDb.find(VehicleType.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 返回首个对象
        VehicleType vehicleType = EDb.findFirst(VehicleType.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 返回一个分页对象
        Page<VehicleType> page = EDb.paginate(VehicleType.class,1,10,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 只返回一个唯一对象，超过1个对象则会抛错，告知业务逻辑错误，对标 jpa 的 getOne 方法
        VehicleType onlyOneVehicleType = EDb.findOnlyOne(VehicleType.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );

        start = System.currentTimeMillis();
        // 匹配普通的 vo 表 和 特殊字段的转义匹配
        VehicleTypeVo vehicleTypeVo = EDb.findFirst(VehicleTypeVo.class,sqlPara);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        System.out.println("内容:"+ JSONUtil.toJsonStr(vehicleTypeVo));

    }

    /**
     * 更新测试
     */
    @Test
    public void testUpdate(){
        SqlPara sqlPara = EDb.getSqlPara("test.updateDemo", "小陈",1);
        // 更新
        EDb.update(sqlPara);
    }

    /**
     * 关系表关联工具sql方法调试
     */
    @Test
    public void testEdbRelSql(){
        Map<String,Object> tableData = new HashMap<>();
        //tableData.put("fields","f1,f2");
        tableData.put(JpaRelPip.tableName,"tb1");
        //
        Map<String,Object> whereData = new HashMap<>();
        whereData.put("k1",1);
        whereData.put("k2","2");
        List<String> columns = new ArrayList<>();
        columns.add("k1");
        columns.add("k2");
        // where条件
        tableData.put(JpaRelPip.params,columns);
        // 拼接的sql
        tableData.put(JpaRelPip.appendSql," and 1=1 order by ");
        // 返回的数量
        tableData.put(JpaRelPip.limit,10);
        // 读取数据的位置
        tableData.put(JpaRelPip.offset,0);
        // sql --
        SqlPara sqlPara = EDb.getSqlPara(EDbRelProxy.jpaEdbRelKey,tableData);


        List<String> ids = new ArrayList<>();
        ids.add("1");
        ids.add("2");
        ids.add("3");
        whereData.put("k3",ids);

        //System.out.println(sqlPara.getSql());
    }


}
