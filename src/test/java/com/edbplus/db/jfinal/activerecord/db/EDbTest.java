package com.edbplus.db.jfinal.activerecord.db;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.VehicleType;
import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.jfinal.activerecord.db.vo.VehicleTypeVo;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import org.apache.lucene.util.RamUsageEstimator;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbTest
 * @Description: TODO
 * @Author 杨志佳
 * @Date 2020/10/14
 * @Version V1.0
 **/
public class EDbTest extends BaseTest {



    /**
     * 根据主键id返回对象的测试
     */
    @Test
    public void testJpaForFindById(){
        Long start = System.currentTimeMillis();
        VehicleType vehicleType = EDb.findById(VehicleType.class,100);
        System.out.println("原数值:" + JSONUtil.toJsonStr(vehicleType));
        System.out.println(System.currentTimeMillis() - start);
    }

    /**
     * jpa更新的测试案例
     */
    @Test
    public void testJpaUpdate(){
        Long start = System.currentTimeMillis();
        VehicleType vehicleType = EDb.findById(VehicleType.class,100);
        System.out.println("原数值:" + JSONUtil.toJsonStr(vehicleType));
        System.out.println(System.currentTimeMillis() - start);
        String updateStr = "_变更";
        if(vehicleType.getVehicleTypeName().contains(updateStr)){
            vehicleType.setVehicleTypeName(vehicleType.getVehicleTypeName().replaceAll(updateStr,""));
        }else{
            vehicleType.setVehicleTypeName(vehicleType.getVehicleTypeName() + updateStr);
        }

        // 如果该字段没有 column 注解，将会无法识别是哪个数据库字段，所以不会回填数据库，但是不会影响赋值操作
        // 如果有 column 注解，则可以赋予null值
//        vehicleType.setCreator(null);
        EDb.use().update(vehicleType);
        // 车辆类型
        vehicleType = EDb.use().findById(VehicleType.class,100);
        // 查看更变情况
        System.out.println("变更值:" +JSONUtil.toJsonStr(vehicleType));
        System.out.println(System.currentTimeMillis() - start);
    }

    /**
     * 不需要查询，直接更新数据库对象的测试
     */
    @Test
    public void testJpaForUpdate(){
        // 返回可操作的Jpa数据操作实体
        VehicleType vehicleType = new VehicleType();
        // 必须指定主键，否则不会执行相应想要更新的相关操作
        vehicleType.setVehicleTypeId(100);
//        vehicleType.setVehicleTypeName("罐车");
//        vehicleType.setCreator("小陈");
        vehicleType.setCreator(null);
        EDb.use().update(vehicleType);
    }

    /**
     * 通过sql返回对象列表的测试
     */
    @Test
    public void testFindJpaList(){
        List<VehicleType> result = EDb.use().find(VehicleType.class," select * from cr_vehicle_type limit 3 ");
        // 返回Jpa对象
        System.out.println(result);

        // 批量修改
        for(int i=0;i<result.size();i++){
            result.get(i).setCreator("创建人-"+i);
            // jpa对象的逐一更新
         //   EDb.use().update(result.get(i));
        }

        // jpa模式的批量更新，必须保证每条记录变更字段个数必须一致，否则会导致更新异常
        EDb.use().batchUpdate(VehicleType.class,result,1000);


    }

    /**
     * 替代 mybatis 的sql模板查询方案
     */
    @Test
    public void testFindFoyTpl(){
        // 获取返回 N 条记录的sql模板 -- 可指定不同的数据库使用不同的数据库模板
        SqlPara sqlPara = EDb.use().getSqlPara("test.findForLimit", 2);
        System.out.println(sqlPara.getSql());
        System.out.println(JSONUtil.toJsonStr(EDb.use().find(VehicleType.class,sqlPara)));
    }



    /**
     * 视图对象拷贝案例测试
     */
    @Test
    public void testCopyToVo(){
        // 数据对象
        VehicleType vehicleType = EDb.findById(VehicleType.class,100);
        //
        VehicleTypeVo vehicleTypeVo =new VehicleTypeVo();
        // 对象拷贝赋值
        BeanUtil.copyProperties(vehicleType,vehicleTypeVo);
        // 打印赋值情况
        System.out.println(JSONUtil.toJsonStr(vehicleTypeVo));
        // 更新vo 会报异常，以此检测不会较差感染
//        EDb.use().update(vehicleTypeVo);
    }

