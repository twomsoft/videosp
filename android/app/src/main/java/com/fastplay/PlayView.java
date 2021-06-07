
package com.fastplay;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class PlayView extends SurfaceView implements SurfaceHolder.Callback {

    long _handle;

    public PlayView(Context context) {
        super(context);
        Init();
    }

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public PlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Init();
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        release();
    }

    void Init()
    {
        setZOrderMediaOverlay(true);
        getHolder().addCallback(this);
        _handle =FastPlay.CreateObject(FastPlay.ObjectTypePlayer);
    }

    // 如果不再需要时需要明确调用 release() 这样可以及时释放内存
    public void release()
    {
        FastPlay.DestroyObject(_handle);
        _handle =0;
    }

    public int GetResolution()
    {
        return FastPlay.Player_GetResolution(_handle);
    }

    // 载入流，例如: view.Load("video://192.168.0.11/1000", AVSdk.PLAY_BIT_VIDEO|AVSdk.PLAY_BIT_AUDIO );
    public boolean Load(String url, int mask)
    {
        return FastPlay.Player_Load(_handle, url, mask);
    }

    // 设置流属性
    // flag: 0(暂停) 1(仅播放音频) 2(仅播放视频) 3(音频+视频)
    public void SetFlags(int mask)
    {
        FastPlay.Player_SetFlags(_handle, mask);
    }

    // 返回流属性
    public int GetFlags()
    {
        return FastPlay.Player_GetFlags(_handle);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        FastPlay.Player_SetWindow(_handle, surfaceHolder.getSurface());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        FastPlay.Player_SetWindow(_handle, null);
    }
}
