package com.edbplus.cloud.jfinal.activerecord.db.dto;

import lombok.Data;

@Data
public class EDBListenerResult {
    // 是否继续执行
    private boolean isNextToDo;
    // 返回执行结果
    private boolean returnResult;
    // 返回结果条数
    private int returnCt;
}
