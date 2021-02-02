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
package com.edbplus.db.generator.util;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.generator.entity.GenTable;
import com.edbplus.db.generator.entity.GenTableColumn;
import com.edbplus.db.generator.jdbc.GenJdbc;
import com.edbplus.db.generator.jdbc.GenMysql;
import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 自动代码生成工具
 *
 * @author MrYang
 * @date 2019-02-26
 */
public class EDbGenCode {

    public static EDbPro edbPro;
    
    public static EDbPro getEDbPro(){
        if(edbPro==null){
            // 返回EDb默认数据源
            edbPro = EDb.use();
            return edbPro;
        }
        return edbPro;
    }

    /**
     * 表数据初始化
     * @param table
     * @param columnList
     */
    public static void loadTableData(GenTable table,List<GenTableColumn> columnList){

        // 获取表名
        String tableName = table.getTableName().trim();

        // 先获取表对象属性
        Record tableRecord = getEDbPro().findFirst(GenMysql.getTableInfoSql(tableName));
        if(tableRecord==null){
            throw new  RuntimeException(" 数据库不存在该表： "+tableName);
        }

        // 转成忽略大小写的map对象,便于取值
        Map<String,Object> tableInfo = new CaseInsensitiveMap(tableRecord.getColumns() );

        // 转换成bean对象
        GenTable genTable = BeanUtil.mapToBean(tableInfo,GenTable.class,false);
        if(table.getCreater() != null ){
            // 回填创建人信息
            genTable.setCreater(table.getCreater());
        }else{
            genTable.setCreater(GenJdbc.creater);
        }
        // 回填项目名称
        if(table.getProjectName() != null ){
            genTable.setProjectName(table.getProjectName());
        }
        // 必须使用赋值的方式，否则对象传递无法发挥作用，对象内存变化无法指定
        BeanUtil.copyProperties(genTable,table);


        List<Record> columns = getEDbPro().find(GenMysql.getTableColumnsSql(tableName));

        GenTableColumn genTableColumn ;
        String[] nums ;
        // 指定类型
        for(Record column:columns){
            genTableColumn = new GenTableColumn();
            genTableColumn = BeanUtil.mapToBean(column.getColumns(),GenTableColumn.class,false);
            // 判断我方数据库字段
            if(GenJdbc.filedTypeMap.get(genTableColumn.getDataType()) == null){
                // 回填表字段名称
                throw new RuntimeException("字段: "+genTableColumn.getColumnName()+" ;目前没有匹配的类型:" + genTableColumn.getDataType() + "建议使用对应的通配类型");
            }
            // 指定字段的java类型
            genTableColumn.setColumnType(GenJdbc.filedTypeMap.get(genTableColumn.getDataType()));
            // 驼峰式字段名 -- 必须将字符转小写，不然驼峰字段转换会出意外
            genTableColumn.setColumnCode(StrUtil.toCamelCase(genTableColumn.getColumnName().toLowerCase()));
            // 通过sql语句获取的字段长度
            if(genTableColumn.getMaxL()!=null && genTableColumn.getMaxL().contains(",")){
                nums = genTableColumn.getMaxL().split(",");
                // 长度
                genTableColumn.setMaxL(nums[0]);
                // 根据长度回填 N 个 9，代表最大值
                genTableColumn.setMaxValue(String.join("", Collections.nCopies(Integer.valueOf(nums[0]) - Integer.valueOf(nums[1]), "9")));
                // 小数位数
                genTableColumn.setDecimalDigit(nums[1]);
            }else{

                if(genTableColumn.getMaxL().length()>0){
                    // 根据长度回填 N 个 9，代表最大值
                    genTableColumn.setMaxValue(String.join("", Collections.nCopies(Integer.valueOf(genTableColumn.getMaxL()), "9")));
                }
                genTableColumn.setDecimalDigit("0");
            }

            // 赋予对象
            columnList.add(genTableColumn);
        }


        // 回填表主键
        for(GenTableColumn column:columnList){
            if(column.getColumnKey().equalsIgnoreCase("PRI")){
                // 设置主键名称
                table.setPriKeyClassName(column.getColumnCode());
                // 设置首字母大写的主键对象
                table.setPriKeyBigClassName(StrUtil.upperFirst(column.getColumnCode()));
                // 主键类型
                table.setPriKeyJavaType(column.getColumnType());


                break;
            }
        }


        // 填充className字段
        loadClassName(table);
    }

