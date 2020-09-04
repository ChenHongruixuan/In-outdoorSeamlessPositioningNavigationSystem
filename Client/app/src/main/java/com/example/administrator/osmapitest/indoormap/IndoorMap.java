package com.example.administrator.osmapitest.indoormap;

import android.util.Log;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Data structure of indoor map
 *
 * @author Qchrx
 * @version 1.2
 * @date 2018/5/18
 */
public class IndoorMap implements Serializable {
    private String mapName; // Name of indoor map
    private int floor;  // Floor of indoor map
    private List<Node> ioMap = new ArrayList<>();   // IOMap
    private List<Node> baseMap = new ArrayList<>();             // Basic map
    private List<List<Node>> businessMap = new ArrayList<>();   // Business map
    private static String TAG = "IndoorMap";

    /*
      The node at the floor junction,
      where true represents the node between the previous floor and false represents the node with the next floor
     */
    private List<Map<GeoPoint, Boolean>> floorSwitchNode = new ArrayList<>();
    private List<IGeoPoint> areaNameList = new ArrayList<>();  // Name of indoor area

    public IndoorMap(String xmlStr) {
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new ByteArrayInputStream(xmlStr.getBytes("utf-8")));
            Element mapStore = document.getRootElement();
            Iterator storeIt = mapStore.elementIterator();
            while (storeIt.hasNext()) {
                Element mapElement = (Element) storeIt.next();  // Traversing the first level child node
                // Traversing attributes
                switch (mapElement.getName()) {
                    case "map_name":
                        mapName = mapElement.attribute(0).getValue();
                        break;
                    case "floor":
                        floor = Integer.valueOf(mapElement.attribute(0).getValue());
                        break;
                    case "io_map":
                        initBasicMap(mapElement.elementIterator(), ioMap);
                        break;
                    case "base_map":
                        initBasicMap(mapElement.elementIterator(), baseMap);
                        break;
                    case "business_map":
                        initBusinessMap(mapElement.elementIterator(), businessMap, areaNameList);
                    default:
                        break;
                }
            }
        } catch (DocumentException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Initialize basic map of indoor map
     *
     * @param nodeIt XML file node iterator
     * @param map    Basic map
     */
    private void initBasicMap(Iterator nodeIt, List<Node> map) {
        while (nodeIt.hasNext()) {
            Element nodeElement = (Element) nodeIt.next();

            List<Attribute> nodeAttributes = nodeElement.attributes();
            Node node = readNode(nodeAttributes);
            map.add(node);
        }
    }

    /**
     * Initialize business map of indoor map
     *
     * @param areaIt       XML file node iterator
     * @param map          Business map
     * @param areaNameList Area name
     */
    private void initBusinessMap(Iterator areaIt, List<List<Node>> map, List<IGeoPoint> areaNameList) {
        while (areaIt.hasNext()) {
            Element areaElement = (Element) areaIt.next();  // Traversing the first level child node
            List<Attribute> areaAttributes = areaElement.attributes();  // Get area attributes
            readArea(areaAttributes, areaNameList);
            Iterator nodeIt = areaElement.elementIterator();
            List<Node> busMapElem = new ArrayList<>();
            while (nodeIt.hasNext()) {
                Element nodeElement = (Element) nodeIt.next();

                List<Attribute> nodeAttributes = nodeElement.attributes();
                Node node = readNode(nodeAttributes);
                busMapElem.add(node);
            }
            map.add(busMapElem);
        }
    }

    /**
     * Parse node
     *
     * @param nodeAttributes Attribute set of node
     * @return Node
     */
    private Node readNode(List<Attribute> nodeAttributes) {
        double lat = 0.0, lon = 0.0;
        Node node = new Node();
        for (Attribute nodeAttribute : nodeAttributes) {
            switch (nodeAttribute.getName()) {
                case "id":
                    node.setId(Integer.valueOf(nodeAttribute.getValue()));
                    break;
                case "attr":
                    node.setAttr(Integer.valueOf(nodeAttribute.getValue()));
                    break;
                case "latitude":
                    lat = Double.valueOf(nodeAttribute.getValue());
                    break;
                case "longitude":
                    lon = Double.valueOf(nodeAttribute.getValue());
                    break;
                default:
                    break;
            }
        }
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        node.setGeoPoint(geoPoint);
        return node;
    }

    private void readArea(List<Attribute> areaAttributes, List<IGeoPoint> areaNameList) {
        double lat = 0.0, lon = 0.0;
        String areaName = "";
        for (Attribute areaAttribute : areaAttributes) {
            switch (areaAttribute.getName()) {
                case "name":
                    areaName = areaAttribute.getValue();
                    break;
                case "latitude":
                    lat = Double.valueOf(areaAttribute.getValue());
                    break;
                case "longitude":
                    lon = Double.valueOf(areaAttribute.getValue());
                    break;
                default:
                    break;
            }
        }
        LabelledGeoPoint geoPoint = new LabelledGeoPoint(lat, lon, areaName);
        areaNameList.add(geoPoint);
    }


    public String getMapName() {
        return mapName;
    }


    public int getFloor() {
        return floor;
    }


    public List<Node> getIoMap() {
        return ioMap;
    }


    public List<Node> getBaseMap() {
        return baseMap;
    }


    public List<List<Node>> getBusinessMap() {
        return businessMap;
    }


    public List<Map<GeoPoint, Boolean>> getFloorSwitchNode() {
        return floorSwitchNode;
    }


    public List<IGeoPoint> getAreaNameList() {
        return areaNameList;
    }
}
