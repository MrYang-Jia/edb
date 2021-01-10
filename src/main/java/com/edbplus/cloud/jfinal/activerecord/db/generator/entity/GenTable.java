package com.edbplus.cloud.jfinal.activerecord.db.generator.entity;

import lombok.Data;

import java.util.Date;

/**
 *  表实体对象
 */
@Data
public class GenTable {
    // 表名称
    private  String tableName ;
    // 主键名称
    private String priKeyClassName;
    // 项目名称
    private String projectName = "xzw-vescort-platform";
    // model - 模块
    private String modelName;

    // java类名称
    private String className;

    // java类名称
    private String smallClassName;

    // 工程路径 - java
    private String entityProjectUrl;
    // jpa项目工程路径
    private String jpaProjectUrl;
    // xls 数据对象路径
    private String xlsProjectUrl;
    // service 接口项目地址
    private String iserviceProjectUrl;
    // service 实例对象地址
    private String serviceProjectUrl;
    // controller
    private String controllerProjectUrl;
    // html 项目路径
    private String htmlProjectUrl;
    // js
    private String jsProjectUrl;


    // controller层Url
    private String controllerWebUrl;
    // controller层Html存放相对路径
    private String controllerHttpUrl;



    // 包名称
    private String entityPackageName;
    // jpa包路径
    private String jpaPackageName;
    // xls对象的包路径
    private String xlsPackageName;
    // 接口api包名
    private String iservicePackageName;
    // service实现类包名
    private String servicePackageName;
    // controller 包名
    private String controllerPackageName;

    // 包路径
    private String entityPackageUrl;
    // xls 包路径
    private String xlsPackageUrl;
    // jpa包路径
    private String jpaPackageUrl;
    // 接口包名地址
    private String iservicePackageUrl;
    // 实现类包名地址
    private String servicePackageUrl;
    // controller 包名地址
    private String controllerPackageUrl;

    // 表类型
    private  String engine ;
    // 表描述
    private  String tableComment ;
    // 表的创建时间
    private Date createTime ;
    // 创建人
    private String creater;

}
