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
package com.edbplus.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName EDbThreadSet
 * @Description: edb线程设置对象，不太适合 quarkus，可能会导致可能性内存溢出，需要改造下，
 *               因为quarkus是 eventGroup 类型，线程创建后会自动消亡，如果执行前发生err事件，虽然概率低，但是会因此造成可能性的内存堆积
 * @Author 杨志佳
 * @Date 2024/2/23
 * @Version V1.0
 **/
public class EDbThreadSet {
    public static ConcurrentMap<Long,Integer> threadQueryTimeOutMap =  new ConcurrentHashMap();

    /**
     * 设置超时时间，单次执行查询时有效，下个语句执行前需要设置，否则会被移除
     * @param seconds
     */
    public static void setQueryTimeOut(int seconds){
        // 获取当前线程id
        long threadId = Thread.currentThread().getId();
        threadQueryTimeOutMap.putIfAbsent(threadId,seconds);// 直接赋予即可，没必要
    }

    /**
     * 获取超时时间设置并移除
     * @return
     */
    public static Integer setQueryTimeOut(Statement statement){
        long threadId = Thread.currentThread().getId();
        Integer queryTimeOut = threadQueryTimeOutMap.get(threadId);
        if(queryTimeOut!=null){
            threadQueryTimeOutMap.remove(threadId);
            try {
                statement.setQueryTimeout(queryTimeOut);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
//        else{
//            try {
//                statement.setQueryTimeout(0);
//            }catch (Throwable throwables){
//                throwables.printStackTrace();
//            }
//        }
        return queryTimeOut;
    }
}
