package #(genClass.jpaPackageName);

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import #(genClass.entityPackageName).Base#(genClass.className);

/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.tableComment) - 实体
 * @author: #(genClass.creater)
 * @create:#(nowdatetime)
 **/
@Table(name = "#(genClass.tableName)")
public class #(genClass.className) extends Base#(genClass.className){

    // 实现自定义枚举

    // 实现基于该业务表的相关处理方法


}
