package #(genClass.iservicePackageName);

import com.edbplus.db.web.shiro.ShiroUser;
import #(genClass.entityPackageName).#(genClass.entityClassName);
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
     * @param save
     * @return
     */
    public boolean save(#(genClass.entityClassName) save,ShiroUser shiroUser);

    /**
     * 更新对象
     * @param update -- key 为数据库字段
     * @return
     */
    public boolean update(#(genClass.entityClassName) update,ShiroUser shiroUser);


    /**
     * 删除对象
     * @param id
     * @return
     */
    public boolean deteteById(#(genClass.priKeyJavaType) id,ShiroUser shiroUser);

    /**
     * 删除多个对象
     * @param idsArray
     * @return
     */
    public int[] deteteByIds( String[] idsArray ,ShiroUser shiroUser);

    /**
     * 查询对象
     * @param id
     * @return
     */
    public #(genClass.entityClassName) findById(#(genClass.priKeyJavaType) id);

    /**
     * 分页查询 -- 返回的是 jfinal 的page对象，与spring不是一个体系，主要是db底层以jfinal为主，这方面的只是体系有点冲突
     * @param tClass -- 传入带有 @Table 注解的class
     * @param pageRequest -- PageRequest.of(1,10) 的方式引入非常方便
     * @param eDbQuery
     * @return
     */
    public <T> Page<T> findByQueryParams(Class<T> tClass,PageRequest pageRequest, EDbQuery eDbQuery);

}
