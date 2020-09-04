package util.database;

import olddata.WifiFingerBssidContainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * FingerGeneration类根据离线采集的磁场和wifi数据训练出室内的指纹库用于定位
 *
 * @author Qchrx
 * @version 1.0
 */
public class FingerGeneration {
    private DatabaseOperation databaseOperation;

    public FingerGeneration(DatabaseOperation databaseOperation) {
        this.databaseOperation = databaseOperation;
    }

    /**
     * 利用采集到的Wifi数据生成wifi指纹库
     * 生成的指纹库用于传统的定位算法，如：NN,K-NN算法
     *
     * @param wifiFingerBssidContainer 用于Wifi定位的AP源的RSSID序列的容器
     */
    public void generateWifiFingerPrintLibrary(WifiFingerBssidContainer wifiFingerBssidContainer) {
        try {
            databaseOperation.deleteTable("testWifiResult");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除表wifiFinger失败");
        }
        List<String> wifiFingerBssidList = wifiFingerBssidContainer.getWifiFingerBssidList();
        StringBuilder sqlStringBuilder = new StringBuilder("CREATE TABLE testWifiResult AS " +
                "(SELECT rssid,x,y,avg(level) as level FROM testwifi WHERE ");
        for (int i = 0; i < wifiFingerBssidList.size(); i++) {
            if (i == wifiFingerBssidList.size() - 1)
                sqlStringBuilder.append("rssid= ").append("'").append(wifiFingerBssidList.get(i)).
                        append("' ").append("GROUP BY x,y,rssid)");
            else
                sqlStringBuilder.append("rssid= ").append("'").append(wifiFingerBssidList.get(i)).
                        append("' ").append("OR ");
        }
        String sqlString = sqlStringBuilder.toString();
        databaseOperation.createTable(sqlString);
    }

