package com.edbplus.db.web.shiro;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShiroUser implements Serializable {

    // 账号
    private String userAccount;
    // 用户昵称
    private String userName;
    // 用户ID -- 主要是有时候需要用到 uuid，才用object替代，便于扩展
    private Object userId;


}
