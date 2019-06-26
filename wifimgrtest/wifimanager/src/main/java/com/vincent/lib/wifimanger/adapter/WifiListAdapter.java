package com.vincent.lib.wifimanger.adapter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vincent.lib.wifimanger.R;
import com.vincent.lib.wifimanger.util.Constants;
import com.vincent.lib.wifimanger.util.WifiUtils;

import java.util.List;


/**
 *
 */
public class WifiListAdapter extends BaseAdapter {
    private List<ScanResult> mWifiList;
    private LayoutInflater mLayoutInflater = null;

    public WifiListAdapter(List<ScanResult> housingInfoData) {
        mWifiList = housingInfoData;
    }

    @Override
    public int getCount() {
        if (mWifiList != null) {
            return mWifiList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mWifiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (mLayoutInflater == null) {
                mLayoutInflater = LayoutInflater.from(viewGroup.getContext());
            }
            convertView = mLayoutInflater.inflate(R.layout.item_wifilist, null);
            viewHolder.tv_name = convertView.findViewById(R.id.name);
            viewHolder.tv_level = convertView.findViewById(R.id.level);
            viewHolder.iv_wifi_connected = convertView.findViewById(R.id.iv_wifi_connected);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        ScanResult entity = mWifiList.get(i);
        viewHolder.tv_name.setText(entity.SSID);
        viewHolder.tv_level.setText(String.format("%s  %d", entity.BSSID, WifiManager.calculateSignalLevel(entity.level, Constants.WIFI_MAX_LEVEL)));

        WifiInfo wifiInfo = WifiUtils.getConnectWifiInfo(convertView.getContext());
        if (null != wifiInfo && wifiInfo.getSSID().replace("\"", "").equals(entity.SSID)) {
            viewHolder.iv_wifi_connected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.iv_wifi_connected.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_level;
        ImageView iv_wifi_connected;
    }

}
