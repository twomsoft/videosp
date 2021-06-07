package com.fff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import com.fastplay.*;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener
{
    Sender mSender;
    PlayView mVideoView;

    final String playURL = "video://192.168.0.103/test";
    final String URL = "video://192.168.0.103/test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    // 全屏模式
        Window wnd = getWindow();
        wnd.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_video);

        WindowManager.LayoutParams params = wnd.getAttributes();
        params.systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        wnd.setAttributes(params);

    // 创建发送器
        Camera2.instance.setDef(360);
        mSender =new Sender();
        mSender.start(URL);

        // 播放器
        mVideoView =findViewById(R.id.videoView);
      mVideoView.Load(playURL, FastPlay.PLAY_BIT_AUDIO|FastPlay.PLAY_BIT_VIDEO);

        findViewById(R.id.checkBox).setOnClickListener(this);
        findViewById(R.id.checkBox2).setOnClickListener(this);
        findViewById(R.id.checkBox3).setOnClickListener(this);

        // 开始屏幕采集
//        ScreenCapture.instance.start(this, mSender);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        ScreenCapture.instance.onStart(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ScreenCapture.instance.end();

        // 需要明确释放
        Camera2.instance.setCallback(null);
        mSender.release();
        mVideoView.release();
    }

    @Override
    public void onClick(View v) {
        CheckBox check = (CheckBox)v;
        switch (v.getId()) {
            case R.id.checkBox:
            {
                int flags = mVideoView.GetFlags() ;
                if (check.isChecked())
                    flags |=FastPlay.PLAY_BIT_AUDIO;
                else
                    flags &= ~FastPlay.PLAY_BIT_AUDIO;
                mVideoView.SetFlags(flags);
            }
                break;
            case R.id.checkBox2:
            {
                int flags =mVideoView.GetFlags() ;
                if (check.isChecked())
                    flags |=FastPlay.PLAY_BIT_VIDEO;
                else
                    flags &= ~FastPlay.PLAY_BIT_VIDEO;
                mVideoView.SetFlags(flags);
            }
                break;

            case R.id.checkBox3:
            {
                mSender.setVideoSource(
                        check.isChecked()?
                                Sender.VideoSource.VideoSourceCamera:
                                Sender.VideoSource.VideoSourceScreen
                );
            }
                break;
        }
    }
}
