package com.edbplus.cloud.jfinal.activerecord.db.jpa.proxy;

//import net.sf.cglib.proxy.Enhancer;
//import net.sf.cglib.proxy.MethodInterceptor;
//import net.sf.cglib.proxy.MethodProxy;


/**
 * @ClassName JpaProxy -- 废弃，该模式损耗内存较高，不合适大量使用
 * @Description: Jpa更新对象
 * @Author 杨志佳
 * @Date 2020/4/27
 * @Version V1.0
 **/
@Deprecated
//public class JpaProxy<T> implements MethodInterceptor {
    public class JpaProxy {
//
//    // 开始记录变更信息的开关
//    @Getter
//    @Setter
//    private boolean startChange = false;
//
//    // 更新对象集
//    @Getter
//    @Setter
//    private Map<String,Object> updateData = new HashMap();
//
//    // jpa 对象
//    @Getter
//    @Setter
//    private T jpa;
//
//    // jpa 类型
//    @Getter
//    private Class<T> jpaClass;
//
//
//    public void setJpaClass(T t){
//        this.jpaClass = (Class<T>) t.getClass();
//    }
//
//
//    /**
//     * 获取实例对象
//     * @param claxx
//     * @return
//     */
//    public <T> T getInstance(Class<T> claxx) {
//        Enhancer enhancer = new Enhancer();
//        enhancer.setSuperclass(claxx);
//        // 回调方法
//        enhancer.setCallback(this);
//        // 创建代理对象
//        return (T) enhancer.create();
//    }
//
//    /**
//     * 获取已存在的实例
//     * @param t
//     * @return
//     */
//    public static <T> JpaProxy load(T t) {
//        JpaProxy jpaProxy = new JpaProxy();
//        // 设置jpa的类型
//        jpaProxy.setJpaClass(t);
//        // 设置新的jpa对象
//        T t2 = (T) jpaProxy.getInstance(t.getClass());
//        // 赋予对象
//        jpaProxy.setJpa(t2);
//        // 赋值对象内容
////        BeanUtils.copyProperties(t,t2); // spring自带的copy性能较差
//        BeanUtil.copyProperties(t,t2,false);
//        // 设置主键
//        jpaProxy.setIdValue(t2);
//        // 打开记录变更属性的开关
//        jpaProxy.setStartChange(true);
//        // 创建代理对象
//        return jpaProxy;
//    }
//
//    /**
//     * 方法切入
//     * @param o
//     * @param method
//     * @param objects
//     * @param methodProxy
//     * @return
//     * @throws Throwable
//     */
//    @Override
//    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
//
//        // 触发原方法
//        Object  object = methodProxy.invokeSuper(o,objects);
//
//        // 记录属性变更的开关
//        if(startChange){
//            // 属性字段定义
//            String key = null;
//            // 返回对象本身
//            if(method.getName().contains("set")){
//                // 获取字段名
//                key =  method.getName().replace("set","");
//                // 首字母转小写即可
//                key = StrKit.firstCharToLowerCase(key);
//            }
//
//            // 只有赋完值后才去处理，不然获取不到属性值
//            if(objects!=null && objects.length == 1){
//                if(jpa!=null){
//                    // 获取字段
//                    Field field = ReflectUtil.getField(jpa.getClass(),key);
//                    // 对特殊字段的处理 -- 如果获取不到属性字段，则可能是 is 命名方法的字段名，会导致 set 方法不会默认添加set前缀，而是将首字母变成大写
//                    if(field == null){
//                        // 前缀加上is ，然后首字母转大写
//                        key = "is" + StrKit.firstCharToUpperCase(key);
//                        // 获取字段
//                        field = ReflectUtil.getField(jpa.getClass(),key);
//                    }
//                    // 获取字段上的column注解，便于获取到真是的 数据库字段名称
//                    Column column =  AnnotationUtil.getAnnotation(field, Column.class);
//                    //
//                    if(column != null){
//                        // 赋予数据字段变更的map对象
//                        updateData.put(column.name().toLowerCase(),ReflectUtil.getFieldValue(jpa,field));
//                    }
//                }
//            }
//
//        }
//
//        return object;
//    }
//
//
//    /**
//     * 获取IDkey
//     * @param t
//     * @param <T>
//     * @return
//     */
//    public <T> void setIdValue(T t){
//        List<FieldAndColumn> idCoumns = JpaAnnotationUtil.getIdFieldAndColumns(t.getClass());
//        for(FieldAndColumn idColumn:idCoumns){
//            // 赋予数据库主键字段名和字段值
//            updateData.put(idColumn.getColumn().name().toLowerCase(),ReflectUtil.getFieldValue(t,idColumn.getField()));
//        }
//    }
//
//    /**
//     * 反向被代理对象获取代理对象
//     * @param proxy
//     * @return
//     */
//    public static JpaProxy getCglibForJpaUpdate(Object proxy) {
//        // 因为被代理对象只有1层，所以这么获取即可
//        Field h = null;
//        JpaProxy jpaProxy = null;
//        try {
//            h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
//            h.setAccessible(true);
//            jpaProxy = (JpaProxy) h.get(proxy);
//        } catch (Throwable e) {
//            //e.printStackTrace();
//        }
//        return jpaProxy;
//    }

}
