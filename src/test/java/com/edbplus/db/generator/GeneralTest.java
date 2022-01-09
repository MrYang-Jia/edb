package com.edbplus.db.generator;

import com.edbplus.db.EDb;
import com.edbplus.db.generator.jdbc.GenJdbc;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.generator.entity.GenTable;
import com.edbplus.db.generator.entity.GenTableColumn;
import com.edbplus.db.generator.util.EDbGenCode;
import org.testng.annotations.Test;

import java.util.*;


public class GeneralTest extends BaseTest {

    //注意最后留小数点
//    String srcPre = "com.edbplus.db.jpa.";
    String srcPre = "com.edbplus.test.";
    // api项目的名称 - 对应 artifactId
    String apiName = "com-edbplus-db-api";
    // -- 指定需要生成的表对象
    String tableName = "vw_driver_route";
    // 存储到哪个子项目
    String projectName = "edb";
    // 包名的路径前缀
    String modelName = "edb";
    // 你自己电脑里项目的父路径
    String projectUrl = "";
    // 当前实际项目的位置 -- 这样子配置的好处是可以指定到任何地方
    String entityProjectUrl ;
    // service-api 路径
    String iserviceProjectUrl ;

    // 数据库repository操作项目url
    String jpaProjectUrl ;
    // service 路径
    String serviceProjectUrl ;

    // controller 路径
    String controllerProjectUrl ;

    // 实体放置路径，格式必须是 com.xxx.xxx.xxx
    // 实体包路径
    String entityPackageName ;
    String xlsPackageName ;

    String jpaPackageName ;
    String iservicePackageName ;
    String servicePackageName ;
    String controllerPackageName ;


    // 子项目模块名
    String projectModel;

    @Test
    public void test(){

//        EDbGenCode.edbPro = EDb.use();// 指定数据源对象
        EDbGenCode.edbPro = EDb.use("pg");// 指定数据源对象

        // 子项目包路径名称
        projectModel = "mode";

        // 去掉表前缀
//        GenJdbc.tablePreRemove = "cr_";

        // 加载项目路径
        loadCurrentUrl();


        System.out.println(entityPackageName);
        // 车辆类型
        createTable("cr_vehicle_type");
        // 车辆类型规格
//        createTable("cr_vehicle_type_mode");
//        // 车辆类型规格关系表
//        createTable("cr_vehicle_type_mode_rel");

    }

    public void createTable(String tableName){
        // == 封装表对象，并生成 javaClass 和 packageName 信息
        GenTable table = new GenTable();
        List<GenTableColumn> columnList = new ArrayList<>();

        table.setTableName(tableName);
        // 项目
        table.setProjectName("edb");
        // 这里添加包名
        table.setModelName("edb-model");
        // 作者
        table.setCreater("MrYang");

        //表数据初始化 -- 主要获取表信息和表字段信息，有此基础才能生成其他跟表相关的对象，从而自定义基础模板
        EDbGenCode.loadTableData(table,columnList);

        // 当前项目目录
        String currentProject = System.getProperty("user.dir");

        //
//        // 生成EDb jpa实体
        EDbGenCode.generatorEDbEntity(entityProjectUrl,entityPackageName,table,columnList);
////        // 生成导出实体
//        EDbGenCode.generatorEDbXlsBean(entityProjectUrl,xlsPackageName,table,columnList);
////        // 生成serviceApi
        EDbGenCode.generatorEDbServiceApi(iserviceProjectUrl,iservicePackageName,table,columnList);
////        // 生成service实现类服务
        EDbGenCode.generatorEDbService(serviceProjectUrl,servicePackageName,table,columnList);
//        // 生成 controller控制层对象无视图
//        EDbGenCode.generatorEDbVueController(controllerProjectUrl,controllerPackageName,table,columnList);
//        // 生成 controller控制层对象 和 视图层对象
//        EDbGenCode.generatorEDbControllerAndView(controllerProjectUrl,controllerPackageName,table,columnList);
          // 生成js文件

        String filePreSrc = GenTable.webUrlFormat(projectModel)+"/"+table.getSmallClassName();
        // weburl + 对象小写驼峰 + Action 设置webController层访问路径
        table.setControllerWebUrl(filePreSrc+"Action");
        String webUrl = System.getProperty("user.dir") + "\\src\\main\\resources";
        String webJsProjectUrl = webUrl + "/static/js/"+filePreSrc+"/";
        // 生成 form JS
        EDbGenCode.generalEDbWeb("edb-template/web/js/form-js.tpl",
                webJsProjectUrl,
                table.getSmallClassName()+"Form.js",
                  table,
                  columnList);

        // 生成 list JS
        EDbGenCode.generalEDbWeb("edb-template/web/js/list-js.tpl",
                webJsProjectUrl,
                table.getSmallClassName()+"List.js",
                table,
                columnList);

        // 设置js的访问路径
        table.setJsWebUrl("/js/"+filePreSrc);
        String webHtmlProjectUrl = webUrl + "/view/"+filePreSrc+"/";
        // 输出html
        EDbGenCode.generalEDbWeb("edb-template/web/html/form-html.tpl",
                webHtmlProjectUrl,
                table.getSmallClassName()+"Form.html",
                table,
                columnList);
        // 输出html
        EDbGenCode.generalEDbWeb("edb-template/web/html/list-html.tpl",
                webHtmlProjectUrl,
                table.getSmallClassName()+"List.html",
                table,
                columnList);
        // html文件存放路径
        table.setControllerHtmlUrl(filePreSrc);
        // controller
        String controllerProjectUrl = System.getProperty("user.dir") +"\\src\\main\\java\\" +GenTable.webUrlFormat(srcPre)+"/"+projectModel+"/";
        table.setControllerPackageName(srcPre+projectModel);
//        // 输出Controller
//        EDbGenCode.generalEDbWeb("edb-template/java/web-controller.tpl",
//                controllerProjectUrl,
//                table.getClassName()+"Controller.java",
//                table,
//                columnList);


    }

    @Test
    public void webUrlFormat(){
        String srcUrl = "pom.pp";
        System.out.println(srcUrl.replaceAll("\\.","/"));
    }




    /**
     * 加载当前项目路径
     */
    public void loadCurrentUrl(){
//        String srcFile = "test";
        String srcFile = "main";
        String currentProjectParent =  System.getProperty("user.dir");

        projectUrl = currentProjectParent;
        // 由于全部要在 test 的目录底下生成脚手架测试文件，所以路径统一替换成 test 目录底下
        // entity 实力放置路径
        entityProjectUrl = projectUrl + "\\src\\"+srcFile+"\\java";
        // service-api 路径
        iserviceProjectUrl = entityProjectUrl;

        // 数据库repository操作项目url
        jpaProjectUrl = projectUrl +"\\src\\"+srcFile+"\\java\\";
        // service 路径
        serviceProjectUrl = jpaProjectUrl;
        // controller 路径
        controllerProjectUrl = projectUrl +"\\src\\"+srcFile+"\\java\\";


        // 实体包路径
        entityPackageName = srcPre + "entity."+ projectModel;
        // xls实体包路径
        xlsPackageName = srcPre + "xls."+ projectModel;
        // jpa包路径
        jpaPackageName = srcPre + modelName + ".repository."+ projectModel;
        // 接口包路径
        iservicePackageName = srcPre  + "service."   + projectModel ;
        // 接口实现类服务
        servicePackageName = srcPre + "service."   + projectModel + ".impl" ;
        // 控制器包路径
        controllerPackageName = srcPre  + "web." + projectModel;

    }



}



