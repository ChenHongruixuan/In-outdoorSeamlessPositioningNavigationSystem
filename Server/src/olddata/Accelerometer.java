package olddata;

/**
 * 这个类用作存储加速度
 *
 * @author Qchrx
 */
public class Accelerometer extends PositionData {
    public Accelerometer(int equipmentID, double accX, double accY, double accZ) {
        super(equipmentID, accX, accY, accZ);
    }

    public Accelerometer(int equipmentID, Double[] arrayAcc) {
        super(equipmentID, arrayAcc);
    }

    public Accelerometer(int id, int equipmentID, double accX, double accY, double accZ) {
        super(id, equipmentID, accX, accY, accZ);
    }

    public Accelerometer(int id, int equipmentID, Double[] arrayAcc) {
        super(id, equipmentID, arrayAcc);
    }
}
