package com.example.componentdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.common.Const;
import com.example.common.Routers;

public class MainActivity extends AppCompatActivity {

    public static final String MUSIC_MAIN = "com.example.music.MAIN";
    public static final String VIDEO_MAIN = "com.example.video.MAIN";
    private static final String TAG = "MainActivity";

    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_main);
        result = findViewById(R.id.app_result);
    }

    public void gotoMusic(View view) {
        startActivity(new Intent(MUSIC_MAIN));
    }

    public void gotoVideo(View view) {
        startActivity(new Intent(VIDEO_MAIN));
    }

    public void gotoLogin(View view) {
        ARouter.getInstance().build(Routers.MUSIC_MAIN)
                .withString("username", "zhangsan")
                .withString("password", "123456")
                .navigation(this, Const.Request.LOGIN, new NavCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        Log.e(TAG, "onFound: " + postcard);
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        Log.e(TAG, "onLost: " + postcard);
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        Log.e(TAG, "onArrival: " + postcard);
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        Log.e(TAG, "onInterrupt: " + postcard);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) return;

        if (requestCode == Const.Request.LOGIN && resultCode == Const.Resp.SUCCESS) {
            String message = data.getStringExtra(Const.Key.MESSAGE);
            result.append(message + "\n");
        }
    }
}
