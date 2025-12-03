package com.monnfamily.enlibraryapp.Utils;

import android.media.MediaPlayer;

import java.io.IOException;

public class SoundManager {

    private final MediaPlayer backgroundPlayer = new MediaPlayer();
    private final MediaPlayer textReadPlayer = new MediaPlayer();
    private int mBGPauseTime = 0;
    AppManager  manager = AppManager.getInstance();

    private static final SoundManager mInstance = new SoundManager();
    public static SoundManager getInstance() {
        return mInstance;
    }
    private SoundManager() {
    }

    private void stopCompleteBG(){
        backgroundPlayer.stop();
        backgroundPlayer.reset();
    }

    public void playBackgroundMusic(String pFilePath){
        try {
            stopCompleteBG();
            if(!manager.getMusicOn()) return;
            backgroundPlayer.setDataSource(pFilePath);
            backgroundPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            backgroundPlayer.setVolume(0.1f,0.1f);
            backgroundPlayer.prepare();
            backgroundPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopBackgroundMusic(){
        backgroundPlayer.pause();
        mBGPauseTime = 0;
    }

    public void pauseBackgroundMusic(){
        if(backgroundPlayer.isPlaying()){
            mBGPauseTime = backgroundPlayer.getCurrentPosition();
            backgroundPlayer.pause();
        }
    }

    public void resumeBackgroundMusic(){
        if(!manager.getMusicOn()) return;
        backgroundPlayer.start();
        backgroundPlayer.seekTo(mBGPauseTime);
    }

    public void resumePageAudio(){
        if(!manager.getmNeedsPageAudio()) return;
        backgroundPlayer.start();
        backgroundPlayer.seekTo(mBGPauseTime);
    }

    public void playPageAudio(String pFilePath){
        try {
            stopPageAudio();
            if(!manager.getPageAudio()) return;
            textReadPlayer.setDataSource(pFilePath);
            textReadPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            textReadPlayer.setVolume(1.0f,1.0f);
            textReadPlayer.prepare();
            textReadPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPageAudio(){
        textReadPlayer.stop();
        textReadPlayer.reset();
    }

    public void pausePageAudio(){
        textReadPlayer.pause();
    }

    public void resumePageAudio(int pSeekTime){
        if(!manager.getPageAudio()) return;
        textReadPlayer.start();
        textReadPlayer.seekTo(pSeekTime);
    }

    public void stopALL(){
        stopPageAudio();
        stopBackgroundMusic();
    }
}
