package com.edbplus.db.generator.elem;

/**
 * @ClassName GenTableIndexElem
 * @Description: 表索引相关字段 -- 对应GenMysql.getTableIndexSql
 * @Author 杨志佳
 * @Date 2020/3/19
 * @Version V1.0
 **/
public class GenTableIndexElem {
    // 描述
    public static String Comment = "Comment";
    // 是否为null ，yes 则可以为null
    public static String Null ="Null";
    // 表名称
    public static String Table = "Table";
    // 平均数值组=索引基数/表总数据行，平均数值组越接近1就越有可能利用索引
    public static String Cardinality = "Cardinality";
    // 是否不是唯一索引 -- 0 不是，1是 ;    当值=0的时候，可以用来判断是否唯一索引
    public static String Non_unique = "Non_unique";
    // Collation=A 不区分大小写
    public static String Collation = "Collation";
    // 字段名称
    public static String Column_name = "Column_name";
    // Packed 指示关键字如何被压缩
    public static String Packed = "Packed";
    // 如果列只是被部分地编入索引，则为被编入索引的字符的数目。如果整列被编入索引，则为NULL。
    public static String Sub_part = "Sub_part";

    // 索引描述
    public static String Index_comment = "Index_comment";

    // 字段在索引中的顺序
    public static String Seq_in_index = "Seq_in_index";

    // 字段名称
    public static String Key_name = "Key_name";

    // Index_type 用过的索引方法（BTREE, FULLTEXT, HASH, RTREE）
    public static String Index_type = "Index_type";



}
