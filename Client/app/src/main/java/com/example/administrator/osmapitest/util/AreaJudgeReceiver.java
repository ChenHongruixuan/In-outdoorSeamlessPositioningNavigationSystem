package com.example.administrator.osmapitest.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.administrator.osmapitest.data.ClientPos;

import java.util.Objects;

/**
 * 判断用户初始位置的广播接收器
 */
public class AreaJudgeReceiver extends BroadcastReceiver {
    private int count = 1;
    private static String TAG = "AreaJudgeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (Objects.requireNonNull(action)) {
            case "locate":
                ClientPos ClientPos = (ClientPos) intent.getSerializableExtra("pos_data");
                // 当前定位来源是GPS且定位精度小于20m，则说明用户处于室外
                if ("gps".equals(ClientPos.getProvider()) &&
                        ClientPos.getAccuracy() < 30) {
                    Intent statusIntent = new Intent("init_outdoor");
                    context.sendBroadcast(statusIntent);
                    break;
                } else {
                    count++;
                    // Log.e(TAG, "" + count);
                }
                Log.e(TAG, "" + ClientPos.getAccuracy());
                Log.e(TAG, "" + ClientPos.getProvider());


                // 当技术器增加到7时，可以认为用户处在室内
                if (count >= 8) {
                    Intent statusIntent = new Intent("init_indoor");
                    context.sendBroadcast(statusIntent);
                }
                break;
        }
    }
}