    /**
     * 保存测试
     */
    @Test
    public void testSave(){
        List<VehicleType> saveList = new ArrayList<>();
        // 数据对象
        VehicleType vehicleType = new VehicleType();
        vehicleType.setVehicleTypeName("测试类型-有ID");
        EDb.use().save(vehicleType);
        System.out.println("打印ID: " + vehicleType.getVehicleTypeId());


        // 批量保存测试，没有返回主键id，如果需要返回，必须for循环的模式才能获取
        vehicleType = new VehicleType();
        vehicleType.setVehicleTypeName("测试类型-无ID-1");
        saveList.add(vehicleType);
        vehicleType = new VehicleType();
        vehicleType.setVehicleTypeName("测试类型-无ID-2");
        saveList.add(vehicleType);
        EDb.use().batchSave(VehicleType.class,saveList,100);

    }

    /**
     * 批量插入 insertValuse
     */
    @Test
    public void testBatchInsert(){
        List<VehicleType> saveList = new ArrayList<>();
        VehicleType vehicleType =null;
        for(int i=0;i<10;i++){
            // 数据对象
            vehicleType = new VehicleType();
            vehicleType.setVehicleTypeName("测试类型-无ID-"+i);
            saveList.add(vehicleType);
        }
        // 批量插入 -- 以每批次插入3条数据位例子
        int count=EDb.use().insertValues(VehicleType.class,saveList,3);
        System.out.println(count);
    }


    @Test
    public void testPage(){
        //
        Page<VehicleType> page = EDb.use().paginate(VehicleType.class,1,5,"select * from cr_vehicle_type where VEHICLE_TYPE_ID=?",100);
        System.out.println("总记录数："+page.getTotalRow());
        System.out.println("返回条数："+page.getList().size());
        System.out.println(page.getList().get(0).getVehicleTypeName());

        page = EDb.use().paginate(VehicleType.class,1,5,"select * "," from cr_vehicle_type where VEHICLE_TYPE_ID=?",200);
        System.out.println(page.getList().get(0).getVehicleTypeName());

        // 给予固定总数算分页 --> 数据量比较大的时候非常有用
        page = EDb.use().paginate(VehicleType.class,1,5,100L,"select *  from cr_vehicle_type where VEHICLE_TYPE_ID=?",200);
        System.out.println("总记录数："+page.getTotalRow());
        System.out.println("返回条数："+page.getList().size());
        System.out.println(page.getList().get(0).getVehicleTypeName());

        // 根据分页对象的查询
        SqlPara sqlPara = new SqlPara();
        sqlPara.setSql("select *  from cr_vehicle_type where VEHICLE_TYPE_ID=?");
        sqlPara.addPara(200);

        page = EDb.use().paginate(VehicleType.class,1,5,sqlPara);
        System.out.println(page.getList().get(0).getVehicleTypeName());

        page = EDb.use().paginate(VehicleType.class,1,5,100,sqlPara);
        System.out.println("总记录数："+page.getTotalRow());
        System.out.println("返回条数："+page.getList().size());
        System.out.println(page.getList().get(0).getVehicleTypeName());
    }


    @Test
    public void deleteById(){

//        VehicleType vehicleType=new VehicleType();
//        // 如果没有id值则不会进行任何操作
//        vehicleType.setVehicleTypeId(247L);
//        EDb.use().deleteById(vehicleType);

        List<VehicleType> vehicleTypes = new ArrayList<>();
        VehicleType vehicleType = new VehicleType();
        // 如果没有id值则不会进行任何操作
//        vehicleType.setVehicleTypeId(295L);
        vehicleTypes.add(vehicleType);
        vehicleType=new VehicleType();
        // 如果没有id值则不会进行任何操作
        vehicleType.setVehicleTypeId(298);
        vehicleTypes.add(vehicleType);
        EDb.use().deleteByIds(vehicleTypes);
    }

    @Test
    public void deleteByIds(){
        List<Object> deleteIds = new ArrayList<>();
        deleteIds.add(294);
        deleteIds.add(293);
        EDb.use().deleteByIds(VehicleType.class,deleteIds);
    }


