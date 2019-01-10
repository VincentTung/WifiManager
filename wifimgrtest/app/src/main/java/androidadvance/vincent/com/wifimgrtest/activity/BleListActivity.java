package androidadvance.vincent.com.wifimgrtest.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import androidadvance.vincent.com.wifimgrtest.R;
import androidadvance.vincent.com.wifimgrtest.adapter.BleListAdapter;
import androidadvance.vincent.com.wifimgrtest.entity.BleInfo;
import androidadvance.vincent.com.wifimgrtest.fragment.InputWifiPwdFragment;
import androidadvance.vincent.com.wifimgrtest.util.ToastUtil;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;

/**
 * 蓝牙
 */
public class BleListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = BleListActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 89;
    private static final int REQUEST_DISABLE_BT = 90;
    private static final int REQUEST_DISCOVERABLE_BT = 91;
    private static final String  UUID ="" ;
    private final RxPermissions mRxPermissions = new RxPermissions(this);
    private ListView mListView = null;
    private BleListAdapter mBLeListAdatper = null;
    private BleListActivity mAdapter = null;
    private ProgressBar mProgressBar = null;
    private List<BluetoothDevice> mBleList = new ArrayList<>();
    private DialogFragment mPwdFragment = null;
    private BluetoothAdapter mBleAdapter = null;
    private BluetoothManager mBleManager = null;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            getPreviousDevices();
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                Log.d(TAG, "action___________" + action);
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothClass bClass =  intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                    mBleList.add(device);
                    Log.d(TAG, "found:---" + device.getName());
                }else if(action.equals(ACTION_DISCOVERY_FINISHED)){
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mBLeListAdatper.notifyDataSetChanged();
                }
            }
//            Bundle b = intent.getExtras();
//            Object[] lstName = b.keySet().toArray();
//            // 显示所有收到的消息及其细节
//            for (int i = 0; i < lstName.length; i++) {
//                String keyName = lstName[i].toString();
//                Log.d(TAG, "-------" + keyName + ">>>" + String.valueOf(b.get(keyName)));
//            }
//            BluetoothDevice device;
//            // 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
////                onRegisterBltReceiver.onBluetoothDevice(device);
//            }
//            //状态改变时
//            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
//                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                switch (device.getBondState()) {
//                    case BluetoothDevice.BOND_BONDING://正在配对
//                        Log.d(TAG, "正在配对......");
////                        onRegisterBltReceiver.onBltIng(device);
//                        break;
//                    case BluetoothDevice.BOND_BONDED://配对结束
//                        Log.d(TAG, "完成配对");
////                        onRegisterBltReceiver.onBltEnd(device);
//                        break;
//                    case BluetoothDevice.BOND_NONE://取消配对/未配对
//                        Log.d(TAG, "取消配对");
////                        onRegisterBltReceiver.onBltNone(device);
//                    default:
//                        break;
//                }
//            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        registerBroadcast();

        mListView = findViewById(R.id.listview);
        mBLeListAdatper = new BleListAdapter(mBleList);
        mListView.setAdapter(mBLeListAdatper);
        mListView.setOnItemClickListener(this);
        mProgressBar = findViewById(R.id.progress_circular);
        mProgressBar.setVisibility(View.GONE);

        if (isSupportBle()) {
            mBleManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            mRxPermissions.request(BLUETOOTH, BLUETOOTH_ADMIN).subscribe(granted -> {
                if (granted) {
                    mBleAdapter = mBleManager.getAdapter();
                    if (mBleAdapter != null) {
                        if (!mBleAdapter.isEnabled()) {
                            openBle();
                        } else {
                           startScan();
                        }
                    } else {
                        ToastUtil.show("蓝牙错误");
                    }
                } else {
                    ToastUtil.show("未获取权限");
                }

            });

        } else {
            ToastUtil.show("当前设备不支持蓝牙");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
        if (mBleAdapter != null) {
            stopScan();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice bleInfo = mBleList.get(position);
        try {
            ParcelUuid[] uuids = bleInfo.getUuids();
//            BluetoothSocket clientSocket = bleInfo.createInsecureRfcommSocketToServiceRecord(java.util.UUID.randomUUID());
//            clientSocket.connect();


           makePair(bleInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 蓝牙配对
     * @param bleInfo
     */
    private void makePair(BluetoothDevice bleInfo) {

        Method method = null;
        try {
            method = BluetoothDevice.class.getMethod("createBond");
            method.invoke(bleInfo);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "开始配对");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //打开蓝牙成功了
               startScan();
            }
        }
    }

    /**
     *
     */
    private void startScan() {
        Log.d(TAG,"startCan....");
        mProgressBar.setVisibility(View.VISIBLE);
        mBleAdapter.startDiscovery();
    }


    /**
     *
     */
    private void stopScan() {
        Log.d(TAG,"stopCan....");
        mProgressBar.setVisibility(View.INVISIBLE);
        mBleAdapter.cancelDiscovery();

    }

    /**
     * 设备是否支持蓝牙
     *
     * @return
     */

    private boolean isSupportBle() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    /**
     * 打开蓝牙
     */
    private void openBle() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    /**
     * 关闭蓝牙
     */
    private void closeBle() {
        Intent disEnableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
        startActivityForResult(disEnableBtIntent, REQUEST_DISABLE_BT);
    }

    /**
     * 设置为可发现
     */
    private void canDistoryed() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);

    }


    /**
     * 之前配对过的蓝牙列表
     */
    private void getPreviousDevices() {

//        mAdapter.clear();
//        Set<BluetoothDevice> pairedDevices = mBleAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            // Loop through paired devices
//            for (BluetoothDevice device : pairedDevices) {
//                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//            }
//        }

    }


    private void registerBroadcast() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        intent.addAction(ACTION_STATE_CHANGED);//动作状态发生了变化
        intent.addAction(ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBroadcastReceiver, intent);
    }

    private void unRegisterBroadcast() {
        if (null != mBroadcastReceiver) {
            this.unregisterReceiver(mBroadcastReceiver);
        }
    }


}
