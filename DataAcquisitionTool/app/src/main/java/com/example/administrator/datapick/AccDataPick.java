package com.example.administrator.datapick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccDataPick extends AppCompatActivity
        implements View.OnClickListener, SensorEventListener {  // 加速度和角速度采集活动

    private EditText freEditText;
    // 声明加速度和角速度显示的文本框
    private TextView accView, gyrView, oriView;

    // 声明传感器管理对象
    private SensorManager aSensorManager;
    // 存储加速度,角速度和方向的数组
    private float[] tempValueAcc, tempValueOri, tempValueGyr;

    // 输出流对象
    private DataOutputStream outAcc, outGyr;
    // 直接采用线程池进行线程管理
    private ExecutorService mThreadPool;
    // 用作判断线程是否结束的标志
    private boolean isStart = true;
    private volatile boolean isStop = false;

    private static String TAG = "AccDataPick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_data_pick);

        // 初始化按钮和文本框
        freEditText = findViewById(R.id.pick_fre);
        accView = findViewById(R.id.acc_view);
        gyrView = findViewById(R.id.gyr_view);
        oriView = findViewById(R.id.ori_view);
        Button accStart = findViewById(R.id.start_pick);
        Button accStop = findViewById(R.id.stop_pick);
        Button accBack = findViewById(R.id.acc_back);

        // 获取传感器管理对象
        aSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();

        // 给按钮注册点击事件
        accStart.setOnClickListener(this);
        accStop.setOnClickListener(this);
        accBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 开始采集
            case R.id.start_pick:
//                if (isStart) {
//                    Toast.makeText(AccDataPick.this,
//                            "开始采集", Toast.LENGTH_SHORT).show();
//                    final int pickFre;  // 采样频率
//                    if (TextUtils.isEmpty(freEditText.getText())) {
//                        pickFre = 20;
//                    } else {
//                        pickFre = Integer.valueOf(freEditText.getText().toString());
//                    }
//                    mThreadPool.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            while (!isStop) {
//                                try {
//                                    // 向服务器端发送数据
//                                    outAcc = new DataOutputStream(SocketService.getSocket().getOutputStream());
//                                    outGyr = new DataOutputStream(SocketService.getSocket().getOutputStream());
//                                    outAcc.writeUTF("Acc" + "," + tempValueAcc[0] + "," +
//                                            tempValueAcc[1] + "," + tempValueAcc[2]);
//                                    outGyr.writeUTF("Gyr" + "," + tempValueGyr[0] + "," +
//                                            tempValueGyr[1] + "," + tempValueGyr[2]);
//                                    outAcc.flush();
//                                    outGyr.flush();
//                                    // 休眠进程，实现50HZ的采样频率
//                                    sleep(pickFre);
//                                } catch (Exception error) {
//                                    error.printStackTrace();
//                                    Log.e(TAG, "send error:" + error.getMessage());
//                                }
//                            }
//                        }
//                    });
//                    isStart = false;
//                }
                break;
            // 停止采集
            case R.id.stop_pick:
                if (!isStart) {
                    isStart = true;
                    isStop = true;  // 数据采集进程终止
                    Toast.makeText(AccDataPick.this,
                            "停止采集", Toast.LENGTH_SHORT).show();
                }
                break;
            // 返回
            case R.id.acc_back:
                isStop = true;  // 停止子线程
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        isStop = true;  // 停止子线程
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为加速度传感器注册监听器
        aSensorManager.registerListener(this,
                aSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        // 为陀螺仪传感器注册监听器
        aSensorManager.registerListener(this,
                aSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);
        aSensorManager.registerListener(this,
                aSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) { // 当传感器的值改变的时候回调该方法
        int type = event.sensor.getType();
        switch (type) {
            // 加速度传感器
            case Sensor.TYPE_ACCELEROMETER:
                tempValueAcc = event.values;
                String acc = "加速度传感器返回的数据："
                        + "\nX方向的加速度：" + tempValueAcc[0]
                        + "\nY方向的加速度：" + tempValueAcc[1]
                        + "\nZ方向的加速度：" + tempValueAcc[2];
                accView.setText(acc);
                break;

            // 陀螺仪
            case Sensor.TYPE_GYROSCOPE:
                tempValueGyr = event.values;
                String gyr = "陀螺仪传感器返回的数据："
                        + "\nX方向的角速度：" + tempValueGyr[0]
                        + "\nY方向的角速度：" + tempValueGyr[1]
                        + "\nZ方向的角速度：" + tempValueGyr[2];
                gyrView.setText(gyr);
                break;

            case Sensor.TYPE_ORIENTATION:
                tempValueOri = event.values;
                String ori = "方向传感器返回的数据："
                        + "\nX方向值：" + tempValueOri[0]
                        + "\nY方向值：" + tempValueOri[1]
                        + "\nZ方向值：" + tempValueOri[2];
                oriView.setText(ori);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    // 重写变化

    }
}