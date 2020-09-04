package data;


import java.util.ArrayList;
import java.util.List;

/**
 * 导航过程中的结点
 */
public class Node {
    private int id; // 结点id

    private double latitude;    // 纬度
    private double longitude;   // 经度
    private List<Integer> adjNodeId;    // 邻接点的id

    public Node(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Node(int id, double latitude, double longitude, List<Integer> adjNodeId) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adjNodeId = adjNodeId;
    }

    public Node(int id, double latitude, double longitude, String initAdjNodeIdStr) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        String[] adjNodeIdStrArr = initAdjNodeIdStr.split(",");
        adjNodeId = new ArrayList<>();
        for (String adjNodeIdStr : adjNodeIdStrArr) {
            adjNodeId.add(Integer.valueOf(adjNodeIdStr));
        }
    }

    public Node() {
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getAdjNodeId() {
        return adjNodeId;
    }

    public void setAdjNodeId(List<Integer> adjNodeId) {
        this.adjNodeId = adjNodeId;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}

