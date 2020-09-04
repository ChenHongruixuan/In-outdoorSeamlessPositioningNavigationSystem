package server;

import data.Node;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.dom4j.DocumentException;
import util.database.DatabaseOperation;
import util.navigation.NavBasicInfo;
import util.navigation.Navigation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;


public class NavigationHttpServlet extends HttpServlet {
    private DatabaseOperation mDatabaseOperation;   // 数据库操作类
    private final DiskFileItemFactory factory = new DiskFileItemFactory();    //  创建一个DiskFileItemFactory工厂
    private final ServletFileUpload upload = new ServletFileUpload(factory);     // 创建一个文件上传解析器
    private Navigation mNavigationUtil;

    @Override
    public void init() throws ServletException {
        super.init();
        String driverName = "org.postgresql.Driver";    // 驱动名
        String url = "jdbc:postgresql://127.0.0.1:5432/indoor_map_para";
        String user = "postgres";
        String password = "c724797";
        mDatabaseOperation = new DatabaseOperation(driverName, url, user, password);
        mNavigationUtil = new Navigation(mDatabaseOperation);
        // 解决上传文件名的中文乱码
        upload.setHeaderEncoding("UTF-8");
        System.out.println("服务器导航模块加载成功");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 判断提交上来的数据是否是上传表单的数据
        if (!ServletFileUpload.isMultipartContent(req)) {
            // 按照传统方式获取数据
            return;
        }
        /*
          使用ServletFileUpload解析器解析上传数据，
          解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
         */
        List<FileItem> list = null;
        try {
            list = upload.parseRequest(req);
        } catch (FileUploadException e) {
            e.printStackTrace();
            System.out.println("解析上传数据出错" + e.getMessage());
        }
        NavBasicInfo navBasicInfo = new NavBasicInfo(); // 导航基本信息
        for (FileItem item : Objects.requireNonNull(list)) {
            // 如果FileItem中封装的是普通输入项的数据
            if (item.isFormField()) {
                String name = item.getFieldName();
                // 解决普通输入项的数据的中文乱码问题
                String value = item.getString("UTF-8");
//            value = new String(value.getBytes("iso8859-1"), "UTF-8");
                switch (name) {
                    case "area_name":
                        navBasicInfo.setDesAreaName(value);
                        break;
                    case "indoor":
                        navBasicInfo.setIndoor(Boolean.valueOf(value));
                        break;
                    case "latitude":
                        navBasicInfo.setLat(Double.valueOf(value));
                        break;
                    case "longitude":
                        navBasicInfo.setLon(Double.valueOf(value));
                        break;
                    case "floor":
                        navBasicInfo.setFloor(Integer.valueOf(value));
                        break;
                    default:
                        break;
                }
            }
        }
        String[] specificArea = navBasicInfo.getDesAreaName().split(",");
        /*
          状态码statusCode代表的含义如下：
          0代表导航无效或者出错
          1代表导航从室外到室外, 2代表从室外到室内
          3代表从室内到室外, 4代表从室内到室内
          5代表从室内到室外到室内
         */
        int statusCode = 0;
        double lat = navBasicInfo.getLat();
        double lon = navBasicInfo.getLon();
        StringBuilder navStrBuilder = new StringBuilder();
        OutputStream outputStream = resp.getOutputStream();
        Node startNode, endNode;
        List<Node> navNodeList;
        String indoorAreaName;
        try {
            if (navBasicInfo.isIndoor()) {   // 如果起始区域处于室内
                switch (specificArea.length) {
                    case 1: // 目标区域为室外
                        statusCode = 3;
                        startNode = new Node(lat, lon);
                        // 获取起始点所在室内区域的名称
                        indoorAreaName = getAreaNameByNode(lat, lon);
                        // 获取该室内区域与室外的接壤点的经纬度
                        Node middleNode = mNavigationUtil.getOutdoorKeyNavNode(indoorAreaName);
                        // 获取室内导航的路径
                        navNodeList = mNavigationUtil.getIndoorNavNode(startNode, middleNode, indoorAreaName);
                        endNode = mNavigationUtil.getOutdoorKeyNavNode(specificArea[0]);
                        navStrBuilder.append(statusCode).append("\n")
                                .append("start").append("\n")
                                .append(startNode.toString()).append("\n")
                                .append("end").append("\n")
                                .append(endNode.toString()).append("\n")
                                .append("indoor").append("\n");
                        for (Node node : navNodeList) {
                            navStrBuilder.append(node.toString()).append("\n");
                        }
                        navStrBuilder.append("outdoor").append("\n")
                                .append(middleNode.toString()).append("\n")
                                .append(endNode.toString()).append("\n");
                        break;
                    case 2: // 目标区域为室内（又分为2种情况，分别是从室内到室内以及从室内到室外再到室内）
                        // 首先先判断目标所在的室内区域与当前位置是否在同一建筑物内
                        boolean inSameArea = judgeIsInSameIndoorArea(lat, lon, specificArea);
                        /*
                          如果在室内同一区域，需要进行的的操作有：
                          根据目的地的名称生成带有经纬度的Node对象作为终点
                          利用起点和终点以及室内区域的结点文件进行路径规划，得到室内导航的结点列表
                         */
                        if (inSameArea) {
                            statusCode = 4;
                            startNode = new Node(lat, lon);
                            endNode = mNavigationUtil.getIndoorDestNavNode(specificArea);
                            // 获取室内导航的路径
                            navNodeList = mNavigationUtil.getIndoorNavNode(startNode, endNode, specificArea[0]);
                            navStrBuilder.append(statusCode).append("\n")
                                    .append("start").append("\n")
                                    .append(startNode.toString()).append("\n")
                                    .append("end").append("\n")
                                    .append(endNode.toString()).append("\n")
                                    .append("indoor").append("\n");
                            for (Node node : navNodeList) {
                                navStrBuilder.append(node.toString()).append("\n");
                            }
                        }
                        /*
                          如果不在室内的同一区域，这种对应实际情况如：从资环楼1楼导航至文典阁1楼。需要进行的操作有：
                          根据目的地名称生成带有经纬度的Node对象作为终点
                          获取两个室内区域的内外交接点的经纬度构造中间的Node对象
                          最后在两个室内区域进行室内路径规划，在两个中间Node对象间进行室外路径规划
                         */
                        else {
                            statusCode = 5;
                            startNode = new Node(lat, lon);
                            // 获取起始点所在室内区域的名称
                            indoorAreaName = getAreaNameByNode(lat, lon);
                            // 获取该室内区域与室外的接壤点的经纬度
                            Node fMiddleNode = mNavigationUtil.getOutdoorKeyNavNode(indoorAreaName);
                            // 获取室内导航的路径
                            List<Node> fNavNodeList = mNavigationUtil.getIndoorNavNode(startNode, fMiddleNode, indoorAreaName);
                            Node sMiddleNode = mNavigationUtil.getOutdoorKeyNavNode(specificArea[0]);
                            // 获取室内导航的路径
                            endNode = mNavigationUtil.getIndoorDestNavNode(specificArea);   // 目标结点
                            List<Node> sNavNodeList = mNavigationUtil.getIndoorNavNode(sMiddleNode, endNode, specificArea[0]);
                            navStrBuilder.append(statusCode).append("\n")
                                    .append("start").append("\n")
                                    .append(startNode.toString()).append("\n")
                                    .append("end").append("\n")
                                    .append(endNode.toString()).append("\n")
                                    .append("indoor").append("\n");
                            for (Node node : fNavNodeList) {
                                navStrBuilder.append(node.toString()).append("\n");
                            }
                            navStrBuilder.append("outdoor").append("\n")
                                    .append(fMiddleNode.toString()).append("\n")
                                    .append(sMiddleNode.toString()).append("\n")
                                    .append("indoor").append("\n");
                            for (Node node : sNavNodeList) {
                                navStrBuilder.append(node.toString()).append("\n");
                            }
                        }
                        break;
                    default:
                        break;

                }
            } else {                        // 如果起始区域处于室外
                switch (specificArea.length) {
                    /*
                      目标区域为室外，则根据室外目标区域名称，获得室外目标区域的结点位置，并添加到navNodeList中
                     */
                    case 1:
                        statusCode = 1;
                        startNode = new Node(lat, lon);
                        endNode = mNavigationUtil.getOutdoorKeyNavNode(specificArea[0]);
                        navStrBuilder.append(statusCode).append("\n")
                                .append("start").append("\n")
                                .append(startNode.toString()).append("\n")
                                .append("end").append("\n")
                                .append(endNode.toString()).append("\n")
                                .append("outdoor").append("\n")
                                .append(startNode.toString()).append("\n")
                                .append(endNode.toString()).append("\n");
                        break;
                    case 2:                 // 目标区域为室内
                        statusCode = 2;
                        startNode = new Node(lat, lon);
                        Node middleNode = mNavigationUtil.getOutdoorKeyNavNode(specificArea[0]);
                        endNode = mNavigationUtil.getIndoorDestNavNode(specificArea);
                        // 获取室内导航的路径
                        navNodeList = mNavigationUtil.getIndoorNavNode(middleNode, endNode, specificArea[0]);
                        navStrBuilder.append(statusCode).append("\n")
                                .append("start").append("\n")
                                .append(startNode.toString()).append("\n")
                                .append("end").append("\n")
                                .append(endNode.toString()).append("\n")
                                .append("outdoor").append("\n")
                                .append(startNode.toString()).append("\n")
                                .append(middleNode.toString()).append("\n");
                        navStrBuilder.append("indoor").append("\n");
                        for (Node node : navNodeList) {
                            navStrBuilder.append(node.toString()).append("\n");
                        }
                        break;
                    default:
                        break;
                }
            }
            outputStream.write(navStrBuilder.toString().getBytes());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("获取目标地点位置信息出错：" + e.getMessage());
        } catch (DocumentException e) {
            e.printStackTrace();
            System.out.println("获取室内导航路径出错：" + e.getMessage());
        }
    }


