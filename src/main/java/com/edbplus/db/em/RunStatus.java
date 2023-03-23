package com.edbplus.db.em;

/**
 * @ClassName RunStatus
 * @Description: 执行状态
 * @Author 杨志佳
 * @Date 2022/3/22
 * @Version V1.0
 **/
public enum RunStatus {
    SUCCESS(1,"success"),
    FAIL(0,"fail");
    private int code;
    private String desc;

    RunStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
