package com.edb.cloud.jfinal.activerecord.db.generator.jdbc;


/**
 * PostgreSql常用表语句
 *
 * @author MrYang
 * @date 2020-10-31
 */
public class GenPg {

    /**
     * 识别用 , 分割的多表语句，并改写语句
     * @param tableName
     * @return
     */
    public static String getTablesSql(String tableName){

        // 如果已经包含了单引号，则不需要再进行一次改写
        if(tableName.contains("'")){
            return tableName;
        }

        String[] tbs = tableName.split(",");
        StringBuilder sql = new StringBuilder("");
        for(String tb : tbs){
            // 前面有字段时，添加逗号即可
            if(sql.length() > 0){
                sql.append(",");
            }
            sql.append("'").append(tb).append("'");
        }
        return sql.toString();
    }

    /**
     * 返回表对象信息Sql
     * @param tableName
     * @return
     */
    public static String getTableInfoSql(String tableName){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" 	relname as \"tableName\" ");
        sql.append(" 	,cast(obj_description(relfilenode,'pg_class') as varchar) as \"tableComment\" ");
        // postgreSql 默认存储引擎即可
        //sql.append(" 	,'Tuple' as ENGINE ");
        sql.append(" FROM ");
        sql.append(" 	pg_class  ");
        sql.append(" WHERE ");
        sql.append(" 	1=1  ");
        // table表名称
        if(tableName != null && tableName.length() > 0){
            if(tableName.contains(",")){
                sql.append(" AND  relname in (").append(getTablesSql(tableName)).append(") ");
            }else{
                sql.append(" AND  relname = '").append(tableName).append("' ");
            }
        }
        sql.append(" order by relname ");
        return sql.toString();
    }

    /**
     * 返回表字段信息sql
     * @param tableName
     * @return
     */
    public static String getTableColumnsSql(String tableName){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" ordinal_position as \"ordinalPosition\", ");
        sql.append(" \"table_name\" as \"tableName\", ");
        sql.append(" \"column_name\" as \"columnName\", ");
        sql.append(" column_default as \"columnDefault\", ");
        sql.append(" udt_name as \"dataType\", ");
        sql.append(" ( ");
        sql.append(" SELECT COL_DESCRIPTION(A.ATTRELID, A.ATTNUM)  ");
        sql.append("   FROM PG_CLASS AS C, PG_ATTRIBUTE AS A ");
        sql.append("  WHERE C.RELNAME = cls.\"table_name\" ");
        sql.append("    AND A.ATTRELID = C.OID ");
        sql.append(" 	 and A.ATTNAME = cls.\"column_name\" ");
        sql.append("    AND A.ATTNUM > 0 ");
        sql.append(" ) as \"columnComment\", ");
        sql.append(" ( ");
        sql.append(" 	SELECT ");
        sql.append("  'PRI' as pri ");
        sql.append(" FROM ");
        sql.append("  pg_constraint ");
        sql.append("  INNER JOIN pg_class ON pg_constraint.conrelid = pg_class.oid ");
        sql.append("  INNER JOIN pg_attribute ON pg_attribute.attrelid = pg_class.oid  ");
        sql.append("  AND pg_attribute.attnum = pg_constraint.conkey [ 1 ] ");
        sql.append("  INNER JOIN pg_type ON pg_type.oid = pg_attribute.atttypid  ");
        sql.append(" WHERE ");
        sql.append("  pg_class.relname = cls.\"table_name\" ");
        sql.append("  and attname =  cls.\"column_name\" ");
        sql.append("  AND pg_constraint.contype = 'p' ");
        sql.append(" )  as \"columnKey\", ");
        sql.append(" (case when column_default like 'nextval%' then 'auto_increment' else '' end) ");
        sql.append("  as \"extra\", ");
        sql.append(" (case when is_nullable = 'NO' then 0 else 1 end) as \"isN\", ");
        sql.append(" ( ");
        // 建议都用精度换算
        sql.append(" case when  udt_name ='numeric' then concat(numeric_precision,',',numeric_scale)  ");
        // 调整大致长度计算，趋近于 mysql 字段对应的数值，实际上可以不用换算，支持当前拥有的长度，只是习惯性的进行换算，有需要的，可自行调整
        sql.append("  when udt_name in ('int8','int4') then concat(round(numeric_precision/3.2))   ");
        sql.append("   when udt_name in ('float4','float8') then concat(numeric_precision/2)   ");
        sql.append("  when udt_name = 'timestamp' then '' ");
        sql.append(" else ");
        // 字符串长度
        sql.append(" concat(character_maximum_length)  ");
        sql.append(" end  ");
        sql.append(" ) as \"maxL\" ");
        sql.append(" FROM information_schema.columns cls  ");
        sql.append(" WHERE 1=1 ");
        //sql.append(" -- and (table_schema, table_name) = ('public', 'tra_goods_source') ");
        sql.append(" and table_schema = 'public'  ");
        // table表名称
        if(tableName != null && tableName.length() > 0){
            if(tableName.contains(",")){
                sql.append(" AND  table_name in (").append(getTablesSql(tableName)).append(") ");
            }else{
                sql.append(" AND  table_name = '").append(tableName).append("' ");
            }
        }
        //sql.append(" and table_name =  'tra_goods_source' ");
        sql.append(" ORDER BY table_name,ordinal_position; ");
        return sql.toString();
    }

}
