package data;

/**
 * wifi数据
 *
 * @author Qchrx
 */
public class WifiData {
    private int id = 0;
    private String imei;
    private float X = 0;
    private float Y = 0;
    private float ori = 0;
    private int level = 0;
    private String ssid;
    private String bssid;


    public WifiData(int id, String imei, float X, float Y, float ori, String ssid, String BSSID, int level) {
        this.id = id;
        this.imei = imei;
        this.X = X;
        this.Y = Y;
        this.ori = ori;
        this.ssid = ssid;
        this.bssid = BSSID;
        this.level = level;
    }

    public String toString() {
        if (ssid.contains("'")) {
            ssid.replace("\'", "\\'");
            return id + "," + imei + "," + X + "," + Y + "," + ori + "," +
                    "'" + ssid + "'" + "," + "'" + bssid + "'" + "," + level;
        } else
            return id + "," + imei + "," + X + "," + Y + "," + ori + "," +
                    "'" + ssid + "'" + "," + "'" + bssid + "'" + "," + level;
    }
}