    /**
     * 根据id主键返回结果集
     */
    @Test
    public void findByIds(){

        List<Object> ids = new ArrayList<>();
        ids.add(100);
        ids.add(200);
        //
        System.out.println(JSONUtil.toJsonStr(EDb.use().findByIds(VehicleType.class,ids)));

    }

    @Test
    public void testFirst(){
        //
        EDb.use().findFirst(" select * from  cr_vehicle_type limit 2");
        // 第一次反射的构建会多耗时，所以必须先执行一次，再经过相应的计算，得出较为准确的执行耗时
        VehicleType vehicleType = EDb.findFirst(VehicleType.class," select * from  cr_vehicle_type ");

        for(int i=0;i<5;i++){
            long start = System.currentTimeMillis();
            // 获取第一条记录
            vehicleType = EDb.findFirst(VehicleType.class," select * from  cr_vehicle_type ");
            // 实际执行耗时统计
            System.out.println(System.currentTimeMillis() - start);
        }

        // 获取唯一记录抛错记录
        vehicleType = EDb.findOnlyOne(VehicleType.class," select * from  cr_vehicle_type ");
        System.out.println(JSONUtil.toJsonStr(vehicleType));
    }


    @Test
    public void testQuery(){
        //
        List<Object> list = new ArrayList<>();
        list.add(100);
        list.add(200);
        EDbQuery eDbQuery = new EDbQuery();
        // 假如只打印 VEHICLE_TYPE_ID,CREATOR 字段
        eDbQuery.fields(" VEHICLE_TYPE_ID,CREATOR ");
        // select * from xxx where 1 = 1 --> and 拼接1
        // 包含 VEHICLE_TYPE_ID 100 、 200 的数据
        eDbQuery.and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.gt, list));
        // 相当于拼接 --> and ( 拼接1   )
        list = new ArrayList<>();
        list.add(100);
        // 剔除 VEHICLE_TYPE_ID = 100 的数据
        eDbQuery.andCom().and(new EDbFilter("VEHICLE_TYPE_ID", EDbFilter.Operator.notIn, list));
        // 根据 VEHICLE_TYPE_ID 进行降序布局
        eDbQuery.orderDESC("VEHICLE_TYPE_ID");

        long start = System.currentTimeMillis();
        // 根据 query 封装，返回数据
        System.out.println(JSONUtil.toJsonStr(EDb.find(VehicleType.class,eDbQuery)));
        System.out.println(System.currentTimeMillis() - start);
        // 根据 query 封装，返回数据
        System.out.println(JSONUtil.toJsonStr(EDb.paginate(VehicleType.class,1,10,eDbQuery)));



    }


    /**
     * 测试内存代理消耗
     */
    @Test
    public void testProxyMem(){
        VehicleType vehicleType = new VehicleType();
        // 先初始化下项目的代码块，便于减少反射测试时首次运行时耗时偏高的问题
       // VehicleType vehicleTypeProxy = EDb.forUpdate(VehicleType.class);
        //计算指定对象本身在堆空间的大小，单位字节
        System.out.println(RamUsageEstimator.shallowSizeOf(vehicleType));
        //计算指定对象本身在堆空间的大小，单位字节 -- 原对象大了点点
//        System.out.println(RamUsageEstimator.shallowSizeOf(vehicleTypeProxy));
        System.out.println("================");
        //计算指定对象及其引用树上的所有对象的综合大小，单位字节
        System.out.println(RamUsageEstimator.sizeOf(vehicleType));
        //计算指定对象及其引用树上的所有对象的综合大小，单位字节 -- 相差2000倍的内存数值(cglib模式底下，后面修改成了 线程模式)，代理对象非常消耗内存，但是发现其实大部分消耗的依赖内存块是公用的 1.8MB(公摊部分，后续的设计可以大胆引入，不管纠结这点公摊部分的内存消耗) ，所以实际上，是可以使用动态代理来完成一些工作
//        System.out.println(RamUsageEstimator.sizeOf(vehicleTypeProxy));
        // 查询后，为了便于快速修改值后能直接做update操作，都统一做了反向代理，便于记录变更的数据字段
        List<VehicleType> vehicleTypeList = EDb.find(VehicleType.class," select * from  cr_vehicle_type limit 2");
        System.out.println(RamUsageEstimator.sizeOf(vehicleTypeList));
        vehicleTypeList = EDb.find(VehicleType.class," select * from  cr_vehicle_type limit 3");
        System.out.println(RamUsageEstimator.sizeOf(vehicleTypeList));
        vehicleTypeList = EDb.find(VehicleType.class," select * from  cr_vehicle_type limit 10");
        // 打印到这里的时候，发现内存变化相对比较稳定
        System.out.println(RamUsageEstimator.sizeOf(vehicleTypeList));


    }

    @Test
    public void test(){
        VehicleType vehicleType = EDb.findFirst(VehicleType.class,"select * from  cr_vehicle_type ");
        System.out.println(RamUsageEstimator.sizeOf(vehicleType));
        vehicleType.setCreator("改成了原对象");
        Page page = EDb.paginate(VehicleType.class,1,10," select * from  cr_vehicle_type");
        System.out.println(RamUsageEstimator.sizeOf(page));
    }

    @Test
    public void test2(){


        VehicleType vehicleType = EDb.findFirst(VehicleType.class,"select * from  cr_vehicle_type ");


        vehicleType.setModifier("修改人变更");
        vehicleType.setCreator(null);
        System.out.println(RamUsageEstimator.sizeOf(vehicleType));
        System.out.println(JSONUtil.toJsonStr(vehicleType));
       // System.out.println(RamUsageEstimator.sizeOf(JpaBuilder.threadLocal.get().get(vehicleType.getEdb_Uuid())));
        // 通过当前线程获取到对象的原始值
       // System.out.println("当前线程" + JSONUtil.toJsonStr(JpaBuilder.threadLocal.get().get(vehicleType.getEdb_Uuid())));

     //   Map<String,Object> data = JpaBuilder.contrastObjReturnColumnMap(JpaBuilder.threadLocal.get().get(vehicleType.getEdb_Uuid()),vehicleType);

       // System.out.println("变更: "+data);

//        VehicleType oVehicleType = (VehicleType) JpaBuilder.threadLocal.get().get(vehicleType);
        super.fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 赋予新的线程
                //JpaBuilder.threadLocal.get().put(oVehicleType,oVehicleType);
                // 这个例子是说明静态的线程对象，在新的线程里是不会存在的
       //         System.out.println("新的线程能否获取到:" + JSONUtil.toJsonStr(JpaBuilder.threadLocal.get().get(vehicleType.getEdb_Uuid())));
            }
        });



    }

    @Test
    public void test3(){
        for(int i=0;i<100;i++){
            // 每16次等待一下，因为设置了线程池数量为16，看下等待2秒后，再执行线程是否会释放资源
            if(i%16 == 0){
                try {
                    // 故意等待两秒，看下线程池的状况
                    Thread.sleep(2 * 1000L);
                }catch (Throwable e){

                }
            }


            super.fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    // 必须使用同一个事务组，才能获取到线程里的对象，否则会自动销毁
                    EDb.txInNewThread(Connection.TRANSACTION_SERIALIZABLE, () -> {
                            //System.out.println(Thread.currentThread().getId() + " -- " + Thread.currentThread().getStackTrace().length + "当前线程临时存储的对象个数："+ JpaBuilder.threadLocal.get().size());
                            // 线程查询
                            VehicleType vehicleType = EDb.findFirst(VehicleType.class,"select * from  cr_vehicle_type ");
                            // 这个例子是说明静态的线程对象，在新的线程里是不会存在的
                        //    System.out.println("新的线程能否获取到:" + JSONUtil.toJsonStr(JpaBuilder.threadLocal.get().get(vehicleType.getEdb_Uuid())));
//                            System.out.println("当前线程临时存储的对象个数："+ JpaBuilder.threadLocal.get().size());
                            return true;
                        }
                    );

                }
            });
        }
       try {
           Thread.sleep(20*1000L);
       }catch (Throwable e){

       }

    }


    @Test
    public void test4(){


        for(int i=0;i<10;i++) {
            super.fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    String url = "http://127.0.0.1:9010/fwjk/test/fwjkTaskAction/findDtPageData";
                    Map<String, Object> map = new HashMap<>();//存放参数
                    map.put("page", 1);
                    map.put("limit", 20);
//发送post请求并接收响应数据
                    String result = HttpUtil.createPost(url).form(map).execute().body();
                    System.out.println("耗时:"+(System.currentTimeMillis() - start));
                }
            });
        }

        try {
            Thread.sleep(20*1000L);
        }catch (Throwable e){

        }

    }



}
