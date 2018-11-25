package com.example.video;

import android.content.Context;
import android.widget.Toast;

import com.example.lifecycle.IApplication;

public class VideoApplication implements IApplication {
    private Context context;

    @Override
    public void onCreate(Context context) {
        this.context = context;
        Toast.makeText(context, "VideoApplication.onCreate()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTerminate() {
        Toast.makeText(context, "VideoApplication.onTerminate()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int priority() {
        return NORMAL;
    }

}
