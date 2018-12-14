package com.fotoable.piano.midi;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.fotoable.piano.utils.MiscUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;


/**
 * 音乐解析后  音节播放
 * Created by houfutian on 2017/6/14.
 */

public class SoundPoolSynth {
    private static final String CLICK_NAME = "click";
    private static final String TAG = SoundPoolSynth.class.getName();
    private static final String[] degrees = new String[]{"a", "a", "a", "c", "c", "d", "d", "d", "f", "f", "g", "g"};
    private static float mClickVolume = 1.0f;
    public static float volumeScale = 0.5f;
    private Queue<Integer> activeStreams;
    private final float half_step = ((float) Math.pow(2.0d, 0.08333333333333333d));
    private final float whole_step = ((float) Math.pow(2.0d, 0.16666666666666666d));
    private final int lowNote = 36;
    private final int topNote = 108;
    private final int maxStreams = 8;
    private long oldtime;
    private File dir;
    private Context mContext;
    private Boolean mInitialized = Boolean.valueOf(false);
    private SoundPool mSoundPool;
    private HashMap<String, Integer> mWavMap = new HashMap();
//    private float[] pitchBendPerChannel;



    private Boolean getInitialized() {
        return this.mInitialized;
    }

    private void setInitialized(Boolean initialized) {
        this.mInitialized = initialized;
    }

    public SoundPoolSynth(Context con){
        this.mContext = con;
//        this.pitchBendPerChannel = new float[128];
        this.activeStreams = new LinkedList();
        dir = this.mContext.getFilesDir();
        initSoundPool();
    }

    public synchronized void onPause() {
        freeSoundPool();
    }

    public synchronized void onStop() {
        freeSoundPool();
    }

    public synchronized void onResume() {
        initSoundPool();
    }

    private void freeSoundPool() {
        synchronized (this.mInitialized) {
            Log.d(TAG, "mSoundPool Stop");
            setInitialized(Boolean.valueOf(false));
            clearSounds();
            releaseSoundPool();
        }
    }

    private void initSoundPool() {
        synchronized (this.mInitialized) {
            if (this.mSoundPool == null) {
                Log.d(TAG, "mSoundPool started initializing");
                this.mSoundPool = new SoundPool( 16, 3, 0);
                loadWAVFiles();
                Log.d(TAG, "mSoundPool finished initializing");
            } else {
                Log.d(TAG, "mSoundPool already initialized");
            }
            setInitialized(Boolean.valueOf(true));
        }
    }

    private void releaseSoundPool() {
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
        this.mWavMap = null;
    }

    private void clearSounds() {
        if (this.activeStreams != null) {
            for (Integer streamID : this.activeStreams) {
                this.mSoundPool.stop(streamID.intValue());
            }
            this.activeStreams.clear();
        }
        if (this.mSoundPool != null && this.mWavMap != null) {
            for (Integer soundID : this.mWavMap.values()) {
                this.mSoundPool.unload(soundID.intValue());
            }
        }
    }

    private void leaveBreadcrumbForFile(File sfFile) {
        String breadcrumb = "SampleID of 0 returned for " + sfFile.getAbsolutePath() + ".";
        if (sfFile.exists()) {
            breadcrumb = breadcrumb + "file exists. ";
            try {
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(sfFile, 268435456);
                if (fd != null) {
                    fd.close();
                } else {
                    breadcrumb = breadcrumb + "  File descriptor returned by open() was null.";
                }
            } catch (FileNotFoundException e) {
                breadcrumb = breadcrumb + " FileNotFoundException was thrown.  This probably means no read permission since the file exists.";
                Log.e(TAG, " FileNotFoundExcpetion opening: " + sfFile.getAbsolutePath(), e);
            } catch (IOException e2) {
                Log.e(TAG, "error loading " + sfFile.getAbsolutePath(), e2);
                breadcrumb = breadcrumb + "  An IOException was thrown.";
            }
        } else {
            breadcrumb = breadcrumb + "file does not exist. ";
        }
//        Crittercism.leaveBreadcrumb(breadcrumb);
    }

    private void loadWAVFiles() {
        String wav;
        this.mWavMap = new HashMap();
        for (int i = 2; i <= 7; i++) {
            for (String str : degrees) {
                wav = str + Integer.toString(i) + "s_16";
                if (!this.mWavMap.containsKey(wav)) {
                    putWAVMap(wav);
                }
            }
        }
        putWAVMap("c8s_16");
        putWAVMap(CLICK_NAME);
    }

    /**
     * 加载 wav 文件
     * @param wav
     */
    private void putWAVMap(String wav){
        File sfFile = new File(dir, wav + ".wav");
        int sampleID = this.mSoundPool.load(sfFile.getAbsolutePath(), 0);
        if (sampleID == 0 && MiscUtils.extractNamedResource(this.mContext, wav, sfFile)) {
            sampleID = this.mSoundPool.load(sfFile.getAbsolutePath(), 0);
        }
        if (sampleID == 0) {
            leaveBreadcrumbForFile(sfFile);
        } else if (this.mWavMap.get(wav) == null) {
            this.mWavMap.put(wav, Integer.valueOf(sampleID));
        }
    }

