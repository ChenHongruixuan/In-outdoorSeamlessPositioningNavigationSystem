package com.example.administrator.osmapitest.indoormap;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;

import com.example.administrator.osmapitest.shared.NowIndoorMap;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.ArrayList;
import java.util.List;


/**
 * Draw indoor map
 */
public class DrawIndoorMap {
    private MapView mMapView;
    private Handler uiHandler;
    private static String TAG = "DrawIndoorMap";

    public DrawIndoorMap(MapView mapView, Handler uiHandler) {
        this.mMapView = mapView;
        this.uiHandler = uiHandler;
    }

    /**
     * Draw indoor map
     *
     * @param mIndoorMap Indoor map
     */
    public void drawIndoorMap(final IndoorMap mIndoorMap) {
        // Draw IOMap
        int ioMapColor = Color.argb(255, 204, 233, 163);
        Polygon ioMapPoly = drawBasicMap(mIndoorMap.getIoMap(), ioMapColor, "IOMap");
        NowIndoorMap.setNIOMap(ioMapPoly);
        // Draw base map
        int baseMapColor = Color.argb(255, 255, 255, 248);
        Polygon baseMapPoly = drawBasicMap(mIndoorMap.getBaseMap(), baseMapColor, "BaseMap");
        NowIndoorMap.setNBaseMap(baseMapPoly);
        // Draw business map
        List<List<Node>> busMap = mIndoorMap.getBusinessMap();
        List<Polygon> busMapPoly = new ArrayList<>();
        int busMapColor = Color.argb(255, 238, 236, 234);
        for (List<Node> busMapElem : busMap) {
            Polygon busMapElemPoly = drawBasicMap(busMapElem, busMapColor, "businessMap");
            busMapPoly.add(busMapElemPoly);
        }
        NowIndoorMap.setNBusinessMap(busMapPoly);
        // Draw area names in business map
        NowIndoorMap.setAreaName(drawAreaName(mIndoorMap.getAreaNameList()));
        Message message = Message.obtain();
        message.what = 1;
        uiHandler.sendMessage(message);
    }

    /**
     * Draw map
     *
     * @param basicMap map
     */
    private Polygon drawBasicMap(final List<Node> basicMap, int argb, String title) {
        List<GeoPoint> geoPointList = new ArrayList<>();
        for (Node mapNode : basicMap) {
            geoPointList.add(mapNode.getGeoPoint());
        }
        Polygon polygon = new Polygon();
        polygon.setStrokeWidth(1);
        polygon.setFillColor(argb);
        geoPointList.add(geoPointList.get(0));
        polygon.setPoints(geoPointList);
        polygon.setTitle(title);
        mMapView.getOverlays().add(polygon);
        return polygon;
    }

    /**
     *  Draw area names in business map
     *
     * @param areaNameList  Area names
     */
    private SimpleFastPointOverlay drawAreaName(List<IGeoPoint> areaNameList) {
        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(Color.parseColor("#0000ff"));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(24);

        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setIsClickable(false).setTextStyle(textStyle);

        SimplePointTheme simplePointTheme = new SimplePointTheme(areaNameList, true);
        final SimpleFastPointOverlay simpleFastPointOverlay = new SimpleFastPointOverlay(simplePointTheme, opt);
        mMapView.getOverlays().add(simpleFastPointOverlay);
        return simpleFastPointOverlay;
    }

    /**
     * Remove indoor map
     */
    public void removeIndoorMap() {

        Polygon ioMapPoly = NowIndoorMap.getNIOMap();
        if (ioMapPoly != null) {
            mMapView.getOverlays().remove(ioMapPoly);
            NowIndoorMap.setNIOMap(null);
        }

        Polygon baseMapPoly = NowIndoorMap.getNBaseMap();
        if (baseMapPoly != null) {
            mMapView.getOverlays().remove(baseMapPoly);
            NowIndoorMap.setNBaseMap(null);
        }

        List<Polygon> busMapPoly = NowIndoorMap.getNBusinessMap();
        if (busMapPoly != null) {
            for (Polygon busMapElemPoly : busMapPoly) {
                mMapView.getOverlays().remove(busMapElemPoly);
            }
            NowIndoorMap.setNBusinessMap(null);
        }

        SimpleFastPointOverlay simpleFastPointOverlay = NowIndoorMap.getAreaName();
        if (simpleFastPointOverlay != null) {
            mMapView.getOverlays().remove(simpleFastPointOverlay);
            NowIndoorMap.setAreaName(null);
        }
        Message message = Message.obtain();
        message.what = 1;
        uiHandler.sendMessage(message);
    }
}
