package #(genClass.servicePackageName);

import com.whbj.cloud.common.service.BaseServiceImpl;
import #(genClass.iservicePackageName).#(genClass.className)Service;
import #(genClass.jpaPackageName).#(genClass.className) ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edb.cloud.jfinal.activerecord.db.EDb;
import com.edb.cloud.jfinal.activerecord.db.EDbPro;


/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.className) service实现类
 * @author: #(genClass.creater)
 * @create: #(nowdatetime)
 **/
@Service
public class #(genClass.className)ServiceImpl extends BaseServiceImpl<#(genClass.className),Long> implements #(genClass.className)Service {

    // 根据情况切换注入的数据源
    @Autowired
    public void seteDbPro(EDbPro eDbPro) {
        // 默认封装的基本数据操作方案
        super.eDbPro = eDbPro;
    }

}
