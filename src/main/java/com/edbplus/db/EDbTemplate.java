
package com.edbplus.db;

import com.jfinal.plugin.activerecord.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbTemplate
 * @Description: DbTemplate 的扩展对象
 * @Author 杨志佳
 * @Date 2021/11/1
 * @Version V1.0
 **/
public class EDbTemplate extends DbTemplate {

    protected EDbPro db;

    public EDbTemplate(EDbPro db, String key, Map<?, ?> data) {
        super(db,key,data);
        this.db = db; // 必须重新赋予当前对象的db，不然会导致找不到对象
        this.sqlPara = db.getSqlPara(key, data);
    }

    public EDbTemplate(EDbPro db, String key, Object[] paras) {
        super(db,key,paras);
        this.db = db; // 必须重新赋予当前对象的db，不然会导致找不到对象
        this.sqlPara = db.getSqlPara(key, paras);
    }

    public EDbTemplate(boolean b, EDbPro db, String content, Map data) {
        super(b,db,content,data);
        this.db = db; // 必须重新赋予当前对象的db，不然会导致找不到对象
        this.sqlPara = db.getSqlParaByString(content, data);
    }

    public EDbTemplate(boolean b, EDbPro db, String content, Object[] paras) {
        super(b,db,content,paras);
        this.db = db; // 必须重新赋予当前对象的db，不然会导致找不到对象
        this.sqlPara = db.getSqlParaByString(content, paras);
    }

    // 以下部分兼容 edb 的扩展模块
    /**
     * 返回sql对应的总记录数
     * @return
     */
    public Long sqlForCount(){
        return this.db.sqlForCount(this.sqlPara);
    }

    /**
     * 获取1条记录
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T findFirst(Class<T> tClass) {
        return this.db.findFirst(tClass,this.sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @return
     */
    public Record findOnlyOne(){
        return this.db.findOnlyOne(this.sqlPara);
    }

    /**
     * 获取唯一记录，超过1条则抛错
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T findOnlyOne(Class<T> tClass)
    {
        return this.db.findOnlyOne(tClass,this.sqlPara);
    }

    /**
     * 通过 sqlPara 语句返回对象实体
     * @param mClass
     * @param <M>
     * @return
     */
    public <M> List<M> find(Class<M> mClass){
        return this.db.find(mClass,this.sqlPara);
    }

    /**
     * 根据 预设的数据库总记录数 和 sqlPara查询对象，返回指定的对象分页列表
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param totalRow
     * @param <M>
     * @return
     */
    public <M> Page<M> paginate(Class<M> mClass, int pageNumber, int pageSize, long totalRow){
        return this.db.paginate(mClass,pageNumber,pageSize,totalRow,this.sqlPara);
    }

    /**
     * 根据 预设的数据库总记录数 和 sqlPara查询对象，返回指定的对象分页列表
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param <M>
     * @return
     */
    public <M> Page<M> paginate(Class<M> mClass, int pageNumber, int pageSize){
        return this.db.paginate(mClass,pageNumber,pageSize,this.sqlPara);
    }

    /**
     * 根据 sqlPara查询对象、是否分组sql,返回指定的对象分页列表
     * @param mClass
     * @param pageNumber
     * @param pageSize
     * @param isGroupBySql
     * @param <M>
     * @return
     */
    public <M> Page<M> paginate(Class<M> mClass,int pageNumber, int pageSize, boolean isGroupBySql)
    {
        return this.db.paginate(mClass,pageNumber,pageSize,isGroupBySql,this.sqlPara);
    }

}
