package algorithm;

import data.WifiDataContainer;
import olddata.WifiFingerBssidContainer;
import olddata.SpatialPosition;
import util.database.DataPreHandle;
import util.database.DatabaseOperation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 这个类实现了以下定位算法：
 * NN,K-NN,W-K-NN,卡尔曼滤波
 */
public class PositionAlgorithm {
    private DatabaseOperation databaseOperation;    // 数据库操作工具类对象
    private WifiFingerBssidContainer wifiFingerBssidContainer;  // 用于Wifi定位的AP源的RSSID序列的容器对象
    private DataPreHandle dataPreHandle;
    public final static int K_NN = 1;
    public final static int W_K_NN = 2;

    /**
     * PositionAlgorithm的构造函数
     *
     * @param databaseOperation        数据库操作工具类对象
     * @param wifiFingerBssidContainer 用于Wifi定位的AP源的RSSID序列的容器
     */
    public PositionAlgorithm(DatabaseOperation databaseOperation, WifiFingerBssidContainer wifiFingerBssidContainer) {
        this.databaseOperation = databaseOperation;
        this.wifiFingerBssidContainer = wifiFingerBssidContainer;
        this.dataPreHandle = new DataPreHandle();
    }

    /**
     * 根据Wifi信号强度利用NN算法进行定位
     *
     * @param bssidList 客户端采集到的wifi数据的bssid列表
     * @param levelList 客户端采集到的wifi数据的强度列表
     * @return 一个封装了定位结果的SpatialPosition类的对象
     * @throws SQLException SQL异常，继续向上抛出
     */
    public SpatialPosition nnLocate(List<String> bssidList, List<Float> levelList) throws SQLException {
        return wknnLocate(bssidList, levelList, 1, K_NN);
    }

    /**
     * 根据Wifi信号强度利用K-NN算法进行定位
     *
     * @param bssidList   客户端采集到的wifi数据的bssid列表
     * @param levelList   客户端采集到的wifi数据的强度列表
     * @param resultCount 筛选出的定位结果数量
     * @return 一个封装了定位结果的SpatialPosition类的对象
     * @throws SQLException SQL异常，继续向上抛出
     */
    public SpatialPosition knnLocate(List<String> bssidList, List<Float> levelList, int resultCount) throws SQLException {
        return wknnLocate(bssidList, levelList, resultCount, K_NN);
    }

