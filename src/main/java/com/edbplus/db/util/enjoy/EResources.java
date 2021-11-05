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
package com.edbplus.db.util.enjoy;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName EResources
 * @Description: EDb rescoureces 工具类
 * @Author 杨志佳
 * @Date 2021/11/4
 * @Version V1.0
 **/
public class EResources {

    /**
     * SpringBoot 底下扫描jar包底下所有根路径为 fileRoot,后缀名为 fileSuf 的文件集( windows or linux )
     * @param fileRoot
     * @param fileSuf
     * @return
     */
    public static List<String> getResources(String fileRoot,String fileSuf){
        // 这个路径必须写成 / 而不能根据环境的 File.separator 进行引用，否则会导致路径失效，导致自动检测文件是否更新失效
        // fileRoot = sql/bus
        List<String> sqlPaths = new ArrayList<>();
        String[] sqlRoutePaths = fileRoot.split("/");
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().
                    // 当前项目所有jar包(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)的资源文件！！！
                            getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + fileRoot + "*/**/*"+fileSuf);
            int idx = 0;
            String filePath = null;
            for(Resource resource : resources){
                filePath = resource.getURI().toString();
                idx = filePath.indexOf(sqlRoutePaths[0]); // 首目录名所在位置匹配
                fileRoot = resource.getURI().toString().substring(idx,filePath.length()); // 截获实际地址信息即可
                // 加载所有的sql文件，自动装载，不需要每次写一个sql文件，丢到配置文件里，然后sql文件的方法命名，建议直接就是 nameSpace.key
                sqlPaths.add(fileRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sqlPaths;
    }

}
