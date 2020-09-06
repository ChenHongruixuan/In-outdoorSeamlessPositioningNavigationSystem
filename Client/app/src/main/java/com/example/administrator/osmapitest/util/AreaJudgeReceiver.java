package com.example.administrator.osmapitest.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.administrator.osmapitest.data.ClientPos;

import java.util.Objects;

/**
 * The broadcast receiver for judging the initial location of user
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
                // If the position source is GPS and the accuracy < 20m, the user is in the outdoor area
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


                // Otherwise, when the count > 7, the user is in the room
                if (count >= 8) {
                    Intent statusIntent = new Intent("init_indoor");
                    context.sendBroadcast(statusIntent);
                }
                break;
        }
    }
}
