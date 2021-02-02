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
package com.edbplus.db.web.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: coreframework-parent
 * @description: 数据表格分页结果
 * @author: 杨志佳
 * @create: 2021-01-12 11:27
 **/
@Data
public class DataTablePageResult<T> extends ApiResult<T> implements Serializable {

    // 数据集
    private List<T> data;

}