    /**
     * 装载className字段
     * @param table
     */
    public static void loadClassName(GenTable table){
        String preTableName = "";
        String className = "";
        String tableName = table.getTableName();
        // 去掉第一个下划线表前缀，避免前端暴露表对象全名称
        //tableName = tableName.substring(tableName.indexOf("_")+1,tableName.length());
        // 非空的时候替换
        if(!StrKit.isBlank(GenJdbc.tablePreRemove)){
            preTableName = GenJdbc.tablePreRemove.trim();
            if(preTableName.indexOf("_") < 0){
                preTableName +="_";
            }
            // 表前缀转小写
            preTableName = preTableName.toLowerCase();
            // 表名转小写并做替换处理
            className = tableName.toLowerCase().replaceAll(preTableName,"");
            // 驼峰转换
            className = StrUtil.upperFirst(StrUtil.toCamelCase(className));
            // 设置javaClass表名称
            table.setClassName(className);
        }else{
            // 设置javaClass表名称, false 大驼峰
            table.setClassName(StrUtil.upperFirst(StrUtil.toCamelCase(tableName)));
        }
        // 转小驼峰 - 大驼峰的首字母小写即可
        table.setSmallClassName(StringUtils.uncapitalize(table.getClassName()));
    }


    /**
     * 加载路径填充封装
     * @param objPre
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadObjUrlAndObjPackageName(String objPre,String projectUrl,String packageName,GenTable table){
        Map<String,Object> tableMap = BeanUtil.beanToMap(table);

        // 去除空格
        projectUrl = projectUrl.trim();
        packageName = packageName.trim();

        // 包名格式判断
        if(packageName.indexOf(".")<0){
            throw new  RuntimeException(" packageName格式有误，必须是com.xxx.xxx的格式 ; 当前格式： "+ packageName);
        }

        String packageUrl = "";
        // 如果不是以文件符结束的
        if(!projectUrl.endsWith(File.separator)){
            projectUrl += File.separator;
        }

        //table.setEntityProjectUrl(projectUrl);
        tableMap.put(objPre + "ProjectUrl",projectUrl);

        // 替换成文件目录
        packageUrl = packageName.replaceAll("\\.","\\\\");
        // 如果不是以文件符结束的
        if(!packageUrl.endsWith(File.separator)){
            packageUrl += File.separator;
        }
        //table.setEntityPackageUrl(packageUrl);
        tableMap.put(objPre + "PackageUrl",packageUrl);
        //去除最后一个空格
        if(packageName.endsWith(".")){
            packageName = packageName.substring(0,packageName.length()-1);
        }
        //table.setEntityPackageName(packageName);
        tableMap.put(objPre + "PackageName",packageName);
        //
        BeanUtil.copyProperties(BeanUtil.mapToBean(tableMap,table.getClass(),false),table);
    }



    /**
     * 实体对象
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadEDbEntityUrlAndPackageName(String projectUrl,String packageName,GenTable table){
        loadObjUrlAndObjPackageName("entity",projectUrl,packageName,table);
    }


    /**
     * 生成实体类 -- EDb jpa实体
     * @param genClass
     * @param list
     */
    public static void generatorEDbEntity(String projectUrl,String packageName,GenTable genClass, List<GenTableColumn> list){
        // 加载文件路径
        loadEDbEntityUrlAndPackageName(projectUrl,packageName,genClass);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        EngineUtil engineUtil = new EngineUtil();
        StringBuilder outBaseJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getEntityProjectUrl() + genClass.getEntityPackageUrl() + File.separator + "base"+ File.separator )
                //.append(File.separator)
                // 大驼峰
                .append("Base"+genClass.getClassName()+".java");

