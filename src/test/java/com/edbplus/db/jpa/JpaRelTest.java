package com.edbplus.db.jpa;


import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.model.CrVehicleType;
import com.edbplus.db.jpa.model.CrVehicleTypeModeRel;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * EDbRel 注解关系对象应用
 */
public class JpaRelTest extends BaseTest {

    // 不能直接赋予对象，需要在beforeTest里执行
    EDbPro eDbPro = null;

    @BeforeTest
    public void initBefor(){
        eDbPro =  EDb.use();
//        eDbPro =  EDb.use("xzw");
        // 做一次查询连接，减少起始jdbc首次执行的耗时偏高问题
        eDbPro.findById(VehicleType.class,1);
    }

    /**
     * @EDbRel 常用情况的测试案例
     */
    @Test
    public void testAllRel(){
        CrVehicleTypeModeRel crVehicleTypeModeRel = eDbPro.findById(CrVehicleTypeModeRel.class,1);
        System.out.println("当前对象信息:"+JSONUtil.toJsonStr(crVehicleTypeModeRel));
        // 查询该对象的所有关联对象信息
        eDbPro.getAllRel(crVehicleTypeModeRel);
        System.out.println("当前对象信息已扩展关联对象信息:"+JSONUtil.toJsonStr(crVehicleTypeModeRel));
    }


    /**
     * 关系对象应用测试
     */
    @Test
    public void testReL(){
        long start = System.currentTimeMillis();
        // 查询id为100的对象数据
        CrVehicleType crVehicleType  = eDbPro.findById(CrVehicleType.class,100);
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        start = System.currentTimeMillis();
        // 通过已查询到的对象，关联查询子对象 CrVehicleTypeModeRels
        List<CrVehicleTypeModeRel> crVehicleTypeModeRels = eDbPro.getRel(crVehicleType).getCrVehicleTypeModesRel();
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        start = System.currentTimeMillis();
        System.out.println("==>查询到的关联子对象集: " + crVehicleTypeModeRels.size());
        // 同时原对象也已赋值，如果重新用上面的读取数据库的方法，会重新刷新数据，如果不需要刷新，则可直接用该数据做对应的业务用途
        System.out.println("==>父对象引用的子对象集：" + crVehicleType.getCrVehicleTypeModesRel().size());
        // 断言验证结果
        assert crVehicleTypeModeRels == crVehicleType.getCrVehicleTypeModesRel() : " 调用子对象返回失败！请检查是否存在逻辑漏洞 ";
        System.out.println("==>通过@EDbRel注解，使用 eDbPro.getRel 方法，调用子对象时返回的结果集回调正常：");
        System.out.println("==>当前子对象:"+crVehicleTypeModeRels.get(0).getCrVehicleTypeMode());
        start = System.currentTimeMillis();
        // 同时，也可单独对子对象做业务对象的扩展关联获取，这种模式适合在后台独立模块独立操作时使用，节省内存节省开销，逐渐释放不需要的资源，比较轻量
        System.out.println("==>关联获取子对象:"+eDbPro.getRel(crVehicleTypeModeRels.get(0)).getCrVehicleTypeMode());
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        start = System.currentTimeMillis();
        // 通过方法标记获取已删除标记的数据
        System.out.println("指向删除数据：" + eDbPro.getRel(crVehicleTypeModeRels.get(0)).getDelCrVehicleType());
        // 如果用这种方式获取已删除的对象，会出现错误的返回结果
        System.out.println("指向@EDbRel的key:"+eDbPro.getRelKey(crVehicleTypeModeRels.get(0),CrVehicleTypeModeRel.noDel));
        System.out.println("耗时:"+ (System.currentTimeMillis()-start) );
        start = System.currentTimeMillis();

        System.out.println("=== 不触发查询 ===");
        System.out.println(eDbPro.getRel(crVehicleTypeModeRels.get(0)).getIsDel());

    }

