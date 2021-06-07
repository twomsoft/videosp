
package com.fastplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Process;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioTrack;

import static android.media.AudioTrack.PERFORMANCE_MODE_LOW_LATENCY;

class Config 
{
    static final int PLAY_SAMPLE_RATE =48000;
    static final int PLAY_FRAME_LEN =PLAY_SAMPLE_RATE/100;
    static final boolean VOIP =true;

    static final int RECORD_SAMPLE_RATE =16000;
    static final int RECORD_FRAME_LEN =RECORD_SAMPLE_RATE/100;
}

class Playback 
{
    AudioTrack mTrack =null;
    Thread mThread =null;
    volatile boolean mRunning =false;

    void Loop()
    {
        short[] frame = new short[Config.PLAY_FRAME_LEN];
        while (mRunning)
        {
            FastPlay.SOUND_READ( frame );
            mTrack.write(frame, 0, Config.PLAY_FRAME_LEN);
        }
        try {
            mTrack.stop();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    static AudioTrack createTrack(int bufferSize)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int usage =   AudioAttributes.USAGE_MEDIA,
                contentType =AudioAttributes.CONTENT_TYPE_MUSIC;

            if (Config.VOIP) {
                usage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
            }

            try
            {
                AudioTrack.Builder builder = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(usage)
                                .setContentType(contentType)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(Config.PLAY_SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build());

                builder.setBufferSizeInBytes(bufferSize);
                builder.setTransferMode(AudioTrack.MODE_STREAM);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setPerformanceMode(PERFORMANCE_MODE_LOW_LATENCY);
                }
                return builder.build();
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        try {
            return new AudioTrack(
                    Config.VOIP ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                    Config.PLAY_SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    boolean start()
    {
        if (mRunning)
            return true;

        int min_buffer =AudioTrack.getMinBufferSize(Config.PLAY_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min_buffer <0)
            return false;

        final int block_len =Config.PLAY_FRAME_LEN *2;
        int blocks =min_buffer/block_len +1;
        if (blocks <5)
            blocks =5;

        mTrack =createTrack(block_len*blocks);
        if (mTrack==null)
            return false;

        try {
            mTrack.play();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        if (mTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
            return false;

        mRunning =true;
        mThread = new Thread("AudioPlaybackThread")
        {
            @Override
            public void run() {
                super.run();
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                Loop();
            }
        };
        mThread.start();
        return true;
    }
    void reset()
    {
        if (mThread!=null)
        {
            mRunning=false;
            try {
                mThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mThread =null;
        }
        if (mTrack!=null){
            mTrack.release();
            mTrack =null;
        }
    }
}

class Record
{
    volatile boolean mRunning =false;
    AudioRecord mRecord=null;
    Thread mThread =null;

    static AudioRecord createRecord(int source, int blockSize)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            try {
                AudioRecord obj = new AudioRecord.Builder()
                        .setAudioSource(source)
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(Config.RECORD_SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                                .build())
                        .setBufferSizeInBytes(blockSize)
                        .build();
                return obj;
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        try {
            AudioRecord obj = new AudioRecord(
                    source,
                    Config.RECORD_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    blockSize);
            return obj;
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    boolean start()
    {
        if (mRunning)
            return true;

        int min_buffer =AudioRecord.getMinBufferSize(Config.RECORD_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min_buffer <0)
            return false;

        final int block_len =Config.RECORD_FRAME_LEN *2;
        int blocks =min_buffer /block_len +1;
        if (blocks <5)
            blocks =5;

        mRecord = createRecord(MediaRecorder.AudioSource.MIC, blocks *block_len);
        if (null ==mRecord)
            return false;

        try {
            mRecord.startRecording();
        }
		catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        if (mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
        {
            return false;
        }

        mRunning =true;
        mThread =new Thread("AudioRecordThread") {
            @Override
            public void run() {
                super.run();
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                Loop();
            }
        };
        mThread.start();
        return true;
    }
    void Loop()
    {
        short[] frame = new short[Config.RECORD_FRAME_LEN];
        while (mRunning)
        {
            int got =mRecord.read(frame, 0, Config.RECORD_FRAME_LEN);
            if (got >0) {
                FastPlay.SOUND_WRITE(frame);
            }
        }
        try {
            mRecord.stop();
        }
        catch(Exception ex) {
        }
    }
    void reset()
    {
        if (mThread!=null)
        {
            mRunning =false;
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread =null;
        }
        if (mRecord!=null)
        {
            mRecord.release();
            mRecord =null;
        }
    }
}

public class Sound
{
    AudioManager mAM =null;
    volatile boolean mAlive =false;

    private Sound() {}
    public static final Sound instance = new Sound();

    Thread mThread ;

    // 设置免提
    public void setHandfree(boolean on)
    {
        mAM.setSpeakerphoneOn(on);
    }

    // 管理线程
    void loop()
    {
        Record record =new Record();
        Playback playback =new Playback();

        while (mAlive) {
            int require =FastPlay.SOUND_TEST();
            int status = (playback.mRunning?2:0) | (record.mRunning?1:0);
            if (status !=require)
            {
                boolean have_play_job = (require&2)==2;
                boolean have_rec_job = (require&1)==1;

                if(have_play_job !=playback.mRunning ) {
                    if(have_play_job)
                        play(playback);
                    else
                        stop(playback);
                }
                if(have_rec_job!=record.mRunning) {
                    if(have_rec_job)
                        record.start();
                    else
                        record.reset();
                }
            }
            synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        record.reset();
        playback.reset();
        mAM.setMode(AudioManager.MODE_NORMAL);
    }

    void init(Context c)
    {
        synchronized (this) {
            if (mAlive) return;
            mAM = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            mThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    loop();
                }
            };
            mAlive = true;
        }
        mThread.start();
    }
    void uninit()
    {
        synchronized (this) {
            if (mAlive) {
                mAlive = false;
                this.notifyAll();
            } else {
                return;
            }
        }
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mThread =null;
        mAM =null;
    }
    static boolean isBluetoothConnected()
    {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null) {
            return false;
        }
        if (!ba.isEnabled()) {
            return false;
        }
        int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);
        return a2dp==BluetoothProfile.STATE_CONNECTED || headset==BluetoothProfile.STATE_CONNECTED;
    }

    void stop(Playback obj)
    {
        obj.reset();
        mAM.setMode(AudioManager.MODE_NORMAL);
    }
    boolean play(Playback obj)
    {
        if (null==mAM)
            return false;

        mAM.setMode(Config.VOIP? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
        if (!obj.start())
        {
            obj.reset();
            return false;
        }
        if (mAM.isWiredHeadsetOn()) {
            mAM.setSpeakerphoneOn(false);
            return true;
        }
        if (isBluetoothConnected()) {
            mAM.startBluetoothSco();
            mAM.setBluetoothScoOn(true);
        }
        mAM.setSpeakerphoneOn(true);
        return true;
    }
}