        File baseFile = new File(outBaseJavaFile.toString());
        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(baseFile.exists()){
            System.out.println("===" + baseFile.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }
        // 先渲染baseJpa
        engineUtil.render(baseUrl.getPath(),
                // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                "/edb-template/java/baseJpa.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outBaseJavaFile
        );

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getEntityProjectUrl() + genClass.getEntityPackageUrl())
                //.append(File.separator)
                // 大驼峰
                .append(genClass.getClassName()+".java");
        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }

        // 再渲染Jpa
        engineUtil.render(baseUrl.getPath(),
                // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                "/edb-template/java/jpa.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }


    /**
     * service接口
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadEDbServiceApiUrlAndServiceApiPackageName(String projectUrl,String packageName,GenTable table){
        loadObjUrlAndObjPackageName("iservice",projectUrl,packageName,table);
    }


    /**
     * 生成数据库服务接口
     * @param serviceApiProjectUrl
     * @param serviceApiPackageName
     * @param genClass
     * @param list
     */
    public final static void generatorEDbServiceApi(String serviceApiProjectUrl,String serviceApiPackageName,GenTable genClass, List<GenTableColumn> list){
        loadEDbServiceApiUrlAndServiceApiPackageName(serviceApiProjectUrl,serviceApiPackageName,genClass);

        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        EngineUtil engineUtil = new EngineUtil();

        StringBuilder outJavaFile =  new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getIserviceProjectUrl() + genClass.getIservicePackageUrl())
                //.append(File.separator)
                // 大驼峰
                .append(genClass.getClassName()+"Service.java");

        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }
        engineUtil.render(baseUrl.getPath(),
                "/edb-template/java/iservice.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }


    /**
     * xls对象数据封装
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadEDbXlsUrlAndPackageName(String projectUrl,String packageName,GenTable table){
        loadObjUrlAndObjPackageName("xls",projectUrl,packageName,table);
    }

    /**
     * 生成数据库服务接口
     * @param projectUrl
     * @param packageName
     * @param genClass
     * @param list
     */
    public final static void generatorEDbXlsBean(String projectUrl,String packageName,GenTable genClass, List<GenTableColumn> list){
        loadEDbXlsUrlAndPackageName(projectUrl,packageName,genClass);

        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        EngineUtil engineUtil = new EngineUtil();

        StringBuilder outJavaFile =  new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getXlsProjectUrl() + genClass.getXlsPackageUrl())
                //.append(File.separator)
                // 大驼峰
                .append(genClass.getClassName()+"Xls.java");

        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }
        engineUtil.render(baseUrl.getPath(),
                "/edb-template/java/baseXls.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }


    /**
     * service实现类
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadEDbServiceUrlAndServicePackageName(String projectUrl,String packageName,GenTable table){
        loadObjUrlAndObjPackageName("service",projectUrl,packageName,table);
    }


    /**
     * 生成数据库服务实现类
     * @param serviceProjectUrl
     * @param servicePackageName
     * @param genClass
     * @param list
     */
    public final static void generatorEDbService(String serviceProjectUrl,String servicePackageName,GenTable genClass, List<GenTableColumn> list){
        // 实现类
        loadEDbServiceUrlAndServicePackageName(serviceProjectUrl,servicePackageName,genClass);

        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        EngineUtil engineUtil = new EngineUtil();
        StringBuilder outJavaFile =   new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getServiceProjectUrl() + genClass.getServicePackageUrl())
                //.append(File.separator)
                // 大驼峰
                .append(genClass.getClassName()+"ServiceImpl.java");

        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }

        engineUtil.render(baseUrl.getPath(),
                "/edb-template/java/service.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }


    /**
     * controller 对象
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadEDbControllerUrlAndControllerPackageName(String projectUrl,String packageName,GenTable table){
        loadObjUrlAndObjPackageName("controller",projectUrl,packageName,table);
        // 需要生成controller的相关web路径
        // 规则表名前缀/表名驼峰+Action
        int firstSp = table.getTableName().indexOf("_");
        // 获取表名第一个下划线前缀
        String preTableName = table.getTableName().substring(0,firstSp);
        // 获取表名第一个下划线之后的所有字符串
        String lastTableName = table.getTableName().substring(firstSp+1,table.getTableName().length());
        String preUrl = preTableName.toLowerCase();
        // 小驼峰
        String lastUrl = StringUtils.uncapitalize(lastTableName);

        if(!StrKit.isBlank(table.getModelName())){
            // 取传递进来的包名 ，而不用表名前缀分包
            preUrl = table.getModelName().trim().toLowerCase();
        }
        // 小写 + 小驼峰
        String webUrl = preUrl + "/" + lastUrl;
        // 小写 + 全小写的小驼峰
        String webHtmlUrl = preUrl + "/" + lastUrl.toLowerCase();
        table.setControllerWebUrl(webUrl);
        table.setControllerHttpUrl(webHtmlUrl);

    }


    /**
     * 生成controller层页面
     * @param controllerProjectUrl
     * @param controllerPackageName
     * @param genClass
     * @param list
     */
    public final static void generatorEDbController(String controllerProjectUrl,String controllerPackageName,GenTable genClass, List<GenTableColumn> list){
        // 实现类
        loadEDbControllerUrlAndControllerPackageName(controllerProjectUrl,controllerPackageName,genClass);

        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        EngineUtil engineUtil = new EngineUtil();
        StringBuilder outJavaFile =   new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getControllerProjectUrl() + genClass.getControllerPackageUrl())
                // 大驼峰
                .append(genClass.getClassName()+"Controller.java");

        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }
        engineUtil.render(baseUrl.getPath(),
                "/edb-template/java/controller.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }


    /**
     * 生成controller层页面
     * @param controllerProjectUrl
     * @param controllerPackageName
     * @param genClass
     * @param list
     */
    public final static void generatorEDbVueController(String controllerProjectUrl,String controllerPackageName,GenTable genClass, List<GenTableColumn> list){
        // 实现类
        loadEDbControllerUrlAndControllerPackageName(controllerProjectUrl,controllerPackageName,genClass);

        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        EngineUtil engineUtil = new EngineUtil();
        StringBuilder outJavaFile =   new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(genClass.getControllerProjectUrl() + genClass.getControllerPackageUrl())
                // 大驼峰
                .append(genClass.getClassName()+"Controller.java");

        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }
        engineUtil.render(baseUrl.getPath(),
                "/edb-template/java/vueController.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }

    /**
     * 生成HTML
     * @param htmlPre
     * @param controllerProjectUrl
     * @param controllerPackageName
     * @param genClass
     * @param list
     */
    public static void generatorEDbHtml(String htmlPre,String controllerProjectUrl,String controllerPackageName,GenTable genClass, List<GenTableColumn> list){
        // 实现类
        loadEDbControllerUrlAndControllerPackageName(controllerProjectUrl,controllerPackageName,genClass);
        String controllerUrl = genClass.getControllerProjectUrl();
        int lastIdx = controllerUrl.lastIndexOf("java");
        String webUrl = controllerUrl.substring(0,lastIdx) +"resources" + controllerProjectUrl.substring(lastIdx+4,controllerUrl.length());

        StringBuilder outJavaFile =    new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(webUrl)
                .append(File.separator )
                .append("views")
                .append(File.separator)
                .append(genClass.getControllerHttpUrl())
                .append(File.separator)
                // 小驼峰
                .append(genClass.getSmallClassName()+StringUtils.capitalize(htmlPre)+".html");

        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }

        EngineUtil engineUtil = new EngineUtil();
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        // 生成相关的html
        engineUtil.render(baseUrl.getPath(),
                "/edb-template/web/html/"+htmlPre+"html.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }

    /**
     * 生成JS
     * @param htmlPre
     * @param controllerProjectUrl
     * @param controllerPackageName
     * @param genClass
     * @param list
     */
    public static void generatorEDbJs(String htmlPre,String controllerProjectUrl,String controllerPackageName,GenTable genClass, List<GenTableColumn> list){
        // 实现类
        loadEDbControllerUrlAndControllerPackageName(controllerProjectUrl,controllerPackageName,genClass);
        String controllerUrl = genClass.getControllerProjectUrl();
        int lastIdx = controllerUrl.lastIndexOf("java");
        String webUrl = controllerUrl.substring(0,lastIdx) +"resources" + controllerProjectUrl.substring(lastIdx+4,controllerUrl.length());

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                .append(webUrl)
                .append(File.separator )
                .append("static")
                .append(File.separator)
                .append("js")
                .append(File.separator)
                .append(genClass.getControllerHttpUrl())
                .append(File.separator)
                // 小驼峰
                .append(genClass.getSmallClassName()+StringUtils.capitalize(htmlPre)+".js");

        File file = new File(outJavaFile.toString());
        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }

        EngineUtil engineUtil = new EngineUtil();
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");
        // 生成相关的js
        engineUtil.render(baseUrl.getPath(),
                "/edb-template/web/js/"+htmlPre+"js.tpl",
                Kv.by("genClass", genClass)
                        .set("fields",list)
                        .set("nowdatetime", DateUtil.now())
                ,
                outJavaFile
        );
    }



    /**
     * 生成HTML和JS
     * @param htmlPre
     * @param controllerProjectUrl
     * @param controllerPackageName
     * @param genClass
     * @param list
     */
    public static void generatorEDbHtmlAndjs(String htmlPre,String controllerProjectUrl,String controllerPackageName,GenTable genClass, List<GenTableColumn> list){
        // 生成HTML
        generatorEDbHtml(htmlPre,controllerProjectUrl,controllerPackageName,genClass,list);
        // 生成JS
        generatorEDbJs(htmlPre,controllerProjectUrl,controllerPackageName,genClass,list);
    }


    /**
     * 生成controller层页面和视图页面
     * @param controllerProjectUrl
     * @param controllerPackageName
     * @param genClass
     * @param list
     */
    public final static void generatorEDbControllerAndView(String controllerProjectUrl,String controllerPackageName,GenTable genClass, List<GenTableColumn> list){
        // 生成controller层
        generatorEDbController(controllerProjectUrl,controllerPackageName,genClass,list);
        // 生成前端页面
        // 生成列表
        generatorEDbHtmlAndjs("list",controllerProjectUrl,controllerPackageName,genClass,list);
        // 生成from表单
        generatorEDbHtmlAndjs("form",controllerProjectUrl,controllerPackageName,genClass,list);


    }







}
