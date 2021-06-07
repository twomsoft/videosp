
package com.fastplay;

import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;

import java.nio.ByteBuffer;

public class FastPlay
{
    static {
        System.loadLibrary("fastplay");
    }

    static WindowManager WM;

    public static void Init(Context c) {
        WM = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Sound.instance.init(c);
        Camera2.instance.init(c);
    }
    public static void Uninit() {
        Sound.instance.uninit();
        Camera2.instance.uninit();
        TEST(0); // cleanup
    }

    static final String ObjectTypePlayer="player", ObjectTypeSender="sender", ObjectTypeFrame ="frame";

    // 创建对象
    static native long CreateObject(String type);

    // 释放对象
    static native void DestroyObject(long handle);

    // 开始发送
    // url: 推流地址，例如 video://192.168.0.102/1000 or audio://192.168.0.11/1002
    // video协议会根据接收方的需要推送音频及视频流，如果接收方只需要视频则只发送视频
    // audio协议支持多路混音，房间内的用户推送到同一个地址，并从该地址接收混音后的流
    static native boolean Sender_Start(long handle, String url);

    // 停止发送
    static native void Sender_End(long handle);

    // 发送属性
    public static final int
        SENDER_BIT_AUDIO =(1),          // 发送音频
        SENDER_BIT_VIDEO =(1<<1)        // 发送视频
                ;
    static native void Sender_SetFlags(long handle, int f);
    static native int Sender_GetFlags(long handle);

    // 视频输出
    static native void DeliverVideo(long sender, long frame);

    // 视频格式转换
    static native void NV21_TO_YUV420P(byte[] input, int resolution, int rotate, long frame );
    static native void RGBA_TO_YUV420P(ByteBuffer input, int resolution, int stride, long frame);

    // 载入流并播放
    // flag: 接收属性，参考 Player_SetFlags()
    static native boolean Player_Load(long player, String url, int flag);

    // 播放属性比特
    public static final int
            PLAY_BIT_AUDIO      =(1),
            PLAY_BIT_VIDEO_LOW  =(1<<1),        // low def
            PLAY_BIT_VIDEO      =(1<<2)         // high def
                    ;
    // 设置属性
    // flag: 0(不接收) 1(仅音频) 2(仅视频) 3(音频+视频)
    static native void Player_SetFlags(long player, int flags);

    // 返回属性
    static native int Player_GetFlags(long player);

    // 设置窗口
    static native void Player_SetWindow(long player, Surface wnd);

    // 返回视频分辨率
    static native int Player_GetResolution(long player);

    // 音频IO
    static native void SOUND_READ(short[] buffer);
    static native void SOUND_WRITE(short[] buffer);
    static native int SOUND_TEST();

    // 设置Sdk用户
    public static native void SetSdkUser(String name);

    public static native int TEST(int op);
}
