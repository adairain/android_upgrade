package com.jz.customsdk;

import android.app.Application;

import com.jz.upgrade.UpgradeManager;
import com.jz.upgrade.UpgradeManagerOption;

/**
 * @author zhouyu
 * @date 2023/3/3 14:45
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        UpgradeManagerOption option = new UpgradeManagerOption();
        option.setPrintLog(true);
        UpgradeManager.Companion.init(this,option);
    }
}
