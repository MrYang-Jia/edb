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
package com.edbplus.db.proxy;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDbPro;
import com.edbplus.db.dto.FieldAndView;
import com.edbplus.db.jpa.JpaAnnotationUtil;
import com.edbplus.db.util.EDbPageUtil;
import com.edbplus.db.util.bean.EDbBeanUtil;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import lombok.Setter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbViewProxy
 * @Description: view视图对象 ，由于 graalvm 无法支持cglib，所以放弃了该实现模式
 * @Author 杨志佳
 * @Date 2020/12/11
 * @Version V1.0
 **/
public class EDbViewProxy implements MethodInterceptor {


    //要代理的原始对象
    private Object oriJpa ;
    // 数据对象
    private EDbPro eDbPro;

    // 起始页
    private int pageNo = 1;

    // 每页的数量
    private int pageSize = 10;

    // 总记录数
    private Long totalRow = null;

    public void pageOf(int pageNo,int pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    /**
     * 设置成必须输入参数类型的
     * @param pageNo
     * @param pageSize
     * @param totalRow
     */
    public void pageOf(int pageNo,int pageSize,long totalRow){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalRow = totalRow;
    }


    // 1- 创建代理对象
    public <T> T createProcy(T target,EDbPro eDbPro){
        // 需要判断是否已经是代理对象，如果是的话，无须二次代理
//        System.out.println("需要被代理的类："+this.getClass().getSimpleName());
        // 赋予操作对象
        this.oriJpa = target;
        // 赋予数据库对象
        this.eDbPro = eDbPro;
        //1-Enhancer类是CGLib中的一个字节码增强器，它可以方便的对你想要处理的类进行扩展
        Enhancer enhancer=new Enhancer();
        //2-将被代理类HuGe设置成父类
        enhancer.setSuperclass(this.oriJpa.getClass());
        //3-设置拦截器
        enhancer.setCallback(this);
        //4-动态生成一个代理类
        Object objProxy = enhancer.create();
        return (T) objProxy;
    }

    //2-实现MethodInterceptor的intercept方法
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        // intercept(this,methodName,Object[] args)
//        System.out.println("before: " + method);
        //调用proxy.invoke()方法，会报java.lang.StackOverflowError错误，原因是invoke()内部会一直被反复调用
        //Object object = proxy.invoke(obj, args);
//        Object object = proxy.invokeSuper(obj, args);
        Object object = null;
        // 获取调用方法的返回对象
        Type returnType = method.getAnnotatedReturnType().getType();
        Map<String,Object> map = null;
        SqlPara sqlPara = null;
        Type packingType = null;
        Page jfinalPage = null;
        // 如果返回类型有值，才进行关系扩展
        if(returnType!= null ){
            // 获取所有字段 -- 包含
            List<FieldAndView> fieldViews = JpaAnnotationUtil.getFieldViews(oriJpa.getClass());
            Class<?> entityClass = null;
            for(FieldAndView fieldAndView :fieldViews) {
                // 排除掉非引用注解字段的get方法
                if (method != null && !method.getName().equals(JpaAnnotationUtil.getFieldReadMethod(fieldAndView.getField(), oriJpa.getClass()).getName())) {
                    continue;
                }
                // 将当前对象的所有属性转换成入参对象
                map = EDbBeanUtil.beanToMap(oriJpa);
                // 将对象先转成json字符串，再转map对象，这样子对象里的对象，就都是map，否则enjoy解析器，基于bean的内部解析时，无法使用类似 map[field] 的语法
//                map = JSONUtil.toBean(JSONUtil.toJsonStr(oriJpa),map.getClass());
                //
                sqlPara = eDbPro.getSqlPara(fieldAndView.getEDbView().name(), map);
                if(sqlPara==null){
                    throw new RuntimeException("未加载sql视图:" + fieldAndView.getEDbView().name());
                }
                if(returnType instanceof ParameterizedType){
                    // 默认 jfinal page
                    entityClass = (Class<?>)((ParameterizedType) returnType).getActualTypeArguments()[0];
                    // 包装类 -- 目前只支持 List jfinal-page spring-page 三种包装类型
                    packingType = ((ParameterizedType) returnType).getRawType();
                    // com.jfinal.plugin.activerecord.Page -- 分页对象的情况
                    if(packingType == Page.class){
                        if(totalRow != null){
                            object = eDbPro.paginate(entityClass,pageNo,pageSize,totalRow,sqlPara);
                        }else{
                            object = eDbPro.paginate(entityClass,pageNo,pageSize,sqlPara);
                        }
                    }
                    // 适配spring分页
                    else if(packingType == org.springframework.data.domain.Page.class){
                        if(totalRow != null){
                            jfinalPage = eDbPro.paginate(entityClass,pageNo,pageSize,totalRow,sqlPara);
                        }else{
                            jfinalPage = eDbPro.paginate(entityClass,pageNo,pageSize,sqlPara);
                        }
                        // 返回对象
                        object = EDbPageUtil.returnSpringPage(jfinalPage);
                    }
                    // 其他类型统一直接当作list返回
                    else if (packingType == List.class){
                        //
                        object = eDbPro.find(entityClass,sqlPara);
                    }else{
                        // 抛错
                        throw new RuntimeException(" view视图只支持单对象或 List 、jfinal-page、spring-data-page 三种数组类型的组合 ");
                    }
//                    //
//                    object = eDbPro.find(entityClass,sqlPara);
                }else{
                    entityClass = Class.forName(returnType.getTypeName());
                    object = eDbPro.findFirst(entityClass,sqlPara);
                }
                // 字段赋值 -- 反射赋值会比较消耗毫秒数
                ReflectUtil.setFieldValue(oriJpa, fieldAndView.getField(), object);
                // 跳出循环
                break;
            }
        }

        // 如果不是rel对象，则返回原属性方法值
        if(object == null){
//            object = proxy.invokeSuper(obj, args);
            // 触发原对象方法的返回结果
            object = method.invoke(oriJpa,args);
        }
        return object;
    }



}
