package com.edb.cloud.web.util;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果集对象 -- todo:待做一个新的项目时，改用该api
 */
@Data
public class EResult  implements Serializable {
    // 0 成功，其他则为失败
    private String code;
    private String msg;
    // 返回结果集
    private Map<String,Object> data = new HashMap<>();
    //总条数--分页条件此参数有值
    private Long count;

    /**
     * 返回结果
     * @param data
     * @return
     */
    public static EResult succ(Object data) {
        return succ("main",data);
    }

    /**
     * 返回成功的结果信息
     * @param key
     * @param data
     * @return
     */
    public static EResult succ(String key,Object data) {
        EResult m = new EResult();
        m.setCode("0");
        m.getData().put(key,data);
        m.setMsg("操作成功");
        return m;
    }

    /**
     * 返回成功的结果
     * @param key
     * @param mess
     * @param data
     * @return
     */
    public static EResult succ(String key,String mess, Object data) {
        EResult m = new EResult();
        m.setCode("0");
        m.getData().put(key,data);
        m.setMsg(mess);
        return m;
    }

    /**
     * 返回失败的信息
     * @param mess
     * @return
     */
    public static EResult fail(String mess) {
        EResult m = new EResult();
        m.setCode("-1");
        m.setData(null);
        m.setMsg(mess);
        return m;
    }

    /**
     * 返回失败的信息
     * @param key
     * @param mess
     * @param data
     * @return
     */
    public static EResult fail(String key,String mess, Object data) {
        EResult m = new EResult();
        m.setCode("-1");
        m.getData().put(key,data);
        m.setMsg(mess);
        return m;
    }
}