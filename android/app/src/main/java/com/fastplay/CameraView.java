
package com.fastplay;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class CameraView extends SurfaceView
        implements View.OnLongClickListener, SurfaceHolder.Callback
{
    public CameraView(Context context)
    {
        super(context);
        this.Init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.Init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.Init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.Init();
    }

    void Init()
    {
        setZOrderMediaOverlay(true);
        getHolder().addCallback(this);
        this.setOnLongClickListener(this);
    }

    // 切换前后摄像头
    public void switchFacing()
    {
        Camera2 cam =Camera2.instance;
        cam.setFacing(!cam.isFront());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int res = Camera2.instance.getResolution();
        if (res ==0)
        {
              super.onMeasure(widthMeasureSpec, heightMeasureSpec);
              return;
        }
        int video_cx =res>>16; // 需要根据实际分辨率调整
        int video_cy =res&0xffff;

        Display display = FastPlay.WM.getDefaultDisplay();
        switch (display.getRotation())
        {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
            {
                int tmp =video_cx ;
                video_cx =video_cy;
                video_cy =tmp;
            }
            break;
        }

        int layout_cx = MeasureSpec.getSize(widthMeasureSpec);
        int layout_cy = MeasureSpec.getSize(heightMeasureSpec);

        int cx =layout_cx;
        int cy = cx *video_cy /video_cx;
        if (cy > layout_cy)
        {
            cy =layout_cy;
            cx =cy *video_cx /video_cy;
        }
        setMeasuredDimension(cx, cy);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Camera2.instance.updateDisplay();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        Camera2.instance.attach(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        Camera2.instance.detach(surfaceHolder);
    }

    @Override
    public boolean onLongClick(View view)
    {
        if (view==this)
        {
            switchFacing();
            return true;
        }
        return false;
    }
}
