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
 * PosDataTransService uploads the user's current position every 2S in the background,
 * and receives the status code returned by the server
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
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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
                  When entering the indoor, the operations need to be carried out are:
                  request the indoor map from the server,
                  open the indoor positioning module,
                  and close the outdoor positioning module
                 */
                case OUTDOOR_TO_INDOOR:
                    Status.setIsIndoor(true);
                    Intent otiIntent = new Intent("outdoor_to_indoor");
                    mContext.sendBroadcast(otiIntent);
                    break;
                /*
                  When entering the outdoor, the operations need to be carried out are:
                  remove the indoor map,
                  close the indoor positioning module,
                  and open the outdoor positioning module
                 */
                case INDOOR_TO_OUTDOOR:
                    Status.setIsIndoor(false);
                    Intent itoIntent = new Intent("indoor_to_outdoor");
                    mContext.sendBroadcast(itoIntent);
                    break;
                case OUTDOOR:
                    Status.setIsIndoor(false);
                    break;
                case INDOOR:
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
