package com.vincent.lib.wifimanger.util;


public interface Constants {

    /**
     * WIFI信号最大值
     */
    int WIFI_MAX_LEVEL = 100;

    /**
     * 正常wifi扫描延迟时间
     */
    long TIME_SCAN_DELAY_NORMAL = 500L;
    /**
     * 刚开启wifi后的扫描延迟时间
     */
    long TIME_SCAN_DELAY_LONG = 5000L;

    /**
     * 刷新wifi列表时间间隔
     */
    long TIME_REFRESH_WIFI_INTERVAL = 2000L;
    String KEY_WIFI_SSID = "SSID";

}
