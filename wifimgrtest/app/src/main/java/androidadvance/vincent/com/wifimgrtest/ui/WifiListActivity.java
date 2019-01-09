package androidadvance.vincent.com.wifimgrtest.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import androidadvance.vincent.com.wifimgrtest.R;
import androidadvance.vincent.com.wifimgrtest.adapter.WifiListAdapter;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

/**
 *
 *
 */
public class WifiListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = WifiListActivity.class.getSimpleName();
    private final RxPermissions mRxPermissions = new RxPermissions(this);
    private ListView mListView = null;
    private WifiListAdapter mAdapter = null;
    private WifiManager mWifiManager = null;
    private ProgressBar mProgressBar = null;
    private List<ScanResult> mWifiList;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String resultAction = intent.getAction();
            if (!TextUtils.isEmpty(resultAction)) {
                if (resultAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    Log.d(TAG, "收到wifi相关的广播：" + resultAction);
                    listWifi();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        mListView = findViewById(R.id.listview);
        mProgressBar = findViewById(R.id.progress_circular);
        registerBroadcast();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mRxPermissions.request(ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_FINE_LOCATION).subscribe(granted -> {
            if (granted) {
                mProgressBar.setVisibility(View.VISIBLE);
                mWifiManager.startScan();
            } else {
                Toast.makeText(this, "未获取权限", Toast.LENGTH_SHORT).show();
            }

        });
        getConnectWifiInfo();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
    }

    /**
     * 当前连接的wifi信息
     */
    private void getConnectWifiInfo() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            Log.d(TAG, "------" + wifiInfo.toString());
            wifiInfo.getSSID();
            wifiInfo.getBSSID();
        }
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unRegisterBroadcast() {
        if (null != mBroadcastReceiver) {
            this.unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void listWifi() {
        mProgressBar.setVisibility(View.INVISIBLE);
        List<ScanResult> list = mWifiManager.getScanResults();
        if (list.isEmpty()) {
            Log.d(TAG, "------no wifi");
        } else {
            mAdapter = new WifiListAdapter(list);
            mWifiList = list;
            mListView.setAdapter(mAdapter);
            for (ScanResult scanResult : list) {
                Log.d(TAG, "------" + scanResult.toString());

            }
            mListView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult wifiInfo = mWifiList.get(position);
        connectWifi(view.getContext(), wifiInfo);
    }

    private void connectWifi(Context context, ScanResult wifiInfo) {

        WifiConfiguration wc = new WifiConfiguration();
        wc = configWifiInfo(context,wifiInfo.SSID,"xxjdwifi013",getType(wifiInfo));
        int netId = mWifiManager.addNetwork(wc);
        if (netId != -1) {
            boolean isConnect = mWifiManager.enableNetwork(netId, false);
            if (isConnect) {
                Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }


    public static WifiConfiguration configWifiInfo(Context context, String SSID, String password, int type) {
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
     *获取热点的加密类型
     */
    private int getType(ScanResult scanResult){
        int type ;
        if (scanResult.capabilities.contains("WPA")) {
            type = 2;
        }
        else if (scanResult.capabilities.contains("WEP")) {
            type = 1;
        }else {
            type = 0;
        }
        return type;
    }


}
