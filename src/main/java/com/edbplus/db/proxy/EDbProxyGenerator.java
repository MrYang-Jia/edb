/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.proxy;

import com.edbplus.db.annotation.EDbView;
import com.edbplus.db.dto.FieldAndView;
import com.edbplus.db.proxy.jfinal.ProxyClass;
import com.edbplus.db.proxy.jfinal.ProxyMethod;
import com.edbplus.db.proxy.jfinal.ProxyMethodCache;
import com.edbplus.db.util.hutool.annotation.EAnnotationUtil;
import com.edbplus.db.util.hutool.reflect.EReflectUtil;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName EDbProxyGenerator
 * @Description:
 * @Author 杨志佳
 * @Date 2022/2/12
 * @Version V1.0
 **/
@Slf4j
public class EDbProxyGenerator {

    protected Engine engine = new Engine("forProxy").setToClassPathSourceFactory();
    protected Template template = engine.getTemplate("com/edbplus/db/proxy/jfinal/proxy_class_template.jf");

    protected boolean printGeneratedClassToConsole = false;

    public ProxyClass generate(Class<?> target) {
        ProxyClass proxyClass = new ProxyClass(target);

        Kv clazz = Kv.create();
        clazz.set("pkg", proxyClass.getPkg());
        clazz.set("name", proxyClass.getName());
        clazz.set("targetName", getTargetName(target));

        @SuppressWarnings("rawtypes")
        TypeVariable[] tvs = target.getTypeParameters();
        clazz.set("classTypeVars", getTypeVars(tvs));
        clazz.set("targetTypeVars", getTargetTypeVars(tvs));

//        List<Class<?>> methodUpperInters = getMethodUpperInterceptors(proxyClass);

        List<Kv> methodList = new ArrayList<>();
        clazz.set("methodList", methodList);
        Method[] methodArray = target.getMethods();
        for (Method m : methodArray) {
            if (isSkipMethod(m)) {
                continue ;
            }
            // 方法级拦截器
//            hasInterceptor(methodUpperInters, proxyClass, m);

            Kv method = Kv.create();
            method.set("methodTypeVars", getTypeVars(m.getTypeParameters()));
            method.set("returnType", getReturnType(m));
            method.set("name", m.getName());
            method.set("throws", getThrows(m));

            Parameter[] paras = m.getParameters();
            List<String> paraTypes = Arrays.asList(paras).stream().map(
                    x -> {
                        // 参考 JDK Parameter
                        StringBuilder sb = new StringBuilder();
                        Type type = x.getParameterizedType();
                        String typename = type.getTypeName();

                        if(x.isVarArgs()) {
                            sb.append(typename.replaceFirst("\\[\\]$", "..."));
                        } else {
                            sb.append(typename);
                        }

                        return sb.toString();
                    }
            )
                    .collect(Collectors.toList());
            method.set("paraTypes", paraTypes);

            // 缓存 ProxyMethod 的 key 值,会自增，随着代理类增加，这个key的值会一直增长，每个类对应的方法key编号都是固定的
            Long proxyMethodKey = ProxyMethodCache.generateKey();
            method.set("proxyMethodKey", proxyMethodKey);

            // 只有一个参数，且该参数是数组或者可变参数时传递 singleArrayPara = true
            if (paras.length == 1) {
                if (paras[0].getType().isArray() || paras[0].isVarArgs()) {
                    method.set("singleArrayPara", true);
                }
            }
            if (m.getReturnType() != void.class) {
                method.set("frontReturn", "return ");
            } else {
                method.set("backReturn", "return null;");
            }
            methodList.add(method); // 已存在于 clazz map对象里，所以只要跟踪 clazz 即可
            ProxyMethod proxyMethod = new ProxyMethod();
            proxyClass.addProxyMethod(proxyMethod);
            proxyMethod.setKey(proxyMethodKey);
            proxyMethod.setTargetClass(target);
            proxyMethod.setMethod(m);
        }

        if (proxyClass.needProxy()) {
            String sourceCode = template.renderToString(clazz);
            proxyClass.setSourceCode(sourceCode); // 赋予源码字符串

            if (printGeneratedClassToConsole) {
                String msg = "Generate proxy class \"" + proxyClass.getPkg() + "." + proxyClass.getName() + "\":";
                System.out.print(msg);
                System.out.println(sourceCode);
            }

            if (log.isDebugEnabled()) {
                String msg = "\nGenerate proxy class \"" + proxyClass.getPkg() + "." + proxyClass.getName() + "\":";
                log.debug(msg + sourceCode);
            }
        }

        return proxyClass;
    }

    protected void getFieldMethod(Class mClass){
        // 获取对象类上的所有字段
        Field[] fields = EReflectUtil.getFields(mClass);
        // 关系
        EDbView eDbView = null;
        //
        List<String> edbViews = new ArrayList<>();
        for( Field field : fields ){
            // 获取关联关系的注解对象
            eDbView =  EAnnotationUtil.getAnnotation(field, EDbView.class);
            if(eDbView != null){
                edbViews.add(field.getName());
            }
        }
    }

    /**
     * 支持对 static 类的代理
     */
    protected String getTargetName(Class<?> target) {
        if (Modifier.isStatic(target.getModifiers())) {
            // 无法兼容主类类名中包含字符 '$'，例如：com.xxx.My$Target&Inner
            // return target.getName().replace('$', '.');

            // 静态类的 getName() 值为 com.xxx.Target&Inner 需要将字符 '$' 替换成 '.'
            String ret = target.getName();
            int index = ret.lastIndexOf('$');
            return ret.substring(0, index) + "." + ret.substring(index + 1);
        } else {
            return target.getSimpleName();
        }
    }

