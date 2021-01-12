package com.edbplus.db.web.api;

/**
 * @program: coreframework-parent
 * @description: API数据返回码
 * @author: 杨志佳
 * @create: 2021-01-12 11:27
 **/
public enum ApiReturnCode {

    SUCCESS (0,"成功"),
    FAIL (500,"系统异常"),
    ;


    private Integer code;
    private String desc;

    ApiReturnCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
