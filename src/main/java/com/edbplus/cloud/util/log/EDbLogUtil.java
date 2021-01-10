package com.edbplus.cloud.util.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

/**
 * @ClassName EDbLogUtil
 * @Description: 日志工具类
 * @Author 杨志佳
 * @Date 2020/10/22
 * @Version V1.0
 **/
@Slf4j
public class EDbLogUtil {

    /**
     * log4J日志配置加载初始化
     * 基础路径默认: src/main/resources/
     */
    public static void loadResoucesForSlf4j(String xmlUrl){
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure("src/main/resources/"+xmlUrl);
        } catch (JoranException e) {
            e.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
        log.debug(" sjf4j 日志装载成功 ");
    }

}
