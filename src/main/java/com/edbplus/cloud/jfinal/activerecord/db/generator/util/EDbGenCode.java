package com.edbplus.cloud.jfinal.activerecord.db.generator.util;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import com.edbplus.cloud.jfinal.activerecord.db.EDb;
import com.edbplus.cloud.jfinal.activerecord.db.EDbPro;
import com.edbplus.cloud.jfinal.activerecord.db.generator.entity.GenTable;
import com.edbplus.cloud.jfinal.activerecord.db.generator.entity.GenTableColumn;
import com.edbplus.cloud.jfinal.activerecord.db.generator.jdbc.GenJdbc;
import com.edbplus.cloud.jfinal.activerecord.db.generator.jdbc.GenMysql;
import com.edbplus.cloud.jfinal.activerecord.db.generator.jdbc.GenPg;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
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

    static {
        if(edbPro==null){
            edbPro = EDb.use();
        }
    }

    /**
     * 表数据初始化
     * @param table
     * @param columnList
     */
    public static void loadTableData( GenTable table, List<GenTableColumn> columnList){

        String tableName = table.getTableName().trim();

        // 先获取表对象属性
        Record tableRecord = null;
        if(edbPro.getConfig().getDialect() instanceof PostgreSqlDialect){
            tableRecord = edbPro.findFirst(GenPg.getTableInfoSql(tableName));
        }else{
            tableRecord = edbPro.findFirst(GenMysql.getTableInfoSql(tableName));
        }

        if(tableRecord==null){
            throw new  RuntimeException(" 数据库不存在该表： "+tableName);
        }

        // 转成忽略大小写的map对象,便于取值
        Map<String,Object> tableInfo = new CaseInsensitiveMap(tableRecord.getColumns() );

        // 转换成bean对象
        GenTable genTable = BeanUtil.mapToBean(tableInfo,GenTable.class,false);

        if(table.getCreater() != null){
            genTable.setCreater(table.getCreater());
        }else{
            // 回填创建人信息
            table.setCreater(GenJdbc.creater);
        }

        if(table.getModelName() != null ){
            genTable.setModelName(table.getModelName());
        }

        if(table.getProjectName() != null ){
            genTable.setProjectName(table.getProjectName());
        }

        // 必须使用赋值的方式，否则对象传递无法发挥作用，对象内存变化无法指定
        BeanUtils.copyProperties(genTable,table);


        List<Record> columns = null;
        if(edbPro.getConfig().getDialect() instanceof PostgreSqlDialect){
            columns = edbPro.find(GenPg.getTableColumnsSql(tableName));
        }else{
            columns = edbPro.find(GenMysql.getTableColumnsSql(tableName));
        }

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
            // 驼峰式字段名
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
                table.setPriKeyClassName(column.getColumnCode());
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
        if(StrUtil.isNotEmpty(GenJdbc.tablePreRemove)){
            preTableName = GenJdbc.tablePreRemove.trim();
            if(preTableName.indexOf("_") < 0){
                preTableName +="_";
            }
            // 表前缀转小写
            preTableName = preTableName.toLowerCase();
            // 表名转小写并做替换处理
            className = tableName.toLowerCase().replaceAll(preTableName,"");
            // 驼峰转换
            className = StrUtil.toCamelCase(className);
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
        BeanUtils.copyProperties(BeanUtil.mapToBean(tableMap,table.getClass(),false),table);
    }


    /**
     * 实体对象
     * @param projectUrl
     * @param packageName
     * @param table
     */
    public static void loadEntityUrlAndPackageName(String projectUrl,String packageName,GenTable table){
        loadObjUrlAndObjPackageName("entity",projectUrl,packageName,table);
    }






    /**
     * 生成实体类
     * @param projectUrl -- 实体对象目录
     * @param packageName -- 实体对象包名
     * @param genTable -- 实体表对象
     * @param genTableColumns -- 实体表对象字段集
     */
    public static void generatorBaseJpaBean(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){

        // 加载文件路径 -- 实体对象目录,实体的包名
        loadEntityUrlAndPackageName(projectUrl,packageName,genTable);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName = "Base" + genTable.getClassName()+".java" ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // 实体对象的目录
                .append(genTable.getEntityProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        if(file.exists()){
            System.out.println(" === "+ javaFileName +" 文件重写 === ");
            //file.delete();
        }


        // 指向目录
        EngineUtil.render(baseUrl.getPath(),
                // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                "/edb-template/java/baseJpa.tpl",
                Kv.by("genClass", genTable)
                        .set("fields",genTableColumns)
                        .set("nowdatetime", DateUtil.now())
                ,outJavaFile
        );
    }


    /**
     * 生成VO实体类
     * @param projectUrl -- 实体对象目录
     * @param packageName -- 实体对象包名
     * @param genTable -- 实体表对象
     * @param genTableColumns -- 实体表对象字段集
     */
    public static void generatorVoBean(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){

        // 加载文件路径 -- 实体对象目录,实体的包名
        loadEntityUrlAndPackageName(projectUrl,packageName,genTable);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName =  genTable.getClassName()+"Vo.java" ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // 实体对象的目录
                .append(genTable.getEntityProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
           System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
           return;
        }


        // 指向目录
        EngineUtil.render(baseUrl.getPath(),
                // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                "/edb-template/java/baseVo.tpl",
                Kv.by("genClass", genTable)
                        .set("fields",genTableColumns)
                        .set("nowdatetime", DateUtil.now())
                ,outJavaFile
        );
    }


    /**
     * 生成 Xls 实体类
     * @param projectUrl -- 实体对象目录
     * @param packageName -- 实体对象包名
     * @param genTable -- 实体表对象
     * @param genTableColumns -- 实体表对象字段集
     */
    public static void generatorXlsBean(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){


        loadObjUrlAndObjPackageName("xls",projectUrl,packageName,genTable);

        // 加载文件路径 -- 实体对象目录,实体的包名
        loadEntityUrlAndPackageName(projectUrl,packageName,genTable);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName =  genTable.getClassName()+"Xls.java" ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // 实体对象的目录
                .append(genTable.getEntityProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        // 已存在则不做任何处理 -- 必须删除后重新创建
        if(file.exists()){
            System.out.println("===" + file.getName() + "已存在，重新生成，需要删除后重建");
            return;
        }


        // 指向目录
        EngineUtil.render(baseUrl.getPath(),
                // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                "/edb-template/java/baseXls.tpl",
                Kv.by("genClass", genTable)
                        .set("fields",genTableColumns)
                        .set("nowdatetime", DateUtil.now())
                ,outJavaFile
        );
    }



    /**
     * 扩展的JPA对象实体方法
     * @param projectUrl
     * @param packageName
     * @param genTable
     * @param genTableColumns
     */
    public static void generatorJpaBean(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){

        loadObjUrlAndObjPackageName("jpa",projectUrl,packageName,genTable);

        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName = genTable.getClassName()+".java" ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // 实体对象的目录
                .append(genTable.getJpaProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        if( !file.exists() ){
            // 指向目录
            EngineUtil.render(baseUrl.getPath(),
                    // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                    "/edb-template/java/jpa.tpl",
                    Kv.by("genClass", genTable)
                            .set("fields",genTableColumns)
                            .set("nowdatetime", DateUtil.now())
                    ,outJavaFile
            );
        }
    }

    /**
     *生成接口
     * @param projectUrl
     * @param packageName
     * @param genTable
     * @param genTableColumns
     */
    public static void generatorService(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){
        loadObjUrlAndObjPackageName("iservice",projectUrl,packageName,genTable);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName = genTable.getClassName()+"Service.java" ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // 实体对象的目录
                .append(genTable.getIserviceProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        if( !file.exists() ){
            // 指向目录
            EngineUtil.render(baseUrl.getPath(),
                    // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                    "/edb-template/java/iservice.tpl",
                    Kv.by("genClass", genTable)
                            .set("fields",genTableColumns)
                            .set("nowdatetime", DateUtil.now())
                    ,outJavaFile
            );
        }
    }


    /**
     * service实现类生成位置
     * @param projectUrl
     * @param packageName
     * @param genTable
     * @param genTableColumns
     */
    public static void generatorServiceImpl(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){
        loadObjUrlAndObjPackageName("service",projectUrl,packageName,genTable);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName = genTable.getClassName()+"ServiceImpl.java" ; ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // service对象的目录
                .append(genTable.getServiceProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        if( !file.exists() ){
            // 指向目录
            EngineUtil.render(baseUrl.getPath(),
                    // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                    "/edb-template/java/service.tpl",
                    Kv.by("genClass", genTable)
                            .set("fields",genTableColumns)
                            .set("nowdatetime", DateUtil.now())
                    ,outJavaFile
            );
        }
    }


    /**
     * controller实现类生成位置
     * @param projectUrl
     * @param packageName
     * @param genTable
     * @param genTableColumns
     */
    public static void generatorController(String projectUrl,String packageName,GenTable genTable, List<GenTableColumn> genTableColumns){
        loadObjUrlAndObjPackageName("controller",projectUrl,packageName,genTable);
        // 模板的位置
        URL baseUrl = EDbGenCode.class.getResource("/edb-template");

        // 生成的java文件名
        String javaFileName = genTable.getClassName()+"Controller.java" ; ;

        StringBuilder outJavaFile = new StringBuilder()
                // .append(System.getProperty("user.dir"))
                // web对象的目录
                .append(genTable.getControllerProjectUrl() )
                // 大驼峰
                .append(javaFileName);
        File file = new File(outJavaFile.toString());

        if( !file.exists() ){
            // 指向目录
            EngineUtil.render(baseUrl.getPath(),
                    // 单独指定，靠上面的传入值只会获取编译后的包的路径，无法取到正确的路径信息
                    "/edb-template/java/controller.tpl",
                    Kv.by("genClass", genTable)
                            .set("fields",genTableColumns)
                            .set("nowdatetime", DateUtil.now())
                    ,outJavaFile
            );
        }
    }














    /**
     * 获取基础路径
     * @param copyWindowsPath
     * @return
     */
    public static String getBaseUrl(String copyWindowsPath){
        // 由于 \ 字符串转义不好处理，所以需要先转 - 最后再转 \
        String currentProject = System.getProperty("user.dir");
        currentProject = currentProject.replaceAll("\\\\","-");
        copyWindowsPath = copyWindowsPath.replaceAll("\\\\","-");
        copyWindowsPath = copyWindowsPath.replaceAll(currentProject,"");
        return  copyWindowsPath.replaceAll("-","\\\\");
    }

    /**
     * 将基础路径转换成包名
     * @param copyWindowsPath
     * @return
     */
    public static String getBasePackageName(String copyWindowsPath){
        // 由于 \ 字符串转义不好处理，所以需要先转 - 最后再转 \
        String currentProject = System.getProperty("user.dir");
        currentProject = currentProject.replaceAll("\\\\","-");
        copyWindowsPath = copyWindowsPath.replaceAll("\\\\","-");
        String replaceSrcStr = "-src-main-java-";
        int idx = copyWindowsPath.indexOf(replaceSrcStr) + replaceSrcStr.length();
        copyWindowsPath = copyWindowsPath.substring(idx,copyWindowsPath.length());
        return  copyWindowsPath.replaceAll("-","\\.");
    }

}
