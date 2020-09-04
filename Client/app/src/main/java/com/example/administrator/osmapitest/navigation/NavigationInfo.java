package com.example.administrator.osmapitest.navigation;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器返回的导航信息
 */
public class NavigationInfo implements Serializable {
    /**
     * 状态码
     * 0代表导航无效或者出错
     * 1代表导航从室外到室外, 2代表从室外到室内
     * 3代表从室内到室外, 4代表从室内到室内
     * 5代表从室内到室外到室内
     */
    private int statusCode;

    private GeoPoint startNode;
    private GeoPoint destNode;

    private GeoPoint outdoorStartNode;
    private GeoPoint outdoorDestNode;

    private List<List<GeoPoint>> indoorNavNodeLists = new ArrayList<>();  // 中间关键结点


    public NavigationInfo(String[] navStrArr) {
        statusCode = Integer.valueOf(navStrArr[0]);
        int typeCode = 4;
        List<GeoPoint> indoorNavNodeList = new ArrayList<>();
        for (int i = 1; i < navStrArr.length; i++) {
            switch (navStrArr[i]) {
                case "indoor":
                    typeCode = 0;
                    continue;
                case "outdoor":
                    typeCode = 1;
                    continue;
                case "start":
                    typeCode = 2;
                    continue;
                case "end":
                    typeCode = 3;
                    continue;
            }
            if (typeCode == 0) {
                String[] posStr = navStrArr[i].split(",");
                double lat = Double.valueOf(posStr[0]);
                double lon = Double.valueOf(posStr[1]);
                indoorNavNodeList.add(new GeoPoint(lat, lon));
                if (i == navStrArr.length - 1 || navStrArr[i + 1].equals("outdoor")) {
                    indoorNavNodeLists.add(indoorNavNodeList);
                    indoorNavNodeList = new ArrayList<>();
                }
            } else if (typeCode == 1) {
                String[] posStr = navStrArr[i].split(",");
                double lat = Double.valueOf(posStr[0]);
                double lon = Double.valueOf(posStr[1]);
                outdoorStartNode = new GeoPoint(lat, lon);
                posStr = navStrArr[i + 1].split(",");
                lat = Double.valueOf(posStr[0]);
                lon = Double.valueOf(posStr[1]);
                outdoorDestNode = new GeoPoint(lat, lon);
                i++;
            } else if (typeCode == 2) {
                String[] posStr = navStrArr[i].split(",");
                double lat = Double.valueOf(posStr[0]);
                double lon = Double.valueOf(posStr[1]);
                startNode = new GeoPoint(lat, lon);
            } else if (typeCode == 3) {
                String[] posStr = navStrArr[i].split(",");
                double lat = Double.valueOf(posStr[0]);
                double lon = Double.valueOf(posStr[1]);
                destNode = new GeoPoint(lat, lon);
            }
        }
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 获取导航的中间和目的地结点
     *
     * @return 中间和目的地结点位置列表
     */
    public List<List<GeoPoint>> getIndoorNavNodeLists() {
        return indoorNavNodeLists;
    }

    /**
     * 获取导航的起始位置
     *
     * @return 起始位置
     */
    public GeoPoint getOutdoorStartNode() {
        return outdoorStartNode;
    }

    public GeoPoint getOutdoorDestNode() {
        return outdoorDestNode;
    }

    public void setIndoorNavNodeLists(List<List<GeoPoint>> indoorNavNodeLists) {
        this.indoorNavNodeLists = indoorNavNodeLists;
    }

    public GeoPoint getDestNode() {
        return destNode;
    }

    public GeoPoint getStartNode() {
        return startNode;
    }
}
