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
package com.edbplus.db.dto;

import cn.hutool.system.SystemUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbProFactory;
import com.edbplus.db.SpringConfig;
import com.edbplus.db.dialect.EDbMysqlDialect;
import com.edbplus.db.listener.EDbListener;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.template.Engine;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName EDbCompositer
 * @Description: edb 基础装载配置对象 , 就跟一个表演者一样
 * @Author 杨志佳
 * @Date 2021/12/27
 * @Version V1.0
 **/
@Data
public class EDbCompositer {

    public static EDbCompositer getInstance(){
        return new EDbCompositer();
    }

    // sql共享方法对象
    private List<String> shareFunctionFilePath = new ArrayList<>();

    // 自定义语法糖对象 - 静态工具类 (通用工具方法，可能需要自己手工指定)
    private List<Object> sharedMethodBeans = new ArrayList<>();

    // 加载sql路径
    private List<String> sqlPaths = new ArrayList<>();

    // 数据操作对象配置
    private SpringConfig activerecordConfig;

    // 数据操作对象插件
    private ActiveRecordPlugin arp;

    // 数据库初始化
    public void initEDb(String configName, DataSource dataSource , Dialect dialect) {
        // 适配spring数据库连接池 -- 适配事务
        activerecordConfig = new SpringConfig(
                // 默认名称 ，使用 Db.use() 时，可获取到
                configName
                // 这里可以替换成 spring体系的datasource
                ,dataSource
                // 事务级别，可以取spring当前的事务级别引入，如果是默认，用该方式即可
                , DbKit.DEFAULT_TRANSACTION_LEVEL
        );
        // 启动插件
        arp = new ActiveRecordPlugin(activerecordConfig);
        arp.setDialect(dialect); // 指定方言解析器，优化统计类语句
        // 定义db实现工厂 继承了父类实现方法
        EDbProFactory eDbProFactory = new EDbProFactory();
        // 设置 edbpro 工厂
        arp.setDbProFactory(eDbProFactory);
        // 设置模板路径
        arp.getEngine().setToClassPathSourceFactory();
        // 如果非linux环境，开启开发者模式
        if(!SystemUtil.getOsInfo().isLinux()){
            // 开发者模式 - 热加载
            arp.setDevMode(true);
        }else{
            // 非开发者模式
            arp.setDevMode(false);
        }
        // 打印sql -- 交予底层统一打印
        arp.setShowSql(false);
        // 添加共享模板
        setEnjoyShareFunction(arp.getEngine());
        // 语法工具
        setEnjoySharedMethod(arp.getEngine());
        // sql模块加载
        setSqlPath(arp);
    }

    /**
     * 携带监听对象启动
     * @param eDbListener
     */
    public void start(EDbListener eDbListener){
        // 启动Record容器
        arp.start();
        // 初始化
        EDb.init(activerecordConfig.getName());
        if(eDbListener!=null){
            // 设置监听对象
            EDb.use().setEDbListener(eDbListener);
        }
    }

    /**
     * 启动无监听方式
     */
    public void start(){
        start(null);
    }

    /**
     * 设置enjoy共享模板
     * @param enjoy
     */
    public void setEnjoyShareFunction(Engine enjoy){
        //添加共享模板 -- 例子
        //
//        if(shareFunctionFilePath.size() == 0){
//            enjoy.addSharedFunction("/sql/sharedfunction/common_function.sql.vm");
//        }
        // 遍历添加共享模板
        for(String sqlFile : shareFunctionFilePath){
            enjoy.addSharedFunction(sqlFile);
        }

    }

    /**
     * 设置语法工具包
     * @param enjoy
     */
    public void setEnjoySharedMethod(Engine enjoy){
        // 添加通用工具类 ,模板可直接引用 StrKit.isBlank("字符串") 等方法
        // enjoy.addSharedMethod(new com.jfinal.kit.StrKit());
        if(sharedMethodBeans.size() == 0){
            // 初始化
            sharedMethodBeans.add(new com.jfinal.kit.StrKit());
        }
        for(Object toolBean:sharedMethodBeans){
            enjoy.addSharedMethod(toolBean);
        }
    }

    /**
     * 设置数据库sql脚本 (便于统一管理)
     * @param arp
     */
    public void setSqlPath(ActiveRecordPlugin arp){
        // 遍历添加共享模板
        for(String sqlFile : sqlPaths){
            // 添加sql文件路径
            arp.addSqlTemplate(sqlFile);
        }
    }


    /**
     * edb资源文件初始化
     * @param sqlPath -- 文件路径
     * @param sqlFileExtension -- sql文件后缀，例如 sql.vm
     */
    public void initResourcesLoad(String sqlPath,String sqlFileExtension){
        // 获取sql资源文件路径下的所有sql文件 -- 使用的是hutool工具类 -- 无效
//        URL url=  ResourceUtil.getResource("sql/bus");
//        File file = FileUtil.file("sql/bus");
        // String sqlPath = "";
        // 这个路径必须写成 / 而不能根据环境的 File.separator 进行引用，否则会导致路径失效，导致自动检测文件是否更新失效
        // sql/bus
        String separatorStr = "/"; // File.separator
        String[] sqlRoutePaths = sqlPath.split("/");
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().
                    // 当前项目所有jar包的资源文件！！！
                            getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + sqlPath + "*/**/*."+ sqlFileExtension);
            int idx = 0;
            String filePath = null;
            for(Resource resource : resources){
//                System.out.println(resource.getURI());
                filePath = resource.getURI().toString();
                idx = filePath.indexOf(sqlRoutePaths[0]); // 首目录名所在位置匹配
                sqlPath = resource.getURI().toString().substring(idx,filePath.length()); // 截获实际地址信息即可
//                sqlPath = separatorStr + sqlRoutePaths[0] + getSqlFilePath(resource.getFile(),sqlRoutePaths[sqlRoutePaths.length-1],separatorStr,separatorStr + resource.getFile().getName());

                //System.out.println("loadSql -> "+sqlPath);
                // 加载所有的sql文件，自动装载，不需要每次写一个sql文件，丢到配置文件里，然后sql文件的方法命名，建议直接就是 nameSpace.key
                sqlPaths.add(sqlPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取sql文件所属路径 --> jar 包内部用该方式无法获取到文件的上级路径和相关文件，所以调整成直接截获实际文件地址即可
     * @param f -- 当前文件
     * @param rootFileName -- 找到指定路径
     * @param separatorStr -- 目录分隔符
     * @param fileName -- 当前文件的层级路径（逐渐补全）
     * @return
     */
    public static String getSqlFilePath(File f, String rootFileName, String separatorStr, String fileName){
        if(f.getParentFile().getName().equals(rootFileName)){
            return separatorStr + f.getParentFile().getName() + fileName;
        }else{
            return getSqlFilePath(f.getParentFile(),rootFileName,separatorStr,separatorStr + f.getParentFile().getName() + fileName);
        }
    }
}
