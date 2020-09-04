package data;

/**
 * 磁场数据
 *
 * @author Qchrx
 */
public class MagData {
    private int id = 0;
    private String imei;
    private float X = 0;
    private float Y = 0;
    private float ori = 0;
    private double T = 0;
    private static int flag = 0;

    public MagData(String imei, float X, float Y, float ori, double T) {
        id = ++flag;
        this.imei = imei;
        this.X = X;
        this.Y = Y;
        this.ori = ori;
        this.T = T;
    }


    public MagData(String imei, Float[] arrayT) {
        id = ++flag;
        this.imei = imei;
        this.X = arrayT[0];
        this.Y = arrayT[1];
        this.ori = arrayT[2];
        this.T = arrayT[3];
    }

    public MagData(int id, String imei, float X, float Y, float ori, double T) {
        this.id = id;
        this.imei = imei;
        this.X = X;
        this.Y = Y;
        this.ori = ori;
        this.T = T;
    }


    public MagData(int id, String imei, Float[] arrayT) {
        this.id = id;
        this.imei = imei;
        this.X = arrayT[0];
        this.Y = arrayT[1];
        this.ori = arrayT[2];
        this.T = arrayT[3];
    }

    public float getX() {
        return X;
    }

    public float getY() {
        return Y;
    }

    public double getT() {
        return T;
    }

    public int getId() {
        return id;
    }

    public String getImei() {
        return imei;
    }

    public float getOri() {
        return ori;
    }

    public String toString() {
        return id + "," + imei + "," + X + "," + Y + "," + ori + "," + T;
    }
}
