package com.edbplus.db.em;

/**
 * @ClassName RunSqlType
 * @Description: 执行sql的类型
 * @Author 杨志佳
 * @Date 2022/3/15
 * @Version V1.0
 **/
public enum RunSqlType {
    select(1, "select"),
    save(2, "save"),
    update(3, "update"),
    delete(4, "delete"),
//    call(5, "call"),
    other(99, "other");

    RunSqlType(int i, String runSqlType) {
    }


}
