package com.fotoable.piano.midi;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;

/**
 * Created by fotoable on 2017/7/3.
 */

public class MyMidiPlayer  {

    private static MediaPlayer mediaPlayer;

    public synchronized static MediaPlayer getInstance() {
        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
        return mediaPlayer;
    }
    public static void stop(){
        MediaPlayer mediaPlayer = getInstance();
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

    }
    public static void start(String path){
        stop();
        try {
            mediaPlayer = getInstance();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 通过异步的方式装载媒体资源
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 装载完毕回调
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                // 在播放完毕被回调
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    mp.reset();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
//            Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show();
        }

    }

}
