package com.example.administrator.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.telephony.TelephonyManager;

import java.util.Locale;
import java.util.Objects;

/**
 * 系统工具类
 */
public class SystemUtil {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机IMEI
     *
     * @return 手机IMEI
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        return Objects.requireNonNull(tm).getDeviceId();
    }

    /**
     * 获取手机后置摄像头的焦距
     *
     * @return 手机焦距
     */
    public static float getFocalLength() {
        Camera mCamera;
        if (Camera.getNumberOfCameras() == 2) {
            int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;   // 后置摄像头
            mCamera = Camera.open(mCameraFacing);
        } else {
            mCamera = Camera.open();
        }
        return mCamera.getParameters().getFocalLength();
    }
}
