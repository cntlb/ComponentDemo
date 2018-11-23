package com.example.componentdemo;

import android.app.Application;

import com.example.lifecycle.AppLifecycleManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLifecycleManager.onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppLifecycleManager.onTerminate();
    }
}
