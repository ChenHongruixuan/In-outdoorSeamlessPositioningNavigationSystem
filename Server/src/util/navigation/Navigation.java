package util.navigation;

import data.Node;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import util.database.DatabaseOperation;
import util.file.StringOperation;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static data.ConstData.INFINITY;
import static util.navigation.Calculation.calDistance;

/**
 * 导航和路径规划算法实现类
 */
public class Navigation {
    private DatabaseOperation mDatabaseOperation;

    public Navigation(DatabaseOperation databaseOperation) {
        mDatabaseOperation = databaseOperation;
    }

    /**
     * 获取导航至目标过程中必要的路径结点
     *
     * @param destAreaName 目标区域名称
     * @return 导航至目标过程中必要的路径结点
     */
    public Node getOutdoorKeyNavNode(String destAreaName) throws SQLException {
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        String sqlStr = "SELECT ori_name FROM area_names WHERE another_name = " + "'" + destAreaName + "'";
        ResultSet rs = stmt.executeQuery(sqlStr);
        rs.next();
        String destAreaOriName = rs.getString("ori_name");
        sqlStr = "SELECT ST_AsText(junc_node) as dest_pos FROM area_para WHERE area_name = '"
                + destAreaOriName + "'";
        rs = stmt.executeQuery(sqlStr);
        rs.next();
        String destStr = rs.getString("dest_pos");
        return StringOperation.getPosFromStr(destStr);

    }

    /**
     * 获取室内目标的结点
     *
     * @param destAreaName 目标区域名称
     * @return 代表导航目标的结点
     */
    public Node getIndoorDestNavNode(String[] destAreaName) throws SQLException {
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        String sqlStr = "SELECT ST_AsText(junc_node) as dest_pos FROM indoor_area_para WHERE out_area_name = '" +
                destAreaName[0] + "'" + " and " + "area_name = '" + destAreaName[1] + "'";
        ResultSet rs = stmt.executeQuery(sqlStr);
        rs.next();
        String destStr = rs.getString("dest_pos");
        return StringOperation.getPosFromStr(destStr);
    }

