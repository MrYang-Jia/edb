/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.generator.entity;

import lombok.Data;

import java.util.Date;

/**
 *  表实体对象
 */
@Data
public class GenTable {
    // 表名称
    private  String tableName ;
    // 表主键的数据库名
    private String priKeyColumnName;
    // 主键名称
    private String priKeyClassName;
    // 首字母大写
    private String priKeyBigClassName;
    // 主键java类型
    private String priKeyJavaType;
    // 项目名称
    private String projectName = "edb-platform";
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
    // js的web访问路径
    private String jsWebUrl;


    // controller层Url
    private String controllerWebUrl;
    // controller层Html存放相对路径
    private String controllerHtmlUrl;

    // controller requestAction 访问路径前缀
    private String actionUrl;



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
    // jpa包路径
    private String jpaPackageUrl;
    // xls 包路径
    private String xlsPackageUrl;
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


    /**
     * 将带有小数点的包名转换成 webUrl 路径的访问模式
     * @param packageName
     * @return
     */
    public static String webUrlFormat(String packageName){
        return packageName.replaceAll("\\.","/");
    }
}
