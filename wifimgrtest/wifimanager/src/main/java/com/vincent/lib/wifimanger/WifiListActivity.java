package com.vincent.lib.wifimanger;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vincent.lib.wifimanger.adapter.WifiListAdapter;
import com.vincent.lib.wifimanger.fragment.InputWifiPwdFragment;
import com.vincent.lib.wifimanger.util.DisposableUtil;
import com.vincent.lib.wifimanger.util.PhoneUtils;
import com.vincent.lib.wifimanger.util.ToastUtil;
import com.vincent.lib.wifimanger.util.WifiConnector;
import com.vincent.lib.wifimanger.util.WifiUtils;
import com.vincent.lib.wifimanger.wifi.Wifi;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static com.vincent.lib.wifimanger.util.Constants.TIME_REFRESH_WIFI_INTERVAL;
import static com.vincent.lib.wifimanger.util.Constants.TIME_SCAN_DELAY_LONG;
import static com.vincent.lib.wifimanger.util.Constants.TIME_SCAN_DELAY_NORMAL;

public class WifiListActivity extends FragmentActivity implements AdapterView.OnItemClickListener, InputWifiPwdFragment.WifiPwdInputListener {

    private long mScanTimeDelay = TIME_SCAN_DELAY_NORMAL;
    private Disposable mScanWifiDisposable = null;
    private WifiManager mWifiManager = null;
    private RxPermissions mRxPermissions = null;
    private List<ScanResult> mWifiList = null;
    private ListView mListView = null;
    private SwipeRefreshLayout mRefreshView = null;
    private WifiListAdapter mAdapter = null;
    private DialogFragment mPwdFragment = null;
    private BroadcastReceiver mScanWifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWifiList();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifilist);
        mRefreshView = findViewById(R.id.swipe);
        mListView = findViewById(R.id.listView);
        mRxPermissions = new RxPermissions(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mRefreshView.setRefreshing(true);
        findViewById(R.id.top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (mWifiManager.isWifiEnabled()) {
            mRxPermissions.request(Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE).subscribe(granted -> {
                if (granted) {
                    mWifiManager.startScan();
                    getWifiList();
                }
            });
        }
    }

    protected void getWifiList() {
        mRxPermissions.request(ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).subscribe(granted -> {
            if (granted) {
                if (PhoneUtils.isGpsEnable(WifiListActivity.this)) {
                    Observable.timer(mScanTimeDelay, TimeUnit.MILLISECONDS).observeOn(
                            AndroidSchedulers.mainThread()
                    ).subscribe(aLong ->
                            showWifiList(mWifiManager.getScanResults())
                    );
                } else {
                    ToastUtil.show(R.string.open_location_service);
                    PhoneUtils.openLocationSetting(this);
                    finish();
                }
            } else {
                grantPermissionFailed();
            }

        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!mWifiManager.isWifiEnabled()) {
            boolean result = mWifiManager.setWifiEnabled(true);
            if (result) {
                mScanTimeDelay = TIME_SCAN_DELAY_LONG;
            } else {
                ToastUtil.show(R.string.open_wifi_first);
                finish();
                return;
            }
        } else {
            mScanTimeDelay = TIME_SCAN_DELAY_NORMAL;
        }

        final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mScanWifiReceiver, filter);
        if (mScanWifiDisposable != null && !mScanWifiDisposable.isDisposed()) {
            mScanWifiDisposable.isDisposed();
        }

        scanWifi();


    }

    void scanWifi() {
        mRxPermissions.request(Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE).subscribe(granted -> {
            if (granted) {
                mWifiManager.startScan();
            } else {
                ToastUtil.show(R.string.grant_net_permission);
            }
        });
    }

    protected void grantPermissionFailed() {
        ToastUtil.show(R.string.ungot_location_service);
        mRefreshView.setRefreshing(false);
    }


    public void showWifiList(List<ScanResult> scanResultList) {

        if (!scanResultList.isEmpty()) {
            WifiUtils.filterWifi(scanResultList);
            mWifiList = scanResultList;
            mAdapter = new WifiListAdapter(scanResultList);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);

        }
        Observable.timer(TIME_REFRESH_WIFI_INTERVAL, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                mScanWifiDisposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                mWifiManager.startScan();
            }

            @Override
            public void onError(Throwable e) {
                mRefreshView.setRefreshing(false);
                DisposableUtil.dispose(mScanWifiDisposable);
            }

            @Override
            public void onComplete() {
                mRefreshView.setRefreshing(false);
                DisposableUtil.dispose(mScanWifiDisposable);
            }
        });
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult scan = mWifiList.get(position);

        if (WifiUtils.isCurrentWifi(this, scan.SSID)) {
            new AlertDialog.Builder(this).setMessage(R.string.search_on_current_wifi).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).create().show();

        } else {
            wifiOperate(scan);
        }

    }

    protected void wifiOperate(final ScanResult scanResult) {

        if (scanResult == null) {
            ToastUtil.show(R.string.invalid_wifi);
            return;
        }
        if (WifiUtils.isAdHoc(scanResult)) {
            ToastUtil.show(R.string.adhoc_not_supported_yet);
            finish();
            return;
        }
        final String security = Wifi.ConfigSec.getScanResultSecurity(scanResult);
        final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, scanResult, security);
        if (config == null) {
            //连接新的的Wifi
            mPwdFragment = InputWifiPwdFragment.show(this, scanResult);
        } else {
            //连接之前连接过的wifi
            WifiConnector connector = new WifiConnector(this, scanResult);
            if (connector.connect()) {
            } else {
                ToastUtil.show(R.string.please_go_system_wifi_setting);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mScanWifiReceiver);
    }


    @Override
    public void onWifiPwdInputCallback(ScanResult result, String pwd) {

        WifiConnector connector = new WifiConnector(this, result);
        connector.connectNewWifi(pwd);
        mPwdFragment.dismiss();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DisposableUtil.dispose(mScanWifiDisposable);
    }
}
