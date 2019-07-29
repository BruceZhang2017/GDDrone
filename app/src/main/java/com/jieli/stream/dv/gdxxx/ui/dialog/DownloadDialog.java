package com.jieli.stream.dv.gdxxx.ui.dialog;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.player.OnDownloadListener;
import com.jieli.lib.dv.control.player.OnRecordListener;
import com.jieli.lib.dv.control.player.PlaybackStream;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DownloadInfo;
import com.jieli.stream.dv.gdxxx.data.OnRecordStateListener;
import com.jieli.stream.dv.gdxxx.data.VideoRecord;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseDialogFragment;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;

import java.io.File;

import static com.jieli.stream.dv.gdxxx.util.IConstant.RTS_TCP_PORT;

/**
 * 下载文件窗口
 */
public class DownloadDialog extends BaseDialogFragment implements View.OnClickListener{
    final String tag = getClass().getSimpleName();
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCounter;
    private NumberProgressBar pbNumber;

    private String title;
    private String content;
    private String counter;
    private int progress;
    private PlaybackStream mStreamPlayer;
//    private MovWrapper mMovWrapper;
    private VideoRecord mRecordVideo;
    private String mOutputPath = "/mnt/sdcard/download.MOV";
    private boolean isRecordPrepared = false;//For no-card of device mode only

    private static final int MAX_PROGRESS = 100;

