package com.edb.cloud.jfinal.activerecord.db.generator.jdbc;

import cn.hutool.core.map.CaseInsensitiveMap;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.edb.cloud.jfinal.activerecord.db.jpa.kit.JpaKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * jdbc初始化工具类-基于jfinal
 *
 * @author MrYang
 * @date 2019-02-26
 */
public class GenJdbc {

    // 数据对象
    public static DruidPlugin dp;
    // 插件
    public static ActiveRecordPlugin arp ;
    // 缩略前缀
    public static String tablePreRemove = "";
    // 创建人
    public static String creater = "mysql脚手架";
    // 最小空闲连接数
    public static  int minIdle = 10;
    // 最大连接数
    public static  int maxActive = 32;

    // 使用大小写不敏感的对象封装
    public static Map<String,String> filedTypeMap =  new CaseInsensitiveMap();

    // 类对象引用时初始化
    static {
        // 数据库数据类型初始化
        initFiledType();
    }

    /**
     * 数据库数据类型初始化
     * key大小写不敏感，所以可以直接拿来用
     */
    public static void initFiledType(){
        // 左侧是 mysql 数据库类型，右侧是 java 对象属性类型
        filedTypeMap.put("int","Integer");
        filedTypeMap.put("tinyint","Integer");
        filedTypeMap.put("BIGINT","Long");
        filedTypeMap.put("VARCHAR","String");
        filedTypeMap.put("CHAR","String");
        filedTypeMap.put("TEXT","String");
        filedTypeMap.put("longtext","String");
        filedTypeMap.put("FLOAT","BigDecimal");
        filedTypeMap.put("DOUBLE","BigDecimal");
        filedTypeMap.put("Decimal","BigDecimal");
        filedTypeMap.put("TIMESTAMP","Date");
        filedTypeMap.put("datetime","Date");
        filedTypeMap.put("DATE","Date");
        filedTypeMap.put("mediumtext","String");
        filedTypeMap.put("bit","Boolean");
        filedTypeMap.put("boolean","Boolean");

        // 兼容pg的相关数据类型 -- 有必须再添加
        filedTypeMap.put("numeric","BigDecimal");
        filedTypeMap.put("bpchar","String");
        filedTypeMap.put("int2","Integer");
        filedTypeMap.put("int4","Integer");
        filedTypeMap.put("int8","Long");
        filedTypeMap.put("float4","BigDecimal");
        filedTypeMap.put("float8","BigDecimal");



    }

    /**
     * 数据库初始化
     * @param configName - 指定数据库别名
     * @param jdbcUrl -- 数据库连接地址
     * @param userName -- 数据库名称
     * @param pwd -- 数据库密码
     */
    public static void init(String configName,String jdbcUrl,String userName,String pwd){
        initForEnjoy(configName,jdbcUrl,userName,pwd,null,null);
    }

    /**
     * 数据初始化
     * @param jdbcUrl
     * @param userName
     * @param pwd
     */
    public static void init(String jdbcUrl,String userName,String pwd){
        initForEnjoy(jdbcUrl,userName,pwd,null,null);
    }


    /**
     * 初始化基于enjoy的数据库连接
     * @param configName
     * @param jdbcUrl
     * @param userName
     * @param pwd
     * @param sqlTplList
     * @param shareSqlTplList
     */
    public static void initForEnjoy(String configName,String jdbcUrl,String userName,String pwd,List<String> sqlTplList,List<String> shareSqlTplList){
        initForEnjoy(configName,jdbcUrl,userName,pwd,sqlTplList,shareSqlTplList,null);
    }

    /**
     * 初始化基于enjoy的数据库连接
     * @param configName
     * @param jdbcUrl
     * @param userName
     * @param pwd
     * @param sqlTplList
     * @param shareSqlTplList
     * @param filterList
     */
    public static void initForEnjoy(String configName,String jdbcUrl,String userName,String pwd,List<String> sqlTplList,List<String> shareSqlTplList,List<Filter> filterList){
        initForEnjoy(configName,jdbcUrl,userName,pwd,sqlTplList,shareSqlTplList,filterList,null);
    }


