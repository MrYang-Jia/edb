package #(genClass.controllerPackageName);


import com.edbplus.db.web.api.ApiResult;
import com.edbplus.db.web.api.DataTablePageResult;
import com.edbplus.db.web.util.WebUtil;
import com.edbplus.db.web.util.ShiroWebUtil;
import #(genClass.entityPackageName).#(genClass.className);
import #(genClass.xlsPackageName).#(genClass.className)Xls;
import #(genClass.iservicePackageName).#(genClass.className)Service;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.jpa.kit.JpaKit;
import com.edbplus.db.query.EDbFilterKit;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import java.util.HashMap;
import org.springframework.ui.ModelMap;
import cn.afterturn.easypoi.entity.vo.NormalExcelConstants;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.view.PoiBaseView;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;


/**
 * @program: #(genClass.projectName)
 * @description:  #(genClass.className) Controller 层
 * @author: #(genClass.creater)
 * @create: #(nowdatetime)
 **/
@Controller
@RequestMapping("#(genClass.controllerWebUrl)Action")
@Slf4j
public class #(genClass.className)Controller {

    /**
    * 注入:#(genClass.tableComment)数据操作对象
    */
    @Autowired
    private #(genClass.className)Service #(genClass.smallClassName)Service;

    /**
     * 分页查询
     * @param request
     * @return
     */
    @RequestMapping(
            value = {"paginate"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public DataTablePageResult<#(genClass.className)> paginate(@RequestParam(required = false) Map whereMap,ModelMap map, HttpServletRequest request) {
        // 去除空指针对象
        EDbFilterKit.removeNullValue(whereMap);
        // 如果没有默认排序字段，则赋予主键字段进行排序
        if(!whereMap.containsKey(WebUtil.field)){
            whereMap.put(WebUtil.field,"#(genClass.priKeyClassName)");
        }
        int pageCt = 1;
        // 判断是否有分页对象
        if(whereMap.containsKey(WebUtil.page)){
            pageCt = Integer.valueOf((String) whereMap.get(WebUtil.page));
        }
        int limitCt = 10;
        // 判断是否有返回条数设置
        if(whereMap.containsKey(WebUtil.limit)) {
            limitCt = Integer.valueOf((String) whereMap.get(WebUtil.limit));
        }
        // 如果超过最大等级，则直接返回5条信息。
        if(pageCt > WebUtil.maxLimitLv_1){
            limitCt = 5;
        }
        // 设置只查询未删除的部分
        whereMap.put(WebUtil.sEqRemoveFlag,"N");
        // 组合查询条件
        EDbQuery eDbQuery = EDbFilterKit.getQueryForFilter(#(genClass.className).class,whereMap);
        // 查询所有对象时的方法
        Page<#(genClass.className)> page =  #(genClass.smallClassName)Service.findByQueryParams(#(genClass.className).class,PageRequest.of(pageCt,limitCt),eDbQuery);
        // 数据对象封装 -- 适配LayUI
        DataTablePageResult<#(genClass.className)> dataTablePageResult = new DataTablePageResult<>();
        // 设置返回的数据对象
        dataTablePageResult.setData(page.getList());
        // 设置成功的返回码 -- 适配LayUI
        dataTablePageResult.setCode(ApiReturnCode.SUCCESS.getCode());
        // 设置返回总条数
        dataTablePageResult.setCount(page.getTotalRow());
        return dataTablePageResult;
    }


    /**
     * 保存车辆信息
     * @param saveObj
     */
    @PostMapping("save")
    @ResponseBody
    @Transactional(rollbackFor=Throwable.class)
    public ApiResult<#(genClass.className)> save(@RequestBody #(genClass.className) saveObj){
        if(saveObj.get#(genClass.priKeyBigClassName)()!=null){
            #(genClass.smallClassName)Service.update(saveObj, ShiroWebUtil.getShiroUser());
        }else{
            #(genClass.smallClassName)Service.save(saveObj, ShiroWebUtil.getShiroUser());
        }
        return ApiResult.success(saveObj);
    }



    /**
     * 删除操作
     * @param keyid
     * @return
     */
    @RequestMapping(value="delete", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor=Throwable.class)
    public ApiResult<#(genClass.className)> delete(@RequestParam(value = "keyid",defaultValue = "") #(genClass.priKeyJavaType) keyid){
        #(genClass.smallClassName)Service.deteteById(keyid, ShiroWebUtil.getShiroUser());
        return ApiResult.successMessage("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "batchdelete", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor=Throwable.class)
    public ApiResult<#(genClass.className)> batchDelete(@RequestParam(value = "ids") String ids) {
        if (StringUtils.isNotEmpty(ids)) {
            String[] idsArrays = ids.split(",");
            #(genClass.smallClassName)Service.deteteByIds(idsArrays, ShiroWebUtil.getShiroUser());
            return ApiResult.successMessage("删除成功!");
        } else {
            return ApiResult.failMessage("异常参数,请检查!");
        }
    }

    /**
     * 获取对象信息 -- 实时获取时使用
     * @param id
     * @return
     */
    @PostMapping("get")
    @ResponseBody
    public ApiResult<#(genClass.className)> get(@RequestParam("id") #(genClass.priKeyJavaType) id) {
        return ApiResult.success( #(genClass.smallClassName)Service.findById(id));
    }




    /**
     * 导出案例 -- 基于 easyPoi 注解的导出
     * @param map
     * @param request
     * @param response
     */
    @RequestMapping("export")
    public void export( @RequestParam(required = false) Map whereMap,ModelMap map, HttpServletRequest request,
                                      HttpServletResponse response) {
        // 去除空指针对象
        EDbFilterKit.removeNullValue(whereMap);
        // 设置页码，可根据需要进行调整
        int pageCt = 1;
        // 设置最大返回条数 2000 条
        int limitCt = WebUtil.maxLimitLv_5;
        // 设置只查询未删除的部分
        whereMap.put(WebUtil.sEqRemoveFlag,"N");
        // 设置查询条件 -- 这里需要指定jpa对象的类型
        EDbQuery eDbQuery = EDbFilterKit.getQueryForFilter(#(genClass.className).class,whereMap);
        // 查询所有对象时的方法 -- 这里指定带有 @Table 注解的对象类型，要与jpa的 @Table 注解一致
        Page<#(genClass.className)Xls> page =  #(genClass.smallClassName)Service.findByQueryParams(#(genClass.className)Xls.class,PageRequest.of(pageCt,limitCt),eDbQuery);
        //  设置表格标题、sheet名称、文件类型
        ExportParams params = new ExportParams("#(genClass.tableComment)", "#(genClass.tableComment)", ExcelType.XSSF);
        // 固定头两列
        params.setFreezeCol(2);
        // 导出的表格名称
        map.put(NormalExcelConstants.FILE_NAME, "#(genClass.tableComment)-"+ DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN) );
        // 数据列表
        map.put(NormalExcelConstants.DATA_LIST, page.getList());
        // 导出的列表对应的数据对象类型
        map.put(NormalExcelConstants.CLASS,#(genClass.className)Xls.class);
        // 表格基础信息项
        map.put(NormalExcelConstants.PARAMS, params);
        // 导出表格
        PoiBaseView.render(map, request, response, NormalExcelConstants.EASYPOI_EXCEL_VIEW);
    }

}
