package com.edb.cloud.jfinal.activerecord.db.query;


import cn.hutool.core.map.MapUtil;
import com.edb.cloud.jfinal.activerecord.db.jpa.kit.JpaKit;
import com.jfinal.kit.StrKit;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;


/**
 * @program: EDbFilter
 * @description: 筛选辅助工具类
 * @author: 杨志佳
 * @create: 2020-04-19 15:05
 **/
public class EDbFilterKit implements Serializable {

    // 自定义分隔符
    public static String splitStr = "_";
    // 自定义过滤前缀 - 对应到map的 key 值，例如  s_eq_name 对应的则是 过滤字段前缀_符号标识_驼峰字段名
    public static String filterPre = "s_";

    // =============================================================================
    // 等于 eq
    public static final String eq = "eq";
    // 不等于 ne
    public static final String ne = "ne";
    // 大于
    public static final String gt = "gt";
    // 大于等于
    public static final String ge = "ge";
    // 小于
    public static final String lt = "lt";
    // 小于等于
    public static final String le = "le";
    // 包含
    public static final String in = "in";
    // 不包含
    public static final String  notIn = "notIn";
    // is null
    public static final String isNull = "isNull";
    // is not null
    public static final String isNotNull = "isNotNull";
    // like 需要自己补充 % 的位置
    public static final String like = "like";
//    // like 'xxx%'
//    public static final String llk = "llk";
//    // like '%xxx'
//    public static final String rlk = "rlk";
//    // 正则表达式 REGEXP -- 情景比较复杂，暂放弃
//    public String reg = "reg";
    // =============================================================================



    /**
     * 通过map集合组合条件，返回查询对象
     * @param mClass
     * @param whereMap
     * @return
     */
    public static EDbQuery getQueryForFilter(Class<?> mClass, Map<String,Object> whereMap){
        //
        EDbQuery eDbQuery = new EDbQuery();
        // 获取驼峰与数据库字段映射集
        Map<String,String> jpaDbColumns  = JpaKit.getJpaDbColumns(mClass);
        // 分割单词
        String filterEc = "";
        // 字段名
        String fieldName = "";
        // 获取排序元素
        if(whereMap.containsKey("field")){
            // 排序字段
            String field = (String) whereMap.get("field");
            String order = "desc";
            // 判断是否有排序的模式字段
            if(whereMap.containsKey("order")){
                String newOrder = (String) whereMap.get("order");
                if(!StringUtils.isEmpty(newOrder)){
                    order = newOrder;
                }
            }
            // 添加排序字段
            if(order.toLowerCase().equals("desc")){
                eDbQuery.orderDESC(jpaDbColumns.get(field));
            }else{
                eDbQuery.orderASC(jpaDbColumns.get(field));
            }
        }


        // 获取当前组合元素
        for (Map.Entry<String, Object> mapData : whereMap.entrySet()) {
            // 拼接组合   and x1 and x2
            if (mapData.getKey().contains(EDbFilterKit.filterPre)){
                // 默认是 s_eq_驼峰字段名
                filterEc = mapData.getKey().split(EDbFilterKit.splitStr)[1];
                fieldName = mapData.getKey().split(EDbFilterKit.splitStr)[2];
                // 加载对象
                loadEDbQueryForAnd(eDbQuery,filterEc,fieldName,mapData.getValue(),jpaDbColumns);
            }
        }


        // andCom 不为 null 的情况
        if( whereMap.get("andCom") != null){
            //
            Map<String,Object> andCom = (Map<String, Object>) whereMap.get("andCom");
            //
            //andCom = JpaKit.toDbColumnMap(TraVehicleSource.class,andCom);
            // for循环匹配key值
            for (Map.Entry<String, Object> mapData : andCom.entrySet()) {
                // 拼接组合 and (   x1 and x2   )
                if (mapData.getKey().contains(EDbFilterKit.filterPre)){
                    // 默认是 s_eq_驼峰字段名
                    filterEc = mapData.getKey().split(EDbFilterKit.splitStr)[1];
                    fieldName = mapData.getKey().split(EDbFilterKit.splitStr)[2];
                    // 加载对象
                    loadEDbQueryForAnd(eDbQuery.andCom(),filterEc,fieldName,mapData.getValue(),jpaDbColumns);
                }
            }

        }

        // orCom 不为 null 的情况
        if( whereMap.get("orCom") != null){
            //
            Map<String,Object> orCom = (Map<String, Object>) whereMap.get("orCom");
            for (Map.Entry<String, Object> mapData : orCom.entrySet()) {
                // 拼接组合 or (   x1 and x2   )
                if (mapData.getKey().contains(EDbFilterKit.filterPre)) {
                    // 默认是 s_eq_驼峰字段名
                    filterEc = mapData.getKey().split(EDbFilterKit.splitStr)[1];
                    fieldName = mapData.getKey().split(EDbFilterKit.splitStr)[2];
                    // 加载对象
                    loadEDbQueryForAnd(eDbQuery.orCom(), filterEc, fieldName, mapData.getValue(), jpaDbColumns);
                }
            }
        }

        return eDbQuery;
    }

