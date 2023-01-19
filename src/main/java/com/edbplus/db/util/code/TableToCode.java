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
import com.alibaba.druid.DbType;
import com.edbplus.db.EDbPro;
import com.edbplus.db.generator.entity.GenTable;
import com.edbplus.db.generator.entity.GenTableColumn;
import com.edbplus.db.generator.jdbc.GenJdbc;
import com.edbplus.db.generator.jdbc.GenMysql;
import com.edbplus.db.generator.jdbc.GenPg;
import com.edbplus.db.util.hutool.str.EStrUtil;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName TableToCode
 * @Description: 表转代码
 * @Author 杨志佳
 * @Date 2022/3/7
 * @Version V1.0
 **/
public class TableToCode {
    /**
     * 获取javaCode
     * @param tableName
     * @param edbPro
     * @return
     */
    public static String javaCode(String tableName,String beanName, EDbPro edbPro){
        if(StrUtil.isEmptyIfStr(beanName)){
            beanName =StrUtil.upperFirst( StrUtil.toCamelCase(beanName));
        }
        if(edbPro.getConfig().getDialect() instanceof MysqlDialect){
            return javaCode(tableName,beanName,edbPro,DbType.mysql);
        }
        if(edbPro.getConfig().getDialect() instanceof PostgreSqlDialect){
            return javaCode(tableName,beanName,edbPro,DbType.postgresql);
        }
        throw new RuntimeException("未识别的数据库类型");
    }

    /**
     * 生成表对象
     * @param tableName
     * @param edbPro
     * @param dbType
     * @return
     */
    public static GenTable genTable(String tableName, EDbPro edbPro, DbType dbType){
        String tableInfoSql = null;
        if(dbType == DbType.mysql || dbType == DbType.mariadb){
            tableInfoSql = GenMysql.getTableInfoSql(tableName);
        }
        if(dbType == DbType.postgresql ){
            tableInfoSql = GenPg.getTableInfoSql(tableName);
        }
        GenTable genTable = edbPro.findFirst(GenTable.class,tableInfoSql);
        return genTable;
    }

    /**
     * 获取
     * @param tableName
     * @param edbPro
     * @param dbType
     * @return
     */
    public static List<GenTableColumn> genTableColumns(String tableName, EDbPro edbPro, DbType dbType){
        String tableColumnSql = null;
        if(dbType == DbType.mysql || dbType == DbType.mariadb){
            tableColumnSql = GenMysql.getTableColumnsSql(tableName);
        }
        if(dbType == DbType.postgresql ){
            tableColumnSql = GenPg.getTableColumnsSql(tableName);
        }
        List<GenTableColumn> genTableColumns = edbPro.find(GenTableColumn.class,tableColumnSql);
        String[] nums ;
        for (GenTableColumn genTableColumn:genTableColumns){
            // 转换成java对象的类型
            // 指定字段的java类型
            genTableColumn.setColumnType(GenJdbc.filedTypeMap.get(genTableColumn.getDataType()));
            // 驼峰式字段名 -- 必须将字符转小写，不然驼峰字段转换会出意外
            genTableColumn.setColumnCode(EStrUtil.toCamelCase(genTableColumn.getColumnName().toLowerCase()));
            // 通过sql语句获取的字段长度
            if(genTableColumn.getMaxL()!=null && genTableColumn.getMaxL().contains(",")){
                nums = genTableColumn.getMaxL().split(",");
                // 长度
                genTableColumn.setMaxL(nums[0]);
                // 根据长度回填 N 个 9，代表最大值
                genTableColumn.setMaxValue(String.join("", Collections.nCopies(Integer.valueOf(nums[0]) - Integer.valueOf(nums[1]), "9")));
                // 小数位数
                genTableColumn.setDecimalDigit(nums[1]);
            }else{

                if(genTableColumn.getMaxL().length()>0){
                    // 根据长度回填 N 个 9，代表最大值
                    genTableColumn.setMaxValue(String.join("", Collections.nCopies(Integer.valueOf(genTableColumn.getMaxL()), "9")));
                }
                genTableColumn.setDecimalDigit("0");
            }
        }
        return genTableColumns;
    }

    public static String javaCode(String tableName,String beanName, EDbPro edbPro,DbType dbType){
        List<GenTableColumn> genTableColumns = genTableColumns(tableName,edbPro,dbType);
        GenTable genTable= genTable(tableName,edbPro,dbType);

        StringBuilder codeStr = new StringBuilder("\n");
        codeStr.append("import java.util.Date;\n");
        codeStr.append("import java.io.File;\n");
        codeStr.append("import java.math.BigDecimal;\n");
        codeStr.append("import com.fasterxml.jackson.annotation.JsonFormat;\n"); // 时间 json 格式
        codeStr.append("import io.quarkus.runtime.annotations.RegisterForReflection;\n");
        codeStr.append("import lombok.Data;\n");
        codeStr.append("import javax.persistence.*;\n"); // jpa注解
        codeStr.append("import org.eclipse.microprofile.openapi.annotations.media.Schema;\n"); // api 注解
        codeStr.append("import java.io.Serializable;\n");
        codeStr.append("\n\n"); // 换行
        codeStr.append("@RegisterForReflection\n");
        codeStr.append("@Data\n");
        codeStr.append("@Schema(title = \""+ genTable.getTableComment() +"\")\n");
        codeStr.append("@Table(name = \""+tableName+"\")\n");
        codeStr.append("public class ").append(StrUtil.upperFirst(beanName)).append(" implements Serializable {\n\n");
        for (GenTableColumn genTableColumn:genTableColumns){
            codeStr.append("\t").append("/**\n").append("     * ").append(genTableColumn.getColumnComment()).append("\n     */\n"); //注释
            if(genTableColumn.getColumnKey().equalsIgnoreCase("PRI")){
                codeStr.append("\t").append("@Id\n"); // 主键注解
            }
            codeStr.append("\t").append("@Column(name=\""+genTableColumn.getColumnName()+"\")\n"); // 主键注解
            if(genTableColumn.getColumnType().equals("Date")){
                codeStr.append("\t").append("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")\n"); // 得加上时间转换的格式
            }
            if(genTableColumn.getMaxValue()!=null){
                // 加长度限制
                codeStr.append("\t").append("@Schema(maxLength = ").append(genTableColumn.getMaxL()).append(",description = \"").append(genTableColumn.getColumnComment()).append("\")\n");
            }else{
                if(genTableColumn.getColumnType().equals("Date")){
                    codeStr.append("\t").append("@Schema(description = \"").append(genTableColumn.getColumnComment()).append("\", example = \"2022-03-01 00:00:00\")\n");
                }else{
                    codeStr.append("\t").append("@Schema(description = \"").append(genTableColumn.getColumnComment()).append("\")\n");
                }
            }
            codeStr.append("\t").append("private ").append(genTableColumn.getColumnType()).append(" ").append(genTableColumn.getColumnCode()).append(";\n");
            codeStr.append("\n");// 换行好看点
        }
        codeStr.append("}\n");

        System.out.println(codeStr);
        return codeStr.toString();

    }

    public static String javaCode(String tableName,String beanName, EDbPro edbPro,String dbType){
        return javaCode(tableName,beanName,edbPro,DbType.valueOf(dbType));
    }
}