package com.example.administrator.osmapitest.shared;

public class Status {
    private static boolean alterIndoorMapIsNotExist = true;
    private static boolean still = true;
    private static boolean indoor = false;  // 用户处在室内还是室外（0是室内，1是室外）

    public static void setAlterIndoorMapIsNotExist(boolean alterIndoorMapIsNotExist) {
        Status.alterIndoorMapIsNotExist = alterIndoorMapIsNotExist;
    }

    public static boolean isAlterIndoorMapIsNotExist() {
        return alterIndoorMapIsNotExist;
    }

    public static boolean isStill() {
        return still;
    }

    public static boolean isIndoor() {
        return indoor;
    }

    public static void setStill(boolean still) {
        Status.still = still;
    }

    public static void setIsIndoor(boolean isIndoor) {
        Status.indoor = isIndoor;
    }
}
