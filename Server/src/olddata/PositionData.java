package olddata;

public class PositionData {
    private int id = 0;
    private final int equipmentID;
    private double dataX = 0;
    private double dataY = 0;
    private double dataZ = 0;
    private static int flag = 0;

    public PositionData(int equipmentID, double dataX, double dataY, double dataZ) {
        id = ++flag;
        this.equipmentID = equipmentID;
        this.dataX = dataX;
        this.dataY = dataY;
        this.dataZ = dataZ;
    }

    public PositionData(int equipmentID, Double[] arrayData) {
        id = ++flag;
        this.equipmentID = equipmentID;
        this.dataX = arrayData[0];
        this.dataY = arrayData[1];
        this.dataZ = arrayData[2];
    }

    public PositionData(int id, int equipmentID, double dataX, double dataY, double dataZ) {
        this.id = id;
        this.equipmentID = equipmentID;
        this.dataX = dataX;
        this.dataY = dataY;
        this.dataZ = dataZ;
    }

    public PositionData(int id, int equipmentID, Double[] arrayData) {
        this.id = id;
        this.equipmentID = equipmentID;
        this.dataX = arrayData[0];
        this.dataY = arrayData[1];
        this.dataZ = arrayData[2];
    }

    public double getDataX() {
        return dataX;
    }

    public double getDataY() {
        return dataY;
    }

    public double getDataZ() {
        return dataZ;
    }

    public int getId() {
        return id;
    }

    public int getEquipmentID() {
        return equipmentID;
    }

    @Override
    public String toString() {
        return id + "," + equipmentID + "," + dataX + "," + dataY + "," + dataZ;

    }
}
