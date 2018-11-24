package com.example.login

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter

import com.example.lifecycle.IApplication

class LoginTestApplication : Application() {
    val app: IApplication = LoginApplication()

    override fun onCreate() {
        super.onCreate()
        ARouter.openLog()
        ARouter.openDebug()
        ARouter.init(this)
        app.onCreate(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        app.onTerminate()
    }
}
