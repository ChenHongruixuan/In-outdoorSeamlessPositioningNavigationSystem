package com.example.administrator.osmapitest.location.indoorloc.inertial;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.osmapitest.data.ClientPos;
import com.example.administrator.osmapitest.shared.NowClientPos;

import java.util.Objects;


/**
 * InertialLocateService is the inertial location module
 * which perform inertial location based on PDR algorithm
 *
 * @author Qchrx
 * @version 1.5
 * @date 2018/3/02
 */
public class InertialLocateService extends Service implements SensorEventListener {

    private boolean isStopInertialLoc = false; // flag for whether the thread of inertial positioning is stopped
    private float[] accNowValue = new float[3];    // storing acceleration values returned by sensor
    private float[] oriNowValue = new float[3];    // storing orientation values returned by sensor
    private double[] accSlidingWindow = new double[31];    // sliding window of acceleration

    private static String TAG = "InertialLocateService";

    private static final double PI_CONST = Math.PI / 180;    // radian corresponding to 1 degree
    private static final double kConst = 0.026795089522;    // the value of k in self defined nonlinear step size algorithm
    private static final double M_L_L_CONST = 111194.926644558;
    private Thread inertialThread;
    private SensorManager sensorManager;
    private int count = 0, stepCount = 0;
    private long startTime, endTime;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        Sensor sensorAcc = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorOri = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorOri, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        inertialThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // initialize the sliding window of acceleration
                initAccSlidingWindow();
                // initialize parameters
                ClientPos ClientPos = (ClientPos) Objects.requireNonNull(intent).getSerializableExtra("init_pos");
                double step, accTempMax = 9.8, accTempMin = 9.8;
                double posLat = ClientPos.getLatitude(), posLon = ClientPos.getLongitude();
                boolean isStartCheckMinValue = false, isStartCheckMaxValue = true;
                // start inertial location
                Log.e(TAG, "开始进行惯性定位");
                startTime = System.currentTimeMillis();
                while (!isStopInertialLoc) {
                    count++;
                    // sliding window of acceleration slides forward
                    moveAccWindow(getAccMod());
                    if (isWalk()) {     //  The user is in motion
                        // There is an effective peak value of acceleration in one step
                        if (isStartCheckMaxValue && accSlidingWindow[15] > accSlidingWindow[14] &&
                                accSlidingWindow[15] > accSlidingWindow[16] &&
                                isEffectiveMaxValue(accSlidingWindow[15]) &&
                                accTempMax < accSlidingWindow[15]) {
                            accTempMax = accSlidingWindow[15];
                            // When the effective peak value of acceleration appears for the first time,
                            // start to detect the effective valley value of acceleration
                            isStartCheckMinValue = true;
                        }
                        // There is an effective valley value of acceleration in one step
                        if (isStartCheckMinValue && accSlidingWindow[15] < accSlidingWindow[14] &&
                                accSlidingWindow[15] < accSlidingWindow[16] &&
                                isEffectiveMinValue(accSlidingWindow[15]) &&
                                accTempMin > accSlidingWindow[15]) {
                            accTempMin = accSlidingWindow[15];
                            // When the effective valley value of acceleration appears for the first time,
                            // start to detect the effective peak value of acceleration
                            isStartCheckMaxValue = false;
                        }
                        // When the effective valley value of acceleration has been detected,
                        // the acceleration starts to rise,
                        // which means that the previous step has been completed and a new step is started
                        if (!isStartCheckMaxValue && accSlidingWindow[15] >= 9.0) {
                            stepCount++;
                            // Obtaining the step size of this step by self defined nonlinear step size algorithm
                            step = getAnStepDistance(accTempMin, accTempMax);
                            float angle = oriNowValue[0];
                            String[] llStr = disToLL(step, posLat, posLon, angle).split(",");
                            posLat = Double.valueOf(llStr[0]);
                            posLon = Double.valueOf(llStr[1]);
                            ClientPos = new ClientPos(posLat, posLon, NowClientPos.getNowFloor());
                            Intent posIntent = new Intent("locate");
                            posIntent.putExtra("pos_data", ClientPos);
                            sendBroadcast(posIntent);
                            // Parameters initialization
                            accTempMax = 9.8;
                            accTempMin = 9.8;
                            isStartCheckMaxValue = true;
                            isStartCheckMinValue = false;
                        }
                    } else {    // The user is at rest
                        posLat = NowClientPos.getNowLatitude();
                        posLon = NowClientPos.getNowLongitude();
                    }
                    try {   // Keep the sampling frequency of 50 Hz
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Inertial locate Wait Error:" + e.getMessage());
                    }
                }
                endTime = System.currentTimeMillis();
                stopSelf();
            }
        });
        inertialThread.setPriority(10);
        inertialThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

