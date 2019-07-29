package com.jieli.stream.dv.gdxxx.audio;

import android.media.AudioRecord;

/**
 * 音频录制管理器
 */

public class AudioRecordManager {
    private String tag = getClass().getSimpleName();

    private static AudioRecordManager instance;
    private AudioRecord audioRecord;
    private AudioConfig config;

    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private boolean isRecord = false;// 设置正在录制的状态
    private int voiceLevel = 1;

    private RecorderListener listener;

    public static final int START_RECORD_OK = 1;
    public static final int ERR_AUDIO_IS_RECORDING = -1;
    public static final int ERR_SD_CARD_NOT_EXIST = -2;


    public void setRecordListener(RecorderListener listener){
        this.listener = listener;
    }

    public interface RecorderListener{
        void onRecord(byte[] data, int dB);
    }

    public AudioRecordManager(){
        config = new AudioConfig();
        createAudioRecord();
    }

    public static AudioRecordManager getInstance(){
        if(instance == null){
            synchronized (AudioRecordManager.class){
                if(instance == null){
                    instance = new AudioRecordManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建AudioRecord对象
     */
    private void createAudioRecord(){
        if(config == null) return;
        try{
            //获得缓冲区字节大小
            bufferSizeInBytes = AudioRecord.getMinBufferSize(config.getAudioSampleRate(),
                    config.getChannelConfig(), config.getAudioFormat());

            if(bufferSizeInBytes != AudioRecord.ERROR_BAD_VALUE){
                //创建AudioRecord对象
                audioRecord = new AudioRecord(config.getAudioInputWay(), config.getAudioSampleRate(),
                        config.getChannelConfig(), config.getAudioFormat(), bufferSizeInBytes);
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }

    /**
     * 判断是否在录音
     */
    public boolean isRecord(){
        return isRecord;
    }

    /**
     * 开始音频录制
     * @return 结果码
     */
    public int startRecord() {
        if(isRecord) {
            return ERR_AUDIO_IS_RECORDING;
        } else {
            if(audioRecord == null)
                createAudioRecord();

            if(audioRecord != null){
                audioRecord.startRecording();
                // 让录制状态为true
                isRecord = true;
            }
            // 开启音频文件写入线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readData();//往文件中写入裸数据
                }
            }).start();

            return START_RECORD_OK;
        }
    }

    /**
     * 停止音频录制
     */
    public void stopRecord() {
        if (audioRecord != null) {
            isRecord = false;//停止数据读取
            if(audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING){
                audioRecord.stop();
            }
            audioRecord.release();//释放资源
            audioRecord = null;
        }
    }

    /**
     * 读取录音数据
     */
    private void readData() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audioData = new byte[bufferSizeInBytes];
        int readSize;
        while (isRecord) {
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION  != readSize) {
                long v = 0;
                // 将 buffer 内容取出，进行平方和运算
                for (byte anAudioData : audioData) {
                    v += anAudioData * anAudioData;
                }
                double mean = v / (double) readSize;
                voiceLevel = (int)(10 * Math.log10(mean));
                if(listener != null){
                    listener.onRecord(audioData, voiceLevel);
                }
            }
        }
    }

    /**
     * 获得音频文件的大小
     */
    public long getRecordFileSize(String path){
        return AudioConfig.getFileSize(path);
    }

    /**
     * 获取实时音量
     * @return 单位：分贝（dB）
     */
    public int getVoiceLevel() {
        return voiceLevel;
    }

    public void release(){
        stopRecord();
        instance = null;
    }


}
