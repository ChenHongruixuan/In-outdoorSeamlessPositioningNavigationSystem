package util.database;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 导出数据库中的数据
 */
public class DataOutput {
    private DatabaseOperation databaseOperation;

    public DataOutput(DatabaseOperation databaseOperation) {
        this.databaseOperation = databaseOperation;
    }

    /**
     * 从指定表中导出加速度数据
     *
     * @param tableName    加速度表的表名
     * @param absolutePath 输出文件的绝对路径
     */
    public void outputAccData(String tableName, String absolutePath) {
        // 创建sql语句
        String rssid;
        double accX, accY, accZ, accTotal;
        Connection connection = databaseOperation.getConnection();
        String sqlString = "SELECT * FROM " + tableName;
        // 编译sql语句
        // 执行查询
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
            FileWriter fw = new FileWriter(absolutePath);
            // 遍历结果
            while (rs.next()) {
                rssid = rs.getString("rssid");
                accX = rs.getFloat("X");
                accY = rs.getFloat("Y");
                accZ = rs.getFloat("level");
                fw.write(rssid + ",");
                fw.write(accX + ",");
                fw.write(accY + ",");
                fw.write(accZ + ",");
                fw.write("\r\n");
                fw.flush();
            }
            fw.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("查询失败: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("向文件中导出数据出错: " + e.getMessage());
        }
    }

    /**
     * 导出wifi指纹库数据
     *
     * @param tableName    wifi指纹库的表名
     * @param absolutePath 输出文件的绝对路径
     */
    public void outputWifiFinger(String tableName, String absolutePath) {
        float posX, posY;
        float[] level = new float[6];
        String sqlString = "SELECT * FROM " + tableName + " ORDER BY id";
        Connection connection = databaseOperation.getConnection();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
            FileWriter fw = new FileWriter(absolutePath);
            // 遍历结果
            while (rs.next()) {
                posX = rs.getFloat("x");
                posY = rs.getFloat("y");
                for (int i = 0; i < level.length; i++) {
                    level[i] = rs.getFloat("level" + (i + 1));
                }
                fw.write(posX + ",");
                fw.write(posY + ",");
                for (float aLevel : level) {
                    fw.write(aLevel + ",");
                }
                fw.write("\r\n");
                fw.flush();
            }
            fw.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("查询失败: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("向文件中导出数据出错: " + e.getMessage());
        }
    }
}
