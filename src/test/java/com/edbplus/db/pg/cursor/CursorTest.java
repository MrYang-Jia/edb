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
package com.edbplus.db.pg.cursor;

import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.util.EDbResultSetUtil;
import com.jfinal.plugin.activerecord.ICallback;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.List;

/**
 * @ClassName CursorTest
 * @Description: 游标测试
 * @Author 杨志佳
 * @Date 2021/7/1
 * @Version V1.0
 **/
public class CursorTest extends BaseTest {

    // pg游标测试
    @Test
    public void pgCursorTest(){
        EDbPro pgDbPro =  EDb.use("pg");
        // 预执行sql
        String declareSql = " select * from cr_vehicle_type ";

        // pg模式下如果想fetchsize生效，只须设置autocommit为flase，也就是需要手工去管理事务
        pgDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            // 游标的测试方案，请在内部完成
            pgDbPro.execute(new ICallback() {
                @Override
                public Object call(Connection conn) throws SQLException {
                    System.out.println("事务状态:"+conn.getAutoCommit());
                    // 创建预编译对象
                    PreparedStatement preparedStatement = conn.prepareStatement(declareSql
                    );
                    // 预读数据，默认为0，提前设置可以提高next的运行效率，不用每次都等半天，调用一次next执行1次游标操作( 提前内存存储 N 条数据)
                    // 预读取 会提前消耗内存,pg则必须设置读取的对象大小
                    preparedStatement.setFetchSize(2000);
                    // 执行sql查询数据
                    ResultSet rs = preparedStatement.executeQuery();
                    // 循环获取下一条记录
                    while (rs.next()) {
                        // 返回map集合
                        System.out.println(EDbResultSetUtil.returnMap(rs));
                    }
                    // 返回结果集设置
                    return null;
                }
            });
            return false;
        });
    }

    // mysql游标测试
    @Test
    public void mysqlCursorTest(){
        EDbPro pgDbPro =  EDb.use();
        // 预执行sql
        String declareSql = " select * from cr_vehicle_type ";

        // pg模式下如果想fetchsize生效，只须设置autocommit为flase，也就是需要手工去管理事务
        pgDbPro.tx(Connection.TRANSACTION_SERIALIZABLE, () -> {
            // 游标的测试方案，请在内部完成
            pgDbPro.execute(new ICallback() {
                @Override
                public Object call(Connection conn) throws SQLException {
                    System.out.println("事务状态:"+conn.getAutoCommit());
                    // 创建预编译对象
                    PreparedStatement preparedStatement = conn.prepareStatement(declareSql
                            // mysql 流读取，需要开启
                            ,ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY
                    );
                    // 预读数据，默认为0，提前设置可以提高next的运行效率，不用每次都等半天，调用一次next执行1次游标操作( 提前内存存储 N 条数据)
                    // 预读取 会提前消耗内存,pg则必须设置读取的对象大小
//                    preparedStatement.setFetchSize(2000);
                    // mysql流读取，直接获取数据流结果 -- mysql 有三个条件forward-only，read-only，fatch size是Integer.MIN_VALUE
                    preparedStatement.setFetchSize(Integer.MIN_VALUE);
                    // 执行sql查询数据
                    ResultSet rs = preparedStatement.executeQuery();
                    // 循环获取下一条记录
                    while (rs.next()) {
                        // 返回map集合
                        System.out.println(EDbResultSetUtil.returnMap(rs));
                    }
                    // 返回结果集设置
                    return null;
                }
            });
            return false;
        });
    }

}
