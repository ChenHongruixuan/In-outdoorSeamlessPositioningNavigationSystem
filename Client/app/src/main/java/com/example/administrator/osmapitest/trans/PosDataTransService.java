package com.example.administrator.osmapitest.trans;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.osmapitest.shared.NowClientPos;
import com.example.administrator.osmapitest.shared.Status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * PosDataTransService在后台每隔2s上传一次用户当前位置，通过接收服务器返回的状态码
 * 判断用户当前的位置状态
 */
public class PosDataTransService extends IntentService {

    public final static byte OUTDOOR_TO_INDOOR = 0;
    public final static byte INDOOR_TO_OUTDOOR = 1;
    public final static byte OUTDOOR = 2;
    public final static byte INDOOR = 3;

    private static String TAG = "PosDataTransService";
    private boolean isSendPosStop = false;
    private Context mContext;

    public PosDataTransService() {
        super("PosDataTransService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PosDataTransCallback mPosDataTransCallback = new PosDataTransCallback();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)       // 设置连接超时
                .readTimeout(30, TimeUnit.SECONDS)         // 设置读超时
                .writeTimeout(30, TimeUnit.SECONDS)        // 设置写超时
                .build();
        while (!isSendPosStop) {
            double nowLongitude = NowClientPos.getNowLongitude();
            double nowLatitude = NowClientPos.getNowLatitude();
            boolean isIndoor = Status.isIndoor();
            // 构造请求体
            RequestBody posDataRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("longitude", String.valueOf(nowLongitude))
                    .addFormDataPart("latitude", String.valueOf(nowLatitude))
                    .addFormDataPart("status", String.valueOf(isIndoor))
                    .build();

            // 构造请求对象
            Request posDataRequest = new Request.Builder()
                    //.url("http://quantum.s1.natapp.cc/SeamlessPositioning/servlet/MapHttpServlet")
                    .url("http://quantum.s1.natapp.cc/servlet/IndoorMapHttpServlet")
                    .post(posDataRequestBody)
                    .build();

            try {
                client.newCall(posDataRequest).enqueue(mPosDataTransCallback);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "线程休眠出错" + e.getMessage(), e);
            }
        }
    }

    private class PosDataTransCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
            Log.e(TAG, "上传位置信息出错：" + e.getMessage(), e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Byte statusCode = response.body().bytes()[0];
            switch (statusCode) {

                /*
                  室外进入室内，需要进行的操作有：
                  向服务器请求室内地图，开启室内定位模块，关闭GPS定位
                 */
                case OUTDOOR_TO_INDOOR:
                    Status.setIsIndoor(true);
                    Intent otiIntent = new Intent("outdoor_to_indoor");
                    mContext.sendBroadcast(otiIntent);
                    break;
                    /*
                      室内进入室外，需要进行的操作有：
                      移除室内地图，关闭室内定位模块，开启GPS定位
                     */
                case INDOOR_TO_OUTDOOR:
                    Status.setIsIndoor(false);
                    Intent itoIntent = new Intent("indoor_to_outdoor");
                    mContext.sendBroadcast(itoIntent);
                    break;
                case OUTDOOR:   // 仍处在室外
                    Status.setIsIndoor(false);
                    break;
                case INDOOR:    // 仍处在室内
                    Status.setIsIndoor(true);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isSendPosStop = true;
    }
}
