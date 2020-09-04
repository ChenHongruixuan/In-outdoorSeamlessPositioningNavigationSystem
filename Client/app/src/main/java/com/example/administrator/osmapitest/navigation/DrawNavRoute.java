package com.example.administrator.osmapitest.navigation;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.administrator.osmapitest.shared.NowNavRoute;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * 室内外导航结点绘制类
 */
public class DrawNavRoute {

    /**
     * * 绘制室外导航路线图
     *
     * @param context    上下文
     * @param mMapView   地图视图
     * @param uiHandler  用于更新UI的Handler
     * @param startPoint 室外导航起始点
     * @param endPoint   室外导航终点
     */
    public Polyline drawOutdoorNavRoute(Context context, final MapView mMapView, final Handler uiHandler,
                                        GeoPoint startPoint, GeoPoint endPoint) {
        // 加载自定义路径
        RoadManager roadManager = new OSRMRoadManager(context);
        ArrayList<GeoPoint> wayPoints = new ArrayList<>();

        wayPoints.add(startPoint);

        wayPoints.add(endPoint);
        Road road = roadManager.getRoad(wayPoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        roadOverlay.setColor(0xFF1B7BCD);
        roadOverlay.setWidth(4);
        mMapView.getOverlays().add(roadOverlay);
        // 发送给主程序对地图进行更新
        Message message = Message.obtain();
        message.what = 1;
        uiHandler.sendMessage(message);
        return roadOverlay;
    }

    /**
     * 绘制室内导航路线图
     *
     * @param mMapView  地图视图
     * @param uiHandler 用于更新UI的Handler
     * @param nodeLists 导航结点列表
     */
    public List<Polyline> drawIndoorNavRoute(final MapView mMapView, final Handler uiHandler,
                                             final List<List<GeoPoint>> nodeLists) {
        List<Polyline> navRoutes = new ArrayList<>();
        for (List<GeoPoint> nodeList : nodeLists) {
            if (nodeList.size() > 0) {
                Polyline indoorNavRoute = new Polyline();
                indoorNavRoute.setWidth(4);
                indoorNavRoute.setColor(0xFF1B7BCD);
                indoorNavRoute.setPoints(nodeList);
                navRoutes.add(indoorNavRoute);
                mMapView.getOverlays().add(indoorNavRoute);
            }
        }
        // 发送给主程序对地图进行更新
        Message message = Message.obtain();
        message.what = 1;
        uiHandler.sendMessage(message);
        return navRoutes;
    }

    /**
     * 绘制导航路径的起始点和终点
     *
     * @param mMapView   地图视图
     * @param uiHandler  用于更新UI的Handler
     * @param startPoint 起始点
     * @param endPoint   终点
     * @return 起始点和终点
     */
    public List<Marker> drawStartAndEndPoint(final MapView mMapView, final Handler uiHandler,
                                             final GeoPoint startPoint, final GeoPoint endPoint) {
        Marker startMarker = new Marker(mMapView);
        Marker endMarker = new Marker(mMapView);
        startMarker.setPosition(startPoint);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        mMapView.getOverlays().add(startMarker);
       mMapView.getOverlays().add(endMarker);
        // 发送给主程序对地图进行更新
        Message message = Message.obtain();
        message.what = 1;
        uiHandler.sendMessage(message);
        List<Marker> startAndDestMarker = new ArrayList<>();
        startAndDestMarker.add(startMarker);
        startAndDestMarker.add(endMarker);
        return startAndDestMarker;
    }

    /**
     * 移除导航路径
     *
     * @param mapView   地图
     * @param uiHandler 用于更新UI的Handler
     */
    public void removeNavRoute(MapView mapView, Handler uiHandler) {
        // 移除起始点和终点
        List<Marker> startAndDestMarker = NowNavRoute.getStartAndDestMarker();
        if (startAndDestMarker != null) {
            for (Marker marker : startAndDestMarker) {
                mapView.getOverlays().remove(marker);
            }
            NowNavRoute.setStartAndDestMarker(null);
        }
        // 移除室外导航路径
        Polyline outdoorNavRoute = NowNavRoute.getOutdoorNavRoute();
        if (outdoorNavRoute != null) {
            mapView.getOverlays().remove(outdoorNavRoute);
            NowNavRoute.setOutdoorNavRoute(null);
        }
        // 移除室内导航路径
        List<Polyline> indoorNavRoutes = NowNavRoute.getIndoorNavRoutes();
        if (indoorNavRoutes != null) {
            for (Polyline polyline : indoorNavRoutes) {
                mapView.getOverlays().remove(polyline);
            }
            NowNavRoute.setIndoorNavRoutes(null);
        }
        // 发送给主程序对地图进行更新
        Message message = Message.obtain();
        message.what = 1;
        uiHandler.sendMessage(message);
    }
}
