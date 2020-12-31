package #(genClass.entityPackageName);

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;
import java.math.BigDecimal;
/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.tableComment) - Vo实体
 * @author: #(genClass.creater)
 * @create:#(nowdatetime)
 **/
@Data
public class #(genClass.className)Vo implements Serializable{

#for(x : fields)
   /**字段说明:#(x.columnName)*/
   /**描述说明:#(x.columnComment)*/
   private  #(x.columnType) #(x.columnCode);

#end

}
