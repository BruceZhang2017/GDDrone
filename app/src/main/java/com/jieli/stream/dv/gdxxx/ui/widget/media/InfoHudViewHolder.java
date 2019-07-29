package com.jieli.stream.dv.gdxxx.ui.widget.media;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.TableLayout;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.task.DebugHelper;

import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaPlayerProxy;

public class InfoHudViewHolder {
    private TableLayoutBinder mTableLayoutBinder;
    private SparseArray<View> mRowMap = new SparseArray<View>();
    private IMediaPlayer mMediaPlayer;
    private Context mContext;
    private long mLoadCost = 0;
    private long mSeekCost = 0;

    public InfoHudViewHolder(Context context, TableLayout tableLayout) {
        mContext = context;
        mTableLayoutBinder = new TableLayoutBinder(context, tableLayout);
    }

    private void appendSection(int nameId) {
        mTableLayoutBinder.appendSection(nameId);
    }

    private void appendRow(int nameId) {
        View rowView = mTableLayoutBinder.appendRow2(nameId, null);
        mRowMap.put(nameId, rowView);
    }

    public void setRowValue(int id, String value) {
        View rowView = mRowMap.get(id);
        if (rowView == null) {
            rowView = mTableLayoutBinder.appendRow2(id, value);
            mRowMap.put(id, rowView);
        } else {
            mTableLayoutBinder.setValueText(rowView, value);
        }
    }

