package androidadvance.vincent.com.wifimgrtest;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();
    WifiManager mWifiManager = null;
    private int req_permission = 87;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        Log.d(TAG,"------"+wifiInfo.toString());
        wifiInfo.getSSID();
        wifiInfo.getBSSID();
        if (ContextCompat.checkSelfPermission(this, ACCESS_WIFI_STATE) == PERMISSION_GRANTED) {

            listWifi();

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_WIFI_STATE}, req_permission);
            }
        }
    }

    private void listWifi() {
        List<ScanResult> list = mWifiManager.getScanResults();
        if (list.isEmpty()) {
            Log.d(TAG, "------no wifi");
        } else {
            for (ScanResult scanResult : list) {
                Log.d(TAG, "------" + scanResult.toString());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == req_permission) {
            if (null != grantResults && grantResults[0] == PERMISSION_GRANTED) {
                listWifi();
            } else {
                Toast.makeText(this, "no authorise", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
