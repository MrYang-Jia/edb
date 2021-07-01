package com.edbplus.db.jfinal.activerecord.db;


import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.CrVehicleType;
import com.edbplus.db.jpa.model.CrVehicleTypeModeRel;
import com.edbplus.db.proxy.EDbRelProxy;
import com.edbplus.db.util.hutool.json.EJSONUtil;
import com.edbplus.db.util.hutool.reflect.EReflectUtil;
import org.apache.lucene.util.RamUsageEstimator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ProxyTest
 * @Description: 代理类的相关测试
 * @Author 杨志佳
 * @Date 2020/10/15
 * @Version V1.0
 **/
public class ProxyTest extends BaseTest {

    // 不能直接赋予对象，需要在beforeTest里执行
    EDbPro eDbPro = null;

    @BeforeTest
    public void initBefor(){
        eDbPro =  EDb.use();
//        eDbPro =  EDb.use("xzw");
    }

//    /**
//     * cglib的模式 -- 目前已废弃
//     */
//    @Test
//    public void testJpaUpdate(){
//        VehicleType jpaVo = new VehicleType();
//        jpaVo.setVehicleTypeId(100);
//        jpaVo.setVehicleTypeName("类型100");
//        JpaProxy<VehicleType> jpaVoJpaProxy = JpaProxy.load(jpaVo);
//        jpaVo = jpaVoJpaProxy.getJpa();
//
//        System.out.println(jpaVo.getClass());
//
//        // CGLIB$$
//        Field[] fields = EReflectUtil.getFields(jpaVo.getClass());
//        for(Field field:fields){
//            System.out.println(field.getName());
//        }
//
//        try {
//
//            jpaVoJpaProxy = JpaProxy.getCglibForJpaUpdate(jpaVo);
//
//            System.out.println(jpaVoJpaProxy.getUpdateData());
//
//            jpaVo.setVehicleTypeName("类型变更");
//            System.out.println(jpaVoJpaProxy.getUpdateData());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



    @Test
    public void testJpaRelProxy(){

        long start =  System.currentTimeMillis();
        // 对象1
        CrVehicleTypeModeRel crVehicleTypeModeRel = new  CrVehicleTypeModeRel();
        crVehicleTypeModeRel.setVehicleTypeId(100);
        crVehicleTypeModeRel.setVehicleTypeModeId(1);
        EDbRelProxy EDbRelProxy = new EDbRelProxy();
        CrVehicleTypeModeRel crVehicleTypeModeRelProxy = EDbRelProxy.createProcy(crVehicleTypeModeRel,eDbPro);
        System.out.println("==>xxx"+crVehicleTypeModeRelProxy.getClass().getSimpleName().indexOf("$$Enhancer"));


        System.out.println("==>"+ EJSONUtil.toJsonStr(crVehicleTypeModeRelProxy.getCrVehicleType()));
        System.out.println("==>"+ EJSONUtil.toJsonStr(crVehicleTypeModeRel.getCrVehicleType()));
        System.out.println(System.currentTimeMillis() - start);
        start =  System.currentTimeMillis();

        CrVehicleType crVehicleType = new CrVehicleType();
        crVehicleType.setVehicleTypeId(100);
        EDbRelProxy crEDbRelProxy = new EDbRelProxy();
        CrVehicleType jpaCrVehicleType = crEDbRelProxy.createProcy(crVehicleType,eDbPro);

        System.out.println("==>"+ EJSONUtil.toJsonStr(jpaCrVehicleType.getCrVehicleTypeModesRel()));
        System.out.println("==>"+ EJSONUtil.toJsonStr(crVehicleType.getCrVehicleTypeModesRel()));

        System.out.println(System.currentTimeMillis() - start);
    }


    /**
     * 经过测试，发现 cglib 内存消耗有 1.8MB 是公摊部分，后续的设计将大胆引入，不会考虑内存损耗的问题 时间:2020-11-14
     */
    @Test
    public void JpaRelProxyMemoryTest(){

        List<Object> crVehicleTypeModeRelList = new ArrayList<>();
        List<Object> crVehicleTypeModeRelList2 = new ArrayList<>();
        // 不断加大
        for(int i=0;i<10000;i++){
            // 特意new一个
            EDbRelProxy EDbRelProxy = new EDbRelProxy();
            // 通过不同的大量对象来验证 cglib 通用模块是公用同一内存快的验证
            // 对象1
            CrVehicleTypeModeRel crVehicleTypeModeRel = new  CrVehicleTypeModeRel();
            CrVehicleTypeModeRel crVehicleTypeModeRelProxy = EDbRelProxy.createProcy(crVehicleTypeModeRel,eDbPro);
            crVehicleTypeModeRelProxy.setModifier("修改人");
            crVehicleTypeModeRelList.add(crVehicleTypeModeRelProxy);
        }



        for(int i=0;i<10000;i++){
            EDbRelProxy EDbRelProxy = new EDbRelProxy();
            // 对象2
            CrVehicleType crVehicleType = new CrVehicleType();
            CrVehicleType crVehicleTypeProxy =  EDbRelProxy.createProcy(crVehicleType,eDbPro);
            crVehicleTypeModeRelList2.add(crVehicleTypeProxy);
        }

        // $$Enhancer 这种都是有固定开销的统一消耗，所以一般要计算所有内存引用大小的时候，需要单独扣减掉这部分伪开销，然后最后再单独加上同一类代理公用的消耗内存部分，则为这次应用统一的内存消耗
        System.out.println(crVehicleTypeModeRelList.get(0).getClass().getSimpleName());
        // 占用引用 1.8 MB
        System.out.println(" 占用:"+ RamUsageEstimator.humanSizeOf(crVehicleTypeModeRelList.get(0)));

        // 引用的依赖占用
        System.out.println(" 占用:"+ RamUsageEstimator.humanSizeOf(crVehicleTypeModeRelList));

        // 占用引用 1.8 MB
        System.out.println(" 占用:"+ RamUsageEstimator.humanSizeOf(crVehicleTypeModeRelList2.get(0)));
        // 引用的依赖占用
        System.out.println(" 占用:"+ RamUsageEstimator.humanSizeOf(crVehicleTypeModeRelList2));

    }

    @Test
    public void test(){
        CrVehicleType crVehicleType = new CrVehicleType();
        Field[] fields = EReflectUtil.getFields(crVehicleType.getClass());
        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(fields[0].getName(), crVehicleType.getClass());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //获得get方法
        Method getMethod = pd.getReadMethod();
        System.out.println(getMethod.getName());
    }



}
