package com.jieli.stream.dv.gdxxx.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;

import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.IConstant;

import java.io.File;

import static com.jieli.stream.dv.gdxxx.util.IConstant.AUDIO_SAMPLE_RATE_DEFAULT;

/**
 * Audio 参数配置
 */

public class AudioConfig {

    //音频输入-麦克风
    private int audioInput = MediaRecorder.AudioSource.MIC;
    //采用频率
    private int audioSampleRate = AUDIO_SAMPLE_RATE_DEFAULT;  //默认使用8KHz
    //输入声道设置
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO; //默认使用单声道
    //输入编码制式和采样大小
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //默认使用pcm_16bit
    //输出声道设置
    private int outputChannel = AudioFormat.CHANNEL_OUT_MONO; //默认使用单声道

    public int getAudioInputWay() {
        return audioInput;
    }

    public void setAudioInputWay(int audioInput) {
        this.audioInput = audioInput;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getOutputChannel() {
        return outputChannel;
    }

    public void setOutputChannel(int outputChannel) {
        this.outputChannel = outputChannel;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    /**
     * 判断是否有外部存储设备sdcard
     * @return true | false
     */
    public static boolean isSdcardExit(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取麦克风输入的原始音频流文件路径
     */
    public static String getRawFilePath(){
        String mAudioRawPath = "";
        if(isSdcardExit()){
            a mApplication = a.getApplication();
            String fileBasePath = AppUtils.splicingFilePath(mApplication.getAppFilePath(),
                    mApplication.getCameraDir(), IConstant.DIR_RECORD);
            mAudioRawPath = fileBasePath + File.separator + IConstant.AUD_DEFAULT_NAME;
        }
        return mAudioRawPath;
    }

    /**
     * 获取文件大小
     * @param path 文件的绝对路径
     */
    public static long getFileSize(String path){
        if(TextUtils.isEmpty(path)) return -1;
        File mFile = new File(path);
        if(!mFile.exists())
            return -1;
        return mFile.length();
    }

}
