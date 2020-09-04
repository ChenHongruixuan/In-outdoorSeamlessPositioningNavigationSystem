package util.database;

import java.sql.*;

/**
 * 类DataBaseOperation提供了数据库操作的一些方法，包括
 * 连接数据库，断开连接，创建表，删除表，插入记录，更新表等
 *
 * @author Qchrx
 * @version 1.2
 */
public class DatabaseOperation {

    private Connection connection;

    public DatabaseOperation(String driverName, String url, String user, String password) {
        connectDatabase(driverName, url, user, password);
    }

    /**
     * 连接数据库
     *
     * @param driverName 驱动名
     * @param url        统一资源标识符
     * @param user       数据库用户名
     * @param password   数据库密码
     */
    private void connectDatabase(String driverName, String url, String user, String password) {  // 连接数据库
        try {
            // 加载驱动
            Class.forName(driverName);
            // 连接数据库
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("数据库连接成功...");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("驱动创建失败...");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接失败...");
        }
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param member    表的列成员
     */
    public void createTable(String tableName, String member) {  // 创建表
        String sql = "CREATE TABLE IF NOT EXISTS  " + tableName + " ( " + member + " ); ";
        try {
            // 利用SQL语句创建表
            Statement statement = connection.createStatement();
            // 如果表不存在则创建表
            // 执行SQL语句
            statement.executeUpdate(sql);
            System.out.println("表" + tableName + "创建成功...");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表" + tableName + "创建失败...");
        }
    }

    /**
     * 利用sql语句直接创建表
     *
     * @param sqlString 创建表的SQL语句
     */
    public void createTable(String sqlString) {
        try {
            // 利用SQL语句创建表
            Statement statement = connection.createStatement();
            // 如果表不存在则创建表
            // 执行SQL语句
            statement.executeUpdate(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表创建失败...");
        }
    }

    /**
     * 向指定表内插入数据
     *
     * @param tableName 插入数据的表名
     * @param m         插入的类
     * @param <V>       泛型，适用于data包中覆盖了toString()方法的类
     */
    public <V> void updateTable(String tableName, V m) {
        try {
            Statement statement = connection.createStatement();
            // 利用SQL语句插入记录
            String sql = "INSERT INTO " + tableName + " VALUES " + "(" + m.toString() + ")";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("记录插入失败..." + e.getMessage());
        }
    }

    /**
     * 直接利用SQL语句插入记录
     *
     * @param insertStr 插入数据的SQL语句
     */
    public void updateTable(String insertStr) {
        try {
            Statement statement = connection.createStatement();
            // 利用SQL语句插入记录
            statement.executeUpdate(insertStr);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("记录插入失败..." + e.getMessage());
        }
    }

    /**
     * 删除表中全部记录
     *
     * @param tableName 表名
     * @throws SQLException 向上抛出SQL异常
     */
    public void deleteAllRecords(String tableName) throws SQLException {    // 删除表中全部记录
        Statement statement = connection.createStatement();
        // 利用SQL语句插入记录
        String sql = "DELETE FROM " + tableName;
        statement.executeUpdate(sql);
    }

    /**
     * 删除指定表
     *
     * @param tableName 表名
     * @throws SQLException 向上抛出SQL异常
     */
    public void deleteTable(String tableName) throws SQLException { // 删除表
        Statement statement = connection.createStatement();
        String sqlString = "DROP TABLE IF EXISTS " + tableName;
        statement.executeUpdate(sqlString);
    }

    /**
     * 断开与数据库的连接
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("断开连接失败...:" + e.getMessage());
        }
    }

    /**
     * 返回表中最后一条记录的ID，如果表内无记录，则返回0
     *
     * @param tableName 表名
     * @return 表中最后一条记录的id
     */
    public int checkTableLastID(String tableName) {
        int id = 0;
        String sql = "SELECT count(*) AS maxid FROM " + tableName;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                id = rs.getInt("maxid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("checkTableLastID error:" + e.getMessage());
        } finally {
            return id;
        }
    }

    /**
     * 方法返回类中持有的和数据库的连接
     *
     * @return 和数据库的连接
     */
    public Connection getConnection() {
        return connection;
    }
}