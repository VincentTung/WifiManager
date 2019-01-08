package androidadvance.vincent.com.wifimgrtest;

import android.app.Application;
import android.content.SharedPreferences;

import com.tencent.mmkv.MMKV;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);

    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }
}