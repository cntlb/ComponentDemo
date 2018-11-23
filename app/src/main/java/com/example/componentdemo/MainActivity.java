package com.example.componentdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String MUSIC_MAIN = "com.example.music.MAIN";
    public static final String VIDEO_MAIN = "com.example.video.MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_main);
    }

    public void gotoMusic(View view) {
        startActivity(new Intent(MUSIC_MAIN));
    }

    public void gotoVideo(View view) {
        startActivity(new Intent(VIDEO_MAIN));
    }
}
