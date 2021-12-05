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
package com.edbplus.db.dialect;

import com.edbplus.db.druid.EDbSelectUtil;

/**
 * @ClassName DialectTool
 * @Description: Dialect 工具类
 * @Author 杨志佳
 * @Date 2021/11/30
 * @Version V1.0
 **/
public class DialectTool {
    public static final String select_1 = "select 1 ";

    /**
     * 去除 order by 关键词
     * @param sql
     * @return
     */
    public static String replaceOrderBy(String sql) {
        return EDbSelectUtil.removeOrder(sql);
    }
}
