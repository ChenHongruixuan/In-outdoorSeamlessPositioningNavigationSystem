package server;

import data.ClientPos;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import util.database.DatabaseOperation;
import util.file.FileOperation;

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

/**
 * 判断客户端处在室内还是室外，以及发送室内地图的服务器
 *
 * @author Qchrx
 * @version 1.1
 */
public class IndoorMapHttpServlet extends HttpServlet {

    private DatabaseOperation mDatabaseOperation;   // 数据库操作类
    private final DiskFileItemFactory factory = new DiskFileItemFactory();    //  创建一个DiskFileItemFactory工厂
    private final ServletFileUpload upload = new ServletFileUpload(factory);     // 创建一个文件上传解析器

    @Override
    public void init() throws ServletException {
        super.init();
        String driverName = "org.postgresql.Driver";    // 驱动名
        String url = "jdbc:postgresql://127.0.0.1:5432/indoor_map_para";
        String user = "postgres";
        String password = "c724797";
        mDatabaseOperation = new DatabaseOperation(driverName, url, user, password);
        // 解决上传文件名的中文乱码
        upload.setHeaderEncoding("UTF-8");
        System.out.println("服务器位置判断模块加载成功");
        System.out.println("服务器室内地图管理模块加载成功");
    }

    /**
     * 相应客户端的Get请求（请求室内地图）
     *
     * @param req  请求
     * @param resp 响应
     * @throws ServletException 向上抛出ServletException异常
     * @throws IOException      向上抛出IOException异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        double longitude = Double.valueOf(req.getHeader("longitude"));
        double latitude = Double.valueOf(req.getHeader("latitude"));
        int floor = Integer.valueOf(req.getHeader("floor"));
        try {
            String mapPath = getIndoorMapPath(longitude, latitude, floor);  // 获取室内地图存储的路径
            String indoorMapXmlStr;
            if (mapPath.length() > 0) { // 遍历地图文件，获取文件中的全部信息，村塾在indoorMapXml字符串中
                List<String> indoorMapXmlList = FileOperation.readTextByLine(mapPath);
                StringBuilder indoorMapXmlStrBuilder = new StringBuilder();
                for (String indoorMapXml : indoorMapXmlList) {
                    indoorMapXmlStrBuilder.append(indoorMapXml).append("\n");
                }
                indoorMapXmlStr = indoorMapXmlStrBuilder.toString();
            } else {    // 如果当前区域不存在室内地图，则返回一个空串
                indoorMapXmlStr = "";
            }
            /*
              将室内地图字符串写入输出流返回给客户端
             */
            OutputStream outputStream = resp.getOutputStream();
            outputStream.write(indoorMapXmlStr.getBytes());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("获取室内地图失败");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ClientPos clientPos = new ClientPos();
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
        for (FileItem item : Objects.requireNonNull(list)) {
            // 如果FileItem中封装的是普通输入项的数据
            if (item.isFormField()) {
                String name = item.getFieldName();
                // 解决普通输入项的数据的中文乱码问题
                String value = item.getString("UTF-8");
                value = new String(value.getBytes("iso8859-1"), "UTF-8");
                switch (name) {
                    case "status":
                        boolean isIndoor = Boolean.valueOf(value);
                        clientPos.setIsIndoor(isIndoor);
                        break;
                    case "longitude":
                        double longitude = Double.valueOf(value);
                        clientPos.setLongitude(longitude);
                        break;
                    case "latitude":
                        double latitude = Double.valueOf(value);
                        clientPos.setLatitude(latitude);
                        break;
                    default:
                        break;
                }
            }
        }
        try {
            boolean isNowIndoor = judgeIndoor(clientPos);   // 用户所处位置是否在室内
            boolean isLastIndoor = clientPos.getIsIndoor(); // 用户上一时刻所处位置是否在室内
            OutputStream outputStream = resp.getOutputStream();
            byte statusCode;
            if (!isNowIndoor && !isLastIndoor) {    // 用户处在室外
                statusCode = 2;
                outputStream.write(statusCode);
                outputStream.close();
            } else if (isNowIndoor && isLastIndoor) {   // 用户处在室内
                statusCode = 3;
                outputStream.write(statusCode);
                outputStream.close();
            } else if (!isNowIndoor) {  // 用户由室内进入室外
                statusCode = 1;
                outputStream.write(statusCode);
                outputStream.close();
            } else {    // 用户由室外进入室内
                statusCode = 0;
                outputStream.write(statusCode);
                outputStream.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("位置判断失败" + e.getMessage());
        }
    }


    /**
     * 根据用户上传的经纬度数据判断用户处在室内还是室外
     *
     * @param clientPos 用户所处位置
     * @return 是否在室内
     */
    private boolean judgeIndoor(ClientPos clientPos) throws SQLException {
        double longitude = clientPos.getLongitude();
        double latitude = clientPos.getLatitude();
        String sqlStr = "SELECT * FROM indoor_map WHERE (ST_Within(ST_GeomFromText(" +
                "'POINT( " + longitude + " " + latitude + " )',4326),geom_area))";
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlStr);
        rs.next();
        return rs.next();
    }

    /**
     * 获得地图XML文件路径
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param floor     请求室内地图的楼层
     * @return 地图XML文件路径
     * @throws SQLException 执行Sql语句会抛出该异常
     */
    private String getIndoorMapPath(double longitude, double latitude, int floor) throws SQLException {
        String sqlStr = "SELECT xml_path,floor FROM (" + "ST_GeomFromText( 'POINT("
                + longitude + " " + latitude + ")',4326) as client "
                + " JOIN (SELECT geom_area,xml_path,floor FROM indoor_map) as tmp "
                + " ON ST_WITHIN(client,geom_area)) " + "WHERE floor = " + floor;
        Connection connection = mDatabaseOperation.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlStr);
        if (rs.next()) {
            return rs.getString("xml_path");
        } else {
            return "";
        }
    }
}
