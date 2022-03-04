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
package com.edbplus.db.generator.entity;


import java.util.Date;
import java.math.BigDecimal;
import lombok.Data;
import java.io.Serializable;

/**
 * 表字段信息 - 对应 GenTableColumnsElem
 */
@Data
public class TableColumnInfo implements Serializable {
    private Object columnDefault; // 默认值
    private Integer isN; // 是否允许为空,0-否，1-是
    private String dataType; // 数据类型，例如 int
    private String extra; // 是否自增等
    private String columnComment; // 字段示意
    private String maxL; // 最大字段长度
    private Long ordinalPosition; // 顺序
    private String columnKey; // 字段索引名，一般为 PRI-主键或其他
    private String tableName; // 表名
    private String columnName; // 字段名
}
