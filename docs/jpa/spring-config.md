
# springBoot 项目引入示例

### SpringBoot 配置指南
- 方式一：初始化 DataSource 对象时，初始化 EDbPro对象 [ DataSource 与 EDbPro 配置实例化参考 ](../../src/test/java/com/edb/cloud/spring/bootConfig/DataSourcesConfig.java)
- 方式二：启动项目后需要引入 EDbPro 初始化对象 [ EDbPro 配置实例化参考 ](../../src/test/java/com/edb/cloud/spring/bootConfig/EDbConfig.java)


**基于Java的相关用例**

- 方式一案例 [ DataSource 与 EDbPro 配置实例化参考 ](../../src/test/java/com/edb/cloud/spring/bootConfig/DataSourcesConfig.java)

```properties

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# \u6570\u636E\u6E90\u540D\u79F0
spring.datasource.name=defaultDataSource
# \u6570\u636E\u5E93\u8FDE\u63A5\u5730\u5740
spring.datasource.url=jdbc:mysql://192.168.1.106:13306/tra_log?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useCompression=true
# \u6570\u636E\u5E93\u7528\u6237\u540D&\u5BC6\u7801\uFF1A
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource


```

```java

package com.edb.cloud.spring.bootConfig;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.EDbPro;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.template.source.ClassPathSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.*;

/**
 * @ClassName DataSourcesConfig
 * @Description: 加载 EDbPro 数据库初始化实体示例  -- 初始化方式配置 1
 * @Author 杨志佳
 * @Date 2020/10/16
 * @Version V1.0
 **/
// 自动装载
@EnableAutoConfiguration
@Configuration
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
        return dataSource;
    }



// ================  web容器下解放注释后可自定义配置 druid 的扩展功能 -- END =====================

    /**
     * 生成数据源1 -- 主数据源
     * @return
     */
    @Bean(name = "eDbPro")
    public EDbPro getEDbPro(){
        // 调用本类的bean方法，会自动经过bean的拦截实现，获取到实例化后的bean实体
        DataSource dataSource = getDataSources();
        // 初始化数据源
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dataSource);
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.setDevMode(false);
        arp.setShowSql(true);
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
        arp.setShowSql(true);
        // 启动Record容器
        arp.start();

        log.debug("===EDb启动成功===");
        return EDb.use();
    }


}



```

```sql
 :: Dubbo Spring Boot (v2.7.7) : https://github.com/apache/dubbo-spring-boot-project
 :: Dubbo (v2.7.7) : https://github.com/apache/dubbo
 :: Discuss group : dev@dubbo.apache.org
 ...
   .   ____          _            __ _ _
  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
 ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
   '  |____| .__|_| |_|_| |_\__, | / / / /
  =========|_|==============|___/=/_/_/_/
  :: Spring Boot ::        (v2.3.0.RELEASE)
2020-10-27 22:17:48.231 [,] [main] DEBUG c.w.c.b.c.EDbConfig - [getEDbPro,59] - ===EDb启动成功===
....
2020-10-27 22:17:52.542 [,] [main] INFO  c.w.c.b.BaseApplication - [logStarted,61] - Started BaseApplication in 4.617 seconds (JVM running for 5.779)

```


- 方式二案例 [ EDbPro 配置实例化参考 ](../../src/test/java/com/edb/cloud/spring/bootConfig/EDbConfig.java)

```java
package com.edb.cloud.spring.bootConfig;

import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.EDbPro;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.template.source.ClassPathSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        arp.setShowSql(true);
        arp.getEngine().addSharedMethod(new com.jfinal.kit.StrKit());
        // 启动Record容器
        arp.start();
        System.out.println("=========  EDb启动成功  ============");

        log.debug("===EDb启动成功===");
        return EDb.use();
    }

}


```



```sql
 :: Dubbo Spring Boot (v2.7.7) : https://github.com/apache/dubbo-spring-boot-project
 :: Dubbo (v2.7.7) : https://github.com/apache/dubbo
 :: Discuss group : dev@dubbo.apache.org
 ...
   .   ____          _            __ _ _
  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
 ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
   '  |____| .__|_| |_|_| |_\__, | / / / /
  =========|_|==============|___/=/_/_/_/
  :: Spring Boot ::        (v2.3.0.RELEASE)
=========  EDb启动成功  ============
2020-10-27 22:57:48.538 [,] [main] DEBUG c.w.c.b.c.EDbConfig - [getEDbPro,59] - ===EDb启动成功===
....
2020-10-27 22:57:50.542 [,] [main] INFO  c.w.c.b.BaseApplication - [logStarted,61] - Started BaseApplication in 4.822 seconds (JVM running for 5.949)

```
