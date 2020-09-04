package olddata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WifiFingerBssidContainer作为一个容器，持有作为wifi定位源的AP的RSSID
 * 使得各模块之间能够共享
 */
public class WifiFingerBssidContainer {
    private List<String> wifiFingerBssidList;

    public WifiFingerBssidContainer(List<String> wifiFingerBssidList) {
        this.wifiFingerBssidList = wifiFingerBssidList;
    }

    public WifiFingerBssidContainer(String... wifiFingerBssidArray) {
        wifiFingerBssidList = new ArrayList<>(Arrays.asList(wifiFingerBssidArray));
    }

    public List<String> getWifiFingerBssidList() {
        return wifiFingerBssidList;
    }

    @Override
    public String toString() {
        return wifiFingerBssidList.toString();
    }
}
