package com.edbplus.db.jfinal.activerecord.db.base;

import com.alibaba.druid.filter.Filter;
import com.edbplus.db.EDb;
import com.edbplus.db.druid.filter.EDbDruidSqlLogFilter;
import com.edbplus.db.generator.jdbc.GenJdbc;
import com.edbplus.db.jpa.VehicleType;
import com.edbplus.db.listener.impl.SqlListener;
import com.edbplus.db.util.hutool.json.EJSONUtil;
import com.edbplus.db.util.log.EDbLogUtil;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName BaseTest
 * @Description: 基础测试准备
 * @Author 杨志佳
 * @Date 2020/10/18
 * @Version V1.0
 **/
public class BaseTest {

    // useAffectedRows=true时，update语句执行多次时，只有修改成功时才会返回1，若记录的值没有变化，返回0.
    String jdbcUrl = "jdbc:mariadb://192.168.1.106:13306/test_log?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useCompression=true&useAffectedRows=true&maxRows=500&TimeZone=Asia/Shanghai";
    String userName = "root";
    String pwd = "dev-whbj@WHBJ";
    // 测试库 stringtype=unspecified -> 允许pg可以输入字符串类型的时间参数
    // https://jdbc.postgresql.org/documentation/head/connect.html#connection-parameters
    String jdbcUrl2 = "jdbc:postgresql://192.168.1.208:15432/tra_log?stringtype=unspecified&currentSchema=public&reWriteBatchedInserts=true&useUnicode=true&characterEncoding=utf8&defaultFetchSize=2"; // maxResultBuffer=100M 最大内存会溢出
    // 账号
    String userName2 = "postgres";
    String pwd2 = "whbj123456";


    @BeforeTest
    public void init(){
        SqlListener sqlListener = new SqlListener();
        List<String> sqlTplList = new ArrayList<>();
        // edb 通用模板sql 加载
//        sqlTplList.add("/edb/sql/all.sql");
        // todo: 基于项目的sql模板，你直接添加在这里，就能快速的进行测试
        sqlTplList.add("/sql/all.sql");
        // 共享模板配置
        List<String> shareSqlTplList = new ArrayList<>();
        // 添加共享sql模板
//        shareSqlTplList.add("/sql/sharedfunction/common_function.sql");

        EDbLogUtil.loadResoucesForSlf4j("logback-edb.xml");

        EDbDruidSqlLogFilter eDbDruidSqlLogFilter = new EDbDruidSqlLogFilter();
        // 只打印真实日志 -- 因为有入参，建议还是开启
        //eDbDruidSqlLogFilter.setOnlyRealsql(true);
        List<Filter> filterList = new ArrayList<>();
        // 添加sql日志打印信息
        filterList.add(eDbDruidSqlLogFilter);

        // 初始化
        GenJdbc.initForEnjoy(null,jdbcUrl,userName,pwd,sqlTplList,shareSqlTplList,filterList);

        filterList = new ArrayList<>();
        eDbDruidSqlLogFilter = new EDbDruidSqlLogFilter();
        eDbDruidSqlLogFilter.setDbType(2); //pg类型的解析,但是 druid 对于日新月异的 druid sql支持，还是有点弱，例如特殊符号强转则无法格式化
        // 添加sql日志打印信息
        filterList.add(eDbDruidSqlLogFilter);
//        GenJdbc.initForEnjoy("pg",jdbcUrl2,userName2,pwd2,sqlTplList,shareSqlTplList,filterList);
//        JpaListener jpaListener = new JpaListener();
//        // 初始化
//        EDb.use().setEDbListener(jpaListener);
//        EDb.use().setConnectListener(sqlListener);
//        // 一个数据库只能设定一个监听 ，所以要绑定监听的数据库对象
//        EDb.use("pg").setEDbListener(jpaListener);
//        EDb.use("pg").setConnectListener(sqlListener);
    }


