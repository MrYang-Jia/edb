package com.edbplus.db.jpa;

import com.alibaba.druid.DbType;
import com.edbplus.db.EDb;
import com.edbplus.db.jfinal.activerecord.db.base.BaseTest;
import com.edbplus.db.jpa.view.VehicleView;
import com.edbplus.db.jpa.vo.CrVehicleTypeVo;
import com.edbplus.db.proxy.EDbProxyFactory;
import com.edbplus.db.util.code.MapToCode;
import com.edbplus.db.util.code.SqlToCode;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.testng.annotations.Test;

import java.util.*;

public class VoTest extends BaseTest {


    @Test
    public void test6(){
        EDbProxyFactory eDbProxyFactory = new EDbProxyFactory();
//        EDbProxyGenerator eDbProxyGenerator = new EDbProxyGenerator();
        VehicleView vehicleView = eDbProxyFactory.get(VehicleView.class);
        System.out.println(vehicleView.getCrVehicleTypeView());
    }

//    @Test
//    public void test5(){
//        ClassLoader loader = VehicleView.class.getClassLoader();
//        Class<?>[] interfaces = new Class[] { VehicleView.class };
//
//        InvocationHandler h = new InvocationHandler() {
//            // proxyBuildColl是对ArrayList进行代理
//            ArrayList target = new ArrayList();
//
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                System.out.println(method.getName() + "执行之前...");
//                if (null != args) {
//                    System.out.println("方法的参数：" + Arrays.asList(args));
//                } else {
//                    System.out.println("方法的参数：" + null);
//                }
//                Object result = method.invoke(target, args);
//                System.out.println(method.getName() + "执行之后...");
//                return result;
//            }
//        };
//
//        Collection proxyBuildCollection2 = (Collection) Proxy.newProxyInstance(loader, interfaces, h);
//
//        proxyBuildCollection2.add("abc");
//        proxyBuildCollection2.size();
//        proxyBuildCollection2.clear();
//        proxyBuildCollection2.getClass().getName();
//    }

    /**
     * vo对象赋值测试
     */
    @Test
    public void test(){
       List<CrVehicleTypeVo> results = EDb.find(CrVehicleTypeVo.class,"select * from cr_vehicle_type where is_del = 1 limit 2");
       System.out.println("==>"+results);
       Page page = EDb.paginate(CrVehicleTypeVo.class,1,2,"select * from cr_vehicle_type ");
       System.out.println(page.getList());

//       System.out.println(EDb.findFirst("select * from cr_vehicle_type where creator='xj' "));

        CrVehicleTypeVo vehicleTypeVo = EDb.use().templateByString("select * from cr_vehicle_type where creator='创建人-1' ").findFirst(CrVehicleTypeVo.class);
        System.out.println("==>"+vehicleTypeVo);

        //CrVehicleTypeVo
    }

    @Test
    public void test2(){
        Arrays.asList("1,2".split(","))
                .forEach(obj -> {
                    System.out.println(obj);
                });

    }

    @Test
    public void test3(){
        int totaolCount = 3;
        int batchSize = 5;
        List<Integer> integers = new ArrayList<>();
        for (int j=0;j<100;j++){
            integers.add(j);
        }
        if(totaolCount>0 && batchSize>0){
            int t = 0;
            int ct = totaolCount/batchSize + 1;
            fj : for(int jt=0;jt<ct;jt++){
                for (int j=0;j<batchSize;j++){
                    if(t>=totaolCount){
                        System.out.println("一批次");
                        break fj;
                    }
                    System.out.println(integers.get(t));
                    t++;
                }
                //
                System.out.println("一批次");
            }
        }
    }