    /**
     * 判断当前位置和导航目的地是否在同一室内区域
     *
     * @param lat      当前位置纬度
     * @param lon      当前位置经度
     * @param areaName 目的地名称
     * @return 是否在同一室内区域
     */
    private boolean judgeIsInSameIndoorArea(double lat, double lon, String[] areaName) throws SQLException {
        /*
          首先获取起始点所在的区域名称
         */

        String startArea = getAreaNameByNode(lat, lon);
        /*
          从服务器获取目标地点本名
         */
        String destFirstName = areaName[0]; // 目的地的一级名,如资环楼
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        String sqlStr = "SELECT ori_name FROM area_names WHERE another_name = " + "'" + destFirstName + "'";
        ResultSet rs = stmt.executeQuery(sqlStr);
        rs.next();
        String destArea = rs.getString("ori_name");
        return destArea.equals(startArea);
    }

    /**
     * 获取点所在室内区域的名称
     *
     * @param lat 当前位置纬度
     * @param lon 当前位置经度
     * @return 点所在室内区域的名称
     */
    private String getAreaNameByNode(double lat, double lon) throws SQLException {
        String sqlStr = "SELECT area_name FROM indoor_map WHERE " + " ST_WITHIN(ST_GeomFromText('POINT( " +
                lon + " " + lat + " )',4326), indoor_map.enter_area)";
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlStr);
        rs.next();
        return rs.getString("area_name");
    }
}
