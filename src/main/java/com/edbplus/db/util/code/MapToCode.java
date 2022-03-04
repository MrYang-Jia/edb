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
package com.edbplus.db.util.code;

import cn.hutool.core.map.CamelCaseMap;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName MapToCode
 * @Description: mapToCode
 * @Author 杨志佳
 * @Date 2022/2/18
 * @Version V1.0
 **/
public class MapToCode {
    /**
     * 生成java对象代码
     * @param dataMap
     * @param beanName
     * @return
     */
    public static String toJavaCode(Map<String,Object> dataMap,String beanName){
        CamelCaseMap<String,Object> camelCaseMap = new CamelCaseMap(dataMap); // 转换成驼峰map
        StringBuilder codeStr = new StringBuilder("\n");
        codeStr.append("import java.util.Date;\n");
        codeStr.append("import java.io.File;\n");
        codeStr.append("import java.math.BigDecimal;\n");
        codeStr.append("import io.quarkus.runtime.annotations.RegisterForReflection;\n");
        codeStr.append("import lombok.Data;\n");
        codeStr.append("import java.io.Serializable;\n");
        codeStr.append("\n\n"); // 换行
        codeStr.append("@RegisterForReflection\n");
        codeStr.append("@Data\n");
        codeStr.append("public class ").append(StrUtil.upperFirst(beanName)).append(" implements Serializable {\n");
        String fieldCode = "";
        for (Map.Entry<String, Object> m : camelCaseMap.entrySet()) {
            if(m.getValue() instanceof String){
                fieldCode = "private String "+m.getKey()+";";
            }else
            if(m.getValue() instanceof Integer){
                fieldCode = "private Integer "+m.getKey()+";";
            }else
            if(m.getValue() instanceof Long){
                fieldCode = "private Long "+m.getKey()+";";
            }else
            if(m.getValue() instanceof Date){
                fieldCode = "private Date "+m.getKey()+";";
            }else
            if(m.getValue() instanceof Double){
                fieldCode = "private BigDecimal "+m.getKey()+";";
            }else
            if(m.getValue() instanceof Float){
                fieldCode = "private BigDecimal "+m.getKey()+";";
            }else
            if(m.getValue() instanceof BigDecimal){
                fieldCode = "private BigDecimal "+m.getKey()+";";
            }else
            if(m.getValue() instanceof BigInteger){
                fieldCode = "private Long "+m.getKey()+";";
            }else
            if(m.getValue() instanceof Boolean){ // 遇到boolean类型，一般都转换成 Integer
                fieldCode = "private Integer "+m.getKey()+";";
            }else
            {
                if(m.getValue() != null){
                    fieldCode = "private "+m.getValue().getClass().getName()+" "+m.getKey()+";";
                }else{// 识别不出来的，默认都同意转换成 String 类型
                    //codeStr.append("\t").append("//todo:回填正确的属性和示意\n");
                    fieldCode = "private 999 "+m.getKey()+";";
                }
            }
            codeStr.append("\t").append(fieldCode).append("\n");
//            System.out.println(fieldCode);
        }
        codeStr.append("}\n");
        System.out.println(codeStr);
        return codeStr.toString();
    }
}
