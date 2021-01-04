

## 说明
- 1、整合 springBoot 与 jfinalDb 的数据层操作，兼容 spring 和 jfinal 的事务
  - 1.1 围绕 DataSource 数据源对象和 jfinal 的配置类 com.jfinal.plugin.activerecord.Config和 spring事务相关的 DataSourceUtils.getConnection、ConnectionHolder、TransactionSynchronizationManager.getResource 类和方法做改造适配
- 2、同时支持 javax.persistence 系列的注解（部分） 
  - 2.1 @Table、 @Id 、 @Column 的注解进行扩展 (后续看情况继续扩展)
- 3、分页对象支持 org.springframework.data.domain.PageRequest 对象，便于扩展
- 4、基于 jfinal - Db 的相关方法进行扩展 （分别继承于 Db 和 DbPro 数据库操作对象）实现
- 5、设计思想(居左原则)：jpa类型的的参数引入，原则上基于现有的 Db or DbPro的左侧对象类型作为参数引入，保证 Db or DbPro 的原汁原味
  - 5.1 设计思想上的居左原则是基于原有的jfinal-db方法上进行改造，例如: EDb.use().find(Bean.class,id); 
  - 5.2 数据更新时，必须是有改变的字段数值才会更新到数据库，从而防并发更新导致数据错乱
- 6、部分改造的 Db、DbPro 方法做特殊说明
  - 6.1 基于 paginate 方法做统计总条数的扩展，也就是说，固定给予一个总记录集，不需要去数据库查，直接返回分页结果
     - 6.1.1 paginate 新增 spring 的 PageRequest 入参，替代分页参数 pageNum 和 pageSize 
     - 6.1.2 paginate 新增 EDbQuery 自定义参数对象，主要是解决jpa的动态查询需求
  - 6.2 基于 findFirst 方法，针对sql语句做 limit 2 包装，主要是因为程序员不小心犯错的可能性有，导致大表全表查询
  - 6.3 新增实现 findOnlyOne 方法，主要是为了实现 1对1 查询，包装从数据结构和查询的准确性，如果超过 1 条记录，则会抛错告知
  - 6.4 新增 insertValues 方法，因为大数据不支持 batchSave 模式，只支持 insert into tables values(...),(...),(...)
  - 6.5 新增 batchSaveRid 方法，batchSave 的扩展，带返回自增主键的方法 (目前只测试了mysql版本，其余待不断完善测试案例)
  - 6.6 使用 update(jpa) 对象方法时,只会默认更新非null值的赋值方式，如果需要更新部分字段，可以用 update(oldJpa,updateJpa) 的方式，或者 update(T.class,Map) 的方式指定更新，这两种方式都支持null值回填，推荐使用 update(T.class,Map) 方式，可以对照数据库说明书的流程字段准确进行数据更新  
  - 6.7 新增 findByIds 方法，可以传入id数组，ids字符串 + 自定义分隔符等方式实现居于主键的快捷查询
  - 6.8 增加类似 jpa 对象 or 全局监听的功能
     - 6.8.1 @EDbSave 可自定义 beforeSave、beforeSave 监听 , EDbListener.java 执行之后
     - 6.8.2 @EDbUpdate 可自定义 beforeSave、beforeSave 监听 , EDbListener.java 执行之后
     - 6.8.3 EDbListener.java 接口类，可自定义 beforeSave、beforeSave 监听，@EDbSave @EDbUpdate 执行之前
     - 6.8.4 @EDbRel 关系注解，可以实现基于当前对象的属性值映射，取到关联数据
     - 6.8.5 @EDbView 模板视图注解，将当前对象当成模板参数传递入指定的模板作为入参条件，获取返回对象信息
    - 其余请参考 jfinal 官网 Db 相关的用法 https://jfinal.com/doc/5-5
- 7、数据库目前只支持 mysql 、 pg 、oracle 的语法
  - 7.1 mysql 和 oracle 的部分扩展的sql语法只适配部分只做了表字段不区分大小写的适配 （待后续做接口适配器解决）
  - 7.2 pg 部分扩展的sql语法只适配了字段全小写的写法 （待后续做接口适配器解决）
- 8、查询对象适配规则
  - 8.1 如果Bean对象里包含 @Column 优先匹配符合该字段的数据（不区分大小写），其次字段属性的命名如果跟数据库查询出来的字段（转小写）后生成的驼峰式（或全小写的字段名）字段匹配则进行赋值 (例如：数据库字段为 age 或 a_ge 都可以匹配上 aGe age 等语法)，不满足时用 @Column 指定即可



# 📑 EDb使用介绍
## 开始

**maven 依赖**

```xml
<!-- spring事务 + jfinalDb + enjoy -->
<dependency>
    <groupId>com.github.mryang-jia</groupId>
    <artifactId>spring-jf-edb</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### JPA使用指南 javax.persistence 的注解配置讲解
- @Table -- 基于 JPA 对象必须在实体类名上添加1个 @Table 注解，并指明数据库表的实际名称
- @Id -- 基于 JPA 对象必须至少指定1个主键字段申明
- @Column -- 基于 JPA 对象，@Column 是申明该属性字段对应的数据库实际名称，如果没有申明，则不会对数据库记录产生任何变化

### 支持的springBoot 版本（已测的部分）
SpringBoot 1.x
SpringBoot 2.x


**基于Java的相关用例**

- [ 基于JPA对象的简单示例 增、删、改、查 ](docs/jpa/jpa.md)
- [ 基于JPA对象的 update 的特殊说明和示例  ](docs/jpa/jpa-update.md)
- [ 基于JPA对象的 insertValues - 大量数据的快速插入 ](docs/jpa/jpa-insertValues.md)
- [ 基于JPA对象的 batchSave - jdbc 批量提交的主要方式 ](docs/jpa/jpa-batchSave.md)
- [ 基于JPA对象的 分页查询 paginate查询案例 ](docs/jpa/jpa-paginate.md)
- [ 替代 mybatis or mybatis-plus 的查询方案 ](docs/jpa/jpa-enjoy.md)
- [ 实体对象 EDbQuery 动态查询示例  ](docs/jpa/jpa-edbQuery.md)
- [ 全局或局部 save/update 监听示例  ](docs/jpa/jpa-listener.md)
- [ springBoot 项目引入示例  ](docs/jpa/spring-config.md)
- [ 基于 @EDbRel 关系对象的应用案例 ](docs/jpa/jpa-edbrel.md)
- [ 基于 @EDbView 关系对象的应用案例 ](docs/jpa/jpa-edbview.md)

## 帮助文档



