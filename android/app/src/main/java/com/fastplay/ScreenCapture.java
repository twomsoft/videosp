package com.fastplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

public class ScreenCapture extends MediaProjection.Callback
        implements ImageReader.OnImageAvailableListener {

    MediaProjectionManager _mpm;
    final DisplayMetrics _displayInfo = new DisplayMetrics();

    MediaProjection _media;
    VirtualDisplay _display;
    ImageReader _reader;
    int _requestCode =2020;

    public interface Callback {
        void onScreenImage(Image img);
    }
    Callback _callback;

    private ScreenCapture() {

    }
    public static final ScreenCapture instance =new ScreenCapture();

    @Override
    public void onStop() {
        super.onStop();

        if (_display != null) {
            _display.release();
            _display =null;
        }
        if (_reader != null) {
            _reader.setOnImageAvailableListener(null, null);
            _reader =null;
        }
        _media.unregisterCallback(this);
        _media =null;
    }

    // 开始采集
    public boolean start(Activity act, Callback callb) {
        if (null !=_media )
            return true;

        _mpm = (MediaProjectionManager) act.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (null ==_mpm)
            return false;

        _callback =callb;
        Intent it = _mpm.createScreenCaptureIntent();
        act.startActivityForResult(it, ++_requestCode);
        return true;
    }

    // 停止采集
    public void end() {
        if (null != _media)
            _media.stop();
    }

    @SuppressLint("WrongConstant")
    public void onStart(int requestCode, int resultCode, Intent data)
    {
        if (Activity.RESULT_OK == resultCode && requestCode == _requestCode)
        {
            _media = _mpm.getMediaProjection(resultCode, data);
            _media.registerCallback(this, null);

            Display display = FastPlay.WM.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(_displayInfo);
            }
            else {
                display.getMetrics(_displayInfo);
            }

            _reader = ImageReader.newInstance(
                    _displayInfo.widthPixels,
                    _displayInfo.heightPixels,
                    PixelFormat.RGBA_8888,
                    1);

            _display = _media.createVirtualDisplay("mirror",
                    _displayInfo.widthPixels,
                    _displayInfo.heightPixels,
                    _displayInfo.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    _reader.getSurface(),
                    null,
                    null);

            _reader.setOnImageAvailableListener(this, null);
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = _reader.acquireLatestImage();
//      Log.w("debug", String.format("%dx%d",image.getWidth(),image.getHeight()) );
        _callback.onScreenImage(image);
        image.close();
    }
}