    public static DownloadDialog newInstance(DownloadInfo fileInfo) {
        DownloadDialog downloadDialog = new DownloadDialog();
        Bundle args = new Bundle();
        args.putSerializable("file_info", fileInfo);
        downloadDialog.setArguments(args);
        return downloadDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_download, container, false);
        if(getDialog() != null){
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        tvTitle = (TextView) view.findViewById(R.id.dialog_download_title);
        tvContent = (TextView) view.findViewById(R.id.dialog_download_content);
        tvCounter = (TextView) view.findViewById(R.id.dialog_download_counter);
        pbNumber = (NumberProgressBar) view.findViewById(R.id.dialog_download_progress_bar);
        Button btnCancel = (Button) view.findViewById(R.id.dialog_download_cancel_btn);

        btnCancel.setOnClickListener(this);
        pbNumber.setMax(MAX_PROGRESS);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() != null && getDialog() != null && getDialog().getWindow() != null){

            Bundle args = getArguments();
            if (args != null) {
                DownloadInfo info = (DownloadInfo) args.getSerializable("file_info");
                if (info == null || TextUtils.isEmpty(info.getPath())) {
                    Dbug.e(tag, "File info error");
                    return;
                }
                Dbug.e(tag, "Selected file " + info.getPath());
                content = "download path : " +info.getPath();
                mStreamPlayer = new PlaybackStream();
                mStreamPlayer.setDownloadDuration(info.getDuration());
                Dbug.e(tag, "path " + info.getPath()+", offset " + info.getOffset());
                ClientManager.getClient().registerNotifyListener(mOnNotifyListener);
                ClientManager.getClient().tryToStartPlayback(info.getPath(), info.getOffset(), new SendResponse() {
                    @Override
                    public void onResponse(Integer integer) {
                        if(integer != Constants.SEND_SUCCESS) {
                            Dbug.e(tag, "Send failed");
                        }
                    }
                });
            }
            initUI();
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = 200;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                params.width = displayMetrics.heightPixels * 4 / 5;
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                params.width = displayMetrics.widthPixels * 4 / 5;
            }
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            getDialog().getWindow().setAttributes(params);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        }
    }

    private final OnNotifyListener mOnNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, Code.getCodeDescription(data.getErrorType()));
                return;
            }
            //Dbug.e(tag, "topic=" + data.getTopic());
            switch (data.getTopic()) {
                case Topic.PLAYBACK:
                    if (mStreamPlayer == null) {
                        mStreamPlayer = new PlaybackStream();
                    }
                    Dbug.i(tag, "create playback..................");
                    mStreamPlayer.setStreamMode(PlaybackStream.Mode.DOWNLOAD);
                    mStreamPlayer.setOnDownloadListener(onDownloadListener);
                    mStreamPlayer.create(RTS_TCP_PORT, ClientManager.getClient().getConnectedIP());
                    break;
            }
        }
    };

    private final OnDownloadListener onDownloadListener = new OnDownloadListener() {
        @Override
        public void onStart() {
            Dbug.w(tag, "start download");

            mRecordVideo = new VideoRecord(mStreamPlayer.getCurrentMediaInfo());
            mRecordVideo.prepare(new OnRecordStateListener() {
                @Override
                public void onPrepared() {
                    isRecordPrepared = true;
                }

                @Override
                public void onStop() {
                    Dbug.i(tag, "Record onStop");
                    isRecordPrepared = false;
                    showToastShort(R.string.record_success);
                }

                @Override
                public void onError(String message) {
                    Dbug.e(tag, "Record error:" + message);
                    if (mRecordVideo != null) {
                        String outputPath = mRecordVideo.getCurrentFilePath();
                        if (!TextUtils.isEmpty(outputPath)) {
                            File file = new File(outputPath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    showToastShort(R.string.record_fail);
                    isRecordPrepared = false;
                    mRecordVideo = null;
                }
            });
        }

        @Override
        public void onReceived(int type, byte[] data) {
//            Dbug.w(tag, "type " + type + ", data=" + data.length);

            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(type, data)) Dbug.e(tag, "Write failed");
            }
        }

        @Override
        public void onProgress(float progress) {
            Dbug.w(tag, "Progress " + progress);
            updateNumberPb((int) (progress*100));
        }

        @Override
        public void onStop() {
            Dbug.w(tag, "onStop " + progress);
           stopDownload();
        }

        @Override
        public void onError(int code, String message) {
            Dbug.e(tag, "Error: code "+ code +", msg="+message);
            a.getApplication().showToastShort("download failed, "+ message);
            closeMovWrapper();
            dismissDialog();
        }
    };

    private final OnRecordListener onRecordListener = new OnRecordListener() {
        @Override
        public void onError(int code, String msg) {
            Dbug.e(tag, "Code " + code + ", msg:"+msg);
            a.getApplication().showToastShort("download failed, "+ msg);
            closeMovWrapper();
            dismissDialog();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeMovWrapper();
        ClientManager.getClient().unregisterNotifyListener(mOnNotifyListener);
        if (mStreamPlayer != null) {
            mStreamPlayer.release();
            mStreamPlayer = null;
        }
    }

    @Override
    public void onClick(View view) {
        if(getActivity() != null && getDialog() != null && view != null){
            switch (view.getId()){
                case R.id.dialog_download_cancel_btn:
                    File file = new File(mOutputPath);
                    if(file.exists()){
                        file.delete();
                    }
                    stopDownload();
                    break;
            }
        }
    }

    public void initUI(){
        if(getActivity() != null && getDialog() != null){
            if(!TextUtils.isEmpty(title)){
                tvTitle.setText(title);
            }else{
                tvTitle.setText(R.string.download_file);
            }
            if(!TextUtils.isEmpty(content)){
                tvContent.setText(content);
            }else{
                tvContent.setText("");
            }
            if(!TextUtils.isEmpty(counter)){
                tvCounter.setText(counter);
            }else {
                tvCounter.setText("");
            }
            if(progress > 0){
                pbNumber.setProgress(progress);
            }
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public void setProgress(int progress) {
        this.progress = progress > MAX_PROGRESS ? MAX_PROGRESS : progress;
    }

    public void updateNumberPb(int progress){
        setProgress(progress);
        if(pbNumber != null){
            pbNumber.setProgress(this.progress);
        }
    }

    public int getProgress() {
        return progress;
    }

    private void stopDownload(){
        ClientManager.getClient().tryToChangePlaybackState(DeviceClient.DEV_PB_STOP, new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != Constants.SEND_SUCCESS)
                    Dbug.e(tag, "Send failed");
            }
        });
        Dbug.w(tag, "stop download");
        closeMovWrapper();
        dismissDialog();
    }

    private void closeMovWrapper(){
        if (mRecordVideo != null) {
            mRecordVideo.close();
            mRecordVideo = null;
        }
    }

    private void dismissDialog(){
        if (isShowing())
            dismiss();
    }
}
