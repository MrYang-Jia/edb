package com.edbplus.thread;

import cn.hutool.core.date.DateUtil;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {


    @Test
    public void test1(){
        System.out.println(DateUtil.format(new Date(),"mm"));
    }

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


    // 定义对象锁
    String key = "lock";

    /**
     * 并发测试案例1
     */
    @Test
    public void test(){
        int countSize = 40;
        // 并发切换数据源
        for(int i=0;i<countSize;i++){
            int finalI = i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String key2 = key;
                    synchronized (key2){
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 打印数字
                        System.out.println(finalI);

                    }
                }
            });
        }

        try {
            Thread.sleep(10*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 并发测试案例2
     */
    @Test
    public void test2(){
        int countSize = 40;
        // 并发切换数据源
        for(int i=0;i<countSize;i++){
            int finalI = i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
//                    printOut(finalI);
//                    TaskDo taskDo =new TaskDo();
//                    taskDo.printOut(finalI);
                    TaskDo.printOut(finalI);
                }
            });
        }
        try {
            Thread.sleep(60*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public synchronized void printOut(int finalI){
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 打印数字
        System.out.println(finalI);
    }




}
