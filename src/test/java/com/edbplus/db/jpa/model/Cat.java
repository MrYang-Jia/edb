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
package com.edbplus.db.jpa.model;

import com.edbplus.db.annotation.EDbRel;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @ClassName Cat
 * @Description: //todo
 * @Author 杨志佳
 * @Date 2024/2/28
 * @Version V1.0
 **/
@Data
@Table(name = "t_cat")
public class Cat {

    @Column(name="age")
    private Integer age;

    @Column(name="name")
    private String name;
}
