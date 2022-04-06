package com.edbplus.db.query.em;

/**
 * @ClassName SqlConnectorEnum
 * @Description: sql连结操作符
 * @Author 杨志佳
 * @Date 2022/4/3
 * @Version V1.0
 **/
public enum SqlConnectorEnum {
    and(1, "and"),
    or(2, "or");

    /**
     *
     * @param i
     * @param sqlConnector
     */
    SqlConnectorEnum(int i, String sqlConnector) {
    }
}
