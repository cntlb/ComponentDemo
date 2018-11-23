package com.example.music;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.lifecycle.IApplication;

public class MusicApplication implements IApplication {
    private Context context;

    @Override
    public void onCreate(Context context) {
        this.context = context;
        Toast.makeText(context, "MusicApplication.onCreate()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTerminate() {
        Toast.makeText(context, "MusicApplication.onTerminate()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int priority() {
        return NORMAL;
    }
}
