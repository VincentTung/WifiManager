package com.vincent.lib.wifimanger.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;


public class PhoneUtils {

    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    public static boolean isLocationEnabled(Context context) {

        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            int locationMode = 0;
            String locationProviders;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;
            } else {
                locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                return !TextUtils.isEmpty(locationProviders);
            }
        }
    }


    public static boolean isGpsEnable(Context context) {
        return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled("gps");
    }

    /**
     * @param context
     */
    public static void openLocationSetting(Context context) {

        Intent intent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * @param context
     */
    public static void openWifiConnectList(Context context) {

        /**
         * 判断手机系统的版本！如果API大于10 就是3.0+
         * 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
         */
        Intent intent;
        if (Build.VERSION.SDK_INT > 10) {
            intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        } else {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        context.startActivity(intent);
    }

//    public static boolean isHighDevice(Context context) {
//        String levelStr;
//        int level = Themis.judgeDeviceLevel(context);
//        switch (level) {
//            case DEVICE_LEVEL_LOW:
//                levelStr = "低端";
//            case DEVICE_LEVEL_MID:
//                levelStr = "中端";
//                break;
//            case Themis.DEVICE_LEVEL_HIGH:
//                levelStr = "高端";
//                break;
//            default:
//                levelStr = "未知";
//        }
////        ToastUtil.show(levelStr);
//        return level == DEVICE_LEVEL_HIGH;
//    }

    public static boolean hasSDCard() {

        return (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED));
    }
}
