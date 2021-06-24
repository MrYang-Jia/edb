package #(genClass.servicePackageName);

import com.edbplus.db.web.shiro.ShiroUser;
import com.edbplus.db.web.util.OperationUtil;
import #(genClass.iservicePackageName).#(genClass.className)Service;
import #(genClass.entityPackageName).#(genClass.entityClassName);
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edbplus.db.EDbPro;
import org.springframework.transaction.annotation.Transactional;
import com.edbplus.db.query.EDbQuery;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.className) service实现类
 * @author: #(genClass.creater)
 * @create: #(nowdatetime)
 **/
@Service
public class #(genClass.className)ServiceImpl  implements #(genClass.className)Service {

    // bean 的大小写与所对应的数据库一致，即可完成相应的多数据源切换
    @Autowired
    private EDbPro eDbPro;

    /**
     * 保存对象并返回自增ID
     * @param saveObj
     * @return
     */
    @Transactional(rollbackFor=Throwable.class)
    public boolean save(#(genClass.entityClassName) saveObj,ShiroUser shiroUser){
        // 加载操作人信息
        OperationUtil.loadJpa(saveObj,shiroUser);
        // 保存信息
        return eDbPro.save(saveObj);
    }

    /**
     * 查询对象
     * @param id
     * @return
     */
    public #(genClass.entityClassName) findById(#(genClass.priKeyJavaType) id){
        // 根据id查询对象
        return eDbPro.findById(#(genClass.className).class,id);
    }


    /**
     * 更新对象
     * @param update -- key 为数据库字段
     * @return
     */
    @Transactional(rollbackFor=Throwable.class)
    public boolean update(#(genClass.entityClassName) update,ShiroUser shiroUser){
        // 保存操作人信息
        OperationUtil.loadJpa(update,shiroUser);
        // 更新信息
        return eDbPro.update(update);
    }


    /**
     * 删除对象
     * @param id
     * @return
     */
    @Transactional(rollbackFor=Throwable.class)
    public boolean deteteById(#(genClass.priKeyJavaType) id,ShiroUser shiroUser){
    #for(x:fields)
    #### 判断是否有逻辑删除字段
        #if(x.columnCode.equals("removeFlag"))
            #set(isRemove = true)
        #end
    #end
    #if(isRemove)
        // 逻辑删除操作
        #(genClass.entityClassName) update = new #(genClass.entityClassName)();
        // 设置唯一主键
        update.set#(genClass.priKeyBigClassName)(id);
        // 将该记录标记为逻辑删除，并记录操作人信息
        OperationUtil.loadJpaDeletion(update,shiroUser);
        // 执行结果
        return eDbPro.update(update);
    #else
        // 根据id删除一条记录
        return eDbPro.deleteById(#(genClass.entityClassName).class,id);
    #end
    }


    /**
     * 删除对象
     * @param idsArray
     * @return
     */
    @Transactional(rollbackFor=Throwable.class)
    public int[] deteteByIds( String[] idsArray ,ShiroUser shiroUser){
    #for(x:fields)
    #### 判断是否有逻辑删除字段
        #if(x.columnCode.equals("removeFlag"))
            #set(isRemove = true)
        #end
    #end
    #if(isRemove)
    #(genClass.entityClassName) update = new #(genClass.entityClassName)();
    List<#(genClass.className)> list = new ArrayList<>();
    for(String id:idsArray){
        update = new #(genClass.entityClassName)();
        #if(genClass.priKeyJavaType.equals("String"))
        // 设置唯一主键
        update.set#(genClass.priKeyBigClassName)(id);
        #else
        // 设置唯一主键
        update.set#(genClass.priKeyBigClassName)(Long.valueOf(id));
        #end
        // 将该记录标记为逻辑删除，并记录操作人信息
        OperationUtil.loadJpaDeletion(update,shiroUser);
        list.add(update);
    }
    // 执行结果
    return eDbPro.batchUpdate(#(genClass.entityClassName).class,list,1000);
    #else
        return new int[]{eDbPro.deleteByIds(#(genClass.entityClassName).class, Arrays.asList(idsArray))};
    #end
    }

    /**
     * 分页查询 -- 返回的是 jfinal 的page对象，与spring不是一个体系，主要是db底层以jfinal为主，这方面的只是体系有点冲突
     * @param tClass -- 传入带有 @Table 注解的class
     * @param pageRequest -- PageRequest.of(1,10) 的方式引入非常方便
     * @param eDbQuery
     * @return
     */
    public <T> Page<T> findByQueryParams(Class<T> tClass,PageRequest pageRequest, EDbQuery eDbQuery){
        if(pageRequest == null){
            // 如果没有分页条件，则默认给予分页对象初始值
            pageRequest = PageRequest.of(1,10);
        }
        return eDbPro.paginate(tClass,pageRequest,eDbQuery);
    }

}
