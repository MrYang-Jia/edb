//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jfinal.plugin.activerecord;

import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.cache.EhCache;
import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.sql.SqlKit;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


/**
 * @ClassName Config
 * @Description: 基于spring事务管理改写，兼容 @Transactional 与 Db.use().tx(......) 类型事务混写
 * @Author 杨志佳
 * @Date 2020/10/13
 * @Version V1.0
 **/
public class Config {

    // 如果这里是静态的话，会导致线程公用同一个连接对象
    private final ThreadLocal<Connection> threadLocal ;

    String name;
    DataSource dataSource;
    Dialect dialect;
    boolean showSql;
    boolean devMode;
    int transactionLevel;
    IContainerFactory containerFactory;
    IDbProFactory dbProFactory;
    ICache cache;
    SqlKit sqlKit;

    Config(String name, DataSource dataSource, int transactionLevel) {
        this.threadLocal = new ThreadLocal();
        this.dbProFactory = IDbProFactory.defaultDbProFactory;
        this.init(name, dataSource, new MysqlDialect(), false, false, transactionLevel, IContainerFactory.defaultContainerFactory, new EhCache());
    }

    public Config(String name, DataSource dataSource, Dialect dialect, boolean showSql, boolean devMode, int transactionLevel, IContainerFactory containerFactory, ICache cache) {
        this.threadLocal = new ThreadLocal();
        this.dbProFactory = IDbProFactory.defaultDbProFactory;
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource can not be null");
        } else {
            this.init(name, dataSource, dialect, showSql, devMode, transactionLevel, containerFactory, cache);
        }
    }

    private void init(String name, DataSource dataSource, Dialect dialect, boolean showSql, boolean devMode, int transactionLevel, IContainerFactory containerFactory, ICache cache) {
        if (StrKit.isBlank(name)) {
            throw new IllegalArgumentException("Config name can not be blank");
        } else if (dialect == null) {
            throw new IllegalArgumentException("Dialect can not be null");
        } else if (containerFactory == null) {
            throw new IllegalArgumentException("ContainerFactory can not be null");
        } else if (cache == null) {
            throw new IllegalArgumentException("Cache can not be null");
        } else {
            this.name = name.trim();
            this.dataSource = dataSource;
            this.dialect = dialect;
            this.showSql = showSql;
            this.devMode = devMode;
            this.setTransactionLevel(transactionLevel);
            this.containerFactory = containerFactory;
            this.cache = cache;
            this.sqlKit = new SqlKit(this.name, this.devMode);
        }
    }

    public Config(String name, DataSource dataSource) {
        this(name, dataSource, new MysqlDialect());
    }

    public Config(String name, DataSource dataSource, Dialect dialect) {
        this(name, dataSource, dialect, false, false, 4, IContainerFactory.defaultContainerFactory, new EhCache());
    }

    private Config() {
        this.threadLocal = new ThreadLocal();
        this.dbProFactory = IDbProFactory.defaultDbProFactory;
    }

    void setDevMode(boolean devMode) {
        this.devMode = devMode;
        this.sqlKit.setDevMode(devMode);
    }

    void setTransactionLevel(int transactionLevel) {
        if (transactionLevel != 0 && transactionLevel != 1 && transactionLevel != 2 && transactionLevel != 4 && transactionLevel != 8) {
            throw new IllegalArgumentException("The transactionLevel only be 0, 1, 2, 4, 8");
        } else {
            this.transactionLevel = transactionLevel;
        }
    }

    static Config createBrokenConfig() {
        Config ret = new Config();
        ret.dialect = new MysqlDialect();
        ret.showSql = false;
        ret.devMode = false;
        ret.transactionLevel = 4;
        ret.containerFactory = IContainerFactory.defaultContainerFactory;
        ret.cache = new EhCache();
        return ret;
    }

    public String getName() {
        return this.name;
    }

    public SqlKit getSqlKit() {
        return this.sqlKit;
    }

    public Dialect getDialect() {
        return this.dialect;
    }

    public ICache getCache() {
        return this.cache;
    }

    public int getTransactionLevel() {
        return this.transactionLevel;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public IContainerFactory getContainerFactory() {
        return this.containerFactory;
    }

    public IDbProFactory getDbProFactory() {
        return this.dbProFactory;
    }

    public boolean isShowSql() {
        return this.showSql;
    }

    public boolean isDevMode() {
        return this.devMode;
    }

    /**
     * 设置当前线程连接
     * @param connection
     */
    public void setThreadLocalConnection(Connection connection) {
        this.threadLocal.set(connection);
        //System.out.println("当前线程2-1:"+Thread.currentThread().getId()+ "对象:" + this.threadLocal.get());
    }

    /**
     * 移除当前线程连接
     */
    public void removeThreadLocalConnection() {
        //System.out.println("当前线程2-3:"+Thread.currentThread().getId() + "准备释放对象:" + this.threadLocal.get());
        // 移除事务层
        this.threadLocal.remove();
    }


    /**
     * 获取连接
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        //System.out.println("当前线程2-2:"+Thread.currentThread().getId() + "对象:" + this.threadLocal.get());
        Connection conn = null;
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);

        // 不是spring的事务管理，则走原来的逻辑
        if(conHolder == null ){
            conn = (Connection)this.threadLocal.get();
            if (conn != null) {
                return conn;
            } else {
                return this.showSql ? (new SqlReporter(this.dataSource.getConnection())).getConnection() : this.dataSource.getConnection();
            }
        }else{
            //
            conn = (Connection)this.threadLocal.get();
            if (conn != null) {
                return conn;
            } else {
                // 通过spring获取连接对象
                conn = DataSourceUtils.getConnection(this.getDataSource());
            }
        }
        return  conn;
    }




    /**
     * 获取当前连接 -- 如果已经有事务的话，这里是会有对象的
     * @return
     */
    public Connection getThreadLocalConnection() {
        Connection connection =  threadLocal.get();
        if(connection != null){
            return connection;
        }
        // 如果是spring事务，则返回spring的事务对象，否则直接取 jfinal 当前线程的事务对象
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if(conHolder == null){
            return threadLocal.get();
        }else{
            // 获取spring的事务对象
            return DataSourceUtils.getConnection(this.getDataSource());
        }
    }

    /**
     * 是否在事务中
     * @return
     */
    public boolean isInTransaction() {
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        // 不是spring的事务管理，则走原来的逻辑
        if(conHolder == null){
            return threadLocal.get() != null;
        }
        return conHolder != null && (conHolder.getConnectionHandle()!=null || conHolder.isSynchronizedWithTransaction());
    }

    public void close(ResultSet rs, Statement st, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException var7) {
                LogKit.error(var7.getMessage(), var7);
            }
        }

        if (st != null) {
            try {
                st.close();
            } catch (SQLException var6) {
                LogKit.error(var6.getMessage(), var6);
            }
        }

        // 关闭连接
        close(conn);
    }

    public void close(Statement st, Connection conn) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException var5) {
                LogKit.error(var5.getMessage(), var5);
            }
        }
        // 关闭连接
        close(conn);
    }

    /**
     * 改造关闭对象
     * @param conn
     */
    public void close(Connection conn) {
        // 是否在事务中
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        // spring事务则走这个方法 && !conHolder.isSynchronizedWithTransaction()
        if (conHolder != null ) {
            // 直接交给spring来关闭 -- 是否释放，在spring里会有判断
            DataSourceUtils.releaseConnection(conn, dataSource);
        }else{
            // 原 jfinal 事务走法
            if (threadLocal.get() == null){
                // 连接
                if (conn != null)
                {
                    try {
                        //conn.close();
                        DataSourceUtils.releaseConnection(conn, dataSource);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }



}
