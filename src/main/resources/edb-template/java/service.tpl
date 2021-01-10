package #(genClass.servicePackageName);

import #(genClass.iservicePackageName).#(genClass.className)Service;
import #(genClass.jpaPackageName).#(genClass.className) ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edbplus.db.EDb;
import com.edbplus.db.EDbPro;
import org.springframework.transaction.annotation.Transactional;
import com.edbplus.db.query.EDbQuery;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Map;

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
    public boolean save(#(genClass.className) saveObj){
        // 保存信息
        return eDbPro.save(saveObj);
    }

    /**
     * 查询对象
     * @param id
     * @return
     */
    public #(genClass.className) findById(Object id){
        // 根据id查询对象
        return eDbPro.findById(#(genClass.className).class,id);
    }


    /**
     * 更新对象
     * @param update -- key 为数据库字段
     * @return
     */
    @Transactional(rollbackFor=Throwable.class)
    public boolean update(Map<String,Object> update){
        // 更新信息
        return eDbPro.update(#(genClass.className).class,update);
    }


    /**
     * 删除对象
     * @param id
     * @return
     */
    @Transactional(rollbackFor=Throwable.class)
    public boolean deteteById(Object id){
        // 根据id删除一条记录
        return eDbPro.deleteById(#(genClass.className).class,id);
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
