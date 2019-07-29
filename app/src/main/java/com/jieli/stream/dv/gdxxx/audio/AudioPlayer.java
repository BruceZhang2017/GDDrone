package com.jieli.stream.dv.gdxxx.audio;

import android.media.AudioTrack;

/**
 * Audio 音频播放器
 */

public class AudioPlayer {

    private AudioTrack mAudioTrack;
    private AudioConfig audioConfig;
    private static AudioPlayer instance;

    // 缓冲区字节大小
    private int bufferSize;

    public AudioPlayer(){
        audioConfig = new AudioConfig();
        createAudioPlayer();
    }

    public static AudioPlayer getInstance() {
        if(instance == null){
            synchronized (AudioPlayer.class){
                if(instance == null){
                    instance = new AudioPlayer();
                }
            }
        }
        return instance;
    }

    /**
     * 创建AudioTrack对象
     */
    public void createAudioPlayer(){
        if(audioConfig == null) return;
        try{
            bufferSize = AudioTrack.getMinBufferSize(audioConfig.getAudioSampleRate(),
                    audioConfig.getOutputChannel(), audioConfig.getAudioFormat());

            if(bufferSize != AudioTrack.ERROR_BAD_VALUE){
                mAudioTrack = new AudioTrack(android.media.AudioManager.STREAM_MUSIC,
                        audioConfig.getAudioSampleRate(), audioConfig.getOutputChannel(),
                        audioConfig.getAudioFormat(), bufferSize, AudioTrack.MODE_STREAM);
            }
            if (mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }

    /**
     * 开始播放
     * @param dataBuf  数据
     */
    public void startPlay(byte[] dataBuf){
        if(dataBuf != null){
            if(mAudioTrack == null || mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
                createAudioPlayer();
            }
            try{
                if (mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                    if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                        mAudioTrack.write(dataBuf, 0, dataBuf.length);
                    }else{
                        mAudioTrack.play();
                        mAudioTrack.write(dataBuf, 0, dataBuf.length);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay(){
        if(mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED){
            if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                mAudioTrack.pause();
                mAudioTrack.flush();
            }
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay(){
        if(mAudioTrack != null){
            if(mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED){
                mAudioTrack.pause();
                mAudioTrack.flush();
                mAudioTrack.stop();
            }
            mAudioTrack.release();
            mAudioTrack = null;
        }
        instance = null;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isPlaying(){
        return mAudioTrack != null && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED
                && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }
}
