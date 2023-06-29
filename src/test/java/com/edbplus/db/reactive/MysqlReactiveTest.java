///**
// * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.edbplus.db.reactive;
//
//
//import cn.hutool.core.thread.ThreadUtil;
//import io.smallrye.mutiny.Multi;
//import io.vertx.mutiny.core.Vertx;
//import io.vertx.mutiny.mysqlclient.MySQLPool;
//import io.vertx.mysqlclient.MySQLConnectOptions;
//
//import io.vertx.sqlclient.PoolOptions;
//import org.testng.annotations.Test;
//
///**
// * @ClassName MysqlReactiveTest
// * @Description: mysql 响应式 测试
// * @Author 杨志佳
// * @Date 2023/6/28
// * @Version V1.0
// **/
//public class MysqlReactiveTest {
//    Vertx vertx = Vertx.vertx();
//
////    String connectionUri = "mysql://root:dev-whbj@WHBJ@192.168.1.106:13306/test_log";
//
//
//    @Test
//    public void test(){
//        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
//                .setPort(13306)
//                .setHost("192.168.1.106")
//                .setDatabase("test_log")
//                .setUser("root")
//                .setPassword("dev-whbj@WHBJ")
////                .setCharset("utf8")
////                .setReconnectAttempts(2)
////                .setReconnectInterval(1000)
//                ;
//
//        // Pool options
//        PoolOptions poolOptions = new PoolOptions()
//                .setMaxSize(5);
//
//        // Create the client pool
//        MySQLPool client = MySQLPool.pool(connectOptions, poolOptions);
//
//        client.query("SELECT * FROM t_t1 limit 1").execute().onItem().transform(rows -> {
//                    System.out.println("this result->"+rows.size());
//                    return Multi.createFrom().iterable(rows);
//                }
//        ).await().indefinitely(); // 单个对象的订阅
//
//        client.query("SELECT * FROM t_t1 limit 10").execute().onItem().transformToMulti(set ->{
//            System.out.println("next rusult->");
//            return Multi.createFrom().iterable(set);
//        } ).onItem().transform(row->{
//            System.out.println(row.getLong("id"));
//            return row;
//        }).onItem().transform(row -> {
//            System.out.println("id->"+row.getLong("id"));
//            return row;
//        }).subscribe().asStream(); // 多个合并集的订阅
//
//
//
//
//        ThreadUtil.sleep(1000L);
//    }
//
//}
