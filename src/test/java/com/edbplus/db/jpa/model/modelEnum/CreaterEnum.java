package com.edbplus.db.jpa.model.modelEnum;

/**
 * @ClassName DeleteEnum
 * @Description: 删除枚举类
 * @Author 杨志佳
 * @Date 2021/5/6
 * @Version V1.0
 **/
public enum CreaterEnum {

    XiaoMing,
    ChenHong;

    public String getLabel() {
        switch (this) {
            case XiaoMing:
                return "小明";
            case ChenHong:
                return "陈红";
        }
        return super.toString();
    }


}
