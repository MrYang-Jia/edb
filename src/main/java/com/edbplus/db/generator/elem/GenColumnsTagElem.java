package com.edbplus.db.generator.elem;

/**
 * @ClassName GenColumnsTagElem
 * @Description: 字段类型
 * @Author 杨志佳
 * @Date 2020/3/17
 * @Version V1.0
 **/
public class GenColumnsTagElem {
    // 自增
    public static String auto_increment = "auto_increment";
    // 主键
    public static String PRI = "PRI";
    // 复合唯一索引标识
    public static String UNI = "UNI";
    // 普通索引标识 - 则该列是非唯一索引的第一列，其中允许在列中多次出现给定值
    public static String MUL = "MUL";



}
