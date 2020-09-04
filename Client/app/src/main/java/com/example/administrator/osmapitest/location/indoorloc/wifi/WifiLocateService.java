package com.example.administrator.osmapitest.location.indoorloc.wifi;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.osmapitest.data.ClientPos;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * WifiDataTransService sends the WiFi data scanned by the client to the server
 * The data format of the uploaded WiFi signal is ：bssid,level \n bssid,level \n...
 *
 * @author Qchrx
 * @version 1.3
 * @date 2018/06/01
 */
public class WifiLocateService extends IntentService {
    private WifiManager mWifiManager;
    // private boolean isPickWifiStop = false;
    private static String TAG = "WifiLocateService";

    public WifiLocateService() {
        super("WifiLocateService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!Objects.requireNonNull(mWifiManager).isWifiEnabled())
            if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                mWifiManager.setWifiEnabled(true);
        mWifiManager.startScan();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        WifiLocateCallback wifiLocateCallback = new WifiLocateCallback();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        mWifiManager.startScan();
        for (int i = 1; i <= 7; i++) {
//            if (Status.isStill()) {
            List<ScanResult> wifiList = mWifiManager.getScanResults();
            StringBuilder wifiDataStrBuilder = new StringBuilder();
            for (ScanResult scanResult : wifiList) {
                String bssid = scanResult.BSSID;
                int level = scanResult.level;
                wifiDataStrBuilder.append(bssid).append(",").append(level).append("\n");
            }

            RequestBody wifiDataRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("wifi_data", wifiDataStrBuilder.toString())
                    .build();
            Request wifiDataRequest = new Request.Builder()
                    // .url("http://quantum.s1.natapp.cc/SeamlessPositioning/servlet/IndoorLocateHttpServlet")
                    .url("http://quantum.s1.natapp.cc/servlet/IndoorLocateHttpServlet")
                    .post(wifiDataRequestBody)
                    .build();

            try {
                client.newCall(wifiDataRequest).enqueue(wifiLocateCallback);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "线程休眠出错：" + e.getMessage(), e);
            }
            wifiList.clear();
        }
    }
//        }
//    }

    private class WifiLocateCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
            Log.e(TAG, "数据上传出错：" + e.getMessage(), e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String[] wifiLocResult = response.body().string().split(",");
            double posLat = Double.valueOf(wifiLocResult[0]);
            double posLon = Double.valueOf(wifiLocResult[1]);
            int floor = Integer.valueOf(wifiLocResult[2]);
            ClientPos ClientPos = new ClientPos(posLat, posLon, floor);
            ClientPos.setProvider("Wifi");
            Intent posIntent = new Intent("locate");
            posIntent.putExtra("pos_data", ClientPos);
            sendBroadcast(posIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // isPickWifiStop = true;
    }
}
