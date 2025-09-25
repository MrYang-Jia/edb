package com.edbplus.db.jpa.vo;

import com.edbplus.db.query.EDbFilter;
import com.edbplus.db.query.EDbQuery;
import com.edbplus.db.query.EDbQueryUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class EDbFilterTest {
    @Test
    public void test(){
        EDbQuery lqw = new EDbQuery();
        lqw.and(
        EDbFilter.between(true,
                "r.create_time", "2025-09-21 11:00:00", "2025-09-21 15:00:00")
        );

        List<Object> paramsList = new ArrayList<>();
        String andSql = EDbQueryUtil.doWhereSql(lqw,paramsList);
        System.out.println(andSql);
        System.out.println(paramsList);
    }
}
