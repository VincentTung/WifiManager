package androidadvance.vincent.com.wifimgrtest.activity;

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
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import androidadvance.vincent.com.wifimgrtest.R;
import androidadvance.vincent.com.wifimgrtest.adapter.WifiListAdapter;
import androidadvance.vincent.com.wifimgrtest.fragment.InputWifiPwdFragment;
import androidadvance.vincent.com.wifimgrtest.util.OnFragmentInteractionListener;
import androidadvance.vincent.com.wifimgrtest.util.ToastUtil;
import androidadvance.vincent.com.wifimgrtest.util.WifiUtils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

/**
 *
 *
 */
public class WifiListActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnFragmentInteractionListener {

    private static final String TAG = WifiListActivity.class.getSimpleName();
    private final RxPermissions mRxPermissions = new RxPermissions(this);
    private ListView mListView = null;
    private WifiListAdapter mAdapter = null;
    private WifiManager mWifiManager = null;
    private ProgressBar mProgressBar = null;
    private List<ScanResult> mWifiList = null;
    private DialogFragment mPwdFragment = null;
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
        registerBroadcast();

        mListView = findViewById(R.id.listview);
        mProgressBar = findViewById(R.id.progress_circular);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mRxPermissions.request(ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).subscribe(granted -> {
            if (granted) {
                mProgressBar.setVisibility(View.VISIBLE);
                mWifiManager.startScan();
            } else {
                ToastUtil.show("未获取权限");
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
        mPwdFragment = InputWifiPwdFragment.show(this, wifiInfo);
    }


    @Override
    public void connectWifi(ScanResult result, String pwd) {
        WifiConfiguration wc = WifiUtils.configWifiInfo(this, result, pwd);
        int netId = mWifiManager.addNetwork(wc);
        if (netId != -1) {
            boolean isConnect = mWifiManager.enableNetwork(netId, false);
            if (isConnect) {
                ToastUtil.show("连接成功");
                mPwdFragment.dismiss();
            } else {
                ToastUtil.show("连接失败");
            }
        } else {
            ToastUtil.show("连接失败");
        }

    }
}
