package com.example.administrator.osmapitest.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.administrator.osmapitest.shared.Status;


/**
 * 用于构造警示框的工具类
 */
public class AlertDialogFactory {
    public static AlertDialog.Builder getNoMapDialog(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle("系统提示")
                .setMessage("当前区域不存在室内地图").setCancelable(false)
                .setNegativeButton("确定", null)
                .setPositiveButton("不再提示", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Status.setAlterIndoorMapIsNotExist(false);
                    }
                });
    }

    public static AlertDialog.Builder getNoNavInfoDialog(final Context context) {
        return new AlertDialog.Builder(context)
                .setTitle("系统提示")
                .setMessage("目标区域不存在，请重新确认").setCancelable(false)
                .setNegativeButton("确定", null);
//                .setPositiveButton("发送错误信息", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast.makeText(context, "已向服务器反映问题，我们会尽快解决", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }
}
