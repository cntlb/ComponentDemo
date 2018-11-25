package com.example.video.test;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.lifecycle.AppLifecycleManager;

public class VideoTestApplication extends Application {
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