    /**
     * 获取导航过程中室内部分的必要结点
     *
     * @param startNode 起始结点
     * @param endNode   终止结点
     * @param areaName  区域名称（如资环楼，文典阁图书馆等）
     * @return 结点列表
     */
    public List<Node> getIndoorNavNode(Node startNode, Node endNode, String areaName) throws SQLException,
            IOException, DocumentException {
        /*
          首先先从数据库中找出当前区域的导航文件路径
         */
        String sqlStr = "SELECT save_path FROM node_path WHERE area_name = '" + areaName + "'";
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlStr);
        rs.next();
        String nodePath = rs.getString("save_path");    // 导航结点文件的存储路径
        /*
          利用SAXReader库解析导航结点xml文件
         */
        SAXReader reader = new SAXReader();
        File xmlFile = new File(nodePath);
        Document document = reader.read(xmlFile);
        Element node = document.getRootElement();
        Iterator nodeIt = node.elementIterator();
        List<Node> nodeList = new ArrayList<>();
        while (nodeIt.hasNext()) {
            Element navElement = (Element) nodeIt.next();  // 遍历结点
            /*
              获取结点属性
             */
            int id = Integer.valueOf(navElement.attribute(0).getValue());
            double lat = Double.valueOf(navElement.attribute(1).getValue());
            double lon = Double.valueOf(navElement.attribute(2).getValue());
            String initAdjNodeIdStr = navElement.attribute(3).getValue();
            nodeList.add(new Node(id, lat, lon, initAdjNodeIdStr));
        }
        // 进行结点匹配，从室内结点中匹配出起始点和终点的结点id
        int startIndex = 0, endIndex = 0;
        double startDis = INFINITY, endDis = INFINITY, tempDis;
        for (Node nodeElem : nodeList) {
            tempDis = calDistance(startNode, nodeElem);
            if (tempDis < startDis) {
                startDis = tempDis;
                startIndex = nodeElem.getId();
            }
            tempDis = calDistance(endNode, nodeElem);
            if (tempDis < endDis) {
                endDis = tempDis;
                endIndex = nodeElem.getId();
            }
        }
        /*
          根据结点信息构建邻接矩阵
         */
        int nodeCount = nodeList.size();
        // 邻接矩阵初始化，对角线元素置0，其余元素为INFINITY
        double[][] adjMatrix = new double[nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                if (i == j) {
                    adjMatrix[i][j] = 0;
                } else {
                    adjMatrix[i][j] = INFINITY;
                }
            }
        }
        // 根据各个结点的拓扑结构和之间的距离构建邻接矩阵
        for (int i = 0; i < nodeCount; i++) {
            List<Integer> adjNodeIdList = nodeList.get(i).getAdjNodeId();
            for (Integer adjNodeId : adjNodeIdList) {
                double dis = calDistance(nodeList.get(i), nodeList.get(adjNodeId));
                if (dis < adjMatrix[i][adjNodeId]) {
                    adjMatrix[i][adjNodeId] = dis;
                    adjMatrix[adjNodeId][i] = dis;
                }
            }
        }
        /*
          利用Dijkstra算法进行路径规划
         */
        List<Integer> navNodeIndexList = getMinRoute(startIndex, endIndex, adjMatrix);
        List<Node> navNodeList = new ArrayList<>();
        for (Integer index : navNodeIndexList) {
            navNodeList.add(nodeList.get(index));
        }
        return navNodeList;
    }

    /**
     * 根据Dijkstra算法进行两点之间的最短路径规划
     *
     * @param startIndex 起始点的索引
     * @param destIndex  目标点的索引
     * @param adjMatrix  邻接矩阵
     */
    private List<Integer> getMinRoute(int startIndex, int destIndex, double[][] adjMatrix) {
        int nodeNum = adjMatrix.length;
        boolean[] find = new boolean[nodeNum];   // S存储已判断的节点
        double[] dist = new double[nodeNum]; // 路径长度
        int[] prev = new int[nodeNum];      // 前驱节点数列
        List<Integer> nodeList = new ArrayList<>();    // 两节点最短路径经过的节点
        for (int i = 0; i < nodeNum; i++) {       // 初始化起点到所有节点的距离
            dist[i] = adjMatrix[startIndex][i];
            find[i] = false;
            if (dist[i] == INFINITY)
                prev[i] = -1;
            else
                prev[i] = startIndex;
        }
        dist[startIndex] = 0;
        find[startIndex] = true;
        for (int i = 1; i < nodeNum; i++) {
            double minDis = INFINITY;
            int u = startIndex;
            for (int j = 0; j < nodeNum; ++j) {
                if ((!find[j]) && dist[j] < minDis) {
                    u = j;                             // u保存当前邻接点中距离最小的点的号码
                    minDis = dist[j];
                }
            }
            find[u] = true;
            for (int j = 0; j < nodeNum; j++) {
                if ((!find[j]) && adjMatrix[u][j] < INFINITY) {
                    if (dist[u] + adjMatrix[u][j] < dist[j]) {     // 在通过新加入的u点路径找到离startIndex点更短的路径
                        dist[j] = dist[u] + adjMatrix[u][j];     // 更新dist
                        prev[j] = u;                     // 记录前驱顶点
                    }
                }
            }
        }
        int k = 1;
        nodeList.add(destIndex);   // 记录起点与终点经过的节点
        while (prev[destIndex] != startIndex) {
            nodeList.add(prev[destIndex]);
            destIndex = nodeList.get(k);
            k++;
        }
        nodeList.add(startIndex);
        return nodeList;
    }

    public static void main(String[] args) throws DocumentException {
//        double[][] adjMatrix = {
//                {0, 1, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY},
//                {1, 0, 1, 1, 1, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY},
//                {INFINITY, 1, 0, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY},
//                {INFINITY, 1, INFINITY, 0, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY},
//                {INFINITY, 1, INFINITY, INFINITY, 0, 1, INFINITY, INFINITY, INFINITY, INFINITY},
//                {INFINITY, INFINITY, INFINITY, INFINITY, 1, 0, 1, 1, INFINITY, INFINITY},
//                {INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, 1, 0, 1, 1, 1},
//                {INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, 1, 0, INFINITY, INFINITY},
//                {INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, 1, INFINITY, 0, INFINITY},
//                {INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, INFINITY, 1, INFINITY, INFINITY, 0},
//        };
//        System.out.println(getMinRoute(1, 7, adjMatrix));
    }
}
