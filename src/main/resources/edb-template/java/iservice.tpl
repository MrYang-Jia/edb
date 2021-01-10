package #(genClass.iservicePackageName);

import #(genClass.jpaPackageName).#(genClass.className);
import com.edbplus.db.query.EDbQuery;
import com.jfinal.plugin.activerecord.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Map;
import java.io.Serializable;


/**
 * @program: #(genClass.projectName)
 * @description:  #(genClass.className) service接口
 * @author: #(genClass.creater)
 * @create: #(nowdatetime)
 **/
public interface #(genClass.className)Service  extends Serializable {

    /**
     * 保存对象并返回自增ID
     * @param saveObj
     * @return
     */
    public boolean save(#(genClass.className) saveObj);

    /**
     * 更新对象
     * @param update -- key 为数据库字段
     * @return
     */
    public boolean update(Map<String,Object> update);


    /**
     * 删除对象
     * @param id
     * @return
     */
    public boolean deteteById(Object id);

    /**
     * 查询对象
     * @param id
     * @return
     */
    public #(genClass.className) findById(Object id);

    /**
     * 分页查询 -- 返回的是 jfinal 的page对象，与spring不是一个体系，主要是db底层以jfinal为主，这方面的只是体系有点冲突
     * @param tClass -- 传入带有 @Table 注解的class
     * @param pageRequest -- PageRequest.of(1,10) 的方式引入非常方便
     * @param eDbQuery
     * @return
     */
    public <T> Page<T> findByQueryParams(Class<T> tClass,PageRequest pageRequest, EDbQuery eDbQuery);

}