    /**
     * 异步获取关系类型的对象，如果需要循环批处理获取数据的时候，可以尝试这种方式，快速返回结果
     * 注意：事务无法保证在一个线程内，由于多线程执行时，事务是单独挂靠在线上上的，因为通讯模式的问题
     */
    @Test
    public void testRelFutrue(){

        // 查询指定id的数据集 A
        List<CrVehicleTypeModeRel> crVehicleTypeModeRels = eDbPro.findByIds(CrVehicleTypeModeRel.class,"1,2,3,4",",");
        // 查询指定id的数据集 B
        List<CrVehicleTypeModeRel> otherModelRels = eDbPro.findByIds(CrVehicleTypeModeRel.class,"5,6",",");
        // 异步结果集
        List<Future<Object>> resultFutrues = new ArrayList<>();
        // 模型关系
        for(CrVehicleTypeModeRel crVehicleTypeModeRel : crVehicleTypeModeRels){
            // 循环获取所有的列表信息 -- 使用异步的方式
            resultFutrues.addAll(eDbPro.getAllRelForFutrue(crVehicleTypeModeRel));
        }
        // 模型关系
        for(CrVehicleTypeModeRel crVehicleTypeModeRel : otherModelRels){
            // 循环获取所有的列表信息 -- 使用异步的方式
            // 只获取已删除的部分
            resultFutrues.addAll(eDbPro.getRelKeyForFutrue(crVehicleTypeModeRel,CrVehicleTypeModeRel.isDel));
        }
        // 如果是使用异步加载，则必须等待异步加载完成后，再获取对象值
        if(resultFutrues!=null && resultFutrues.size()>0){
            for(Future future : resultFutrues){
                try {
                    // 未返回结果，则会等待
                    if(future.get()!=null){
                        System.out.println(future.get().getClass().getName());
                    }else{
                        System.out.println("返回的结果集为null");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        assert  crVehicleTypeModeRels.size() > 0 : "异步获取数据失败！！！请检查是否是数据库本身没有数据，还是本身逻辑漏洞";
        //
        System.out.println(JSONUtil.toJsonStr(crVehicleTypeModeRels.get(1).getCrVehicleType()));
        System.out.println(JSONUtil.toJsonStr(crVehicleTypeModeRels.get(1).getCrVehicleTypeMode()));
        //
        assert  otherModelRels.size() > 0 : "异步获取数据失败！！！请检查是否是数据库本身没有数据，还是本身逻辑漏洞";
        System.out.println(JSONUtil.toJsonStr(otherModelRels.get(1).getDelCrVehicleType()));
    }

    @Test
    public void test(){
        long start = System.currentTimeMillis();
        String str = "and VEHICLE_TYPE_MODE_ID = #( VEHICLE_TYPE_MODE_ID ) #测试卡点) \n and VEHICLE_TYPE_ID = #(VEHICLE_TYPE_ID) and IS_DEL = 1 and (1=1) ";

//        List<String> results =  ReUtil.findAll("(#\\(){1}(.*?)(\\){1})",str,0);
        List<String> results =  ReUtil.findAll("#\\(([^#\\(]*)\\)",str,0);
        for(String regStr:results) {
            System.out.println("匹配："+regStr);

            // 对应字段
            System.out.println("变更:"+regStr.replaceAll("#\\(([^#\\(]*)\\)","$1").replaceAll(" ",""));
            //s.replaceAll("\\)","");
        }
        // 新sql语句
        System.out.println("输出："+str.replaceAll("#\\(([^#\\(]*)\\)","?"));


        System.out.println("耗时:"+(System.currentTimeMillis() - start));

        Template template = Engine.use().getTemplateByString(str);

        // 有提供赋值对象的方法，但是没有提取 #() 的参数对象方法....
        String result = template.renderToString(Kv.by("VEHICLE_TYPE_MODE_ID", 456));
        System.out.println(result);
    }



}