    public void setMediaPlayer(IMediaPlayer mp) {
        mMediaPlayer = mp;
        if (mMediaPlayer != null) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500);
        } else {
            mHandler.removeMessages(MSG_UPDATE_HUD);
        }
    }

    private static String formatedDurationMilli(long duration) {
        if (duration >=  1000) {
            return String.format(Locale.getDefault(), "%.2f sec", ((float)duration) / 1000);
        } else {
            return String.format(Locale.getDefault(), "%d msec", duration);
        }
    }

    private static String formatedSpeed(long bytes,long elapsed_milli) {
        if (elapsed_milli <= 0) {
            return "0 B/s";
        }

        if (bytes <= 0) {
            return "0 B/s";
        }

        float bytes_per_sec = ((float)bytes) * 1000.f /  elapsed_milli;
        if (bytes_per_sec >= 1000 * 1000) {
            return String.format(Locale.getDefault(), "%.2f MB/s", ((float)bytes_per_sec) / 1000 / 1000);
        } else if (bytes_per_sec >= 1000) {
            return String.format(Locale.getDefault(), "%.1f KB/s", ((float)bytes_per_sec) / 1000);
        } else {
            return String.format(Locale.getDefault(), "%d B/s", (long)bytes_per_sec);
        }
    }

    public void updateLoadCost(long time)  {
        mLoadCost = time;
    }

    public void updateSeekCost(long time)  {
        mSeekCost = time;
    }

    private static String formatedSize(long bytes) {
        if (bytes >= 100 * 1000) {
            return String.format(Locale.getDefault(), "%.2f MB", ((float)bytes) / 1000 / 1000);
        } else if (bytes >= 100) {
            return String.format(Locale.getDefault(), "%.1f KB", ((float)bytes) / 1000);
        } else {
            return String.format(Locale.getDefault(), "%d B", bytes);
        }
    }

    private static final int MSG_UPDATE_HUD = 1;
    private static int count = 0;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_HUD: {
                    InfoHudViewHolder holder = InfoHudViewHolder.this;
                    IjkMediaPlayer mp = null;
                    if (mMediaPlayer == null)
                        break;
                    if (mMediaPlayer instanceof IjkMediaPlayer) {
                        mp = (IjkMediaPlayer) mMediaPlayer;
                    } else if (mMediaPlayer instanceof MediaPlayerProxy) {
                        MediaPlayerProxy proxy = (MediaPlayerProxy) mMediaPlayer;
                        IMediaPlayer internal = proxy.getInternalMediaPlayer();
                        if (internal != null && internal instanceof IjkMediaPlayer)
                            mp = (IjkMediaPlayer) internal;
                    }
                    if (mp == null)
                        break;

                    int vdec = mp.getVideoDecoder();
                    switch (vdec) {
                        case IjkMediaPlayer.FFP_PROPV_DECODER_AVCODEC:
                            setRowValue(R.string.vdec, "avcodec");
                            break;
                        case IjkMediaPlayer.FFP_PROPV_DECODER_MEDIACODEC:
                            setRowValue(R.string.vdec, "MediaCodec");
                            break;
                        default:
                            setRowValue(R.string.vdec, "");
                            break;
                    }

                    String audioDecoder = mp.getMediaInfo().mAudioDecoder;
                    setRowValue(R.string.audio_decoder, audioDecoder);
//                    float fpsOutput = mp.getVideoOutputFramesPerSecond();
//                    float fpsDecode = mp.getVideoDecodeFramesPerSecond();
//                    setRowValue(R.string.fps, String.format(Locale.getDefault(), "%.2f / %.2f", fpsDecode, fpsOutput));

//                    long videoCachedDuration = mp.getVideoCachedDuration();
//                    long audioCachedDuration = mp.getAudioCachedDuration();
//                    long videoCachedBytes    = mp.getVideoCachedBytes();
//                    long audioCachedBytes    = mp.getAudioCachedBytes();
//                    long tcpSpeed            = mp.getTcpSpeed();
//                    long bitRate             = mp.getBitRate();
//                    long seekLoadDuration    = mp.getSeekLoadDuration();
                    int videoWidth = mp.getVideoWidth();
                    int videoHeight = mp.getVideoHeight();
                    int audioSampleRate = 0;
                    String videoCodecName = "";
                    String audioCodecName = "";
                    if(mp.getMediaInfo().mMeta != null){
                        if(mp.getMediaInfo().mMeta.mAudioStream != null) {
                            audioSampleRate = mp.getMediaInfo().mMeta.mAudioStream.mSampleRate;
                            audioCodecName = mp.getMediaInfo().mMeta.mAudioStream.mCodecName;
                        }
                        if( mp.getMediaInfo().mMeta.mVideoStream != null){
                            videoCodecName = mp.getMediaInfo().mMeta.mVideoStream.mCodecName;
                        }
                    }

                    setRowValue(R.string.video_rate, videoWidth +"x"+videoHeight);
                    if(audioSampleRate > 0){
                        setRowValue(R.string.audio_sample_rate, audioSampleRate+"");
                    }
                    if(!TextUtils.isEmpty(videoCodecName)){
                        setRowValue(R.string.v_codec, videoCodecName);
                    }
                    if(!TextUtils.isEmpty(audioCodecName)){
                        setRowValue(R.string.a_codec, audioCodecName);
                    }
//                    setRowValue(R.string.v_cache, String.format(Locale.getDefault(), "%s, %s", formatedDurationMilli(videoCachedDuration), formatedSize(videoCachedBytes)));
//                    setRowValue(R.string.a_cache, String.format(Locale.getDefault(), "%s, %s", formatedDurationMilli(audioCachedDuration), formatedSize(audioCachedBytes)));
//                    setRowValue(R.string.load_cost, String.format(Locale.getDefault(), "%d ms", mLoadCost));
//                    setRowValue(R.string.seek_cost, String.format(Locale.getDefault(), "%d ms", mSeekCost));
//                    setRowValue(R.string.seek_load_cost, String.format(Locale.getDefault(), "%d ms", seekLoadDuration));
//                    setRowValue(R.string.tcp_speed, String.format(Locale.getDefault(), "%s", formatedSpeed(tcpSpeed, 1000)));
//                    setRowValue(R.string.bit_rate, String.format(Locale.getDefault(), "%.2f kb/s", bitRate/1000f));
                    count++;
                    if(count >= 2) {
                       count = 0;
                        setRowValue(R.string.bit_rate, DebugHelper.getNetSpeed(mContext));
                    }

                    mHandler.removeMessages(MSG_UPDATE_HUD);
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500);
                }
            }
            return false;
        }
    });
}
