
package com.fastplay;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Camera2 implements Camera.PreviewCallback
{
    Camera mDevice =null;
    int mRotation =0;
    int mResolution =0;
    boolean mFront =true;
    Display mDisplay;
    SurfaceHolder mSurface;
    byte[] mBuffer =null;
    long mOutputFrame =0;
    final Camera.CameraInfo Info = new Camera.CameraInfo();
    final ReentrantLock mLock =new ReentrantLock();

    public static final Camera2 instance = new Camera2();

    public interface Callback {
        void onCameraFrame(long frame);
    }
    Callback mCallback;

    class DummyCallback implements Callback {
        @Override
        public void onCameraFrame(long frame) {
        }
    }
    final DummyCallback mDummyCallback =new DummyCallback();

    public void setCallback(Callback cb) {
        if (cb==null)
            cb =mDummyCallback;
        mLock.lock();
        mCallback =cb;
        mLock.unlock();
    }

    public void setDef(int def) {
        if (def < 360)
            mResolution =(320<<16)|240;
        else if(def < 720)
            mResolution =(640<<16)|480;
        else if(def < 1080)
            mResolution =(1280<<16)|720;
        else
            mResolution =(1920<<16)|1080;
    }

    void init(Context ctx)
    {
        mDisplay =FastPlay.WM.getDefaultDisplay();
        mCallback = mDummyCallback;
        mOutputFrame =FastPlay.CreateObject(FastPlay.ObjectTypeFrame);
    }
    void uninit()
    {
        release();
        mDisplay =null;
        FastPlay.DestroyObject(mOutputFrame);
        mOutputFrame =0;
    }

    private Camera2()
    {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        if ( data!=null && camera==mDevice )
        {
            FastPlay.NV21_TO_YUV420P(data, mResolution, mRotation, mOutputFrame);
            mLock.lock();
            mCallback.onCameraFrame(mOutputFrame);
            mLock.unlock();
            camera.addCallbackBuffer(data);
        }
    }
    void attach(SurfaceHolder h)
    {
        release();
        mSurface = h;
        start();
    }
    void detach(SurfaceHolder h)
    {
        if (mSurface ==h) {
            release();
            mSurface =null;
        }
    }
    public void setFacing(boolean front)
    {
        if (mFront != front) {
            release();
            mFront = front;
            start();
        }
    }
    public boolean isFront()
    {
        return mFront;
    }
    public void updateDisplay()
    {
        if (mDevice==null)
            return;

        int degrees = getDisplayRotation();
        int rotation =0;
        if (Info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees = (Info.orientation + degrees) % 360;
            rotation =degrees;
            degrees = (360 - degrees) % 360;
        }
        else {
            degrees = ( Info.orientation - degrees + 360) % 360;
            rotation = degrees;
        }
        mDevice.setDisplayOrientation(degrees);
        mRotation=rotation;
    }
    int getDisplayRotation()
    {
        switch (mDisplay.getRotation())
        {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
    public void release()
    {
        if (mDevice ==null) return;
        mDevice.stopPreview();
        mDevice.setPreviewCallback(null);
        mDevice.release();
        mDevice=null;
    }
    void start()
    {
        if(mSurface ==null ) return;
        if(mCallback ==null ) return;
        if(mDevice!=null) return;

        int id =getId();
        if (id ==-1) {
            mDevice =Camera.open();
            Info.orientation =270;
            Info.facing = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        else {
            mDevice =Camera.open(id);
            Camera.getCameraInfo(id, Info);
        }
        if(mDevice ==null)
        {
            Log.e("debug", "no camera");
            return ;
        }
        if(!start2())
        {
            mDevice.release();
            mDevice =null;
            return;
        }
    }
    boolean start2()
    {
        assert (mSurface!=null);
        try	{
            Camera.Size size =getSize();
            if (null==size)
                return false;

            mDevice.setPreviewDisplay(mSurface );
            Camera.Parameters p = mDevice.getParameters();
            p.setPreviewFormat(ImageFormat.NV21);
            p.setPreviewSize(size.width, size.height);
            setFps(p);
            mDevice.setParameters(p);
            mResolution= (size.width<<16) |size.height;

            int required = size.width * size.height * ImageFormat.getBitsPerPixel(p.getPreviewFormat())/8;
            if (mBuffer==null || mBuffer.length !=required) {
                mBuffer = new byte[required];
            }
            mDevice.addCallbackBuffer(mBuffer);
            mDevice.setPreviewCallbackWithBuffer(this);
            mDevice.startPreview();
            updateDisplay();
            return true;
        }
        catch (Exception e) {
            Log.e("debug", e.getLocalizedMessage());
            return false;
        }
    }
    int getId()
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int count =Camera.getNumberOfCameras();
        for (int i=0; i < count; i++)
        {
            Camera.getCameraInfo(i, info);
            switch (info.facing)
            {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    if (mFront)
                        return i;
                    continue;

                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    if (!mFront)
                        return i;
                    continue;
            }
        }
        return -1;
    }
    Camera.Size getSize()
    {
        Camera.Parameters p = mDevice.getParameters();
        List<Camera.Size> list = p.getSupportedPreviewSizes();
        Camera.Size size=null;
        int cand =0;
        for (Camera.Size s:list)
        {
            int res = (s.width<<16)|s.height;
            if (res == mResolution)
                return s;

            if (res > mResolution)
                continue;

            if(res > cand) {
                size =s;
                cand =res;
            }
        }
        return size;
    }
    void setFps(Camera.Parameters p)
    {
        List<int[]> supported = p.getSupportedPreviewFpsRange();
        for (int[] fps : supported)
        {
             if (fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] >= 30000)
             {
                p.setPreviewFpsRange(fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                return;
             }
        }
    }

    int getResolution() {
        return mResolution;
    }
}
