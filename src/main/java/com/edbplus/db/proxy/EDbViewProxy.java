package com.edbplus.db.proxy;


import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDbPro;
import com.edbplus.db.dto.FieldAndView;
import com.edbplus.db.jpa.JpaAnnotationUtil;
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
 * @Description: view视图对象
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

    public void pageOf(int pageNo,int pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
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
//        System.out.println("before: " + method);
        //调用proxy.invoke()方法，会报java.lang.StackOverflowError错误，原因是invoke()内部会一直被反复调用
        //Object object = proxy.invoke(obj, args);
//        Object object = proxy.invokeSuper(obj, args);
        Object object = null;
        // 获取调用方法的返回对象
        Type returnType = method.getAnnotatedReturnType().getType();
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
                Map<String,Object> map = new HashMap<>();
                // 原来只转一层map的时候，bean对象无法使用  map[field] 的语法  //BeanUtil.beanToMap(oriJpa);
                // 将对象先转成json字符串，再转map对象，这样子对象里的对象，就都是map，否则enjoy解析器，基于bean的内部解析时，无法使用类似 map[field] 的语法
                map = JSONUtil.toBean(JSONUtil.toJsonStr(oriJpa),map.getClass());
                //
                SqlPara sqlPara = eDbPro.getSqlPara(fieldAndView.getEDbView().name(), map);
                if(sqlPara==null){
                    throw new RuntimeException("未加载sql视图:" + fieldAndView.getEDbView().name());
                }
                if(returnType instanceof ParameterizedType){
                    // 默认 jfinal page
                    entityClass = (Class<?>)((ParameterizedType) returnType).getActualTypeArguments()[0];
                    // com.jfinal.plugin.activerecord.Page -- 分页对象的情况
                    if(((ParameterizedType) returnType).getRawType() == Page.class){
                        object = eDbPro.paginate(entityClass,pageNo,pageSize,sqlPara);
                    }
                    // 适配spring分页
                    else if(((ParameterizedType) returnType).getRawType() == org.springframework.data.domain.Page.class){
                        Page jfinalPage = eDbPro.paginate(entityClass,pageNo,pageSize,sqlPara);
                        // spring 分页从0开始，所以默认-1
                        Pageable pageable = PageRequest.of(pageNo-1,pageSize);
                        //
                        org.springframework.data.domain.Page page = new org.springframework.data.domain.PageImpl(jfinalPage.getList(),pageable,jfinalPage.getTotalRow());
                        // 返回对象
                        object = page;
                    }
                    // 其他类型统一直接当作list返回
                    else{
                        //
                        object = eDbPro.find(entityClass,sqlPara);
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
            object = proxy.invokeSuper(obj, args);
        }
        return object;
    }



}
