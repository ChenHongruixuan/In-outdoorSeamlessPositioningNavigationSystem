package com.example.administrator.osmapitest.shared;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.util.List;

/**
 * 代表当前室内地图
 */
public class NowIndoorMap {
    private static Polygon nBaseMap;    // 底图
    private static List<Polygon> nBusinessMap;  // 业务地图
    private static List<Polyline> nNavRoute;    // 导航路径
    private static Polygon nIOMap;  // 覆盖图
    private static SimpleFastPointOverlay areaName; // 区域名

    public static Polygon getNIOMap() {
        return nIOMap;
    }

    public static List<Polygon> getNBusinessMap() {
        return nBusinessMap;
    }

    public static List<Polyline> getNNavRoute() {
        return nNavRoute;
    }

    public static Polygon getNBaseMap() {
        return nBaseMap;
    }

    public static SimpleFastPointOverlay getAreaName() {
        return areaName;
    }

    public static void setNIOMap(Polygon nIOMap) {
        NowIndoorMap.nIOMap = nIOMap;
    }

    public static void setNBaseMap(Polygon nBaseMap) {
        NowIndoorMap.nBaseMap = nBaseMap;
    }

    public static void setNBusinessMap(List<Polygon> nBusinessMap) {
        NowIndoorMap.nBusinessMap = nBusinessMap;
    }

    public static void setNNavRoute(List<Polyline> nNavRoute) {
        NowIndoorMap.nNavRoute = nNavRoute;
    }

    public static void setAreaName(SimpleFastPointOverlay areaName) {
        NowIndoorMap.areaName = areaName;
    }
}
