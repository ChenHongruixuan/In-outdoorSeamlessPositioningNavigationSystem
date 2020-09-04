package olddata;

import java.util.List;

public class SpatialPosition {
    private double xPos;
    private double yPos;
    private float levelVar;
    private int floor;

    public SpatialPosition(List<SpatialPosition> spatialPositionList) {
        float sumX = 0, sumY = 0;
        for (SpatialPosition spatialPosition : spatialPositionList) {
            sumX += spatialPosition.getXPos();
            sumY += spatialPosition.getYPos();
        }
        this.xPos = sumX / spatialPositionList.size();
        this.yPos = sumY / spatialPositionList.size();
    }

    public SpatialPosition(double xPos, double yPos) {
        this(xPos, yPos, 0, 0);
    }

    public SpatialPosition(double xPos, double yPos,int floor) {
        this(xPos, yPos, floor, 0);
    }

    public SpatialPosition(double xPos, double yPos, int floor, float levelVar) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.levelVar = levelVar;
        this.floor = floor;
    }

    public double getXPos() {
        return xPos;
    }

    public double getYPos() {
        return yPos;
    }

    public float getLevelVar() {
        return levelVar;
    }

    public String toString() {
        return xPos + "," + yPos + "," + floor;
    }
}
