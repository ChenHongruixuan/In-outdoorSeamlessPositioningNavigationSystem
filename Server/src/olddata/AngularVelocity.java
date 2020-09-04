package olddata;

/**
 * 这个类用作存储角速度
 *
 * @author Qchrx
 */
public class AngularVelocity extends PositionData {

    public AngularVelocity(int equipmentID, double angX, double angY, double angZ) {
        super(equipmentID,angX,angY,angZ);
    }

    public AngularVelocity(int equipmentID, Double[] arrayAng) {
        super(equipmentID,arrayAng);
    }

    public AngularVelocity(int id, int equipmentID, double angX, double angY, double angZ) {
        super(id,equipmentID,angX,angY,angZ);
    }

    public AngularVelocity(int id, int equipmentID, Double[] arrayAng) {
        super(id,equipmentID,arrayAng);
    }
}

