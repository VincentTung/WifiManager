package androidadvance.vincent.com.wifimgrtest.adapter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import androidadvance.vincent.com.wifimgrtest.R;

import static androidadvance.vincent.com.wifimgrtest.util.Constant.WIFI_LEVEL;

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
            convertView = mLayoutInflater.inflate(R.layout.item_wif_list, null);
            viewHolder.tv_name = convertView.findViewById(R.id.name);
            viewHolder.tv_level = convertView.findViewById(R.id.level);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        ScanResult entity = mWifiList.get(i);
        viewHolder.tv_name.setText(entity.SSID);
        viewHolder.tv_level.setText(String.valueOf(WifiManager.calculateSignalLevel(entity.level, WIFI_LEVEL)));
        return convertView;
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_level;
    }

}
