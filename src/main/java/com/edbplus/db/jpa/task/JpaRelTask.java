/**
 * Copyright (c) 2021 , YangZhiJia 杨志佳 (edbplus@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edbplus.db.jpa.task;

import com.edbplus.db.EDbPro;
import com.edbplus.db.util.EDbPageUtil;
import com.edbplus.db.util.hutool.reflect.EReflectUtil;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class JpaRelTask implements Callable<Object> {

    private Class<?> jpaClass;
    private SqlPara sqlPara;
    private EDbPro eDbPro;
    private Object oriJpa;
    private Field field;
    private Integer pageNo;
    private Integer pageSize;

    // 0 - 单体对象，1 - List ,2 - jfinal_page,3 - spring_page
    private int arrayType = 0;

    /**
     *
     * @param oriJpa
     * @param oriJpaField
     * @param jpaClass
     * @param sqlPara
     * @param eDbPro
     * @param arrayType 0 - 单体对象，1 - List ,2 - jfinal_page,3 - spring_page
     * @param pageNo
     * @param pageSize
     */
    public JpaRelTask(Object oriJpa,Field oriJpaField,Class<?> jpaClass, SqlPara sqlPara, EDbPro eDbPro,int arrayType,Integer pageNo,Integer pageSize){
        this.jpaClass = jpaClass;
        this.sqlPara = sqlPara;
        this.eDbPro = eDbPro;
        this.oriJpa =oriJpa;
        this.field = oriJpaField;
        this.arrayType = arrayType;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    @Override
    public Object call() throws Exception {
        Object object = null;
        if(arrayType == 1){
            object = eDbPro.find(jpaClass, sqlPara);
        }else  if(arrayType == 0){
            object = eDbPro.findFirst(jpaClass, sqlPara);
        }else  if(arrayType == 2){
            object = eDbPro.paginate(jpaClass,pageNo,pageSize, sqlPara);
        }else  if(arrayType == 3){
            Page jfinalPage = eDbPro.paginate(jpaClass,pageNo,pageSize, sqlPara);
            object = EDbPageUtil.returnSpringPage(jfinalPage);
        }
        // 字段赋值 -- 反射赋值会比较消耗毫秒数
        EReflectUtil.setFieldValue(oriJpa, field, object);
        return object;
    }

}
