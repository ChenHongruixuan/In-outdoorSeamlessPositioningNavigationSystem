package com.example.administrator.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.administrator.datapick.AccDataPick;
import com.example.administrator.datapick.MagDataPick;
import com.example.administrator.datapick.R;
import com.example.administrator.datapick.WifiDataPick;
import com.example.administrator.nfc.ReadTextActivity;
import com.example.administrator.nfc.WriteTextActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 动态获取权限
        getPermissions();
        // 初始化按钮和文本框
        // 连接,断开连接,磁场采集,加速度数据采集按钮变量
        Button buttonMag = findViewById(R.id.mag_data_pick);
        Button buttonAcc = findViewById(R.id.acc_data_pick);
        Button buttonWifi = findViewById(R.id.wifi_data_pick);
        Button buttonRNfc = findViewById(R.id.read_nfc_text);
        Button buttonWNfc = findViewById(R.id.write_nfc_text);

        // 给按钮注册事件
        buttonMag.setOnClickListener(this);
        buttonAcc.setOnClickListener(this);
        buttonWifi.setOnClickListener(this);
        buttonRNfc.setOnClickListener(this);
        buttonWNfc.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 加速度数据采集
            case R.id.acc_data_pick:
                Intent intentAcc = new Intent(MainActivity.this, AccDataPick.class);
                startActivity(intentAcc);
                break;
            case R.id.mag_data_pick:
                Intent intentMag = new Intent(MainActivity.this, MagDataPick.class);
                startActivity(intentMag);
                break;
            //wifi信号采集
            case R.id.wifi_data_pick:
                Intent intentWifi = new Intent(MainActivity.this, WifiDataPick.class);
                startActivity(intentWifi);
                break;

            case R.id.read_nfc_text:
                Intent intentRNfc = new Intent(MainActivity.this, ReadTextActivity.class);
                startActivity(intentRNfc);
                break;

            case R.id.write_nfc_text:
                Intent intentWNfc = new Intent(MainActivity.this, WriteTextActivity.class);
                startActivity(intentWNfc);
                break;

            default:
                break;
        }
    }

    /**
     * 动态获取权限
     */
    private void getPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // 将未申请的权限一次性申请
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }

    /**
     * 如果有权限没有被同意，则取消程序
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

}