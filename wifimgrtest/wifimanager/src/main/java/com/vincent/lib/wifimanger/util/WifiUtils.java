package com.vincent.lib.wifimanger.util;


import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.vincent.lib.wifimanger.wifi.Wifi;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.vincent.lib.wifimanger.util.Constants.WIFI_MAX_LEVEL;

public class WifiUtils {
    public WifiManager mWifiManager = null;

    public static WifiConfiguration configWifiInfo(final Context context, final ScanResult se, final String password) {
        return configWifiInfo(context, se.SSID, password, getType(se));
    }

    public static WifiConfiguration configWifiInfo(final Context context, final String SSID, final String password, final int type) {
        WifiConfiguration config = null;
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig == null) continue;
                if (existingConfig.SSID.equals("\"" + SSID + "\"")  /*&&  existingConfig.preSharedKey.equals("\""  +  password  +  "\"")*/) {
                    config = existingConfig;
                    break;
                }
            }
        }
        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 分为三种情况：0没有密码1用wep加密2用wpa加密
        if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == 1) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {   // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 获取热点的加密类型
     */
    public static int getType(ScanResult scanResult) {
        int type;
        if (scanResult.capabilities.contains("WPA")) {
            type = 2;
        } else if (scanResult.capabilities.contains("WEP")) {
            type = 1;
        } else {
            type = 0;
        }
        return type;
    }

    /**
     * @param context
     * @return
     */
    public static List<ScanResult> getWifiList(Activity context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiMgr.getScanResults();
    }

    /**
     * 获取当前连接wifi信息
     *
     * @param context
     * @return
     */
    public static WifiInfo getConnectWifiInfo(Context context) {
        if(context == null || context.getApplicationContext() == null) return  null;
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) {
            return wifiMgr.getConnectionInfo();
        } else {
            return null;
        }
    }


    public static WifiConfiguration IsExsits(String ssid, Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Iterator localIterator = wifiMgr.getConfiguredNetworks().iterator();
        while (localIterator.hasNext()) {
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localIterator.next();
            if (localWifiConfiguration.SSID.equals("\"" + ssid + "\"")) {
                return localWifiConfiguration;
            }
        }
        return null;
    }

    public static void filterWifi(List<ScanResult> list) {
        Iterator<ScanResult> iter = list.iterator();
        while (iter.hasNext()) {
            ScanResult info = iter.next();
            if (WifiManager.calculateSignalLevel(info.level, WIFI_MAX_LEVEL) <= 1) {
                iter.remove();
            }
            if (TextUtils.isEmpty(info.SSID.replace("\"", ""))) {
                iter.remove();
            }
        }
        Collections.sort(list, (wifi1, wifi2) -> WifiManager.calculateSignalLevel(wifi2.level, WIFI_MAX_LEVEL) - WifiManager.calculateSignalLevel(wifi1.level, WIFI_MAX_LEVEL));
    }

    public static boolean isAdHoc(final ScanResult scanResule) {
        return scanResule.capabilities.indexOf("IBSS") != -1;
    }


    public static boolean isCurrentWifi(Context contex,String targetSSID) {
        WifiInfo curWifiInfo = getConnectWifiInfo(contex);
        if (curWifiInfo != null) {
            String ssid = curWifiInfo.getSSID();
            return !TextUtils.isEmpty(ssid) && ssid.equals(Wifi.convertToQuotedString(targetSSID));
        }
        return false;
    }

}