    /**
     * 根据返回的数据生成code
     */
    @Test
    public void createCode(){

        String sql = " SELECT\n" +
                "    tgs.GSID ,\n" +
                "    tgs.ID,\n" +
                "    tgs.NO,\n" +
                "    tgs.TYPE,\n" +
                "    tgs.STATUS,\n" +
                "    tgs.SOURCE_TYPE,\n" +
                "    tgs.SOURCE_ID,\n" +
                "    tgs.TRANSPORT_TYPE,\n" +
                "    tgs.GOODS_NAME,\n" +
                "    tgs.GOODS_BIG_TYPE_ID,\n" +
                "    tgs.GOODS_BIG_TYPE_ID_INT,\n" +
                "    tgs.GOODS_TYPE_ID,\n" +
                "    tgs.GOODS_TYPE_ID_INT,\n" +
                "    tgs.GOODS_TYPE_NAME,\n" +
                "    tgs.GOODS_PIECES,\n" +
                "    tgs.GOODS_UNIT_ID,\n" +
                "    tgs.GOODS_WEIGTH,\n" +
                "    tgs.GOODS_VOLUME,\n" +
                "    tgs.START_PROVINCE,\n" +
                "    tgs.START_CITY,\n" +
                "    tgs.START_DISTRICT,\n" +
                "    tgs.START_LOCATION,\n" +
                "    tgs.END_PROVINCE,\n" +
                "    tgs.END_CITY,\n" +
                "    tgs.END_DISTRICT,\n" +
                "    tgs.END_LOCATION,\n" +
                "    tgs.ARRIVAL_TIME,\n" +
                "    tgs.DISPATCH_TIME,\n" +
                "    tgs.RELEASE_DATE,\n" +
                "    tgs.RELEASE_TIME,\n" +
                "    tgs.BIDSNUMBER,\n" +
                "    tgs.MARGIN,\n" +
                "    tgs.ADD_VALUE,\n" +
                "    tgs.CONTACT,\n" +
                "    tgs.CONTACT_PHONE,\n" +
                "    tgs.ACCOUNT_ID,\n" +
                "    tgs.CREATETIME,\n" +
                "    tgs.UPDATETIME,\n" +
                "    tgs.UPDATEBY,\n" +
                "    tgs.ENABLED,\n" +
                "    tgs.DEAL_TIME,\n" +
                "    tgs.CREATEBY,\n" +
                "    tgs.LOAD_VEHICLE,\n" +
                "    tgs.USER_ID_INT,\n" +
                "    tgs.VEHICLE_TYPE_ID,\n" +
                "    tgs.USE_VC_TYPE,\n" +
                "    tgs.USE_VC_LENGTH,\n" +
                "    tgs.LOAD_SOURCE_CODE,\n" +
                "    (SELECT DEFINE_VALUE FROM SYS_DICT_DEFINE LSC WHERE LSC.DEFINE_CODE = tgs.LOAD_SOURCE_CODE AND LSC.TYPE_CODE = 'LOAD_SOURCE_CODE' ) AS loadSourceName,\n" +
                "    tgs.BUSINESS_STATUS,\n" +
                "    tgs.ARRIVAL_START_TIME,\n" +
                "    tgs.ARRIVAL_END_TIME,\n" +
                "    tgs.TRANSACTION_STATUS_ID,\n" +
                "    tgs.QUERY_DISPLAY_STATUS,\n" +
                "    (SELECT LOS_NAME FROM LOM_ORDER_STATUS WHERE LOS_ID = tgs.TRANSACTION_STATUS_ID) AS losName,\n" +
                "    (SELECT LOS_CODE FROM LOM_ORDER_STATUS WHERE LOS_ID = tgs.TRANSACTION_STATUS_ID) AS losCode,\n" +
                "    ( SELECT TRANSPORT_REMARK FROM TRA_GOODS_SOURCE_REMARK WHERE TRA_GOODS_SOURCE_REMARK.GSID = tgv.GSID)AS transportRemark,\n" +
                "    ( SELECT IFNULL( TRA_GVS_PRICE.MIN_PRICE, 0.00 ) FROM TRA_GVS_PRICE WHERE TRA_GVS_PRICE.BIZ_ID = tgv.GSID AND TRA_GVS_PRICE.BIZ_TYPE = '10' ) AS minPrice,\n" +
                "    ( SELECT IFNULL( TRA_GVS_PRICE.TOP_PRICE, 0.00 ) FROM TRA_GVS_PRICE WHERE TRA_GVS_PRICE.BIZ_ID = tgv.GSID AND TRA_GVS_PRICE.BIZ_TYPE = '10' ) AS topPrice,\n" +
                "    ( SELECT IFNULL( TRA_GVS_PRICE.SIN_PRICE, 0.00 ) FROM TRA_GVS_PRICE WHERE TRA_GVS_PRICE.BIZ_ID = tgv.GSID AND TRA_GVS_PRICE.BIZ_TYPE = '10' ) AS sinPrice,\n" +
                "    (SELECT PRICE_COMPANY FROM TRA_GVS_PRICE WHERE TRA_GVS_PRICE.BIZ_ID = tgv.GSID AND TRA_GVS_PRICE.BIZ_TYPE = '10')AS priceCompany,\n" +
                "  case tgv.GSID when 1 then 1 when 0 then 0 end testGsid, "+ // 测试用
                "    CASE (SELECT PRICE_COMPANY FROM TRA_GVS_PRICE WHERE TRA_GVS_PRICE.BIZ_ID = tgv.GSID AND TRA_GVS_PRICE.BIZ_TYPE = '10')\n" +
                "        WHEN '10'  THEN  '元/吨'\n" +
                "        WHEN '20'  THEN  '元/车'\n" +
                "        END priceCompanyName,\n" +
                "    ( SELECT AUTH_STATUS FROM SYS_ACCOUNT WHERE tgs.USER_ID_INT = SYS_ACCOUNT.USER_ID_INT)AS authStatus,\n" +
                "    ( SELECT GOODS_TYPE_NAME FROM SYS_GOODS_TYPE WHERE SYS_GOODS_TYPE.GTID = tgv.GOODS_TYPE_ID ) AS goodsBigTypeName,\n" +
                "    ( SELECT GOODS_TYPE_CODE FROM SYS_GOODS_TYPE WHERE SYS_GOODS_TYPE.GTID = tgv.GOODS_TYPE_ID ) AS goodsTypeCode,\n" +
                "    ( SELECT BASE_NUMBER FROM TRA_GVS_DETAIL_NUM WHERE TRA_GVS_DETAIL_NUM.BIZ_ID = tgv.GSID AND TRA_GVS_DETAIL_NUM.BIZ_TYPE = '10' limit 1)AS baseNumber,\n" +
                "    ( SELECT DETAIL_NUMBER FROM TRA_GVS_DETAIL_NUM WHERE TRA_GVS_DETAIL_NUM.BIZ_ID = tgv.GSID AND TRA_GVS_DETAIL_NUM.BIZ_TYPE = '10' limit 1)AS detailNumber,\n" +
                "    ( SELECT TRUE_NAME FROM SYS_USER WHERE  ID = (SELECT DEVELOPMENT_USER_ID FROM SYS_ACCOUNT WHERE USER_ID_INT = tgs.USER_ID_INT)) AS developmentName,\n" +
                "    ( SELECT ACCOUNT_TYPE FROM SYS_ACCOUNT WHERE tgs.USER_ID_INT = SYS_ACCOUNT.USER_ID_INT ) AS accountType,\n" +
                "    ( SELECT SYS_ATTACHMENT.ATTACHMENT_DEST_NAME FROM SYS_ATTACHMENT WHERE SYS_ATTACHMENT.id = (SELECT ATTACHMENT_ID FROM SYS_ACCOUNT_ATTACHMENT WHERE SYS_ACCOUNT_ATTACHMENT.USER_ID_INT = tgs.user_id_int AND SYS_ACCOUNT_ATTACHMENT.ATTACHMENT_TYPE = 6))AS attDestName,\n" +
                "    ( SELECT SYS_ATTACHMENT.ATTACHMENT_PATH FROM SYS_ATTACHMENT WHERE SYS_ATTACHMENT.id = (SELECT ATTACHMENT_ID FROM SYS_ACCOUNT_ATTACHMENT WHERE SYS_ACCOUNT_ATTACHMENT.USER_ID_INT = tgs.user_id_int AND SYS_ACCOUNT_ATTACHMENT.ATTACHMENT_TYPE = 6))AS attPath,\n" +
                "    ( SELECT TRA_GVS_DEFAULT_IMG_RELATION.IMG_URL FROM TRA_GVS_DEFAULT_IMG_RELATION WHERE TRA_GVS_DEFAULT_IMG_RELATION.PHONE = tgs.CONTACT_PHONE AND tgs.SOURCE_TYPE = 6 LIMIT 1) AS ossDefaultUrl,\n" +
                "    (SELECT IMG_PATH FROM PT_UC_HEAD_IMG WHERE PT_UC_HEAD_IMG.USER_ID = tgs.user_id_int LIMIT 1) AS ossHeadUrl,\n" +
                "    tgv.ROUTE_ID,\n" +
                "    (SELECT DISTANCE FROM cr_routes_source where cr_routes_source.ROUTE_ID = tgv.ROUTE_ID) AS distance,\n" +
                "        0 CALL_COUNT,\n" +
                "        0 VIEW_COUNT,\n" +
                "    tgs.SUITABLE_SOURCE,tgs.TANK_FUNCTION,\n" +
                "tgs_1.gsid2,\n " + // 测试用
                "    (SELECT string_agg(tt.TAG_NAME,','  ) tagNam\n" +
                "     FROM\n" +
                "         (\n" +
                "             SELECT\n" +
                "                 mrtl.TAG_NAME\n" +
                "             FROM\n" +
                "                 mkt_report_main mrm\n" +
                "                     LEFT JOIN mkt_report_main_tag_type_relation mrmttr ON mrm.RM_ID = mrmttr.RM_ID\n" +
                "                     LEFT JOIN mkt_report_tag_type_relation mrttr ON mrttr.RTTR_ID = mrmttr.RTTR_ID\n" +
                "                     LEFT JOIN mkt_report_tag_label mrtl ON mrtl.RTL_ID = mrttr.RTL_ID\n" +
                "             WHERE\n" +
                "                 mrm.REMOVE_FLAG = 'N'\n" +
                "               AND mrtl.REMOVE_FLAG = 'N'\n" +
                "               AND mrtl.TAG_NAME IS NOT NULL\n" +
                "               AND mrm.REPORTED_USER_ID = tgs.user_id_int\n" +
                "               AND mrm.REPORT_USER_ID = ?\n" +
                "               AND mrttr.TAG_TYPE = 'PEOPLE'\n" +
                "               AND mrttr.TAG_FEEDBACK_TYPE != '30'\n" +
                "             GROUP BY mrtl.RTL_ID\n" +
                "             ORDER BY count( mrtl.RTL_ID ) DESC,mrtl.sort asc\n" +
                "         ) as tt ) as label_names\n" +
                "FROM\n" +
                "    tra_goods_view AS tgv\n" +
                "    LEFT JOIN tra_goods_source tgs on tgv.GSID = tgs.GSID and tgs.ENABLED = 1\n" +
                "    LEFT JOIN tra_goods_source_1 tgs_1 on tgv.GSID = tgs_1.GSID and tgs_1.ENABLED = 1\n" +
                "\n" +
                "\n" +
                "\n" +
                "WHERE 1=1 AND tgv.STATUS = 'N' AND tgs.NO is not null\n" +
                "  and (tgv.BUSINESS_STATUS >= ? or ( tgv.BUSINESS_STATUS = 5 and tgs.user_id_int = ? ) )\n" +
                "\n" +
                "  and not EXISTS (\n" +
                "        select GSID from tra_goods_source where ENABLED = 1 and user_id_int = ? and SOURCE_TYPE = 6\n" +
                "                                            and gsid = tgv.GSID\n" +
                "    )\n" +
                "  and not EXISTS (\n" +
                "        select target_user_id from uc_user_blacklist where remove_flag = 'N' and user_id = ?\n" +
                "                                                       and tgs.user_id_int = target_user_id\n" +
                "    )\n" +
                "\n" +
                "        AND tgv.RELEASE_DATE >= ?\n" +
                "        AND tgv.RELEASE_TIME >= ?\n" +
                "        AND tgv.QUERY_DISPLAY_STATUS = ?\n" +
                "        AND tgv.GOODS_WEIGTH >= ?\n" +
                "        AND tgv.GOODS_WEIGTH <= ?\n" +
                "\n" +
                "    and not exists (\n" +
                "        select  blacklist.list_id from tra_gvs_blacklist blacklist\n" +
                "            where blacklist.phone = tgs.CONTACT_PHONE and blacklist.enabled = 1 and blacklist.biz_type = 20 and tgs.source_type = 6\n" +
                "    )\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "       ORDER BY tgv.RELEASE_TIME DESC\n" +
                "\n" +
                " limit 1 ";

        //
//        String javaCode = SqlToCode.javaCode(sql,EDb.use(),DbType.mysql);

        String javaCode = SqlToCode.javaCode(sql,"appGoodSourceVo",EDb.use("pg"),DbType.postgresql);
        System.out.println(javaCode);

//
//        SelectItems selectItems = SelectParser.getSelectNames(sql, DbType.mysql.name());
////        System.out.println(JSONUtil.toJsonPrettyStr(selectItems));
//        sql = GenMysql.getTableColumnsSql(SelectItems.splitSqlParams(selectItems.tables),SelectItems.splitSqlParams(selectItems.columns));
//        // 从数据库里获取所有表的字段，并回填到对应的结构体上
//        List<TableColumnInfo> list = EDb.find(TableColumnInfo.class,sql);
//        System.out.println(JSONUtil.toJsonPrettyStr(list));
//        Map<String,SelectItemInfo> selectItemInfoMap = null;
//        SelectItemInfo selectItemInfo = null;
//        for(TableColumnInfo tableColumnInfo:list){
//            selectItemInfoMap = selectItems.get(tableColumnInfo.getTableName().toLowerCase(Locale.ROOT));
//            if(selectItemInfoMap != null){ // 存在表
//                selectItemInfo = selectItemInfoMap.get(tableColumnInfo.getColumnName().toLowerCase(Locale.ROOT));
//                if(selectItemInfo !=null ){ // 存在字段
//                    selectItemInfo.setColumnType(tableColumnInfo.getDataType()); // 数据库的字段类型
//                    selectItemInfo.setJavaType(MysqlToJava.filedTypeMap.get(tableColumnInfo.getDataType())); // java 对用的数据类型
//                    selectItemInfo.setMaxL(tableColumnInfo.getMaxL()); // 字段的最大长度，如果没有的话为null ，存在 单精度和双精度两类长度，例如  11 或 11,2
//                    if(tableColumnInfo.getMaxL()!=null && tableColumnInfo.getMaxL().length()>0  ){
//                        if(tableColumnInfo.getMaxL().contains(",")){
//                            selectItemInfo.setJavaLength(Integer.valueOf(tableColumnInfo.getMaxL().split(",")[0])); // 例如 11,2 ，只需要获取左侧第一个位置即可
//                        }else{
//                            selectItemInfo.setJavaLength(Integer.valueOf(tableColumnInfo.getMaxL()));
//                        }
//                    }
//                }else{
//                    System.out.println("未匹配字段->"+tableColumnInfo.getTableName()+"."+tableColumnInfo.getColumnName());
//                }
//            }
//        }
//        System.out.println(JSONUtil.toJsonPrettyStr(selectItems));
//
//
//        String beanName = "appGoodSourceVo";
//        StringBuilder codeStr = new StringBuilder("\n");
//        codeStr.append("import java.util.Date;\n");
//        codeStr.append("import java.io.File;\n");
//        codeStr.append("import java.math.BigDecimal;\n");
//        codeStr.append("import io.quarkus.runtime.annotations.RegisterForReflection;\n");
//        codeStr.append("import lombok.Data;\n");
//        codeStr.append("import java.io.Serializable;\n");
//        codeStr.append("\n\n"); // 换行
//        codeStr.append("@RegisterForReflection\n");
//        codeStr.append("@Data\n");
//        codeStr.append("public class ").append(StrUtil.upperFirst(beanName)).append(" implements Serializable {\n");
//        // 迭代字段
//        selectItems.forEach((tableName, tableColumn) -> {
////            System.out.println(tableName + "：" + tableColumn);
//            // 第一层是表
//            // 第二层是表字段映射
//            tableColumn.forEach((column,item)->{
//                codeStr.append("\t").append("private ").append(item.getJavaType()).append(" ").append(item.getJavaCodeName()).append(";\n");
//            });
//        });
//        codeStr.append("}\n");
//
//        System.out.println(codeStr);
        //        System.out.println(GenMysql.getTableColumnsSql("cr_vehicle_type","vehicle_type_id"));
    }