    /**
     *
     * 是否插入耳机调节音量大小
     * @param state
     */
    public static void setVolumeScaleForHeadphones(int state) {
        if (state == 1) {
            volumeScale = 0.2f;
        } else {
            volumeScale = 0.5f;
        }
    }

    /**
     * 根据Note值查找对应的wav音符
     * @param note
     * @return
     */
    public String noteToWav(int note) {
        if (note >= topNote) {
            note = 107;
        } else if (note <= lowNote) {
            note = lowNote;
        }
        return degrees[(note + 3) % 12] + Integer.toString((note / 12) - 1) + "s_16";
    }

    private int needStep(int note) {
        int degree = (note + 3) % 12;
        if (note >= topNote || note < lowNote) {
            return 0;
        }
        if (degree > 1 && degrees[degree] == degrees[degree - 2]) {
            return 2;
        }
        if (degree <= 0 || degrees[degree] != degrees[degree - 1]) {
            return 0;
        }
        return 1;
    }

    /**
     *  播放音节
     *
     * @param channel
     * @param pitch
     * @param velocity
     * @param time
     */
    public void noteOn(int channel, int pitch, int velocity,long time) {
        this.oldtime = time;
        synchronized (this.mInitialized) {
            if (getInitialized().booleanValue()) {
//                if (channel < 0) {
//                    Log.w(TAG, "Invalid channel " + channel + ". Setting to 0.");
//                    channel = 0;
//                } else if (channel >= this.pitchBendPerChannel.length) {
//                    Log.w(TAG, "Invalid channel " + channel + " setting to " + (this.pitchBendPerChannel.length - 1) + ".");
//                    channel = this.pitchBendPerChannel.length - 1;
//                }

                float rate = 1.0f;
                float volume = (volumeScale * ((float) velocity)) / 127.0f;
                while (pitch > topNote) {
                    pitch -= 12;
                }
                while (pitch < lowNote) {
                    pitch += 12;
                }
                String s = noteToWav(pitch);
                switch (needStep(pitch)) {
                    case 1:
                        rate = this.half_step;
                        break;
                    case 2:
                        rate = this.whole_step;
                        break;
                }
//                if (this.pitchBendPerChannel[channel] != 0.0f) {
//                    rate = (float) (((double) rate) * Math.pow(2.0d, ((double) this.pitchBendPerChannel[channel]) / 12.0d));
//                }
                playSound(s, volume, rate);
                return;
            }
            Log.d(TAG, "getInitialized() returned false, SoundPoolSynth.noteOn() returning early");
        }
    }

    private void playSound(String id, float volume, float rate) {
        if (id != null && this.mWavMap != null) {
            Integer sound = (Integer) this.mWavMap.get(id);
            if (sound != null) {
                int streamID = this.mSoundPool.play(sound.intValue(), volume, volume, 0, 0, rate);
                if (streamID != 0) {
                    while (this.activeStreams.size() >= maxStreams) {
                        this.mSoundPool.stop(((Integer) this.activeStreams.poll()).intValue());
                    }
                    this.activeStreams.offer(Integer.valueOf(streamID));
                }
            }
        }
    }

    public static void setClickVolume(float volume) {
        mClickVolume = volume;
    }

    public void playClick() {
        if (mClickVolume > 0.0f) {
            playSound(CLICK_NAME, mClickVolume, 1.0f);
        }
    }

    public void noteOff (long time){
        if (time > oldtime){
            while (activeStreams != null && !activeStreams.isEmpty()) {
                this.mSoundPool.stop((activeStreams.poll()).intValue());
            }
        }
    }

//    public void pitchBend(int channel, float pitchDiff) {
//        if (channel >= 0 && channel < 128) {
//            this.pitchBendPerChannel[channel] = pitchDiff;
//        }
//    }

    public static void prepareResources(Context context) {
        String wav;
        File sfFile;
        File dir = context.getFilesDir();
        File clickFile = new File(dir, CLICK_NAME + ".wav");
        if (!(clickFile.exists() || MiscUtils.extractNamedResource(context, CLICK_NAME, clickFile))) {
//            showToastOnce();
        }
        File syncFile = new File(dir, "sync_loop.wav");
        if (!(syncFile.exists() || MiscUtils.extractNamedResource(context, "sync_loop", syncFile))) {
//            showToastOnce();
        }
        for (int i = 2; i <= 7; i++) {
            for (String str : degrees) {
                wav = str + Integer.toString(i) + "s_16";
                sfFile = new File(dir, wav + ".wav");
                if (!(sfFile.exists() || MiscUtils.extractNamedResource(context, wav, sfFile))) {
//                    showToastOnce();
                }
            }
        }
        wav = "c8s_16";
        sfFile = new File(dir, wav + ".wav");
        if (!sfFile.exists() && !MiscUtils.extractNamedResource(context, wav, sfFile)) {
//            showToastOnce();
        }
    }

//    private static void showToastOnce() {
//        if (sShowToast) {
////            MagicApplication.getInstance().showToast(MagicApplication.getContext().getString(R.string.error_opening_file), 1);
//            sShowToast = false;
//        }
//    }



}
