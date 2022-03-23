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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.edbplus.db.EDbPro;
import com.edbplus.db.em.RunSqlType;
import com.edbplus.db.listener.ConnectListener;
import com.edbplus.db.em.RunStatus;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

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
    public void loss(EDbPro eDbPro, RunSqlType runSqlType, Long lossTimeMillis, String sql, Object[] params, RunStatus runStatus) {
        if(openLog){
//            log.info("sql-> "+sql + "->" + JSONUtil.toJsonStr(params) +" -> "+lossTimeMillis);
            try {
                log.info("sql-> "+lossTimeMillis+"("+runStatus.getDesc()+") ->"+format(sql,params) );
            }catch (Throwable e){
                log.error("lossSqlErr:",e);
            }

        }
    }

    public String format(final String strPattern, final Object... argArray) {
        if (StrUtil.isBlank(strPattern) || ArrayUtil.isEmpty(argArray)) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();

        // 初始化定义好的长度以获得更好的性能
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;// 记录已经处理到的位置
        int delimIndex;// 占位符所在位置
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf("?", handledPosition);
            if (delimIndex == -1) {// 剩余部分无占位符
                if (handledPosition == 0) { // 不带占位符的模板直接返回
                    return strPattern;
                }
                // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
                sbuf.append(strPattern, handledPosition, strPatternLength);
                return sbuf.toString();
            }

            sbuf.append(strPattern, handledPosition, delimIndex);
            if(argArray[argIndex] instanceof String){
                sbuf.append("'").append(StrUtil.utf8Str(argArray[argIndex])).append("'");
            }
            else if(argArray[argIndex] instanceof Date){ // 处理时间的参数格式
                sbuf.append("'").append(DateUtil.formatDateTime((Date) argArray[argIndex])).append("'");
            }
            else{
                sbuf.append(" ").append(StrUtil.utf8Str(argArray[argIndex])).append(" ");
            }
            handledPosition = delimIndex + 1;
        }

        // append the characters following the last {} pair.
        // 加入最后一个占位符后所有的字符
        sbuf.append(strPattern, handledPosition, strPattern.length());

        return sbuf.toString();
    }
}
