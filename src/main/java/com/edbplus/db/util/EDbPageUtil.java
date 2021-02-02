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
package com.edbplus.db.util;

import com.jfinal.plugin.activerecord.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * EDbPage工具类
 */
public class EDbPageUtil {

    /**
     * 返回springPage
     * @param jfinalPage
     * @return
     */
    public static org.springframework.data.domain.Page returnSpringPage(Page jfinalPage){
        // spring 分页从0开始，所以默认-1
        Pageable pageable = PageRequest.of(jfinalPage.getPageNumber()-1,jfinalPage.getPageSize());
        //
        org.springframework.data.domain.Page page = new org.springframework.data.domain.PageImpl(jfinalPage.getList(),pageable,jfinalPage.getTotalRow());
        return page;
    }

}
