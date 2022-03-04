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
package com.edbplus.db.config;

import cn.hutool.core.io.FileUtil;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.template.Engine;

import java.io.File;
import java.util.List;

/**
 * @ClassName ArpUtil
 * @Description: ActiveRecordPlugin 组件的延申扩展
 * @Author 杨志佳
 * @Date 2022/2/26
 * @Version V1.0
 **/
public class ArpUtil {

    /**
     * 共享方法模板
     * @param enjoy
     */
    public static void setEnjoyShareFunction(Engine enjoy, String prePath){
        String sqlPre = prePath.split("/")[0];
        List<File> sharedFiles = FileUtil.loopFiles(prePath);
        int idx = 0;
        String path = "";
        // 遍历添加共享模板
        for(File sqlFile : sharedFiles){
            path = sqlFile.getPath();
            idx = path.indexOf(sqlPre+File.separator);
            enjoy.addSharedFunction(path.substring(idx,path.length()).replaceAll("\\\\","/"));
        }
    }

    /**
     * 设置主数据库sql脚本 (便于统一管理)
     * @param arp
     */
    public static void setSqlPath(ActiveRecordPlugin arp, String prePath){
        String sqlPre = prePath.split("/")[0];
        // 引入该项目的所有sql文件
        List<File> busFiles = FileUtil.loopFiles(prePath);
        int idx = 0;
        String path = "";
        // 遍历添加共享模板
        for(File sqlFile : busFiles){
            path = sqlFile.getPath();
            idx = path.indexOf(sqlPre+File.separator);
            // 添加sql文件路径
            arp.addSqlTemplate(path.substring(idx,path.length()).replaceAll("\\\\","/"));
        }
    }
}
