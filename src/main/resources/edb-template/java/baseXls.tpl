package #(genClass.xlsPackageName);

import java.io.Serializable;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;
import java.math.BigDecimal;
import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelEntity;

/**
 * @program: #(genClass.projectName)
 * @description: #(genClass.tableComment) - 实体
 * @author: #(genClass.creater)
 * @create:#(nowdatetime)
 **/
@Data
public class #(genClass.className)Xls implements Serializable{

#for(x : fields)
#if("PRI".equals(x.columnKey))
   @Excel(name = "唯一编码")
   private #(x.columnType) #(x.columnCode);
#else
   /**字段说明:#(x.columnName)*/
   /**描述说明:#(x.columnComment)*/
   #if("Date".equals(x.columnType))
   @Excel(name = "#(x.columnComment)" ,width = 18 ,format = "yyyy/MM/dd HH:mm")
   #else
   @Excel(name = "#(x.columnComment)")
   #end
   private  #(x.columnType) #(x.columnCode);

 #end

#end

}
