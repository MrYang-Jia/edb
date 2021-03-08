package com.edbplus.db.jpa.validation;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.edbplus.db.annotation.EDbUpdate;
import com.edbplus.db.jpa.model.CrVehicleType;
import com.edbplus.db.jpa.model.CrVehicleTypeModeRel;
import com.edbplus.db.jpa.util.EDbValidatorUtils;
import org.springframework.validation.FieldError;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ValidatorTest {

    /**
     *
     */
    @Test
    public void test(){
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount("1378848445");
//        sysUser.setUserPwd("123456");
//        sysUser.setUserName("测试账号");

        // 获取校验不合规的集合信息 -- 主要是返回给前端，通过返回的中文或英文提示信息，判断是什么不符合条件
        List<FieldError> validatorMessages = EDbValidatorUtils.validated(sysUser, EDbUpdate.class);
        if(validatorMessages.size()>0){
            // 打印不合规的信息
            System.out.println(validatorMessages);
        }

    }


    /**
     * 复制一个对象的方法，需要采用流拷贝的方式，不然对象之间是复用的
     */
    @Test
    public void testCopy(){
        List<SysUser> list = new ArrayList<>();
        SysUser sysUser = new SysUser();
        sysUser.setUserName("小陈");
        list.add(sysUser);
        //
        List<SysUser> list2 = new ArrayList<>();
        // 流拷贝list
        list2 = ObjectUtil.cloneByStream(list);
        // 流拷贝对象
//        SysUser sysUser1 = ObjectUtil.cloneByStream(sysUser);
        // 历史方式
        SysUser sysUser1 = new SysUser();
        BeanUtil.copyProperties(sysUser,sysUser1,false);
        sysUser.setUserName("小陈2");
        // 打印的结果是不会变化的
        System.out.println(list2);
        // 打印的结果是不会变化的
        System.out.println(sysUser1);

        // 错误的方式 -- 打印的结果会变化，这种称呼为 浅拷贝
        // 浅复制的方式，如果内部对象是对象的话，你就算独立new一个，内存指针还是不变，只能通过拷贝的方式来进行new 对象
//        list2 = CollectionUtil.newCopyOnWriteArrayList(list);
//        list2.addAll(list);
//        list2 = list.stream().collect(Collectors.toList());

    }

    @Test
    public void testCopyBean(){
        CrVehicleTypeModeRel crVehicleTypeModeRel = new CrVehicleTypeModeRel();
        crVehicleTypeModeRel.setCreator("主角");
        CrVehicleType crVehicleType = new CrVehicleType();
        crVehicleType.setVehicleTypeName("类型");
        crVehicleTypeModeRel.setCrVehicleType(crVehicleType);
    }

}
