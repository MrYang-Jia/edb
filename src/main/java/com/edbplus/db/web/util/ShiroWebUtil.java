/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.web.util;

import cn.hutool.json.JSONUtil;
import com.edbplus.db.web.shiro.ShiroUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * @program: coreframework-parent
 * @description: shiroWeb工具类
 * @author: 杨志佳
 * @create: 2020-12-18 08:50
 **/
@Slf4j
public class ShiroWebUtil {



    /**
     * 校验调用层是否是controller层
     */
    public static void checkController(){
        // =====================================  防御使用方式错误的方法 ===================================================
        // 调用栈
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        // 计算调用层出现的位置
        int perviousCt = 0;
        for(StackTraceElement stackTraceElement: stackTraceElements){
            if(stackTraceElement.getClassName().equals(OperationUtil.class.getName())){
                // 标记为在当前类里 -- 主要是排除第一次调用时，不是指向当前类，引用时会判断错误
                if(perviousCt == 0){
                    perviousCt++;
                }
            }
            // 当下一个调用方法层不是 loadJpa 方法的时候
            if(!stackTraceElement.getClassName().equals(OperationUtil.class.getName())){
                // 如果发现不是当前类
                if(perviousCt == 1){
                    String perviousClassName = stackTraceElement.getClassName();
                    // 不允许在 controller 之外的地方引用
                    if(perviousClassName.indexOf("Controller") == -1){
                        throw new RuntimeException("调用失败,必须在controller层引用;当前应用层为:" + perviousClassName);
                    }
                    // 跳出循环
                    break;
                }
            }
        }
        // ========================================================================================
    }


    /**
     * 兼容shiroWeb 和Gapi-token模式
     * 获取pc端和app端页面的 user 对象信息
     * @return
     */
    public static ShiroUser getShiroUser(){
        return getShiroUser(true);
    }

    /**
     * 获取shiroUser对象
     * @param ck -- 是否校验判断是当前层是否是controller层调用
     * @return
     */
    public static ShiroUser getShiroUser(boolean ck){
        if(ck){
            // 校验是否是controller
            checkController();
        }

        // 系统自定义绑定到shiro上的对象
        ShiroUser shiroUser = null;
        RequestAttributes getRequestAttributes = RequestContextHolder.getRequestAttributes();
        // 只要是http请求，就可以尝试捕获是否是shiro请求
        if(getRequestAttributes != null){
            String token = null;
            HttpServletRequest request =((ServletRequestAttributes)getRequestAttributes).getRequest();
            token = request.getHeader("token");
            // 如果是
            if(token !=null ){
                shiroUser = getShiroUser(token);
            }else{
                try {
//                    // 如果没有配置shiro的话，会出现未知异常
//                    Subject subject = SecurityUtils.getSubject();
//                    if(subject!=null){
//                        // 获取当前会话 -- 基于cookie等模式创建的会话
//                        Session session = subject.getSession();
//                        // 避免没有登录时没有获取到对应信息报错
//                        if(session != null ){
//                            // 获取权限信息
//                            Object objUser = session.getAttribute("shiroUser");
//                            if(objUser instanceof String){
//                                shiroUser = JSONUtil.toBean((String) objUser,ShiroUser.class);
//                            }else{
//                                shiroUser = (ShiroUser)session.getAttribute("shiroUser");
//                            }
//                        }
//                    }
                }catch (Throwable e){
                    // 无须做任何操作
                }
            }
        }
        return shiroUser;
    }

    /**
     * 通过 token 获取用户信息
     * @param token
     * @return
     */
    public static ShiroUser getShiroUser(String token){

        // 根基实际业务扩展shiro对象
        return null;
    }


}
