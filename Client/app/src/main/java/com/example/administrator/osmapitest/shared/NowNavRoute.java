package com.example.administrator.osmapitest.shared;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

/**
 * 代表当前的导航路径
 */
public class NowNavRoute {
    private static List<Marker> startAndDestMarker;

    private static List<Polyline> indoorNavRoutes;

    private static Polyline outdoorNavRoute;

    public static void setOutdoorNavRoute(Polyline outdoorNavRoute) {
        NowNavRoute.outdoorNavRoute = outdoorNavRoute;
    }

    public static Polyline getOutdoorNavRoute() {
        return outdoorNavRoute;
    }

    public static void setIndoorNavRoutes(List<Polyline> indoorNavRoutes) {
        NowNavRoute.indoorNavRoutes = indoorNavRoutes;
    }

    public static List<Polyline> getIndoorNavRoutes() {
        return indoorNavRoutes;
    }

    public static List<Marker> getStartAndDestMarker() {
        return startAndDestMarker;
    }

    public static void setStartAndDestMarker(List<Marker> startAndDestMarker) {
        NowNavRoute.startAndDestMarker = startAndDestMarker;
    }
}
