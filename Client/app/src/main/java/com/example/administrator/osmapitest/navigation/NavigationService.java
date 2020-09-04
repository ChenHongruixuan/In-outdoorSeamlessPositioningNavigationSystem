package com.example.administrator.osmapitest.navigation;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.osmapitest.data.ClientPos;
import com.example.administrator.osmapitest.shared.NowClientPos;
import com.example.administrator.osmapitest.shared.Status;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * NavigationService将当前位置信息和目的地发送到服务器
 * 接收服务器返回的状态码，目标地点和中间结点的经纬度进行导航
 * 状态码statusCode代表的含义如下：
 * 0代表导航无效或者出错
 * 1代表导航从室外到室外, 2代表从室外到室内
 * 3代表从室内到室外, 4代表从室内到室内
 * 5代表从室内到室外到室内, 6代表导航结束
 */
public class NavigationService extends IntentService {
    private Context context;
    private static String TAG = "NavigationService";
    private boolean isArriveDest = false;

    public NavigationService() {
        super("NavigationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String areaName = Objects.requireNonNull(intent).getStringExtra("area_name");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)       // 设置连接超时
                .readTimeout(30, TimeUnit.SECONDS)          // 设置读超时
                .writeTimeout(30, TimeUnit.SECONDS)        // 设置写超时
                .build();
        while (!isArriveDest) {
            // 构造请求体
            double lat = NowClientPos.getNowLatitude();
            double lon = NowClientPos.getNowLongitude();
            int floor = NowClientPos.getNowFloor();
            boolean isIndoor = Status.isIndoor();
            RequestBody navDataRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("area_name", areaName)
                    .addFormDataPart("indoor", String.valueOf(isIndoor))
                    .addFormDataPart("latitude", String.valueOf(lat))
                    .addFormDataPart("longitude", String.valueOf(lon))
                    .addFormDataPart("floor", String.valueOf(floor))
                    .build();
            Request navDataRequest = new Request.Builder()
                    // .url("http://quantum.s1.natapp.cc/SeamlessPositioning/servlet/IndoorLocateHttpServlet")
                    .url("http://quantum.s1.natapp.cc/servlet/NavigationHttpServlet")
                    .post(navDataRequestBody)
                    .build();
            try {
                Response navResponse = client.newCall(navDataRequest).execute();
                String navStr = navResponse.body().string();
                if (navStr.length() == 0) {
                    Intent navIntent = new Intent("no_nav_info");
                    context.sendBroadcast(navIntent);
                    context.sendBroadcast(navIntent);
                } else {
                    Log.e(TAG, navStr);
                    String[] navStrArr = navStr.split("\n");
                    if (navStrArr[0].equals("6")) {
                        isArriveDest = true;
                        Intent navIntent = new Intent("stop_nav");
                        context.sendBroadcast(navIntent);
                    } else {
                        NavigationInfo navigationInfo = new NavigationInfo(navStrArr);
                        Intent navIntent = new Intent("navigate");
                        navIntent.putExtra("nav_info", navigationInfo);
                        context.sendBroadcast(navIntent);
                    }
                }
                Thread.sleep(10000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "线程休眠出错" + e.getMessage());
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isArriveDest = true;
    }
}
