
package com.edbplus.db.jdbc;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.druid.filter.Filter;
import com.edbplus.db.EDb;
import com.edbplus.db.druid.filter.EDbDruidSqlLogFilter;
import com.edbplus.db.generator.jdbc.GenJdbc;
import com.edbplus.db.jdbc.domain.KnownFruits;
import com.edbplus.db.jdbc.domain.MoneyCount;
import com.edbplus.db.jfinal.activerecord.db.base.JpaListener;
import com.edbplus.db.jpa.VehicleType;
import com.edbplus.db.listener.impl.SqlListener;
import com.edbplus.db.util.hutool.http.EHttpUtil;
import com.edbplus.db.util.log.EDbLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName TransactionTest
 * @Description: 事务测试
 * @Author 杨志佳
 * @Date 2022/7/9
 * @Version V1.0
 **/
@Slf4j
public class TransactionTest {

    String jdbcUrl = "jdbc:mysql://192.168.1.106:13306/tra_log?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useCompression=true&useAffectedRows=true&maxRows=500&TimeZone=Asia/Shanghai0";
    String userName = "root";
    String pwd = "dev-whbj@WHBJ";
    int transactionLevel =  2; // 事务等级 0，1，2，4，8 ，默认用 4-RR模式，批量锁， 2是 RC 模式，锁单行

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

//    @BeforeTest
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
        GenJdbc.initForEnjoy(transactionLevel,null,jdbcUrl,userName,pwd,sqlTplList,shareSqlTplList,filterList);

        filterList = new ArrayList<>();
        eDbDruidSqlLogFilter = new EDbDruidSqlLogFilter();
        eDbDruidSqlLogFilter.setDbType(2); //pg类型的解析,但是 druid 对于日新月异的 druid sql支持，还是有点弱，例如特殊符号强转则无法格式化
        // 添加sql日志打印信息
        filterList.add(eDbDruidSqlLogFilter);
        JpaListener jpaListener = new JpaListener();
        // 初始化
        EDb.use().setEDbListener(jpaListener);
        EDb.use().setConnectListener(sqlListener);
    }

    /**
     * 测试并行插入时，并行插入同一行数据时，不同事务模式下的表现情况
     *
     */
    @Test
    public void test1(){
        // RC TRANSACTION_READ_COMMITTED 模式下，会优先认可首次修改的记录，第二次执行的行为则抛弃，有顺序执行的预判，不会因为提交顺序有所影响，而是执行时间的先后有影响 -- 这种模式下，流程 2 异常，同时另一个事务里，无法通过 find 查找到其他事务的数据 -- 最不容易缩表
        // RR TRANSACTION_REPEATABLE_READ 模式下，优先认可谁先提交，谁先提交则成功，晚提交的则异常 -- 这种模式下 流程 1 异常 ，同时另一个事务里，无法通过 find 查找到其他事务的数据 -- 可能会锁表
        // S TRANSACTION_SERIALIZABLE 模式下，会获取到另外一个线程里正则执行的数据，但是数据的写入稳定性最好，可预判性最佳，目前用于单机模式的客户最佳，写代码预判最佳

        transactionLevel =  Connection.TRANSACTION_SERIALIZABLE; // 2-RC模式,4-RR模式,8-S模式（性能最差，最容易锁表）
        init();
        KnownFruits knownFruits=new KnownFruits();
        knownFruits.setId(4);
        knownFruits.setName("测试-"+ RandomUtil.randomInt());
        // 第一个执行为插入数据
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                EDb.tx(transactionLevel, () -> {
                    EDb.save(knownFruits);
                    ThreadUtil.sleep(1000);
                    return true;// 写入
                });
                }catch (Throwable e){
                    log.error("环节1异常",e);
                }
            }
        });

        // 第2个执行为插入 同样的数据 数据
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EDb.tx(transactionLevel, () -> {
                        ThreadUtil.sleep(100); // 故意慢个100毫秒，目的是检测哪个环节报错了
                        System.out.println("流程2校验是否存在->"+(EDb.findById(KnownFruits.class,4)!=null));
                        EDb.save(knownFruits);
                        return true;// 写入
                    });
                }catch (Throwable e){
                    log.error("环节2异常",e);
                }

            }
        });

        ThreadUtil.sleep(3000L);
        EDb.deleteById(knownFruits);// 最后都删除掉该数据，便于循环测试
    }

    /**
     * 更新金额测试
     */
    @Test
    public void test2(){
        // 执行结果是 三种事务模式下，都是一样的结果情况，但是注意的是sql的预判动作，都是交予了数据库去执行判断，并且前期是数据库里已存在该数据，该测试情况与数据库是否已存在该记录的情况是不一样的
        // s TRANSACTION_SERIALIZABLE 模式下， 执行成功，结果与实际相符，但是操作项必须由数据库执行，然后执行顺序则是 谁优先则信任谁，与事务提交顺序无关
        // RC TRANSACTION_READ_COMMITTED 模式下， 执行成功，结果与实际相符，但是操作项必须由数据库执行，然后执行顺序则是 谁优先则信任谁，与事务提交顺序无关
        // RR TRANSACTION_REPEATABLE_READ 模式下, 执行成功，结果与实际相符，但是操作项必须由数据库执行，然后执行顺序则是 谁优先则信任谁，与事务提交顺序无关
        transactionLevel =  Connection.TRANSACTION_SERIALIZABLE; // 2-RC模式,4-RR模式,8-S模式（性能最差，最容易锁表）
        // 初始化数据库
        init();

        MoneyCount moneyCount = new MoneyCount();
        moneyCount.setId(1);
        moneyCount.setZje(100); // 初始金额 100
        EDb.save(moneyCount);

        // 第一个执行为插入数据
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EDb.tx(transactionLevel, () -> {
                        int czs = EDb.update("update money_count set zje=zje-60 where zje >= zje-60 and (zje-60) >0");
                        if(czs>0){
                            System.out.println("流程1操作正常");
                        }
                        ThreadUtil.sleep(1000);
                        return true;// 写入
                    });
                }catch (Throwable e){
                    log.error("环节1异常",e);
                }
            }
        });

        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EDb.tx(transactionLevel, () -> {
                        ThreadUtil.sleep(100);
                        int czs = EDb.update("update money_count set zje=zje-60 where zje >= zje-60 and (zje-60) >0");
                        if(czs>0){
                            System.out.println("流程2操作正常");
                        }else{
                            System.out.println("流程2操无执行操作");
                        }
                        return true;// 写入
                    });
                }catch (Throwable e){
                    log.error("环节2异常",e);
                }
            }
        });

        ThreadUtil.sleep(3000L);

        EDb.deleteById(moneyCount);
    }

}
