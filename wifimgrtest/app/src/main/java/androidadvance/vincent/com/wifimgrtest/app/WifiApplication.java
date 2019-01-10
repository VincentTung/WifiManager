package androidadvance.vincent.com.wifimgrtest.app;

import android.app.Application;
import android.content.SharedPreferences;
import com.tencent.mmkv.MMKV;
import androidadvance.vincent.com.wifimgrtest.util.ToastUtil;

/**
 *
 */
public class WifiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initSDK();

    }

    private void initSDK() {
        ToastUtil.init(this);
        MMKV.initialize(this);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }
}
