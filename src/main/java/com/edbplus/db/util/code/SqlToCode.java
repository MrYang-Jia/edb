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

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.DbType;
import com.edbplus.db.EDbPro;
import com.edbplus.db.druid.sql.parser.SelectItemInfo;
import com.edbplus.db.druid.sql.parser.SelectItems;
import com.edbplus.db.druid.sql.parser.SelectParser;
import com.edbplus.db.generator.entity.TableColumnInfo;
import com.edbplus.db.generator.jdbc.GenMysql;
import com.edbplus.db.generator.jdbc.GenPg;
import com.edbplus.db.generator.util.SqlToJava;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @ClassName SqlToCode
 * @Description: sql语句转javaCode
 * @Author 杨志佳
 * @Date 2022/2/23
 * @Version V1.0
 **/
public class SqlToCode {
    /**
     * 获取javaCode
     * @param sql
     * @param edbPro
     * @param dbType
     * @return
     */
    public static String javaCode(String sql,String beanName, EDbPro edbPro, String dbType){
        return javaCode(sql,beanName,edbPro,DbType.valueOf(dbType));
    }

    /**
     * 获取javaCode
     * @param sql
     * @param edbPro
     * @param dbType
     * @param beanName -- 生产的实体对象名称
     * @return
     */
    public static String javaCode(String sql,String beanName, EDbPro edbPro, DbType dbType){
        SelectItems selectItems = SelectParser.getSelectNames(sql, dbType.name());
//        System.out.println(JSONUtil.toJsonPrettyStr(selectItems));
        if(dbType == DbType.mysql || dbType == DbType.mariadb){ // mysql 或 mariadb
            sql = GenMysql.getTableColumnsSql(SelectItems.splitSqlParams(selectItems.tables),SelectItems.splitSqlParams(selectItems.columns));
        }else{
            // pg 的字段属性sql
            sql = GenPg.getTableColumnsSql(SelectItems.splitSqlParams(selectItems.tables),SelectItems.splitSqlParams(selectItems.columns));
        }
        // 从数据库里获取所有表的字段，并回填到对应的结构体上
        List<TableColumnInfo> list = edbPro.find(TableColumnInfo.class,sql);
        // 打印sql查询出来的字段集合
//        System.out.println(JSONUtil.toJsonPrettyStr(list));
        Map<String, SelectItemInfo> selectItemInfoMap = null;
        SelectItemInfo selectItemInfo = null;
        for(TableColumnInfo tableColumnInfo:list){
            selectItemInfoMap = selectItems.get(tableColumnInfo.getTableName().toLowerCase(Locale.ROOT));
            if(selectItemInfoMap != null){ // 存在表
                selectItemInfo = selectItemInfoMap.get(tableColumnInfo.getColumnName().toLowerCase(Locale.ROOT));
                if(selectItemInfo !=null ){ // 存在字段
                    selectItemInfo.setColumnType(tableColumnInfo.getDataType()); // 数据库的字段类型
                    selectItemInfo.setJavaType(SqlToJava.filedTypeMap.get(tableColumnInfo.getDataType())); // java 对用的数据类型
                    selectItemInfo.setMaxL(tableColumnInfo.getMaxL()); // 字段的最大长度，如果没有的话为null ，存在 单精度和双精度两类长度，例如  11 或 11,2
                    if(tableColumnInfo.getMaxL()!=null && tableColumnInfo.getMaxL().length()>0  ){
                        if(tableColumnInfo.getMaxL().contains(",")){
                            selectItemInfo.setJavaLength(Integer.valueOf(tableColumnInfo.getMaxL().split(",")[0])); // 例如 11,2 ，只需要获取左侧第一个位置即可
                        }else{
                            selectItemInfo.setJavaLength(Integer.valueOf(tableColumnInfo.getMaxL()));
                        }
                    }
                    selectItemInfo.setColumnComment(tableColumnInfo.getColumnComment()); // 注释
                }else{
                    System.out.println("not find column->"+tableColumnInfo.getTableName()+"."+tableColumnInfo.getColumnName());
                }
            }
        }
        // 打印组装的结果集
//        System.out.println(JSONUtil.toJsonPrettyStr(selectItems));
        StringBuilder codeStr = new StringBuilder("\n");
        codeStr.append("import java.util.Date;\n");
        codeStr.append("import java.io.File;\n");
        codeStr.append("import java.math.BigDecimal;\n");
        codeStr.append("import com.fasterxml.jackson.annotation.JsonFormat;\n"); // 时间 json 格式
        codeStr.append("import io.quarkus.runtime.annotations.RegisterForReflection;\n");
        codeStr.append("import lombok.Data;\n");
        codeStr.append("import org.eclipse.microprofile.openapi.annotations.media.Schema;\n"); // api 注解
        codeStr.append("import java.io.Serializable;\n");
        codeStr.append("\n\n"); // 换行
        codeStr.append("@RegisterForReflection\n");
        codeStr.append("@Data\n");
        codeStr.append("@Schema(title = \""+StrUtil.upperFirst(beanName)+"\")\n");
        codeStr.append("public class ").append(StrUtil.upperFirst(beanName)).append(" implements Serializable {\n");
        // 迭代字段
        selectItems.forEach((tableName, tableColumn) -> {
//            System.out.println(tableName + "：" + tableColumn);
            // 第一层是表
            // 第二层是表字段映射
            tableColumn.forEach((column,item)->{
                //
                if(item.getJavaType().contains("Date")){
                    codeStr.append("\t").append("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")\n"); // 得加上时间转换的格式
                }
                if(item.getJavaLength()!=null){
                    // 加长度限制
                    codeStr.append("\t").append("@Schema(maxLength = ").append(item.getJavaLength()).append(",description = \"").append(item.getColumnComment()).append("\")\n");
                }else{
                    // 增加描述
                    codeStr.append("\t").append("@Schema(description = \"").append(item.getColumnComment()).append("\")\n");
                }
                codeStr.append("\t").append("private ").append(item.getJavaType()).append(" ").append(item.getJavaCodeName()).append(";\n");
            });
        });
        codeStr.append("}\n");

        System.out.println(codeStr);
        return codeStr.toString();
    }
}
