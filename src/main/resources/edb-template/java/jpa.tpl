package #(genClass.entityPackageName);

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import #(genClass.entityPackageName).base.Base#(genClass.entityClassName);

/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.tableComment) - 实体
 * @author: #(genClass.creater)
 * @create:#(nowdatetime)
 **/
@Table(name = "#(genClass.tableName)")
public class #(genClass.entityClassName) extends Base#(genClass.entityClassName)<#(genClass.entityClassName)>{

    // 实现自定义枚举

    // 实现基于该业务表的相关处理方法


}
