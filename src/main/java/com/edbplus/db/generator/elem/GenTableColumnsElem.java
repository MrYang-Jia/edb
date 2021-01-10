package com.edbplus.db.generator.elem;


/**
 * 表字段对象描述
 *
 * @author MrYang
 * @date 2019-02-26
 */
public class GenTableColumnsElem {

    // 字段顺序
    public static String ordinalPosition = "ordinalPosition";

    public static String tableName = "tableName";

    // 表字段名称
    public static String columnName = "columnName";

    // 初始值
    public static String columnDefault = "columnDefault";

    // 数据类型
    public static String dataType = "dataType";

    // 字段描述
    public static String columnComment = "columnComment";

    // 主键 - PRI 对应唯一主键
    public static String columnKey = "columnKey";

    // 函数 - auto_increment 对应自增id
    public static String extra = "extra";

    // 是否允许为null ， 0 -不允许，1-允许
    public static String isN = "isN";

    // 最大长度, 数字或NULL ，NULL 一般为时间 或 TEXT
    public static String maxL = "maxL";

}