    /**
     * 初始化基于enjoy的数据库连接
     * @param configName
     * @param jdbcUrl
     * @param userName
     * @param pwd
     * @param sqlTplList
     * @param shareSqlTplList
     * @param filterList
     * @param driverClass
     */
    public static void initForEnjoy(String configName,String jdbcUrl,String userName,String pwd,List<String> sqlTplList,List<String> shareSqlTplList,List<Filter> filterList,String driverClass){


        if(driverClass!=null){
            dp = new DruidPlugin(jdbcUrl, userName, pwd,driverClass);
        }else {
            dp = new DruidPlugin(jdbcUrl, userName, pwd);
        }

        // 最小空闲连接数
        dp.setMinIdle(minIdle);
        // 最大活动连接数
        dp.setMaxActive(maxActive);

        if(configName==null){
            arp = new ActiveRecordPlugin(dp);
        }else{
            arp = new ActiveRecordPlugin(configName,dp);
        }

        // 定于 pg 的解析器
        if(jdbcUrl.contains("postgresql")){
            arp.setDialect(new PostgreSqlDialect());
        }

        // 打印sql
        arp.setShowSql(false);
        // 打印sql -- 交予底层统一打印
//        GenJdbc.arp.setShowSql(false);
        // 判断是否存在需要添加的sql模板
        if(sqlTplList!=null){
            // 设置模板的读取环境 从class的资源目录底下获取
            arp.getEngine().setToClassPathSourceFactory();
            for(String sqlTpl:sqlTplList){
                arp.addSqlTemplate(sqlTpl);
            }
        }

        // 判断共享模板是否存在
        if(shareSqlTplList!=null){
            for(String sqlTpl:shareSqlTplList){
                // 添加共享模板
                arp.getEngine().addSharedFunction(sqlTpl);
            }
        }
        // 获取 Enjoy 模板
        Engine engine = arp.getEngine();
        // 启动对 空格 的压缩，例如sql的输出就会更精简
        engine.setCompressorOn(' ');

        engine.addSharedObject("JpaKit", new JpaKit());

        // 支持模板热加载，绝大多数生产环境下也建议配置成 true，除非是极端高性能的场景
//        engine.setDevMode(true);

        // 提高性能 ，但首次加载的时候会比较慢，并发时能提高性能，应该是做了缓存
        //engine.setFastMode(true);

        dp.start();
        arp.start();

        // 添加druid过滤器，需要重启
        addFilterList(filterList, (DruidDataSource) dp.getDataSource());
        try {
            ((DruidDataSource) dp.getDataSource()).restart();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 基于sql文件进行初始化
     * @param jdbcUrl
     * @param userName
     * @param shareSqlTplList
     * @param pwd
     */
    public static void initForEnjoy(String jdbcUrl,String userName,String pwd,List<String> sqlTplList,List<String> shareSqlTplList){
        initForEnjoy(null,jdbcUrl,userName,pwd,sqlTplList,shareSqlTplList);
    }


    /**
     * 基于sql文件进行初始化
     * @param jdbcUrl
     * @param userName
     * @param pwd
     * @param sqlTplList
     */
    public static void initForEnjoy(String jdbcUrl,String userName,String pwd,List<String> sqlTplList){
        initForEnjoy(jdbcUrl,userName,pwd,sqlTplList,null);
    }


    /**
     * 添加druid 过滤器
     * @param filterList
     * @param ds
     */
    public static void addFilterList(List<Filter> filterList, DruidDataSource ds) {
        if (filterList != null) {
            List<Filter> targetList = ds.getProxyFilters();
            Iterator var3 = filterList.iterator();

            while(var3.hasNext()) {
                Filter add = (Filter)var3.next();
                boolean found = false;
                Iterator var6 = targetList.iterator();

                while(var6.hasNext()) {
                    Filter target = (Filter)var6.next();
                    if (add.getClass().equals(target.getClass())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    targetList.add(add);
                }
            }
        }

    }


}
