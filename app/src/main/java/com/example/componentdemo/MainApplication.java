package com.example.componentdemo;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.lifecycle.AppLifecycleManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.openLog();
        ARouter.openDebug();
        ARouter.init(this);
        AppLifecycleManager.onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppLifecycleManager.onTerminate();
    }
}
