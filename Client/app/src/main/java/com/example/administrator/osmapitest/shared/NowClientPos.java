package com.example.administrator.osmapitest.shared;

import com.example.administrator.osmapitest.data.ClientPos;

/**
 * 类NowClientPos是公共类
 * 其中存放了静态变量nowLongitude,nowLatitude和isIndoor
 * 代表了客户端当前的经纬度和位置状态
 * 用作程序间的数据共享
 */
public class NowClientPos {
    private static double nowLongitude;    // 用户当前经度
    private static double nowLatitude; // 用户当前纬度
    private static int nowFloor;   // 楼层


    /**
     * 获取用户当前纬度
     *
     * @return 当前纬度
     */
    public static double getNowLatitude() {
        return nowLatitude;
    }

    /**
     * 获取用户当前经度
     *
     * @return 当前经度
     */
    public static double getNowLongitude() {
        return nowLongitude;
    }


    /**
     * 获取用户当前楼层
     *
     * @return 用户的楼层
     */
    public static int getNowFloor() {
        return nowFloor;
    }

    /**
     * 设置用户当前经度
     *
     * @param nowLongitude 用户当前经度
     */
    public static void setNowLongitude(double nowLongitude) {
        NowClientPos.nowLongitude = nowLongitude;
    }

    /**
     * 设置用户当前纬度
     *
     * @param nowLatitude 用户当前纬度
     */
    public static void setNowLatitude(double nowLatitude) {
        NowClientPos.nowLatitude = nowLatitude;
    }


    /**
     * 设置楼层
     *
     * @param nowFloor 楼层
     */
    public static void setNowFloor(int nowFloor) {
        NowClientPos.nowFloor = nowFloor;
    }

    /**
     * 设置位置参数
     *
     * @param ClientPos 用户位置
     */
    public static void setPosPara(ClientPos ClientPos) {
        NowClientPos.nowLongitude = ClientPos.getLongitude();
        NowClientPos.nowLatitude = ClientPos.getLatitude();
        NowClientPos.nowFloor = ClientPos.getFloor();
    }
}
