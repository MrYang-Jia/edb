package com.edbplus.db.web.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: coreframework-parent
 * @description: API结果数据类
 * @author: 杨志佳
 * @create: 2021-01-12 11:27
 **/
@Data
public class ApiResult<T> implements Serializable {

    //代码
    private Integer code;
    //消息
    private String msg;
    //对象
    private T main;

    //总条数--分页条件此参数有值
    private Integer count;

    public static <T>  ApiResult<T> successMessage(String msg) {
        ApiResult<T> ret = ApiResult.success();
        ret.setMsg(msg);
        return ret;
    }

    public static <T> ApiResult<T> success() {
        ApiResult<T> ret = new ApiResult<T>();
        ret.setCode(ApiReturnCode.SUCCESS.getCode());
        ret.setMsg(ApiReturnCode.SUCCESS.getDesc());
        return ret;
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> ret = ApiResult.success();
        ret.setMain(data);
        ret.setCount(1);
        return ret;
    }



    public static <T> ApiResult<T> fail() {
        ApiResult<T> ret = new ApiResult<T>();
        ret.setCode(ApiReturnCode.FAIL.getCode());
        ret.setMsg(ApiReturnCode.FAIL.getDesc());
        return ret;
    }


    public static <T>  ApiResult<T> failMessage(String msg) {
        ApiResult<T> ret = ApiResult.fail();
        ret.setMsg(msg);
        return ret;
    }


}
