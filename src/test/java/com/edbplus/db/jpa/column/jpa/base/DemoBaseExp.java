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
package com.edbplus.db.jpa.column.jpa.base;

import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

/**
 * @ClassName DemoBase
 * @Description: //todo
 * @Author 杨志佳
 * @Date 2021/4/25
 * @Version V1.0
 **/
@Data
public class DemoBaseExp {
    /**字段说明:CREATOR*/
    /**描述说明:创建人*/
    @Column(name="CREATOR")
    private  String creator;

    /**字段说明:CREATE_TIME*/
    /**描述说明:创建时间*/
    @Column(name="CREATE_TIME")
    private Date createTime;
}
