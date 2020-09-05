package com.example.administrator.datapick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.util.SystemUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Wifi采集活动
 */
public class WifiDataPick extends AppCompatActivity implements View.OnClickListener,
        SensorEventListener {

    private String imei;
    private int scanCount;
    private boolean isUiThreadStop = false;
    private static String TAG = "WifiDataPick";
    private float[] tempOri;

    private TextView wifiTextView;
    private EditText xEdit, yEdit, xInterval, yInterval;

    private WifiManager wifiManager;

    private SensorManager mSensorManager;   // 声明传感器管理对象

    private OkHttpClient myOkHttpClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_data_pick);
        imei = SystemUtil.getIMEI(getApplicationContext());
        initView();
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // OkHttpClient初始化
        myOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)       // 设置连接超时
                .readTimeout(15, TimeUnit.SECONDS)         // 设置读超时
                .writeTimeout(15, TimeUnit.SECONDS)        // 设置写超时
                .build();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!Objects.requireNonNull(wifiManager).isWifiEnabled())
            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isUiThreadStop) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wifiTextView.setText(obtainWifiInfo());
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "sleep error:" + e.getMessage());
                    }
                }
            }
        }).start();

    }

    /**
     * 控件初始化
     */
    private void initView() {
        xEdit = findViewById(R.id.x_position);
        yEdit = findViewById(R.id.y_position);
        xInterval = findViewById(R.id.x_interval);
        yInterval = findViewById(R.id.y_interval);
        wifiTextView = findViewById(R.id.wifi_information_exhibit);
        Button sendWifi = findViewById(R.id.wifi_send);    // 发送按钮
        Button wifiBack = findViewById(R.id.wifi_back);    // 返回按钮
        Button addX = findViewById(R.id.add_value_x);      // x步进按钮
        Button addY = findViewById(R.id.add_value_y);      // y步进按钮
        // 给按钮注册事件
        wifiBack.setOnClickListener(this);
        addX.setOnClickListener(this);
        addY.setOnClickListener(this);
        sendWifi.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wifi_send:    // 发送数据按钮
                // 当x坐标或y坐标有未输入的情况，弹出警告框
                if (TextUtils.isEmpty(xEdit.getText()) || TextUtils.isEmpty(yEdit.getText())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(WifiDataPick.this);
                    dialog.setTitle("系统提示").setMessage("x坐标和y坐标至少有一个未输入!")
                            .setCancelable(false).setPositiveButton("OK", null).show();
                    break;
                }
                sendWifiData();
                break;
            case R.id.add_value_x:  // x步进按钮
                // 未输入x坐标步进值，弹出警告框
                if (TextUtils.isEmpty(xInterval.getText())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(WifiDataPick.this);
                    dialog.setTitle("系统提示").setMessage("未输入x坐标步进值!")
                            .setCancelable(false).setPositiveButton("OK", null).show();
                    break;
                }
                float tempX = Float.valueOf(xEdit.getText().toString());
                float intervalX = Float.valueOf(xInterval.getText().toString());
                xEdit.setText(String.valueOf(tempX + intervalX));
                break;

            case R.id.add_value_y:  // y步进按钮
                // 未输入y坐标步进值，弹出警告框
                if (TextUtils.isEmpty(yInterval.getText())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(WifiDataPick.this);
                    dialog.setTitle("系统提示").setMessage("未输入y坐标步进值!")
                            .setCancelable(false).setPositiveButton("OK", null).show();
                    break;
                }
                double tempY = Double.valueOf(yEdit.getText().toString());
                double intervalY = Double.valueOf(yInterval.getText().toString());
                yEdit.setText(String.valueOf(tempY + intervalY));
                break;
            case R.id.wifi_back:
                isUiThreadStop = true;
                finish();
                break;
        }
    }

    /**
     * 利用Post请求向服务器发送Wifi数据
     */
    private void sendWifiData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wifiManager.startScan();
                    List<ScanResult> wifiList = wifiManager.getScanResults();
                    for (ScanResult scanResult : wifiList) {
                        String SSID = scanResult.SSID;
                        String BSSID = scanResult.BSSID;
                        int level = scanResult.level;
                        // 构造请求对象
                        RequestBody sendWifiDataRequestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("type", "wifi")
                                .addFormDataPart("imei", imei)
                                .addFormDataPart("x_pos", xEdit.getText().toString())
                                .addFormDataPart("y_pos", yEdit.getText().toString())
                                .addFormDataPart("ori", String.valueOf(tempOri[0]))
                                .addFormDataPart("ssid", SSID)
                                .addFormDataPart("bssid", BSSID)
                                .addFormDataPart("wifi_level", String.valueOf(level))
                                .build();
                        // 构造请求对象
                        Request sendWifiDataRequest = new Request.Builder()
                                //.url("http://quantum.s1.natapp.cc/SeamlessPositioning/servlet/LocateDataPickServlet")
                                .url("http://quantum.s1.natapp.cc/servlet/LocateDataPickServlet")
                                .post(sendWifiDataRequestBody)
                                .build();
                        myOkHttpClient.newCall(sendWifiDataRequest).execute();
                    }
                } catch (IOException error) {
                    error.printStackTrace();
                    Log.e(TAG, "Wifi数据发送失败：" + error.getMessage(), error);
                }
            }
        }).start();
    }

    /**
     * 获取wifi信息
     *
     * @return wifi信息字符串
     */
    private String obtainWifiInfo() {
        wifiManager.startScan();
        List<ScanResult> wifiList = wifiManager.getScanResults();
        scanCount++;
        StringBuilder wifiInformation = new StringBuilder(scanCount + "\nScan result:");

        for (ScanResult scanResult : wifiList)
            wifiInformation.append("\nwifi网络ID：").append(scanResult.SSID).
                    append("\nMac地址:").append(scanResult.BSSID).
                    append("\nWifi信号强度：").append(scanResult.level).append("\n");
        return wifiInformation.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_ORIENTATION:
                tempOri = event.values;
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    // 重写变化

    }
}
