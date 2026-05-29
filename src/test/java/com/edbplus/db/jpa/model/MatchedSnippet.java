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
package com.edbplus.db.jpa.model;

import lombok.Data;

/**
 * 匹配片段 - 用于测试 @EDbStrToBean 的List转换
 */
@Data
public class MatchedSnippet {

    /**
     * 片段ID
     */
    private Integer snippetId;

    /**
     * 片段内容
     */
    private String content;

    /**
     * 匹配分数
     */
    private Double score;

    /**
     * 是否精确匹配
     */
    private Boolean exactMatch;

}
