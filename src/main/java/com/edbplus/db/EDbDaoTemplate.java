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

import com.jfinal.plugin.activerecord.Model;
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
public class EDbDaoTemplate<M extends EDbModel> {
    protected EDbModel<M> dao;
    protected SqlPara sqlPara;

    public EDbDaoTemplate(EDbModel dao, String key, Map<?, ?> data) {
        this.dao = dao;
        this.sqlPara = dao.getSqlPara(key, data);
    }

    public EDbDaoTemplate(EDbModel dao, String key, Object... paras) {
        this.dao = dao;
        this.sqlPara = dao.getSqlPara(key, paras);
    }

    public EDbDaoTemplate(boolean byString, EDbModel dao, String content, Map<?, ?> data) {
        this.dao = dao;
        this.sqlPara = dao.getSqlParaByString(content, data);
    }

    public EDbDaoTemplate(boolean byString, EDbModel dao, String content, Object... paras) {
        this.dao = dao;
        this.sqlPara = dao.getSqlParaByString(content, paras);
    }


    public SqlPara getSqlPara() {
        return this.sqlPara;
    }

    public List<M> find() {
        return this.dao.find(this.sqlPara);
    }

    public M findFirst() {
        return this.dao.findFirst(this.sqlPara);
    }

    public M findOnlyOne() {
        return this.dao.findOnlyOne(this.sqlPara);
    }

    public Page<M> paginate(int pageNumber, int pageSize) {
        return this.dao.paginate(pageNumber, pageSize, this.sqlPara);
    }

    public Page<M> paginate(int pageNumber, int pageSize, boolean isGroupBySql) {
        return this.dao.paginate(pageNumber, pageSize, isGroupBySql, this.sqlPara);
    }

}