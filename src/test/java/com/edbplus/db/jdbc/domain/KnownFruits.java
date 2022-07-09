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
package com.edbplus.db.jdbc.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName KnownFruits
 * @Description: KnownFruits
 * @Author 杨志佳
 * @Date 2022/7/9
 * @Version V1.0
 **/
@Table(name="known_fruits")
public class KnownFruits {
    @Setter
    @Getter
    @Id
    @Column(name = "id")
    private int id;

    @Setter
    @Getter
    @Column(name = "name")
    private String name;
}
