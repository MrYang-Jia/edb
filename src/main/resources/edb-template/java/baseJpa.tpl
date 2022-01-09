package #(genClass.entityPackageName).base;

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;
import java.math.BigDecimal;
import com.edbplus.db.EDbModel;

/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.tableComment) - 实体
 * @author: #(genClass.creater)
 * @create:#(nowdatetime)
 **/
@Data
@Table(name = "#(genClass.tableName)")
public class Base#(genClass.entityClassName) <M extends EDbModel> extends EDbModel<M>{

#for(x : fields)
#if("PRI".equals(x.columnKey))
   @Id
   @Column(name="#(x.columnName)")
   private #(x.columnType) #(x.columnCode);
#else
   /**字段说明:#(x.columnName)*/
   /**描述说明:#(x.columnComment)*/
   @Column(name="#(x.columnName)")
   private  #(x.columnType) #(x.columnCode);
 #end

#end

}
