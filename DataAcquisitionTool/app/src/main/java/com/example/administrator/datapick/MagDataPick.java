package com.example.administrator.datapick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MagDataPick extends AppCompatActivity
        implements View.OnClickListener, SensorEventListener {  // 磁场采集活动

    private TextView magView;
    private EditText xEdit, yEdit, xInterval, yInterval;

    private SensorManager mSensorManager;   // 声明传感器管理对象
    private float[] tempT;  // 存储磁场的数组
    private float[] tempOri;    //
    private OkHttpClient myOkHttpClient;

    private static String TAG = "MagDataPick";
    private String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_data_pick);
        imei = SystemUtil.getIMEI(getApplicationContext());
        // 初始化按钮和文本框
        magView = findViewById(R.id.mag_view);
        xEdit = findViewById(R.id.x_position);
        yEdit = findViewById(R.id.y_position);
        xInterval = findViewById(R.id.x_interval);
        yInterval = findViewById(R.id.y_interval);
        Button sendMag = findViewById(R.id.mag_send);     // 发送按钮
        Button getMag = findViewById(R.id.mag_get);        // 采集按钮
        Button magBack = findViewById(R.id.mag_back);     // 返回按钮
        Button addX = findViewById(R.id.add_value_x);     // x步进按钮
        Button addY = findViewById(R.id.add_value_y);     // y步进按钮
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // OkHttpClient初始化
        myOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)       // 设置连接超时
                .readTimeout(30, TimeUnit.SECONDS)         // 设置读超时
                .writeTimeout(30, TimeUnit.SECONDS)        // 设置写超时
                .build();
        // 给按钮注册点击事件
        sendMag.setOnClickListener(this);
        getMag.setOnClickListener(this);
        magBack.setOnClickListener(this);
        addX.setOnClickListener(this);
        addY.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {   // 按钮点击事件函数
        switch (v.getId()) {
            case R.id.mag_send: // 发送按钮
                // 当x坐标或y坐标有未输入的情况，弹出警告框
                if (TextUtils.isEmpty(xEdit.getText()) || TextUtils.isEmpty(yEdit.getText())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MagDataPick.this);
                    dialog.setTitle("系统提示").setMessage("x坐标和y坐标至少有一个未输入!")
                            .setCancelable(false).setPositiveButton("OK", null).show();
                    break;
                }
                sendMagData();
                break;
            case R.id.mag_back: // 返回按钮
                finish();
                break;

            case R.id.add_value_x:  // x步进按钮
                // 未输入x坐标步进值，弹出警告框
                if (TextUtils.isEmpty(xInterval.getText())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MagDataPick.this);
                    dialog.setTitle("系统提示").setMessage("未输入x坐标步进值!")
                            .setCancelable(false).setPositiveButton("OK", null).show();
                    break;
                }
                double tempX = Double.valueOf(xEdit.getText().toString());
                double intervalX = Double.valueOf(xInterval.getText().toString());
                xEdit.setText(String.valueOf(tempX + intervalX));
                break;

            case R.id.add_value_y:  // y步进按钮
                // 未输入y坐标步进值，弹出警告框
                if (TextUtils.isEmpty(yInterval.getText())) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MagDataPick.this);
                    dialog.setTitle("系统提示").setMessage("未输入y坐标步进值!")
                            .setCancelable(false).setPositiveButton("OK", null).show();
                    break;
                }
                double tempY = Double.valueOf(yEdit.getText().toString());
                double intervalY = Double.valueOf(yInterval.getText().toString());
                yEdit.setText(String.valueOf(tempY + intervalY));
                break;
        }
    }

    private void sendMagData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                double magLevel =
                        Math.sqrt(tempT[0] * tempT[0] + tempT[1] * tempT[1] + tempT[2] * tempT[2]);
                // 构造请求对象
                RequestBody sendMagDataRequestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", "mag")
                        .addFormDataPart("imei", imei)
                        .addFormDataPart("x_pos", xEdit.getText().toString())
                        .addFormDataPart("y_pos", yEdit.getText().toString())
                        .addFormDataPart("ori", String.valueOf(tempOri[0]))
                        .addFormDataPart("mag_level", String.valueOf(magLevel))
                        .build();
                // 构造请求对象
                Request sendMagDataRequest = new Request.Builder()
//                        .url("http://quantum.s1.natapp.cc/SeamlessPositioning/servlet/LocateDataPickServlet")
                        .url("http://quantum.s1.natapp.cc/servlet/LocateDataPickServlet")
                        .post(sendMagDataRequestBody)
                        .build();
                try {
                    myOkHttpClient.newCall(sendMagDataRequest).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "磁场数据发送失败：" + e.getMessage(), e);
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为磁场传感器注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                // 得到磁场的值
                tempT = event.values;
                String mag = "磁场传感器返回的数据："
                        + "\nX方向的磁场：" + tempT[0]
                        + "\nY方向的磁场：" + tempT[1]
                        + "\nZ方向的磁场：" + tempT[2];
                magView.setText(mag);
                break;
            case Sensor.TYPE_ORIENTATION:
                // 得到磁场的值
                tempOri = event.values;
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    // 重写变化

    }
}
