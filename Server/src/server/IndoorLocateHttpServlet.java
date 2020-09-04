package server;

import algorithm.PositionAlgorithm;
import olddata.SpatialPosition;
import olddata.WifiFingerBssidContainer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import util.database.DatabaseOperation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IndoorLocateHttpServlet extends HttpServlet {
    private DatabaseOperation mDatabaseOperation;   // 数据库操作类
    private PositionAlgorithm mPositionAlgorithm;   // 室内定位算法类
    private final DiskFileItemFactory factory = new DiskFileItemFactory();    //  创建一个DiskFileItemFactory工厂
    private final ServletFileUpload upload = new ServletFileUpload(factory);     // 创建一个文件上传解析器

    @Override
    public void init() throws ServletException {
        super.init();
        String driverName = "org.postgresql.Driver";    // 驱动名
        String url = "jdbc:postgresql://127.0.0.1:5432/indoor_location_data";
        String user = "postgres";
        String password = "c724797";
        mDatabaseOperation = new DatabaseOperation(driverName, url, user, password);
        WifiFingerBssidContainer wifiFingerBssidContainer = new WifiFingerBssidContainer(
                "40:e3:d6:76:44:33", "40:e3:d6:76:44:30", "40:e3:d6:76:43:a3",
                "94:b4:0f:cc:89:b1", "40:e3:d6:76:44:03");
        mPositionAlgorithm = new PositionAlgorithm(mDatabaseOperation, wifiFingerBssidContainer);
        // 解决上传文件名的中文乱码
        upload.setHeaderEncoding("UTF-8");
        System.out.println("服务器室内定位模块加载成功");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
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
        for (FileItem item : Objects.requireNonNull(list)) {
            // 如果FileItem中封装的是普通输入项的数据
            if (item.isFormField()) {
                // 解决普通输入项的数据的中文乱码问题
                String value = item.getString("UTF-8");
                value = new String(value.getBytes("iso8859-1"), "UTF-8");
                String[] wifiStrArr = value.split("\n");

                List<String> bssidList = new ArrayList<>();
                List<Float> levelList = new ArrayList<>();
                for (String wifiStr : wifiStrArr) {   // 使用readLine方法，一次读一行
                    String[] tempString = wifiStr.split(",");
                    bssidList.add(tempString[0]);
                    levelList.add(Float.valueOf(tempString[1]));
                }

                try {
                    SpatialPosition spatialPosition = mPositionAlgorithm.wknnLocate
                            (bssidList, levelList, 3, PositionAlgorithm.W_K_NN);
                    System.out.println(spatialPosition);
                    OutputStream outputStream = resp.getOutputStream();
                    outputStream.write(spatialPosition.toString().getBytes());
                    outputStream.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("wifi定位出错" + e.getMessage());
                }
            }
        }
    }
}