    /**
     * 方法返回值为 int[] 时 method.getReturnType().getName() 返回值为: [I
     * 需要识别并转化
     */
    protected String getReturnType(Method method) {
        // return method.getReturnType().getName();
        // return method.getAnnotatedReturnType().getType().getTypeName();
        return method.getGenericReturnType().getTypeName();
    }

    /**
     * 获取子类泛型变量，也可用于获取方法泛型变量
     */
    @SuppressWarnings("rawtypes")
    protected String getTypeVars(TypeVariable[] typeVars) {
        if (typeVars == null|| typeVars.length == 0) {
            return null;
        }

        StringBuilder ret = new StringBuilder();

        ret.append('<');
        for (int i=0; i<typeVars.length; i++) {
            TypeVariable tv = typeVars[i];
            if (i > 0) {
                ret.append(", ");
            }

            ret.append(tv.getName());

            // T extends Map & List & Set
            Type[] bounds = tv.getBounds();
            if (bounds.length == 1) {
                if (bounds[0] != Object.class) {
                    ret.append(" extends ").append(bounds[0].getTypeName());
                    continue ;
                }
            } else {
                for (int j=0; j<bounds.length; j++) {
                    String tn = bounds[j].getTypeName();
                    if (j > 0) {
                        ret.append(" & ").append(tn);
                    } else {
                        ret.append(" extends ").append(tn);
                    }
                }
            }
        }

        return ret.append('>').toString();
    }

    /**
     * 获取父类泛型变量
     *
     * 相对于 getTypeVars(...) 取消了 TypeVariable.getBounds() 内容的生成，否则编译错误
     */
    @SuppressWarnings("rawtypes")
    protected String getTargetTypeVars(TypeVariable[] typeVars) {
        if (typeVars == null|| typeVars.length == 0) {
            return null;
        }

        StringBuilder ret = new StringBuilder();
        ret.append('<');
        for (int i=0; i<typeVars.length; i++) {
            TypeVariable tv = typeVars[i];
            if (i > 0) {
                ret.append(", ");
            }
            ret.append(tv.getName());
        }
        return ret.append('>').toString();
    }

    /**
     * 获取方法抛出的异常
     */
    protected String getThrows(Method method) {
        Class<?>[] throwTypes = method.getExceptionTypes();
        if (throwTypes == null || throwTypes.length == 0) {
            return null;
        }

        StringBuilder ret = new StringBuilder().append("throws ");
        for (int i=0; i<throwTypes.length; i++) {
            if (i > 0) {
                ret.append(", ");
            }
            ret.append(throwTypes[i].getName());
        }
        return ret.append(' ').toString();
    }

    /**
     * 跳过不能代理的方法
     * 1：非 public
     * 2：final、static、abstract
     * 3：方法名为：toString、hashCode、equals
     */
    protected boolean isSkipMethod(Method method) {
        int mod = method.getModifiers();
        if ( ! Modifier.isPublic(mod) ) {
            return true;
        }

        if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isAbstract(mod)) {
            return true;
        }

        String n = method.getName();
        if (n.equals("toString") || n.equals("hashCode") || n.equals("equals")) {
            return true;
        }

        return false;
    }

    /**
     * 获取 method 上层的拦截器，也即获取 global、class 这两层拦截器
     * 注意：global 层拦截器已结合 class 层 @Clear 注解处理过
     */
    protected List<Class<?>> getMethodUpperInterceptors(ProxyClass proxyClass) {
        List<Class<?>> ret = null;
//      Table clearOnClass = proxyClass.getTarget().getAnnotation(Table.class);

        return ret;
    }

    protected void removeInterceptor(List<Class<?>> target, Class<?>[] clearInters) {
        if (target.isEmpty() || clearInters.length == 0) {
            return ;
        }

        for (Iterator<Class<?>> it = target.iterator(); it.hasNext();) {
            Class<?> interClass = it.next();
            for (Class<?> c : clearInters) {
                if (c == interClass) {
                    it.remove();
                    break ;
                }
            }
        }
    }

    /**
     * 当前 method 是否存在有效拦截器
     * 1：如果存在 method 级拦截器，则 return true
     * 2：否则结合 method 级的 @Clear 考察 global、class 两层拦截器的留存
     *    global、class 两层拦截器已作为参数 methodUpperInters 被传入
     *    methodUpperInters 中的拦截器已结合 class 级 @Clear 处理过
     */
    protected boolean hasInterceptor(List<Class<?>> methodUpperInters, ProxyClass proxyClass, Method method) {

        List<Class<?>> ret=methodUpperInters;
//        EDbView eDbView = method.getAnnotation(EDbView.class);
//        if(method.getName().contains("CrVehicleTypeView"))
//        {
//            System.out.println("crVehicleTypeView");
//        }
//        if(eDbView!=null){
//            System.out.println("========eDbView=====");
//        }

        return ret != null && ret.size() > 0;
    }

    protected List<Class<?>> copyInterceptors(List<Class<?>> methodUpperInters) {
        List<Class<?>> ret = new ArrayList<>(methodUpperInters.size());
        for (Class<?> c : methodUpperInters) {
            ret.add(c);
        }
        return ret;
    }

    /**
     * 配置打印生成类到控制台
     */
    public void setPrintGeneratedClassToConsole(boolean printGeneratedClassToConsole) {
        this.printGeneratedClassToConsole = printGeneratedClassToConsole;
    }

    public void setProxyClassTemplate(String proxyClassTemplate) {
        template = engine.getTemplate(proxyClassTemplate);
    }
}
