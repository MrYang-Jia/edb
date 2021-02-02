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
package com.edbplus.db.generator.util;

import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @program: coreframework-parent
 * @description: Engine模板工具
 * @author: MrYang
 * @create: 2018-05-12 14:38
 **/
@Slf4j
public class EngineUtil {


    /**
     * 根据具体模板生成文件
     * @param templateFileName  模板文件名称
     * @param kv                渲染参数
     * @param filePath          输出目录
     * @return
     */
    public static boolean render(String baseTemplatePath, String templateFileName, Kv kv, StringBuilder filePath)  {
        //
        // baseTemplatePath 暂时无用途
        // 根据路径生成文件
        return render(templateFileName,kv,filePath,false);
    }

    /**
     * 不追加写入生成文件
     * @param templateFileName
     * @param kv
     * @param filePath
     * @return
     */
    public static boolean render(String templateFileName, Kv kv, StringBuilder filePath)  {
        return render(templateFileName,kv,filePath,false);
    }

    /**
     * 根据路径生成文件
     * @param templateFileName
     * @param kv
     * @param filePath
     * @return
     */
    public static boolean render(String templateFileName, Kv kv, StringBuilder filePath, boolean isAppend)  {
        BufferedWriter output = null;
        PrintWriter pw = null;
        try {
            log.debug("模板路径:{}",templateFileName);
            log.debug("输出路径:{}",filePath);

            File file = new File(filePath.toString());
            File path = new File(file.getParent());
            if ( ! path.exists() ) {
                // 创建所有上级目录
                path.mkdirs();
            }

            FileWriter fw = new FileWriter(file);

            // 是否追加写入
            if(isAppend){
                // 追加写入的对象
                pw = new PrintWriter(fw);
                Engine.use()
                        // 设置模板路径
                        //.setBaseTemplatePath(baseTemplatePath)
                        .setSourceFactory(new ClassPathSourceFactory())
                        // 获取模板 - 通过指定文件名的方式获取 ,请勿用 baseTemplatePath 拼接，会无法获取到
                        .getTemplate(templateFileName)
                        // 键值对的方式写入文件里
                        .render(kv, pw);
            }else{
                // 不追加写入
                output = new BufferedWriter(fw);
                Engine.use()
                        // 设置模板路径
                        //.setBaseTemplatePath(baseTemplatePath) ,请勿用 baseTemplatePath 拼接，会无法获取到
                        .setSourceFactory(new ClassPathSourceFactory())
                        // 获取模板 - 通过指定文件名的方式获取
                        .getTemplate(templateFileName)
                        // 键值对的方式写入文件里
                        .render(kv, output);
            }



            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally{
            try { if( output != null ) output.close(); } catch (IOException e) {}
        }
    }

}
