package com.example.administrator.osmapitest.location.outdoorloc;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.example.administrator.osmapitest.data.ClientPos;

public class OutdoorLocListener implements LocationListener {

    private Context mContext;
    private static String TAG = "OutdoorLocListener";

    public OutdoorLocListener(Context context) {
        mContext = context;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            ClientPos ClientPos = new ClientPos(latitude, longitude);
            ClientPos.setAccuracy(location.getAccuracy());
            ClientPos.setProvider(location.getProvider());
            ClientPos.setFloor(0);  // 室外则楼层为0
            Intent posIntent = new Intent("locate");
            posIntent.putExtra("pos_data", ClientPos);
            mContext.sendBroadcast(posIntent);
        }
    }
}
