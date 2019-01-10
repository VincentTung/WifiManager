package androidadvance.vincent.com.wifimgrtest.fragment;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidadvance.vincent.com.wifimgrtest.R;
import androidadvance.vincent.com.wifimgrtest.util.OnFragmentInteractionListener;
import androidadvance.vincent.com.wifimgrtest.util.ToastUtil;

public class InputWifiPwdFragment extends DialogFragment implements View.OnClickListener {

    private static final String KEY_SSID = "SSID";
    private TextView mNameView;
    private TextView mPwdView;
    private Button mConfirmButton;
    private OnFragmentInteractionListener mListener;
    private ScanResult mScanResult;

    public static InputWifiPwdFragment newInstance(ScanResult result) {
        InputWifiPwdFragment fragment = new InputWifiPwdFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_SSID, result);
        fragment.setArguments(args);
        return fragment;
    }

    public static DialogFragment show(FragmentActivity activity, ScanResult result) {
        DialogFragment dialogFragment = newInstance(result);
        dialogFragment.show(activity.getSupportFragmentManager(), InputWifiPwdFragment.class.getSimpleName());
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = inflater.inflate(R.layout.fragment_wifi_pwd, container, false);
        mNameView = contentView.findViewById(R.id.name);
        mPwdView = contentView.findViewById(R.id.pwd);
        mConfirmButton = contentView.findViewById(R.id.confirm);
        mConfirmButton.setOnClickListener(this);

        if (getArguments() != null) {
            mScanResult = getArguments().getParcelable(KEY_SSID);
            if (mScanResult != null) {
                mNameView.setText(mScanResult.SSID);
            }
        }
        return contentView;
    }

    @Override
    public void onClick(View v) {
        if (v == mConfirmButton) {
            String pwd = mPwdView.getText().toString().trim();
            if (TextUtils.isEmpty(pwd)) {
                ToastUtil.show("请输入密码");
            } else {
                mListener.connectWifi(mScanResult, pwd);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
