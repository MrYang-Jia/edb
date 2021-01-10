package com.edbplus.db.spring.bootConfig;

import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.template.source.ClassPathSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @ClassName EDbConfig
 * @Description: edb配置文件 -- 初始化配置方式 - 2
 * @Author 杨志佳
 * @Date 2020/10/23
 * @Version V1.0
 **/
@Slf4j
// 打开注释即可
//@EnableAutoConfiguration
//@Configuration
public class EDbConfig {

    // // 等待db1Source启动后注入
    // // 如果注入的时候报说找不到数据源，则可能是启动顺序的问题导致，可以尝试这么引入   @Qualifier("dataSource")
    @Autowired
    DataSource dataSource;

    /**
     * 生成数据源1 -- 主数据源
     * @return
     */
    @Bean(name = "eDbPro")
    public EDbPro getEDbPro(){
        // 调用本类的bean方法，会自动经过bean的拦截实现，获取到实例化后的bean实体

        // 初始化数据源
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.setDevMode(true);
        // 打印sql -- 交予底层统一打印,建议设置成false，自己定义监听器，或者交予 druid 的sql打印信息即可
        arp.setShowSql(true);
        //基础数据模板
        // arp.addSqlTemplate("/sql/all.sql");
        //添加共享模板
//        arp.getEngine().addSharedFunction("/sql/sharedfunction/common_function.sql");
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
//        // 如果不是linux环境
//        if(!SystemUtil.getOsInfo().isLinux()){
//            // 开发者模式
//            arp.setDevMode(true);
//        }else{
//            // 非开发者模式 -- 生产用该方式
//            arp.setDevMode(false);
//        }

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
        // 初始化数据源
        ActiveRecordPlugin arp = new ActiveRecordPlugin("xzw",dataSource);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.setDevMode(true);
        arp.setShowSql(true);
        //基础数据模板
        // arp.addSqlTemplate("/sql/all.sql");
        //添加共享模板
//        arp.getEngine().addSharedFunction("/sql/sharedfunction/common_function.sql");
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
//        // 如果不是linux环境
//        if(!SystemUtil.getOsInfo().isLinux()){
//            // 开发者模式
//            arp.setDevMode(true);
//        }else{
//            // 非开发者模式 -- 生产用该方式
//            arp.setDevMode(false);
//        }
        // 启动Record容器
        arp.start();
        System.out.println("=========  xzwEDbPro启动成功  ============");

        log.debug("=== xzwEDbPro启动成功===");
        return EDb.use("xzw");
    }
}
