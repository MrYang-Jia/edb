package com.edbplus.db.proxy;


import com.edbplus.db.EDbPro;
import com.edbplus.db.jpa.util.JpaRelUtil;
import lombok.Setter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @ClassName EDbRelProxy
 * @Description: Jpa关系对象
 * @Author 杨志佳
 * @Date 2020/11/18
 * @Version V1.0
 **/
public class EDbRelProxy implements MethodInterceptor {

    public static String jpaEdbRelKey = "edb.EDbRel";

    //要代理的原始对象
    private Object oriJpa ;

    // 自定义字段
    @Setter
    private String fields;

    @Setter
    private Integer limit;

    @Setter
    private Integer offset;

    // 数据对象
    private EDbPro eDbPro;



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
            // 同步的方式，获取指定对象
            object = JpaRelUtil.getRelObject(null,fields,limit,offset,eDbPro,oriJpa,returnType.getTypeName(),method,true,false);
        }

//        returnType.getTypeName()
        // 如果不是rel对象，则返回原属性方法值
        if(object == null){
            object = proxy.invokeSuper(obj, args);
        }
        return object;
    }



}
