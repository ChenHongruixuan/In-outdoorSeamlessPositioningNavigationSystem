package data;

public class ClientPos {
    private double latitude;    // 纬度
    private double longitude;   // 经度
    private boolean isIndoor;   // 是否在室内
    private int floor;  // 楼层

    public ClientPos() {
    }

    public ClientPos(double longitude, double latitude, boolean isIndoor) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.isIndoor = isIndoor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setIsIndoor(boolean indoor) {
        isIndoor = indoor;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean getIsIndoor() {
        return isIndoor;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
