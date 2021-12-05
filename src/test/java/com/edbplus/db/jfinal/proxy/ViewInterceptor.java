///**
// * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.edbplus.db.jfinal.proxy;
//
//import cn.hutool.core.util.ReflectUtil;
//import com.edbplus.db.EDbPro;
//import com.edbplus.db.dto.FieldAndView;
//import com.edbplus.db.jpa.JpaAnnotationUtil;
//import com.edbplus.db.util.EDbPageUtil;
//import com.edbplus.db.util.bean.EDbBeanUtil;
//import com.jfinal.aop.Interceptor;
//import com.jfinal.aop.Invocation;
//import com.jfinal.plugin.activerecord.Page;
//import com.jfinal.plugin.activerecord.SqlPara;
//import com.jfinal.proxy.Proxy;
//import net.sf.cglib.proxy.Enhancer;
//import net.sf.cglib.proxy.MethodProxy;
//
//import java.lang.reflect.Method;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.List;
//import java.util.Map;
//
///**
// * @ClassName ViewInterceptor
// * @Description: view 代理测试
// * @Author 杨志佳
// * @Date 2021/12/4
// * @Version V1.0
// **/
//public class ViewInterceptor implements Interceptor {
//
//    @Override
//    public void intercept(Invocation invocation) {
//        System.out.println(invocation.getMethodName());
//        System.out.println("被代理啦");
//    }
//
//}
