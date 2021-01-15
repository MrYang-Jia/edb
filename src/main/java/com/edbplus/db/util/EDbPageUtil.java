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