//    public void onHandleIntent(@Nullable Intent intent) {
//
//    }

    /**
     * Initialize the parameters of inertial location, pad the sliding window of acceleration
     */
    private void initAccSlidingWindow() {
        for (int i = 0; i < accSlidingWindow.length; i++) {
            accSlidingWindow[i] = getAccMod();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "Initialization Sleep Error:" + e.getMessage());
            }
        }
    }

    /**
     * Sliding window of acceleration move forward along the time
     *
     * @param accModel Magnitude of acceleration
     */
    private void moveAccWindow(double accModel) {
        System.arraycopy(accSlidingWindow, 1, accSlidingWindow, 0, 30);
        accSlidingWindow[30] = accModel;
    }

    /**
     * Judging whether the user is in motion according to the standard deviation of acceleration
     * When the standard deviation of acceleration is greater than 0.8, the user is considered to be in motion
     *
     * @return If the user is in motion
     */
    private boolean isWalk() {
        return getAccStd() >= 0.8;
    }

    /**
     * Obtain the step size of one step by self defined nonlinear step size algorithm
     *
     * @param accMin Valley value of acceleration in a step
     * @param accMax Peak value of acceleration in a step
     * @return step size
     */
    private double getAnStepDistance(double accMin, double accMax) {
        return kConst * ((accMax - accMin) * 3.5 + Math.pow(accMax - accMin, 0.25));
    }

    /**
     * Method for returning the magnitude of the current acceleration
     *
     * @return Magnitude of acceleration collected by acceleration sensor
     */
    private double getAccMod() {
        return Math.pow(accNowValue[0] * accNowValue[0]
                + accNowValue[1] * accNowValue[1]
                + accNowValue[2] * accNowValue[2], 0.5);
    }

    /**
     * Method for calculating the standard deviation of acceleration in sliding window
     *
     * @return Standard deviation of acceleration in the sliding window
     */
    private double getAccStd() {
        return Math.sqrt(getAccVar());
    }


    /**
     * Method for calculating the variance of acceleration in sliding window
     *
     * @return Variance of acceleration in the sliding window
     */
    private double getAccVar() {
        double accVar = 0;
        double accMean = getAccMean();
        for (double anAccSlidingWindow : this.accSlidingWindow)
            accVar += (anAccSlidingWindow - accMean) * (anAccSlidingWindow - accMean);
        return accVar / accSlidingWindow.length;
    }

    /**
     * Method for calculating the mean of acceleration in sliding window
     *
     * @return Mean of acceleration in the sliding window
     */
    private double getAccMean() {
        double sumAcc = 0;
        for (double anAccSlidingWindow : this.accSlidingWindow)
            sumAcc += anAccSlidingWindow;
        return sumAcc / accSlidingWindow.length;
    }

    /**
     * Judging whether the current peak acceleration exceeds the threshold value and become an effective peak acceleration
     *
     * @return whether becoming an effective peak acceleration
     */
    private boolean isEffectiveMaxValue(double acc) {
        return acc > 11.8 && acc < 20.0;
    }

    /**
     * Judging whether the current valley acceleration exceeds the threshold value and become an effective peak acceleration
     *
     * @return whether becoming an effective valley acceleration
     */
    private boolean isEffectiveMinValue(double acc) {
        return acc < 8.8 && acc > 4.0;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            // Acceleration sensor
            case Sensor.TYPE_ACCELEROMETER:
                accNowValue = event.values;
                break;
            // Orientation sensor
            case Sensor.TYPE_ORIENTATION:
                oriNowValue = event.values;
                break;
        }
    }

    /**
     * transform distance into latitude and longitude
     *
     * @param dis   distance
     * @param lat   latitude
     * @param lon   longitude
     * @param angle angle
     * @return string composed of latitude and longitude
     */
    public String disToLL(double dis, double lat, double lon, float angle) {
        // transform distance into longitude
        double newLon = lon + (dis * Math.sin(angle * PI_CONST)) / (M_L_L_CONST * Math.cos(lat * PI_CONST));
        // transform distance into latitude
        double newLat = lat + (dis * Math.cos(angle * PI_CONST)) / M_L_L_CONST;
        return newLat + "," + newLon;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {   // stop sub thread of inertial location
        super.onDestroy();
        isStopInertialLoc = true;
        sensorManager.unregisterListener(this);
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
//                + "test.txt";   // 获得SD卡根目录;
//        File file = new File(path);
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileOutputStream fileOutputStream = new FileOutputStream(file);
//            byte[] videoSETimeByte = (count + "," + stepCount + "," + (endTime - startTime)).getBytes();
//            fileOutputStream.write(videoSETimeByte);
//            fileOutputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        // Log.e(TAG,"已停止惯性定位");
    }
}