    /**
     * 生成按时间序列采集的wifi信号
     * 用于检测神经网络的训练效果
     *
     * @param wifiFingerBssidContainer 用于Wifi定位的AP源的RSSID序列的容器
     */
    public void generateTestWifiData(WifiFingerBssidContainer wifiFingerBssidContainer) {
        List<String> wifiFingerBssidList = wifiFingerBssidContainer.getWifiFingerBssidList();

        try {
            databaseOperation.deleteTable("tempTimeWifiResult");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除表timeWifiResult失败");
        }
        // 生成中间表tempTimeWifiResult
        String sqlString = "CREATE TABLE tempTimeWifiResult AS \n" +
                " (SELECT *  FROM testwifi where x = 0 and y = 2 and ( rssid = '6c:f3:7f:bc:1e:22' \n" +
                "\tor rssid = '6c:f3:7f:bc:1e:20' or rssid = '6c:f3:7f:bc:1e:30' or rssid = '6c:f3:7f:bc:1e:32' \n" +
                "   or rssid = '6c:f3:7f:bc:1e:21'  or rssid = '6c:f3:7f:bc:1e:31') order by id) ";
        databaseOperation.createTable(sqlString);

        try {
            databaseOperation.deleteTable("timeWifi");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除表timeWifi失败");
        }
        StringBuilder initTableString = new StringBuilder
                ("CREATE TABLE timeWifi ( " + "id Integer PRIMARY KEY, " + "x  Integer, " + "y Integer, ");
        for (int i = 0; i < wifiFingerBssidList.size(); i++) {
            if (i == wifiFingerBssidList.size() - 1) {
                initTableString.append("level").append(i + 1).append(" ").append("numeric);");
            } else {
                initTableString.append("level").append(i + 1).append(" ").append("numeric ").append(", ");
            }
        }
        sqlString = initTableString.toString();
        databaseOperation.createTable(sqlString);
        // 初始化timeWifi表中内容
        for (int i = 0; i < 25; i++) {
            String insertStr = "INSERT INTO timeWifi VALUES( " + (i + 1) + "," + 0 + ", " + 2 + ", " +
                    "0,0,0,0,0,0)";
            databaseOperation.updateTable(insertStr);
        }
        // 更新表中记录
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < wifiFingerBssidList.size(); j++) {
                sqlString = "UPDATE timeWifi SET level" + (j + 1) + "= " +
                        "(SELECT level FROM (SELECT * FROM temptimewifiresult limit 6 " +
                        " offset " + (6 * i) + " ) as a where rssid = '" + wifiFingerBssidList.get(j) + "') " +
                        "where id =  " + (i + 1);
                databaseOperation.updateTable(sqlString);
            }
        }
    }


    /**
     * 生成磁场和Wifi的最终指纹库，用于定位
     *
     * @param xMin                     区域x坐标最小值
     * @param yMin                     区域y坐标最小值
     * @param xMax                     区域x坐标最大值
     * @param yMax                     区域y坐标最大值
     * @param wifiFingerBssidContainer 用于Wifi定位的AP源的RSSID序列的容器
     */
    public void generateFinalFingerPrintLibrary(int xMin, int yMin, int xMax, int yMax,
                                                WifiFingerBssidContainer wifiFingerBssidContainer) {
        try {
            databaseOperation.deleteTable("finalFinger");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除表finalFinger失败");
        }
        // 创建表finalFinger
        StringBuilder initTableString = new StringBuilder
                ("CREATE TABLE finalFinger ( " + "x  Integer, " + "y Integer, ");
        List<String> wifiFingerBssidList = wifiFingerBssidContainer.getWifiFingerBssidList();
        for (int i = 0; i < wifiFingerBssidList.size(); i++) {
            if (i == wifiFingerBssidList.size() - 1) {
                initTableString.append("level").append(i + 1).append(" ").append("real);");

            } else {
                initTableString.append("level").append(i + 1).append(" ").append("real ").append(", ");
            }
        }
        // initTableString.append("mag").append(" ").append("numeric);");
        String sqlString = initTableString.toString();
        databaseOperation.createTable(sqlString);
        // 初始化表中内容
        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                String insertStr = "INSERT INTO finalFinger VALUES( " + i + ", " + j + ", " +
                        "0,0,0,0,0,0)";
                databaseOperation.updateTable(insertStr);
            }
        }
        // 根据生成的wifi指纹库和磁场指纹库更新FinalFingerPrint中的记录
        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                for (int k = 0; k < wifiFingerBssidList.size(); k++) {
//                    if (k < wifiFingerBssidList.size()) {
                    String updateStr = "UPDATE finalFinger SET level" + (k + 1) + "= " +
                            " (SELECT level FROM wififinger WHERE x= " + i + " and y= " + j +
                            " and rssid = " + "'" + wifiFingerBssidList.get(k) + "'" + " ) " +
                            " WHERE x= " + i + " and " + " y= " + j;
                    databaseOperation.updateTable(updateStr);
//                    } else {
//                        String updateStr = "UPDATE finalFinger SET mag= " + (i + j) +
//                                " WHERE x= " + i + " and " + " y= " + j;
////                        String updateStr = "UPDATE finalFinger SET mag= " +
////                                " (SELECT level FROM magFinger WHERE x= " + i + " and " + " y= " + j + " ) " +
////                                " WHERE x= " + i + " and " + " y= " + j;
//                        databaseOperation.updateTable(updateStr);
//                    }
                }
            }
        }
    }


    /**
     * 生成用于神经网络训练的wifi数据
     *
     * @param wifiFingerBssidContainer 用于Wifi定位的AP源的RSSID序列的容器
     */
    public void generateNNTrainData(WifiFingerBssidContainer wifiFingerBssidContainer) {
        List<String> wifiFingerBssidList = wifiFingerBssidContainer.getWifiFingerBssidList();
        // 创建表TempNNRssidWifi，它当中的数据是wifiData子集，只包括了用于定位的6个AP源的数据
        try {
            databaseOperation.deleteTable("TempNNRssidWifi");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除表TempNNRssidWifi失败");
        }
        StringBuilder sqlStringBuilder = new StringBuilder("CREATE TABLE TempNNRssidWifi AS " +
                "(SELECT * FROM wifiData WHERE ");
        for (int i = 0; i < wifiFingerBssidList.size(); i++) {
            if (i == wifiFingerBssidList.size() - 1) {
                sqlStringBuilder.append("RSSID = ").append("'").append(wifiFingerBssidList.get(i)).append("' ").append(" ) ");
            } else {
                sqlStringBuilder.append("RSSID = ").append("'").append(wifiFingerBssidList.get(i)).append("' ").append(" or ");
            }
        }
        databaseOperation.createTable(sqlStringBuilder.toString());

        // 将表TempNNRssidWifi的数据写入List中
        List<Float> xPosList = new ArrayList<>();
        List<Float> yPosList = new ArrayList<>();
        List<Float> levelList = new ArrayList<>();
        String sqlString = "SELECT * FROM tempnnrssidwifi";
        // 编译sql语句
        // 执行查询
        try {
            Statement stmt = databaseOperation.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
            // 遍历结果
            while (rs.next()) {
                xPosList.add(rs.getFloat("x"));
                yPosList.add(rs.getFloat("y"));
                levelList.add(rs.getFloat("level"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("查询失败: " + e.getMessage());
        }

        // 创建表NNRssidWifi
        try {
            databaseOperation.deleteTable("NNRssidWifi");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除表NNRssidWifi失败");
        }
        sqlStringBuilder = new StringBuilder
                ("CREATE TABLE NNRssidWifi ( " + "id Integer PRIMARY KEY, " + "x  Integer, " + "y Integer, ");
        for (int i = 0; i < wifiFingerBssidList.size(); i++) {
            if (i == wifiFingerBssidList.size() - 1) {
                sqlStringBuilder.append("level").append(i + 1).append(" ").append("numeric);");
            } else {
                sqlStringBuilder.append("level").append(i + 1).append(" ").append("numeric ").append(", ");
            }
        }
        databaseOperation.createTable(sqlStringBuilder.toString());

        for (int i = 0; i < 1461; i++) {
            sqlStringBuilder = new StringBuilder("INSERT INTO NNRssidWifi VALUES ( " + (i + 1));
            sqlStringBuilder.append(" , ").append(xPosList.get(6 * i)).append(" , ").append(yPosList.get(6 * i)).append(" , ");
            for (int j = 0; j < 6; j++) {
                if (j == 5) {
                    sqlStringBuilder.append(levelList.get(6 * i + j)).append(" ) ");
                } else {
                    sqlStringBuilder.append(levelList.get(6 * i + j)).append(" , ");
                }
            }
            databaseOperation.updateTable(sqlStringBuilder.toString());
        }
    }

    /**
     * 补充指纹库中缺少的AP源
     *
     * @param wifiFingerBssidContainer 用于Wifi定位的AP源的RSSID序列的容器
     */
    public void supplementFinger(WifiFingerBssidContainer wifiFingerBssidContainer) throws SQLException {
        List<String> wifiFingerBssidList = wifiFingerBssidContainer.getWifiFingerBssidList();
        Statement stmt = databaseOperation.getConnection().createStatement();
        for (int i = 6; i <= 6; i = i + 2) {
            for (int j = 4; j <= 4; j = j + 2) {
                for (String wifiFingerBssid : wifiFingerBssidList) {
                    String sqlStr = "SELECT * FROM avg_init_finger WHERE rssid = " + "'" + wifiFingerBssid + "'" +
                            " and x_pos = " + i + " and y_pos = " + j;
                    ResultSet rs = stmt.executeQuery(sqlStr);
                    if (!rs.next()) {    // 不存在该AP源
                        String updateStr = "INSERT INTO avg_init_finger VALUES ( " + "'" + wifiFingerBssid + "'" +
                                " , " + i + " , " + j + " , " + " -100 )";
                        databaseOperation.updateTable(updateStr);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        String driverName = "org.postgresql.Driver";    // 驱动名
        String url = "jdbc:postgresql://127.0.0.1:5432/indoor_location_data";
        String user = "postgres";
        String password = "c724797";
        DatabaseOperation dbo = new DatabaseOperation(driverName, url, user, password);
        WifiFingerBssidContainer wfbc = new WifiFingerBssidContainer("40:e3:d6:76:44:33",
                "40:e3:d6:76:44:30", "40:e3:d6:76:43:a3", "94:b4:0f:cc:89:b1", "40:e3:d6:76:44:03");
        FingerGeneration fg = new FingerGeneration(dbo);
        try {
            fg.supplementFinger(wfbc);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
