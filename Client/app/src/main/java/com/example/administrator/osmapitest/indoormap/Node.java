package com.example.administrator.osmapitest.indoormap;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

/**
 * Data structure of indoor map nodes
 */
public class Node implements Serializable{
    private int id;
    /**
     * Attr represents the attributes of a node
     * Where 0 represents the common indoor point,
     * 1 represents the node at the junction of the current floor and the previous floor,
     * and 2 represents the node at the junction of the current floor and the next floor
     */
    private int attr;
    private GeoPoint geoPoint;

    public Node(int id, int attr, double latitude, double longitude) {
        this.id = id;
        this.attr = attr;
        geoPoint = new GeoPoint(latitude, longitude);
    }

    public Node() {
    }

    public int getId() {
        return id;
    }

    public int getAttr() {
        return attr;
    }

    public double getLatitude() {
        return geoPoint.getLatitude();
    }

    public double getLongitude() {
        return geoPoint.getLongitude();
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setAttr(int attr) {
        this.attr = attr;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setId(int id) {
        this.id = id;
    }
}
