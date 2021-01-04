package #(genClass.controllerPackageName);


import #(genClass.jpaPackageName).#(genClass.className);
import #(genClass.xlsPackageName).#(genClass.className)Xls;
import #(genClass.iservicePackageName).#(genClass.className)Service;
import com.edb.cloud.jfinal.activerecord.db.query.EDbQuery;
import com.edb.cloud.jfinal.activerecord.db.jpa.kit.JpaKit;
import com.edb.cloud.jfinal.activerecord.db.query.EDbFilterKit;
import com.jfinal.plugin.activerecord.Page;
import com.xzw.coreframework.corebase.web.apiresult.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import org.springframework.ui.ModelMap;
import cn.afterturn.easypoi.entity.vo.NormalExcelConstants;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.view.PoiBaseView;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @program: #(genClass.projectName)
 * @description:  #(genClass.className) Controller 层
 * @author: #(genClass.creater)
 * @create: #(nowdatetime)
 **/
@RestController
#if(genClass.modelName)
@RequestMapping("#(genClass.modelName)/#(genClass.smallClassName)Action")
#else
@RequestMapping("#(genClass.smallClassName)Action")
#end
public class #(genClass.className)Controller {

    @Autowired
    private #(genClass.className)Service #(genClass.smallClassName)Service;

    /**
     * 保存车辆信息
     * @param saveObj
     */
    @PostMapping("save")
    @Transactional(rollbackFor=Throwable.class)
    public ApiResult save(@RequestBody #(genClass.className) saveObj){
        #(genClass.smallClassName)Service.save(saveObj);
        return ApiResult.success("保存成功");
    }




    /**
     * 更新车辆信息
     * @param update -- 只提交变更的属性值，前端事先要处理，可以包装前端对象工具类解决
     */
    @PostMapping("update")
    @Transactional(rollbackFor=Throwable.class)
    public ApiResult update(@RequestBody Map<String, Object> update){
        // 更新对象 -- 一般前端是驼峰对象，需要转换成数据库对应的表字段，所以这里需要这么操作
        #(genClass.smallClassName)Service.update(JpaKit.toDbColumnMap(#(genClass.className).class,update));
        return ApiResult.success("更新成功");
    }

    /**
     * 保存车辆信息
     * @param id
     */
    @PostMapping("delete")
    @Transactional(rollbackFor=Throwable.class)
    public ApiResult delete(@RequestParam("id") long id){
        #(genClass.smallClassName)Service.deteteById(id);
        return ApiResult.success("删除成功");
    }

    /**
     * 获取对象信息
     * @param id
     * @return
     */
    @PostMapping("get")
    public ApiResult<#(genClass.className)> getVehicle(@RequestParam("id") long id) {
        return ApiResult.success( #(genClass.smallClassName)Service.findById(id));
    }


    /**
     * 查询列表
     * @param whereMap 查询条件
     * @return
     */
    @PostMapping("list")
    public ApiResult<#(genClass.className)> getVehicles(@RequestBody Map<String, Object> whereMap) {
        // 组合查询条件
        EDbQuery eDbQuery = EDbFilterKit.getQueryForFilter(#(genClass.className).class,whereMap);
        // 查询所有对象时的方法
        Page<#(genClass.className)> page =  #(genClass.smallClassName)Service.findByQueryParams(#(genClass.className).class,PageRequest.of(1,10),eDbQuery);
        // 设置返回的数据集
        ApiResult apiResult = ApiResult.success(page.getList());
        // 设置总页数
        apiResult.setCount(Long.valueOf(page.getTotalRow()));
        // 返回结果集
        return apiResult;
    }



    /**
     * 导出案例 -- 基于 easyPoi 注解的导出
     * @param map
     * @param request
     * @param response
     */
    @RequestMapping("load")
    public void downloadByPoiBaseView( @RequestParam(required = false) Map whereMap,ModelMap map, HttpServletRequest request,
                                      HttpServletResponse response) {
        // 设置查询条件 -- 这里需要指定jpa对象的类型
        EDbQuery eDbQuery = EDbFilterKit.getQueryForFilter(#(genClass.className).class,whereMap);
        // 查询所有对象时的方法 -- 这里指定带有 @Table 注解的对象类型，要与jpa的 @Table 注解一致
        Page<#(genClass.className)Xls> page =  #(genClass.smallClassName)Service.findByQueryParams(#(genClass.className)Xls.class,PageRequest.of(1,10),eDbQuery);
        //  设置表格标题、sheet名称、文件类型
        ExportParams params = new ExportParams("#(genClass.tableComment)", "#(genClass.tableComment)", ExcelType.XSSF);
        // 固定头两列
        params.setFreezeCol(2);
        // 导出的表格名称
        map.put(NormalExcelConstants.FILE_NAME, "#(genClass.tableComment)-"+ DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN) );
        // 数据列表
        map.put(NormalExcelConstants.DATA_LIST, page.getList());
        // 导出的列表对应的数据对象类型
        map.put(NormalExcelConstants.CLASS, #(genClass.className)Xls.class);
        // 表格基础信息项
        map.put(NormalExcelConstants.PARAMS, params);
        // 导出表格
        PoiBaseView.render(map, request, response, NormalExcelConstants.EASYPOI_EXCEL_VIEW);
    }


}
