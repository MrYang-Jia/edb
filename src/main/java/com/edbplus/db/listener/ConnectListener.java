/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.listener;

import com.edbplus.db.EDbPro;
import com.edbplus.db.em.RunSqlType;
import com.edbplus.db.em.RunStatus;

/**
 * @ClassName ConnectListener
 * @Description: connect连接监听器
 * @Author 杨志佳
 * @Date 2022/3/15
 * @Version V1.0
 **/
public interface ConnectListener {

    /**
     * 损耗监听
     * @param eDbPro -- 执行器
     * @param lossTimeMillis -- 耗时，毫秒
     * @param sql -- 执行语句
     * @param params -- 入参
     */
    public void loss(EDbPro eDbPro, RunSqlType runSqlType, Long lossTimeMillis, String sql, Object[] params, RunStatus runStatus);

}