    /**
     * 根据Wifi信号强度利用W-K-NN算法进行定位
     *
     * @param bssidList   客户端采集到的wifi数据的bssid列表
     * @param levelList   客户端采集到的wifi数据的强度列表
     * @param resultCount 筛选出的定位结果数量
     * @return 一个封装了定位结果的SpatialPosition类的对象
     * @throws SQLException SQL异常，继续向上抛出
     */
    public SpatialPosition wknnLocate(List<String> bssidList, List<Float> levelList,
                                      int resultCount, int type) throws SQLException {
        List<String> wifiFingerBssidList = wifiFingerBssidContainer.getWifiFingerBssidList();
        WifiDataContainer wifiDataContainer =
                dataPreHandle.preHandleWifiData(bssidList, levelList, wifiFingerBssidList); // 对采集到的Wifi数据进行预处理

        // 将List转为数组，便于后续操作
        String[] bssidArray = wifiDataContainer.getBssidArray();
        Float[] levelArray = wifiDataContainer.getLevelArray();
        /*
          在执行过程中，报错
          sql语句中，level-level_scan的形式，如果level为负数，则用括号括起
          形如level-(level_scan)
         */
        String sqlString = "SELECT w7.x_pos,w7.y_pos,w7.floor,w7.level+w8.level AS level FROM \n" +
                "(SELECT w5.x_pos,w5.y_pos,w5.floor,w5.level+w6.level AS level FROM \n" +
                "(SELECT w3.x_pos,w3.y_pos,w3.floor,w3.level+w4.level AS level FROM \n" +
                "(SELECT w1.x_pos,w1.y_pos,w1.floor,w1.level+w2.level AS level FROM \n" +
                "(SELECT x_pos,y_pos,floor,(wifi_level-(" + levelArray[0] + "))*(wifi_level-(" + levelArray[0] + ")) as level \n" +
                "FROM wifi_finger WHERE rssid = '" + bssidArray[0] + "') AS w1 JOIN \n" +
                "(SELECT x_pos,y_pos,floor, (wifi_level-(" + levelArray[1] + "))*(wifi_level-(" + levelArray[1] + ")) as level \n" +
                "FROM wifi_finger WHERE rssid = '" + bssidArray[1] + "') AS w2 \n" +
                "ON (w1.x_pos = w2.x_pos AND w1.y_pos = w2.y_pos)) AS w3 JOIN \n" +
                "(SELECT x_pos,y_pos,floor,(wifi_level-(" + levelArray[2] + "))*(wifi_level-(" + levelArray[2] + ")) as level \n" +
                "FROM wifi_finger WHERE rssid = '" + bssidArray[2] + "') AS w4 \n" +
                "ON (w3.x_pos = w4.x_pos AND w3.y_pos=w4.y_pos)) AS w5 JOIN \n" +
                "(SELECT x_pos,y_pos,floor,(wifi_level-(" + levelArray[3] + "))*(wifi_level-(" + levelArray[3] + ")) as level \n" +
                "FROM wifi_finger WHERE rssid = '" + bssidArray[3] + "') AS w6 \n" +
                "ON (w5.x_pos = w6.x_pos AND w5.y_pos=w6.y_pos)) AS w7 JOIN \n" +
                "(SELECT x_pos,y_pos,floor,(wifi_level-(" + levelArray[4] + "))*(wifi_level-(" + levelArray[4] + ")) as level \n" +
                "FROM wifi_finger WHERE rssid = '" + bssidArray[4] + "') AS w8 \n" +
                "ON (w7.x_pos = w8.x_pos AND w7.y_pos=w8.y_pos) " + " ORDER BY level";

//        String sqlString = "SELECT x,y,(level1- (" + levelArray[0] + ")) * (level1-(" + levelArray[0] + ")) + " +
//                "(level2- (" + levelArray[1] + ")) * (level2-(" + levelArray[1] + ")) + " +
//                "(level3- (" + levelArray[2] + ")) * (level3-(" + levelArray[2] + ")) + " +
//                "(level4- (" + levelArray[3] + ")) * (level4-(" + levelArray[3] + ")) + " +
//                "(level5- (" + levelArray[4] + ")) * (level5-(" + levelArray[4] + ")) + " +
//                "(level6- (" + levelArray[5] + ")) * (level6-(" + levelArray[5] + ")) " +
//                "as level FROM tradtionalwifitable ORDER BY level";

        Connection connection = databaseOperation.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sqlString);
        SpatialPosition spatialPosition;
        int labelCount = 1;
        int floor = 0;
        List<SpatialPosition> spList = new ArrayList<>();
        while (rs.next() && labelCount <= resultCount) { // 返回查询的第一条记录
            double x = rs.getDouble("x_pos");
            double y = rs.getDouble("y_pos");
            float levelVar = rs.getFloat("level");
            floor = rs.getInt("floor");
            spatialPosition = new SpatialPosition(x, y, floor, levelVar);
            spList.add(spatialPosition);
            labelCount++;
        }
        float finalX = 0;
        float finalY = 0;
        float finalVar = 0;
        switch (type) {
            case K_NN:
                for (SpatialPosition aSpList : spList) {
                    finalX += aSpList.getXPos();
                    finalY += aSpList.getYPos();
                }
                return new SpatialPosition(finalX / spList.size(), finalY / spList.size(), floor);
            case W_K_NN:
                for (SpatialPosition aSpList : spList) {
                    finalX += aSpList.getXPos() * Math.sqrt(aSpList.getLevelVar());
                    finalY += aSpList.getYPos() * Math.sqrt(aSpList.getLevelVar());
                    finalVar += Math.sqrt(aSpList.getLevelVar());
                }
            default:
                return new SpatialPosition(finalX / finalVar, finalY / finalVar, floor);
        }
    }

