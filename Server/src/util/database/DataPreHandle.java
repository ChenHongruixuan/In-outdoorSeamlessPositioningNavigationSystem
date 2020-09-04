package util.database;

import data.WifiDataContainer;

import java.util.List;

/**
 * 对服务器发送的数据进行预处理
 */
public class DataPreHandle {

    public WifiDataContainer preHandleWifiData(List<String> bssidList, List<Float> levelList,
                                               List<String> wifiFingerBssidList) {
        for (int i = 0; i < bssidList.size(); i++) {
            if (!wifiFingerBssidList.contains(bssidList.get(i))) {
                bssidList.remove(i);
                levelList.remove(i);
                i--;
            }
        }

        // 补充未扫描到的wifi源，将其信号强度置为-100
        for (String wifiFingerBssid : wifiFingerBssidList) {
            if (!bssidList.contains(wifiFingerBssid)) {
                bssidList.add(wifiFingerBssid);
                levelList.add(-100f);
            }
        }
        String[] bssidArray = bssidList.toArray(new String[0]);
        Float[] levelArray = levelList.toArray(new Float[bssidList.size()]);
        return new WifiDataContainer(bssidArray, levelArray);
    }
}