    // 设置一定数量的并发线程池 ，本机测试建议是 2倍的cpu核心数
    // 静态化，这样子线程池就可以复用，避免额外开销
    public static ExecutorService fixedThreadPool =
//            new ThreadPoolExecutor(
//            // 使用1个cpu的资源,最大无上限 -- 这种情况是不管来多少服务，我都受理
//            1, Integer.MAX_VALUE,
//            // 线程空闲超过60秒会自动销毁
//            1L, TimeUnit.SECONDS,
//            new SynchronousQueue<Runnable>());
            // 线程池配置
//            new ThreadPoolExecutor(4, 4,
//                    1L, TimeUnit.SECONDS,
//                    new LinkedBlockingQueue<Runnable>());

            Executors.newFixedThreadPool(50);

    private final AtomicInteger atomicI = new AtomicInteger(0);

    /**
     * 正常情况注释掉
     */
//    @Test
    public void testSource(){
        String sql = "select count(1) from tra_goods_source where release_time>='2021-04-14 00:00:00'";
        for(int i=0;i<50;i++){
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    System.out.println(EDb.use("pg").findFirst(sql));
                    System.out.println("耗时："+ ( System.currentTimeMillis() -start ));
                    System.out.println("累计:"+atomicI.getAndIncrement());
                }
            });
        }

        try {
            Thread.sleep(10000000*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    //        @Test
    public void testCk(){
        EDb.use("pg").find(" select gsid,CREATETIME from tra_goods_source where gsid in('45259508','45259504') ");
    }


    /**
     * 切换数据源的测试
     */
//    @Test
    public void testUseDb(){
        int countSize = 1000;

        // 并发切换数据源
        for(int i=0;i<countSize;i++){
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    // 只打印1次初始化信息，避免并发时互相影响
//                    EDb.use("pg").find(" select * from app_activity limit 1 ");
                    doSql();
                }
            });
        }

//        for(int i=0;i<countSize;i++){
//            fixedThreadPool.execute(new Runnable() {
//                @Override
//                public void run() {
//                    // 只打印1次初始化信息，避免并发时互相影响
//                    EDb.use().find(" select * from cr_vehicle_type limit 1 ");
//                }
//            });
//        }

        try {
            Thread.sleep(20*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doSql(){

        final VehicleType vehicleType =   new VehicleType();
        // 异步执行
        EDb.use().txInNewThread(Connection.TRANSACTION_SERIALIZABLE, () -> {
            // 数据对象
            vehicleType.setVehicleTypeName("原:小汽车");
            vehicleType.setCreatorName("小陈陈");
            // 如果有多个数据库，可以用 EDb.use("数据库标识1") 指定
            EDb.save(vehicleType);
            System.out.println("对象1:"+ EJSONUtil.toJsonStr(EDb.findById(VehicleType.class,vehicleType.getVehicleTypeId())));
            return false;
        });
        //
        System.out.println("对象2:"+EJSONUtil.toJsonStr(EDb.findById(VehicleType.class,vehicleType.getVehicleTypeId())));

        EDb.use("pg").txInNewThread(Connection.TRANSACTION_SERIALIZABLE, () -> {
            EDb.use("pg").find(" select * from app_activity limit 1 ");
            return false;
        });


    }


    //@Test
    public void test2(){
        String sql  = "INSERT INTO `app_activity`(`id`, `name`, `image_id`, `click_like`, `display_number`, `click_number`, `type`, `whether_enabled`, `enabled`, `createTime`, `createBy`, `updateTime`, `updateBy`) VALUES ('022b71806ece11e82ba61db1136fe9e3', '啊', 'fa6fc4a06ecd11e82ba61db1136fe9e3', 'www.sina.com.cn', 0, 0, 3, 1, 0, '2018-06-13 13:52:51', 'admin', '2018-06-13 13:52:51', 'admin');\n";
        Future<Boolean> future = EDb.use("pg").txInNewThread(Connection.TRANSACTION_SERIALIZABLE, () -> {
            EDb.use("pg").update(sql);
            EDb.update(sql);
            return false;
        });

//        EDb.use("pg").update(sql);
//        EDb.use("main").update(sql);

        try {
            future.get();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
