package androidadvance.vincent.com.wifimgrtest.adapter;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import androidadvance.vincent.com.wifimgrtest.R;
import androidadvance.vincent.com.wifimgrtest.entity.BleInfo;

/**
 *
 */
public class BleListAdapter extends BaseAdapter {
    private List<BluetoothDevice> mBleList;
    private LayoutInflater mLayoutInflater = null;

    public BleListAdapter(List<BluetoothDevice> housingInfoData) {
        mBleList = housingInfoData;
    }

    @Override
    public int getCount() {
        if (mBleList != null) {
            return mBleList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mBleList.get(position);
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
        BluetoothDevice entity = mBleList.get(i);
        viewHolder.tv_name.setText(TextUtils.isEmpty(entity.getName())?entity.getAddress():entity.getName());
        viewHolder.tv_level.setText("");
        return convertView;
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_level;
    }

}
