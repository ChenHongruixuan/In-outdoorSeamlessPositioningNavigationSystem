package util.navigation;

/**
 * 导航基本信息
 */
public class NavBasicInfo {
    private String desAreaName; // 目标区域
    private double lat; // 起始地点纬度
    private double lon;    // 起始地点经度
    private int floor;  // 楼层
    private boolean indoor; // 起始地点是否在室内

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getFloor() {
        return floor;
    }

    public String getDesAreaName() {
        return desAreaName;
    }

    public boolean isIndoor() {
        return indoor;
    }

    public void setIndoor(boolean indoor) {
        this.indoor = indoor;
    }

    public void setDesAreaName(String desAreaName) {
        this.desAreaName = desAreaName;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}

