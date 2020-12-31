### 关系扩展
#sql("EDbRel")
select
  #if(fields)
     #(fields)
  #else
     *
  #end
    from #(tableName) where 1=1
  ### 判断是否是map类型
  #if(JpaKit.isList(params))
    #for(param : params)
    and #(param) = ?
    #end
  #if(appendSql)
    #(appendSql)
  #end

  #if(limit)
     limit #(limit)
  #end

  #if(offset)
      offset #(offset)
  #end

  #end

#end


