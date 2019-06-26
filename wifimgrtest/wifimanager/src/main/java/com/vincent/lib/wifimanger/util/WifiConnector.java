package com.vincent.lib.wifimanger.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import com.vincent.lib.wifimanger.R;
import com.vincent.lib.wifimanger.wifi.Wifi;

public class WifiConnector {

    private final Context mContext;
    private final String mScanResultSecurity;
    private final ScanResult mScanResult;
    private final int mNumOpenNetworksKept;
    private boolean mIsOpenNetwork = false;

    private WifiManager mWifiManager;

    public WifiConnector(Context context, ScanResult scanResult) {
        this.mContext = context;
        this.mScanResult = scanResult;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mScanResultSecurity = Wifi.ConfigSec.getScanResultSecurity(scanResult);
        mIsOpenNetwork = Wifi.ConfigSec.isOpenNetwork(mScanResultSecurity);
        mNumOpenNetworksKept = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);


    }

    /**
     * 连接新wifi
     */
    public boolean connectNewWifi(String pwd) {

        boolean connResult;
        if (mIsOpenNetwork) {
            connResult = Wifi.connectToNewNetwork(mContext, mWifiManager, mScanResult, null, mNumOpenNetworksKept);
        } else {
            connResult = Wifi.connectToNewNetwork(mContext, mWifiManager, mScanResult
                    , pwd
                    , mNumOpenNetworksKept);
        }

        if (!connResult) {
            ToastUtil.show(R.string.connect_failed);
        }
        return connResult;
    }

    /**
     * 连接已经连接过的wifi（仅限app创建的wifi连接）
     */
    public boolean connect() {
        final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
        boolean connResult = false;
        if (config != null) {
            connResult = Wifi.connectToConfiguredNetwork(mContext, mWifiManager, config, false);
        }
        if (!connResult) {
            ToastUtil.show(R.string.connect_failed);
        }
        return connResult;

    }

    /**
     * 移除wifi
     */
    private boolean forget() {
        final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
        boolean result = false;
        if (config != null) {
            result = mWifiManager.removeNetwork(config.networkId)
                    && mWifiManager.saveConfiguration();
        }
        if (!result) {
            ToastUtil.show(R.string.option_failed);
        }

        return result;
    }


    /**
     * 更新wifi密码（仅限app创建的wifi连接）
     *
     * @param newPwd
     */
    private boolean changePwd(String newPwd) {

        final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
        boolean saveResult = false;
        if (config != null) {
            saveResult = Wifi.changePasswordAndConnect(mContext, mWifiManager, config
                    , newPwd
                    , mNumOpenNetworksKept);
        }

        if (!saveResult) {
            ToastUtil.show(R.string.option_failed);
        }
        return saveResult;
    }

}

