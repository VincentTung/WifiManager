package com.vincent.test.wifimanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.vincent.lib.wifimanger.WifiListActivity;
import com.vincent.test.wifimanager.R;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.wifi_list).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.wifi_list:
                startActivity(new Intent(this, WifiListActivity .class));
                break;

            default:
                break;
        }
    }
}
