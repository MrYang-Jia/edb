/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db;

import com.edbplus.db.druid.EDbSelectUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;

import java.util.List;
import java.util.Map;

/**
 * @ClassName EDbDaoTemplate
 * @Description: EDbDaoTemplate
 * @Author 杨志佳
 * @Date 2021/11/5
 * @Version V1.0
 **/
@JsonIgnoreType
// 因为方法名以getxxx开头，如果没有参数的话，会被当作是属性对象返回给前端，所以接下来方法名命名要注意不能以get开头
@JsonIgnoreProperties({"realJpaClass","dbPro", "tableName","columnsMap","relKey","relKeyForFutrue","allRel","allRelForFutrue","countSql"})
public class EDbDaoTemplate<M> {
    protected EDbDao<M> dao;
    protected SqlPara sqlPara;

    public EDbDaoTemplate(EDbDao dao, String key, Map<?, ?> data) {
        this.dao = dao;
        this.sqlPara = dao.sqlPara(key, data);
    }

    public EDbDaoTemplate(EDbDao dao, String key, Object... paras) {
        this.dao = dao;
        this.sqlPara = dao.sqlPara(key, paras);
    }

    public EDbDaoTemplate(boolean byString, EDbDao dao, String content, Map<?, ?> data) {
        this.dao = dao;
        this.sqlPara = dao.sqlParaByString(content, data);
    }

    public EDbDaoTemplate(boolean byString, EDbDao dao, String content, Object... paras) {
        this.dao = dao;
        this.sqlPara = dao.sqlParaByString(content, paras);
    }


    public SqlPara sqlPara() {
        return this.sqlPara;
    }

    public List<M> find() {
        return this.dao.find(this.sqlPara);
    }

    /**
     * 重置查询sql的返回条数
     * @param limit
     * @return
     */
    public List<M> find(int limit) {
        return this.dao.find(this.sqlPara,limit);
    }

    /**
     * 重置查询sql的返回条数和起始位
     * @param limit
     * @param offset
     * @return
     */
    public List<M> find(int limit,int offset) {
        return this.dao.find(this.sqlPara,limit,offset);
    }

    public M findFirst() {
        return this.dao.findFirst(this.sqlPara);
    }

    public M findOnlyOne() {
        return this.dao.findOnlyOne(this.sqlPara);
    }

    /**
     * 给定数据总长度的分页查询
     * @param pageNumber -- 当前页
     * @param pageSize -- 分页数量
     * @param totalRow -- 总记录数
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize,long totalRow) {
        return this.dao.paginate(pageNumber, pageSize,totalRow, this.sqlPara);
    }

    /**
     * 分页查询
     * @param pageNumber -- 当前页
     * @param pageSize  -- 分页数量
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize) {
        return this.dao.paginate(pageNumber, pageSize, this.sqlPara);
    }

    /**
     * 分页查询
     * @param pageNumber -- 当前页
     * @param pageSize -- 分页数量
     * @param isGroupBySql -- 是否groupBySql
     * @return
     */
    public Page<M> paginate(int pageNumber, int pageSize, boolean isGroupBySql) {
        return this.dao.paginate(pageNumber, pageSize, isGroupBySql, this.sqlPara);
    }

    /**
     * 通过 sqlPara 执行更新语句
     * @return
     */
    public int update() {
        return this.dao.update(this.sqlPara);
    }

}
