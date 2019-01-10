package androidadvance.vincent.com.wifimgrtest.util;

import android.net.wifi.ScanResult;

public interface OnFragmentInteractionListener {
    void connectWifi(ScanResult result, String pwd);
}
