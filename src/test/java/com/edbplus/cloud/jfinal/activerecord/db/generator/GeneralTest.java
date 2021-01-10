package com.edbplus.cloud.jfinal.activerecord.db.generator;

import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import com.edbplus.cloud.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.cloud.jfinal.activerecord.db.generator.entity.GenTable;
import com.edbplus.cloud.jfinal.activerecord.db.generator.entity.GenTableColumn;
import com.edbplus.cloud.jfinal.activerecord.db.generator.util.EDbGenCode;
import org.testng.annotations.Test;

import java.util.*;


public class GeneralTest extends BaseTest {



    @Test
    public void testCreateVo(){
        String sql = " select * from tra_vehicle_source limit 1 ";
    }


    @Test
    public void test(){

//        EDbGenCode.edbPro = EDb.use("xzw");
        EDbGenCode.edbPro = EDb.use();
        // 车辆类型
        createTable("cr_vehicle_type");
        // 车辆类型规格
        createTable("cr_vehicle_type_mode");
        // 车辆类型规格关系表
        createTable("cr_vehicle_type_mode_rel");

    }

    public void createTable(String tableName){
        // == 封装表对象，并生成 javaClass 和 packageName 信息
        GenTable table = new GenTable();
        List<GenTableColumn> columnList = new ArrayList<>();

        table.setTableName(tableName);
        // 项目
        table.setProjectName("xzw-dac");
        // 这里添加包名
        table.setModelName("xzw-dac-model");
        // 作者
        table.setCreater("MrYang");

        //表数据初始化 -- 主要获取表信息和表字段信息，有此基础才能生成其他跟表相关的对象，从而自定义基础模板
        EDbGenCode.loadTableData(table,columnList);

        // 当前项目目录
        String currentProject = System.getProperty("user.dir");

        // 实体数据对象
        String baseBeanUrl = currentProject + "\\src\\test\\java\\com\\edb\\cloud\\jfinal\\activerecord\\db\\jpa\\model\\base";
        String baseBeanPackageName ="com.edb.cloud.jfinal.activerecord.db.jpa.model.base";
        // 生成 baseJpa 对象
        EDbGenCode.generatorBaseJpaBean(baseBeanUrl,baseBeanPackageName,table,columnList);

        // 扩展的实体数据对象
        String beanUrl = currentProject + "\\src\\test\\java\\com\\edb\\cloud\\jfinal\\activerecord\\db\\jpa\\model";
        String beanPackageName ="com.edb.cloud.jfinal.activerecord.db.jpa.model";
        // 生成继承 baseJpa 对象的子类 -- 不会覆盖
        EDbGenCode.generatorJpaBean(beanUrl,beanPackageName,table,columnList);

        // 指定目录生成 Vo 视图
        String voBeanUrl = currentProject + "\\src\\test\\java\\com\\edb\\cloud\\jfinal\\activerecord\\db\\jpa\\vo";
        String voBeanPackageName ="com.edb.cloud.jfinal.activerecord.db.jpa.vo";
        // 生成 Vo 对象 -- 不会覆盖
        EDbGenCode.generatorVoBean(voBeanUrl,voBeanPackageName,table,columnList);

        // 指定目录生成 Vo 视图
        String xlsBeanUrl = currentProject + "\\src\\test\\java\\com\\edb\\cloud\\jfinal\\activerecord\\db\\jpa\\xls";
        String xlsBeanPackageName ="com.edb.cloud.jfinal.activerecord.db.jpa.xls";
        // 生成 Vo 对象 -- 不会覆盖
        EDbGenCode.generatorXlsBean(xlsBeanUrl,xlsBeanPackageName,table,columnList);

//        // 按顺序生成对应的
//        String serviceUrl = currentProject + "\\src\\test\\java\\com\\edb\\cloud\\jfinal\\activerecord\\db\\service";
//        String servicePackageName ="com.edb.cloud.jfinal.activerecord.db.service";
//        // 生成继承 baseJpa 对象的子类 -- 不会覆盖
//        EDbGenCode.generatorService(serviceUrl,servicePackageName,table,columnList);
//
//        String serviceImplUrl = currentProject + "\\src\\test\\java\\com\\edb\\cloud\\jfinal\\activerecord\\db\\serviceimpl";
//        String serviceImplPackageName ="com.edb.cloud.jfinal.activerecord.db.serviceimpl";
//        // 生成继承 baseJpa 对象的子类 -- 不会覆盖
//        EDbGenCode.generatorServiceImpl(serviceImplUrl,serviceImplPackageName,table,columnList);
    }


    /**
     * 加载当前项目路径
     */
    @Test
    public void loadCurrentUrl(){
        String currentProject = System.getProperty("user.dir");
        System.out.println(currentProject);
//        int startIdx  = currentProject.indexOf("xzw-bss-crm-test");
//        String currentProjectParent = currentProject.substring(0,startIdx);


    }



}



