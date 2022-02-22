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

import com.edbplus.db.proxy.jfinal.Callback;
import com.edbplus.db.proxy.jfinal.ProxyMethod;
import com.edbplus.db.proxy.jfinal.ProxyMethodCache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @ClassName EDbInvocation
 * @Description: edb接口触发器
 * @Author 杨志佳
 * @Date 2022/2/12
 * @Version V1.0
 **/
public class EDbInvocation {
    // 无入参的方法
    private static final Object[] NULL_ARGS = new Object[0];	// Prevent new Object[0] by jvm for args of method invoking
    private Object target; // 代理的对象
    private Method method; // 代理的方法
    private Object[] args; // 代理方法的入参
    private Callback callback; // 回调函数,如果是get的话，则返回父类的返回值
    private Object returnValue; // 调用get方法时，实际返回的参数

    public EDbInvocation(Object target, Long proxyMethodKey, Callback callback, Object... args) {
        this.target = target;
        ProxyMethod proxyMethod = ProxyMethodCache.get(proxyMethodKey);
        this.method = proxyMethod.getMethod();
        this.callback = callback;
        this.args = args;
    }

    public EDbInvocation(Object target, Long proxyMethodKey, Callback callback) {
        this(target, proxyMethodKey, callback, NULL_ARGS);
    }

    /**
     * 代理对象方法触发
     */
    public void invoke() {
        try {
            // todo:可以用来做代理时的方法触发器，目前暂时不考虑
            System.out.println("== 触发咯 =="+this.method.getName());
            returnValue = callback.call(args);//返回值的处理
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null) {t = e;}
            throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public Object getArg(int index) {
        if (index >= args.length)
            throw new ArrayIndexOutOfBoundsException();
        return args[index];
    }

    public void setArg(int index, Object value) {
        if (index >= args.length)
            throw new ArrayIndexOutOfBoundsException();
        args[index] = value;
    }

    public Object[] getArgs() {
        return args;
    }

    /**
     * Get the target object which be intercepted
     * <pre>
     * Example:
     * OrderService os = getTarget();
     * </pre>
     */
    public <T> T getTarget() {
        return (T)target;
    }



    /**
     * Get the return value of the target method
     */
    public <T> T getReturnValue() {
        return (T)returnValue;
    }

    /**
     * Set the return value of the target method
     */
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }



}
