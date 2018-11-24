package com.example.login

import android.content.Context
import android.widget.Toast
import com.example.lifecycle.IApplication

class LoginApplication : IApplication {
    lateinit var context: Context
    override fun onCreate(context: Context) {
        this.context = context
        Toast.makeText(context, "LoginApplication.onCreate", Toast.LENGTH_SHORT).show()
    }

    override fun onTerminate() {
        Toast.makeText(context, "LoginApplication.onTerminate", Toast.LENGTH_SHORT).show()
    }

    override fun priority(): Int = IApplication.MAX
}