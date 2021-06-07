package com.fff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import com.fastplay.FastPlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FastPlay.Init(this);

        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FastPlay.Uninit();
        System.exit(0);
    }

    public boolean checkPermission()
    {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        int HasRecorderPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        int HasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        int HasAudioSettingPermission = checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS);

        List<String> permissions = new ArrayList<>();
        if (HasRecorderPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (HasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (HasAudioSettingPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (permissions.isEmpty())
            return true;

        String[] a = new String[permissions.size()];
        permissions.toArray(a);
        requestPermissions(a, 2020);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int result: grantResults){
            if(result!=PackageManager.PERMISSION_GRANTED) return;
        }
        startVideo();
    }

    void startVideo() {
        Intent i = new Intent(this, VideoActivity.class);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button1:
                if (checkPermission()) {
                    startVideo();
                }
                break;

            case R.id.button2:
                FastPlay.TEST(1);
                break;
        }
    }
}
