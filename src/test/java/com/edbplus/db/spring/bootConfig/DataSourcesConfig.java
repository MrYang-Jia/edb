package com.edbplus.db.spring.bootConfig;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.EDbProFactory;
import com.edbplus.db.SpringConfig;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.template.source.ClassPathSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.*;

/**
 * @ClassName DataSourcesConfig
 * @Description: 加载 EDbPro 数据库初始化实体示例  -- 初始化方式配置 1
 * @Author 杨志佳
 * @Date 2020/10/16
 * @Version V1.0
 **/
// 需要的情况下再放开注解
//@EnableAutoConfiguration
//@Configuration
@Slf4j
public class DataSourcesConfig {

    /**
     * 通过加装配置，自动装载 dataSource
     * 默认名称以实体对象驼峰名称命名 DataSource ==> dataSource
     * @return
     */
    @Bean
    // 这个是参考阿里云构造器初始化项目上的配置案例
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource getDataSources() {
        DataSource dataSource = new DruidDataSource();
        List<Filter> filterList = new ArrayList<>();
//        DruidSqlLogFilter druidSqlLogFilter = new DruidSqlLogFilter();
//        // 设置日志监听
//        filterList.add(druidSqlLogFilter);
//        // 添加过滤
//        DruidSqlUtil.addFilterList(filterList, (DruidDataSource) dataSource);
        System.out.println("===数据库加载中===");
        return dataSource;
    }

// ================  web容器下解放注释后可自定义配置 druid 的扩展功能 =====================

//
//    /**
//     * 配置druid后台管理的servlet
//     *
//     * @return
//     */
//    @Bean
//    public ServletRegistrationBean druidServlet() {
//        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/edb-druid/*");
//        Map<String, String> initParams = new HashMap<>();
//        initParams.put("loginUsername", "edbAdmin");
//        initParams.put("loginPassword", "edbAdmin");
//        //默认就是允许所有访问
//        initParams.put("allow", "");
//        //禁止访问的ip
////        initParams.put("deny", "11.11.11.11");
//        bean.setInitParameters(initParams);
//        return bean;
//    }
//
//    /**
//     * 配置druid后台管理的filter
//     *
//     * @return
//     */
//    @Bean
//    public FilterRegistrationBean druidFilter() {
//        FilterRegistrationBean bean = new FilterRegistrationBean();
//        bean.setFilter(new WebStatFilter());
//        Map<String, String> initParams = new HashMap<>();
//        //设置不拦截的路径  *.cs *.js    /druid/*
//        initParams.put("exclusions","*.js,*.css,/druid/*");
//        bean.setInitParameters(initParams);
//        //设置filter拦截 那些请求
//        bean.setUrlPatterns(Arrays.asList("/*"));
//        return bean;
//    }


// ================  web容器下解放注释后可自定义配置 druid 的扩展功能 -- END =====================

    /**
     * 生成数据源1 -- 主数据源
     * @return
     */
    @Bean(name = "eDbPro")
    public EDbPro getEDbPro(){
        // 调用本类的bean方法，会自动经过bean的拦截实现，获取到实例化后的bean实体
        DataSource dataSource = getDataSources();
        // 适配spring数据库连接池 -- 适配事务
        SpringConfig activerecordConfig = new SpringConfig(
                // 默认名称 ，使用 Db.use() 时，可获取到
                DbKit.MAIN_CONFIG_NAME
                // 这里可以替换成 spring体系的datasource
                ,dataSource
                // 事务级别 ，如果是spring时，可使用spring的事务级别替代，这个是属于数据库事务级别定义的，都一样
                , DbKit.DEFAULT_TRANSACTION_LEVEL
        );
        // 初始化数据源
        ActiveRecordPlugin arp = new ActiveRecordPlugin(activerecordConfig);
        // 定义db实现工厂 继承了父类实现方法
        EDbProFactory eDbProFactory = new EDbProFactory();
        // 设置 edbpro 工厂
        arp.setDbProFactory(eDbProFactory);
        // 定义enjoy视图工厂目录
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.setDevMode(false);
        arp.setShowSql(true);
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
        arp.setShowSql(true);
        // 启动Record容器
        arp.start();
        // 重点：需要初始化一次配置信息，便于 EDb 继承 Db 的config信息
        EDb.init();
        log.debug("===EDb启动成功===");
        return EDb.use();
    }

    /**
     * 生成数据源2 -- 这个只是示例
     * @return
     */
    // 假设有第二个数据源
    @Bean(name = "xzwEDbPro")
    public EDbPro getXzwEDbPro(){
        DataSource dataSource = getDataSources();
        // 适配spring数据库连接池 -- 适配事务
        SpringConfig activerecordConfig = new SpringConfig(
                // 默认名称 ，使用 Db.use() 时，可获取到
                "xzw"
                // 这里可以替换成 spring体系的datasource
                ,dataSource
                // 事务级别 ，如果是spring时，可使用spring的事务级别替代，这个是属于数据库事务级别定义的，都一样
                , DbKit.DEFAULT_TRANSACTION_LEVEL
        );
        // 初始化数据源
        ActiveRecordPlugin arp = new ActiveRecordPlugin(activerecordConfig);
        // 定义db实现工厂 继承了父类实现方法
        EDbProFactory eDbProFactory = new EDbProFactory();
        // 设置 edbpro 工厂
        arp.setDbProFactory(eDbProFactory);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.setDevMode(false);
        arp.setShowSql(true);
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
        // 打印sql
        arp.setShowSql(true);
        // 启动Record容器
        arp.start();
        log.debug("=== xzwEDbPro启动成功===");
        return EDb.use("xzw");
    }

}