    @Test
    public void codeTest(){
        String sql =" SELECT  ordinal_position ordinalPosition,  table_name tableName,  \tcolumn_name columnName,  \tCOLUMN_DEFAULT columnDefault,  \tdata_type dataType,  \tcolumn_comment columnComment,  \tcolumn_key columnKey,  \textra,  \t(  \t\tCASE  \t\tWHEN IS_NULLABLE = 'YES' THEN  \t\t\t'1'  \t\tELSE  \t\t\t'0'  \t\tEND  \t) isN,  \tSUBSTRING(  \t\tcolumn_type,  \t\tINSTR(column_type, '(') + 1,  \t\tINSTR(column_type, ')') - INSTR(column_type, '(') - 1  \t) maxL  FROM  \tinformation_schema. COLUMNS  WHERE   table_name = 'cr_vehicle_type'  \n" +
                " and column_name = 'vehicle_type_id'  AND table_schema = (SELECT DATABASE())  ORDER BY \n" +
                " table_name,ordinal_position ";

        Record record = EDb.use().findFirst(sql);
        MapToCode.toJavaCode(record.getColumns(),"TableColumnInfo");

    }

    @Test
    public void strTest(){
        String prePath ="sql";
        String sqlPre = prePath.split("/")[0];
        System.out.println(sqlPre);
    }

}
