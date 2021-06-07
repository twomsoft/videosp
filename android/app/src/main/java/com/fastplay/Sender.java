
package com.fastplay;

import android.media.Image;
import android.util.Log;

public class Sender implements Camera2.Callback, ScreenCapture.Callback {

    // 视频源类型
    public enum VideoSource {
        VideoSourceNone,
        VideoSourceCamera,      // 摄像头
        VideoSourceScreen       // 屏幕
    }
    VideoSource _source =VideoSource.VideoSourceCamera; // 默认摄像头

    long _handle = FastPlay.CreateObject(FastPlay.ObjectTypeSender);
    long _frame =0; // 用于屏幕采集

    public void setVideoSource(VideoSource src)
    {
        _source =src;
    }

    public void release() {
        FastPlay.DestroyObject(_handle);
        _handle = 0;
        FastPlay.DestroyObject(_frame);
        _frame = 0;
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        release();
    }

    public boolean start(String url) {
        if (!FastPlay.Sender_Start(_handle, url))
            return false;

        // 连接摄像头
        Camera2.instance.setCallback(this);
        return true;
    }

    public void end() {
        Camera2.instance.setCallback(null);

        FastPlay.Sender_End(_handle);
    }

    @Override
    public void onCameraFrame(long frame)
    {
        // 摄像头采集画面
        if (_source == VideoSource.VideoSourceCamera)
            FastPlay.DeliverVideo(_handle, frame);
    }

    // 设置发送属性: 音频+视频 开关
    public void setFlags(int f) {
        FastPlay.Sender_SetFlags(_handle, f);
    }

    public int getFlags() {
        return FastPlay.Sender_GetFlags(_handle);
    }

    @Override
    public void onScreenImage(Image img) {
        // 屏幕采集画面
        if (_source ==VideoSource.VideoSourceScreen)
        {
            if (_frame==0)
                _frame =FastPlay.CreateObject(FastPlay.ObjectTypeFrame);
            Image.Plane plane = img.getPlanes()[0];
            FastPlay.RGBA_TO_YUV420P(
                    plane.getBuffer(),
                    (img.getWidth()<<16)|img.getHeight(),
                    plane.getRowStride(),
                    _frame);
            FastPlay.DeliverVideo(_handle, _frame);
        }
    }
}
