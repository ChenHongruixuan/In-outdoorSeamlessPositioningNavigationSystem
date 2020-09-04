package com.example.administrator.osmapitest.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.example.administrator.osmapitest.shared.NowNavRoute;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;


/**
 * 实现导航和路径规划的子线程
 *
 * @author Qchrx
 * @version 1.1
 * @date 2018/07/05
 */
public class NavigationThread implements Runnable {
    private Intent intent;
    private DrawNavRoute mDrawNavRoute;
    private Context context;
    private MapView mMapView;
    private Handler uiHandler;

    public NavigationThread(DrawNavRoute drawNavRoute, Context context,
                            MapView mapView, Handler handler) {
        mDrawNavRoute = drawNavRoute;
        this.context = context;
        mMapView = mapView;
        uiHandler = handler;
    }

    @Override
    public void run() {
        NavigationInfo navigationInfo =
                (NavigationInfo) intent.getSerializableExtra("nav_info");


        int statusCode = navigationInfo.getStatusCode();

        GeoPoint outdoorStartPoint = navigationInfo.getOutdoorStartNode();
        GeoPoint outdoorEndPoint = navigationInfo.getOutdoorDestNode();
        GeoPoint startPoint = navigationInfo.getStartNode();
        GeoPoint destPoint = navigationInfo.getDestNode();
        List<List<GeoPoint>> indoorNavNode = navigationInfo.getIndoorNavNodeLists();
        List<Marker> startAndDestMarker =
                mDrawNavRoute.drawStartAndEndPoint(mMapView, uiHandler, startPoint, destPoint);
        NowNavRoute.setStartAndDestMarker(startAndDestMarker);
        Polyline road;
        List<Polyline> navIndoorRoutes;
        switch (statusCode) {
            case 0: // 导航出错
                break;
            case 1: // 室外到室外
                road = mDrawNavRoute.drawOutdoorNavRoute(context,
                        mMapView, uiHandler, outdoorStartPoint, outdoorEndPoint);
                NowNavRoute.setOutdoorNavRoute(road);
                break;
            case 4: // 室内到室内
                navIndoorRoutes = mDrawNavRoute.drawIndoorNavRoute(
                        mMapView, uiHandler, indoorNavNode);
                NowNavRoute.setIndoorNavRoutes(navIndoorRoutes);
                break;
            case 2: // 室外到室内

            case 3: // 室内到室外

            case 5: // 室内到室外到室内

            default:
                road = mDrawNavRoute.drawOutdoorNavRoute(context,
                        mMapView, uiHandler, outdoorStartPoint, outdoorEndPoint);
                navIndoorRoutes = mDrawNavRoute.drawIndoorNavRoute(
                        mMapView, uiHandler, indoorNavNode);
                NowNavRoute.setOutdoorNavRoute(road);
                NowNavRoute.setIndoorNavRoutes(navIndoorRoutes);
                break;
        }
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