    /**
     * 拼接组合sql信息
     * @param baseQuery
     * @param filterEc
     * @param fieldName
     * @param value
     * @param jpaDbColumns
     */
    public static void loadEDbQueryForAnd(EDbBaseQuery baseQuery,String filterEc,String fieldName,Object value,Map<String,String> jpaDbColumns ){
        if(value!=null){
            switch(filterEc){
                // 相等
                case EDbFilterKit.eq :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.eq,value));
                    break;
                // 不相等
                case EDbFilterKit.ne :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.ne,value));
                    break;
                // 大于
                case EDbFilterKit.gt :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.gt,value));
                    break;
                // 大于等于
                case EDbFilterKit.ge :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.ge,value));
                    break;
                // 小于
                case EDbFilterKit.lt :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.lt,value));
                    break;
                // 小于等于
                case EDbFilterKit.le :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.le,value));
                    break;
                // 包含
                case EDbFilterKit.in :
                    //
                    if(value instanceof String){
                        // 将字符串转成list
                        baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.in, Arrays.asList(((String) value).split(","))));
                    }else{
                        baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.in,value));
                    }
                    break;
                // 不包含
                case EDbFilterKit.notIn :
                    //
                    if(value instanceof String){
                        // 将字符串转成list
                        baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.notIn, Arrays.asList(((String) value).split(","))));
                    }else{
                        baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.notIn,value));
                    }
                    break;
                // is null
                case EDbFilterKit.isNull :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.isNull,null));
                    break;
                // is not null
                case EDbFilterKit.isNotNull :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.isNotNull,null));
                    break;
                // like 相似
                case EDbFilterKit.like :
                    baseQuery.and(new EDbFilter(jpaDbColumns.get(fieldName), EDbFilter.Operator.like,value));
                    break;
                default :
                    //其他规则
            }
        }
    }

    /**
     * 移除空对象
     * @param baseQuery
     */
    public static void removeEmetyForQuery(EDbBaseQuery baseQuery){
        if(baseQuery.getAndEDbFilters()!=null){
            for(EDbFilter eDbFilter:baseQuery.getAndEDbFilters()){
                if (StringUtils.isEmpty(eDbFilter.getValue())){
                    baseQuery.getAndEDbFilters().remove(eDbFilter);
                }
            }
        }

    }

    /**
     * 去除空指针的对象
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> removeNullValue(Map<K, V> map) {
        if (MapUtil.isEmpty(map)) {
            return map;
        } else {
            Iterator iter = map.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<K, V> entry = (Map.Entry)iter.next();
                if (StrKit.isBlank(String.valueOf(entry.getValue()))) {
                    iter.remove();
                }
            }
            return map;
        }
    }

}

