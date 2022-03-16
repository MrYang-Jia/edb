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
package com.edbplus.db.listener.impl;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDbPro;
import com.edbplus.db.em.RunSqlType;
import com.edbplus.db.listener.ConnectListener;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName SqlListener
 * @Description: sql监听
 * @Author 杨志佳
 * @Date 2022/3/15
 * @Version V1.0
 **/
@Slf4j
public class SqlListener implements ConnectListener {
    // 默认打印sql日志
    @Setter
    Boolean openLog =  true;

    @Override
    public void loss(EDbPro eDbPro, RunSqlType runSqlType, Long lossTimeMillis, String sql, Object[] params) {
        if(openLog){
            log.info("sql-> "+sql + "->" + JSONUtil.toJsonStr(params) +" -> "+lossTimeMillis);
        }
    }
}