//    public SpatialPosition bayesLocate(List<String> bssidList, List<Float> levelList, int resultCount, int type) {
//
//    }

    // 卡尔曼滤波所需系数
    private SpatialPosition lastStatusSP;
    private static final float observeR = 3.5f;    // 观测误差
    private static final float statusQ = 0.8f;  // 状态预测误差
    private static float mseP;  // T时刻的最优MSE

    /**
     * 对定位算法的定位结果启动卡尔曼滤波
     *
     * @param bssidList 客户端采集到的wifi数据的bssid列表
     * @param levelList 客户端采集到的wifi数据的强度列表
     * @param kfCount   当前滤波轮次
     * @return 一个封装了位置信息的SpatialPosition类的对象
     * @throws SQLException SQL语句执行异常，继续向上抛出
     */
    public SpatialPosition kalmanFilterLocate(List<String> bssidList, List<Float> levelList, int kfCount)
            throws SQLException {
        SpatialPosition observePos = wknnLocate(bssidList, levelList, 4, W_K_NN);
        if (kfCount == 0) { // 卡尔曼滤波器启动
            lastStatusSP = observePos;
            mseP = observeR;
            return lastStatusSP;
        } else {
            mseP += statusQ;    // MSE一步预测
            float matrixH = mseP / (mseP + observeR);
            // 滤波估计方程
            double nowX = lastStatusSP.getXPos() + matrixH * (observePos.getXPos() - lastStatusSP.getXPos());
            double nowY = lastStatusSP.getYPos() + matrixH * (observePos.getYPos() - lastStatusSP.getYPos());
            lastStatusSP = new SpatialPosition(nowX, nowY);
            // 滤波MSE更新
            mseP *= (1 - matrixH);
            return lastStatusSP;
        }
    }


    /**
     * 测试算法
     */
    public static void main(String[] args) throws SQLException {
        int PORT = 8723;    //监听的端口号
        String driverName = "org.postgresql.Driver";    // 驱动名
        String url = "jdbc:postgresql://127.0.0.1:5432/indoor_location_data";
        String user = "postgres";
        String password = "c724797";

        DatabaseOperation dbc = new DatabaseOperation(driverName, url, user, password);


        WifiFingerBssidContainer wfbc = new WifiFingerBssidContainer("40:e3:d6:76:44:33",
                "40:e3:d6:76:44:30", "40:e3:d6:76:43:a3", "94:b4:0f:cc:89:b1", "40:e3:d6:76:44:03");
        PositionAlgorithm pa = new PositionAlgorithm(dbc, wfbc);

        List<String> list1 = new ArrayList<>();
        List<Float> list2 = new ArrayList<>();
        list1.add("40:e3:d6:76:44:33");
        list1.add("40:e3:d6:76:44:30");
        list1.add("40:e3:d6:76:43:a3");
        list1.add("94:b4:0f:cc:89:b1");
        list1.add("40:e3:d6:76:44:03");


        list2.add(-100.0f);
        list2.add(-100.0f);
        list2.add(-90.0f);
        list2.add(-75.0f);
        list2.add(-60.0f);
        System.out.println(pa.wknnLocate(list1, list2, 3, W_K_NN));
//        String sqlString = "SELECT * FROM " + "timewifi" + " ORDER BY id";
//        Connection connection = dbc.getConnection();
//
//        Statement stmt = connection.createStatement();
//        ResultSet rs = stmt.executeQuery(sqlString);
//        // 遍历结果
//        int j = 0;
//        while (rs.next()) {
//            for (int i = 0; i < 5; i++) {
//                list2.add(rs.getFloat("level" + (i + 1)));
//            }
//            System.out.println(pa.kalmanFilterLocate(list1, list2, j).toString());
//            list2.clear();
//            j = j + 1;
//        }
    }
}
