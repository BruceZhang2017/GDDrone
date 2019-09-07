package com.jieli.stream.dv.gdxxx.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArrayMap;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.jieli.lib.dv.control.DeviceClient;
import com.jieli.lib.dv.control.connect.listener.OnConnectStateListener;
import com.jieli.lib.dv.control.connect.response.SendResponse;
import com.jieli.lib.dv.control.intercom.IntercomManager;
import com.jieli.lib.dv.control.json.bean.NotifyInfo;
import com.jieli.lib.dv.control.json.bean.RequestCmd;
import com.jieli.lib.dv.control.json.bean.SettingCmd;
import com.jieli.lib.dv.control.utils.Constants;
import com.jieli.lib.dv.control.utils.Dlog;
import com.jieli.stream.dv.gdxxx.baidu.utils.LocationService;
import com.jieli.stream.dv.gdxxx.data.FlyData;
import com.jieli.stream.dv.gdxxx.data.FlyFollowModel;
import com.jieli.stream.dv.gdxxx.data.FlyHoverModel;
import com.jieli.stream.dv.gdxxx.data.FlyLockModel;
import com.jieli.stream.dv.gdxxx.data.FlyStateModel;
import com.jieli.stream.dv.gdxxx.data.OnTakePictureListener;
import com.jieli.stream.dv.gdxxx.data.takeoffOrLandingModel;
import com.jieli.stream.dv.gdxxx.task.DebugHelper;
import com.jieli.stream.dv.gdxxx.task.IDebugListener;
import com.jieli.lib.dv.control.player.OnRealTimeListener;
import com.jieli.lib.dv.control.player.RealtimeStream;
import com.jieli.lib.dv.control.player.Stream;
import com.jieli.lib.dv.control.receiver.listener.OnNotifyListener;
import com.jieli.lib.dv.control.utils.Code;
import com.jieli.lib.dv.control.utils.Topic;
import com.jieli.lib.dv.control.utils.TopicKey;
import com.jieli.lib.dv.control.utils.TopicParam;
import com.jieli.media.codec.FrameCodec;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.DeviceDesc;
import com.jieli.stream.dv.gdxxx.bean.DeviceSettingInfo;
import com.jieli.stream.dv.gdxxx.data.OnRecordStateListener;
import com.jieli.stream.dv.gdxxx.data.VideoCapture;
import com.jieli.stream.dv.gdxxx.data.VideoRecord;
import com.jieli.stream.dv.gdxxx.ui.a;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.WaitingDialog;
import com.jieli.stream.dv.gdxxx.ui.service.CommunicationService;
import com.jieli.stream.dv.gdxxx.ui.widget.PopupMenu;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IMediaController;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IRenderView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.IjkVideoView;
import com.jieli.stream.dv.gdxxx.ui.widget.media.InfoHudViewHolder;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.ClientManager;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.IConstant;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.ShareTimer;
import com.jieli.stream.dv.gdxxx.util.WifiHelper;
import com.jieli.stream.dv.gdxxx.util.WifiP2pHelper;
import com.nineoldandroids.view.ViewHelper;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.content.Context.MODE_PRIVATE;
import static com.jieli.lib.dv.control.utils.Constants.SEND_SUCCESS;

import com.baidu.*;

//设置参数：
//        1.camera参数：亮度(0~127)，对比度(0~512),曝光度(-3~+3)和白平衡(0~4)
//        2.wifi参数（option）：wifi名称，密码[option]
//        3.固件版本
//        4.APP版本
//        5.OTA升级

//解决超时会自动返回问题。


//亮度-128-127(0) 曝光-3-3(0) 对比度0-511(255) 白平衡0-4（0）
//seekBarBrightness


public class MainActivity extends BaseActivity implements View.OnClickListener,
        IntercomManager.OnSocketErrorListener, SensorEventListener {
    private String tag = getClass().getSimpleName();
    private ImageView mAdjustResolutionBtn;

    private RelativeLayout leftControlBar;
    private RelativeLayout rightControlBar;
    private LinearLayout topControlBar;
    private RelativeLayout bottomControlBar;

    private RelativeLayout leftPanel;
    private RelativeLayout rightPanel;

    private TextView tvTemp;
    private TextView tvDistance;
    private TextView tvSpeedV;
    private TextView tvSpeedH;
    private TextView tvPlanet;
    private ImageView ivLock;
    private Switch mSwitch;

    private IjkVideoView mStreamView;
    private RelativeLayout mSnapshootFlash;
    private long mSnapshootFlashTime;
    private boolean mSnapshootFlashVisible;
    private ProgressBar progressBarLoading;
    private ImageButton btnPlay;//Live only
    private TableLayout mMoreHub;
    private View viewBackground;

    private NotifyDialog mErrorDialog;
    private PopupMenu popupMenu;
    private WaitingDialog mWaitingDialog;
    private WaitingDialog mAdjustingDialog;

    private PowerManager.WakeLock wakeLock;
    private RealtimeStream mRealtimeStream;
    private FrameCodec mFrameCodec = null;
    private VideoRecord mRecordVideo;
    private VideoCapture mVideoCapture;
    private DebugHelper mDebugHelper;
    private int mTakePictureCount = 0;

    private int mCameraType = DeviceClient.CAMERA_FRONT_VIEW;
    private int recordStatus;
    private boolean isIJKPlayerOpen;
    private boolean isRecordPrepared = false;//For no-card of device mode only
    private boolean isCaptureBusying = false;//For no-card of device mode only
    private boolean isCaptureBusying_Main = false;
    private boolean isAdjustResolution;
    private boolean isRtspEnable;
    private boolean isStartDebug;
    private int threshold;
    private int fps;

    private int kDeadValue = 200;

    private static final long DELAY_TIME = 100L;

    private static final int MSG_TAKE_VIDEO = 0x0a00;
    private static final int MSG_TAKE_PHOTO = 0x0a01;
    private static final int MSG_FPS_COUNT = 0x0a05;
    private static final int MSG_BACEPRESSED = 0x0a87;

    private boolean isGSensor = false;
    TextView txtStatus, txtRecorderTime;
    ImageView txtRecorderRed;
    long mRecorderTimeCount = 0;
    ShareTimer mPublicTimer = null, mRecorderTimer = null;
    ImageButton btnWifi, btnRecorder, btnTakePhoto, btnHidePanel, btnMore, btnGSensor, btnSensorSetting, btnPhotos, btnLandingOrTakeoff, btnFlyBack, btnSpeed, btnSettings;
    ImageView imgGSensorPanel;
    ImageView btnOilPoint, btnGSensorPoint;
    RelativeLayout centerSensorPanel;
    ImageView imgBattery;
    RelativeLayout rl_comprass;
    ImageView iv_arrow;
    ImageView iv_top_left_bg;
    boolean isHeadless = false;

    private static final int MSG_RECONNECTION_DEVICE = 0;
    private static final int MSG_STOP_RECONNECTION_DEVICE = 1;
    private WaitingDialog waitingDialog;
    private NotifyDialog upgradeNotifyDialog;
    private WifiP2pHelper mWifiP2pHelper;

    private NotifyDialog openWifiDialog;
    private NotifyDialog reconnectionDialog;

    private LocationService locationService;
    int latitude; // 纬度
    int longitude; // 经度
    int latitudeB;
    int longitudeB;
    private boolean temSwitchHiden = true;

    private boolean gpsStarting = false;

    int flyType = 0; // 飞机模式
    int flyStatus = 0; // 飞机状态

    int flyBackCount = 0; // 一键返航执行次数，不超过3次
    int cancelFlyBackCount = 0; // 取消一键返航执行次数，不超过3次
    int flyFollowCount = 0; // GPS跟随执行次数，不超过3次
    int cancelFlyFollowCount = 0; // 取消GPS跟随执行次数，不超过3次
    int flyUpCount = 0; // 一键起飞执行次数，不超过3次
    int flyDownCount = 0; // 一键降落执行次数，不超过3次

    //ljw
    int radius = 80;//圆半径
    private Handler delayHandler = new Handler();
    private static int speedLevel = PreferencesHelper.SPEED_LEVEL_L;
    //private

    private boolean isReConnectDev = false;
    private int reConnectNum;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message != null) {
                switch (message.what) {
                    case MSG_TAKE_VIDEO:
                        if (isPlaying()) {
                            if (!AppUtils.isFastDoubleClick(1500)) {
                                if (isRecordPrepared) {
                                    stopLocalRecording_Loop();
                                } else {
                                    //startLocalRecording();
                                    startLocalRecording_Loop();
                                }
                            } else {
                                a.getApplication().showToastShort(R.string.dialod_wait);
                            }
                        } else {
                            a.getApplication().showToastShort(R.string.open_rts_tip);
                            //if (mTryConnectCount < 5) {
                                //mTryConnectCount++;
                                tryConnectWifiDevice();
                            //}
                        }
                        break;
                    case MSG_TAKE_PHOTO:
                        if (isPlaying()) {
                            //方法二
//                            if(isCaptureBusying_Main==true){
//                                a.getApplication().showToastShort(R.string.dialod_wait);
//                                break;
//                            }
//                            isCaptureBusying_Main=true;

                            mSnapshootFlashVisible = true;
                            mSnapshootFlash.setVisibility(View.VISIBLE);
                            mSnapshootFlashTime = SystemClock.uptimeMillis();

                            mTakePictureCount++;

                            //String outPath = AppUtils.splicingFilePath(a.getApplication().getAppName(), a.getApplication().getUUID(),a.getApplication().getCameraDir(), IConstant.DIR_DOWNLOAD) + File.separator + AppUtils.getLocalPhotoName();
                            //解决微信分享失败问题，即先保存进相册再扫描进数据库

                            final String sFilePath = AppUtils.splicingFilePath(a.getApplication().getAppFilePath(), a.getApplication().getCameraDir(), IConstant.DIR_DOWNLOAD);
                            final String sFileName = AppUtils.getLocalPhotoName();
                            String outPath = sFilePath + File.separator + sFileName;
                            Log.e("PhotoFile", outPath);
                            if (!TextUtils.isEmpty(outPath)) {
                                mStreamView.takePicture(outPath, new OnTakePictureListener() {
                                    @Override
                                    public void onCompleted(String sFile) {
                                        mTakePictureCount--;
                                        //1.所有文件存在到相册目录，2.扫描插入图库，否则无法微信分享
                                        a.getApplication().insertPhotoPath(sFile);
                                    }

                                    @Override
                                    public void onFailed(String sFile, String sResult) {
                                        Dbug.w("takePicture-onFailed", " onFailed");
                                        //失败多重试一次
                                        mStreamView.takePicture(sFile, null);
                                    }
                                });
                            }
                            shootSound();
                        } else {
                            if (!isAdjustResolution) {
                                a.getApplication().showToastShort(R.string.open_rts_tip);
                            }
                            //if (mTryConnectCount < 5) {
                               // mTryConnectCount++;
                                tryConnectWifiDevice();
                            //}
                        }
                        break;
                    case MSG_FPS_COUNT:
                        updateDebugFps(fps);
                        fps = 0;
                        mHandler.removeMessages(MSG_FPS_COUNT);
                        mHandler.sendEmptyMessageDelayed(MSG_FPS_COUNT, 1000);
                        break;


                    case MSG_STOP_RECONNECTION_DEVICE:
                        mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                        //reConnectNum = 0;
                        isReConnectDev = false;
                        //removeDeviceWifiMsg();
                        //mApplication.switchWifi();
                        dismissWaitingDialog();
                        break;
                    case MSG_RECONNECTION_DEVICE:
                        //需要重连，重连3次，不成功返回
                        Dbug.i(tag, "reconnecting reConnectNum=" + reConnectNum);
                        if (mWifiHelper.isWifiOpen()) {

                            //if (mTryConnectCount < 5) {
                               // mTryConnectCount++;
                                tryConnectWifiDevice();
                            //}
                        } else {
                            showOpenWifiDialog();
                        }
                        break;
                    case MSG_BACEPRESSED:
                        mHandler.removeMessages(MSG_BACEPRESSED);
                        break;
                }
            }
            return false;
        }
    });

    private void removeDeviceWifiMsg() {
        String saveSSID = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(CURRENT_WIFI_SSID, null);
        if (!TextUtils.isEmpty(saveSSID)) {
            PreferencesHelper.remove(getApplicationContext(), saveSSID);
            PreferencesHelper.remove(getApplicationContext(), CURRENT_WIFI_SSID);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (context != null && !TextUtils.isEmpty(action)) {
                switch (action) {
                    case ACTION_EMERGENCY_VIDEO_STATE:
                        int videoState = intent.getIntExtra(ACTION_KEY_VIDEO_STATE, -1);
                        int errorCode = intent.getIntExtra(ACTION_KEY_ERROR_CODE, -1);
                        if (errorCode == -1) {
                            switch (videoState) {
                                case STATE_START:
                                    closeRTS();
                                    break;
                                case STATE_END:
                                    if (mHandler != null) {
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                openRTS();
                                            }
                                        }, 300);
                                    }
                                    break;
                            }
                        } else {
                            if (mHandler != null) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        openRTS();
                                    }
                                }, 300);
                            }
                        }
                        break;
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, CommunicationService.class));
        registerBroadcast();
    }

    // 定义水平仪能处理的最大倾斜角，超过该角度，气泡将直接在位于边界。
    int MAX_ANGLE = 30;
    // 定义Sensor管理器
    SensorManager mSensorManager = null;
    TextView txtHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //修改APP语言
        final String lan = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_APP_LANGUAGE_CODE, "-1");
        if (!"-1".equals(lan))
            AppUtils.changeAppLanguage(getApplicationContext(), lan);

        Dbug.i(tag, "==================CREATE===============");
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_main);


        isRtspEnable = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(KEY_RTSP, false);
        //isRtspEnable=true;//解决图传机率性掉线问题。录像会出现问题。无法录像和不是720P

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, tag);
        wakeLock.setReferenceCounted(false);
        //wakeLock.acquire(60*60*60);
        wakeLock.acquire();


        threshold = AppUtils.dp2px(this, 20);


        //rt stream
        mStreamView = (IjkVideoView) findViewById(R.id.mStreamView);
        mStreamView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        mStreamView.setOnErrorListener(mOnErrorListener);
        mStreamView.setMediaController(iMediaController);
        mStreamView.setOnTouchListener(mOnTouchListener);
        mStreamView.setOnCompletionListener(onCompletionListener);
        mStreamView.setOnPreparedListener(onPreparedListener);

        //RelativeLayout mOther=(RelativeLayout) findViewById(R.id.mOther);
        //mOther.bringToFront();

        //mStreamView.setBackgroundResource(android.R.color.white);

        progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);

        mSnapshootFlash = (RelativeLayout) findViewById(R.id.mSnapshootFlash);

        //preview mode
        leftControlBar = (RelativeLayout) findViewById(R.id.leftControlBar);
        rightControlBar = (RelativeLayout) findViewById(R.id.rightControlBar);
        topControlBar = (LinearLayout) findViewById(R.id.topControlBar);
        bottomControlBar = (RelativeLayout) findViewById(R.id.bottomControlBar);

        leftPanel = (RelativeLayout) findViewById(R.id.leftPanel);
        rightPanel = (RelativeLayout) findViewById(R.id.rightPanel);
        rl_comprass = (RelativeLayout) findViewById(R.id.rl_comprass);
        iv_arrow = (ImageView) findViewById(R.id.iv_arrow);
        rl_comprass.setRotation(-90);
        iv_arrow.setRotation(90);
        iv_top_left_bg = (ImageView) findViewById(R.id.iv_top_left_bg);

        mAdjustResolutionBtn = (ImageView) findViewById(R.id.left_bar_adjust_resolution_btn);

        btnPlay = findImageButton_AutoBack(R.id.btnPlay, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRTS();
            }
        });


        //反转视图
        mAdjustResolutionBtn.setOnClickListener(this);

        mMoreHub = (TableLayout) findViewById(R.id.mMoreHub);
        viewBackground = (View) findViewById(R.id.viewBackground);

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/BITSUMISHIPROBOOK_0.OTF");
        tvTemp = (TextView)findViewById(R.id.tv_temp);
        //txtStatus = (TextView) findViewById(R.id.txtStatus);
        tvDistance = (TextView) findViewById(R.id.txtDistance);
        tvDistance.setTypeface(custom_font);
        tvSpeedV = (TextView) findViewById(R.id.txtSpeedV);
        tvSpeedV.setTypeface(custom_font);
        tvSpeedH = (TextView) findViewById(R.id.txtSpeedH);
        tvSpeedH.setTypeface(custom_font);
        tvPlanet = (TextView) findViewById(R.id.txtPlanet);
        tvPlanet.setTypeface(custom_font);
        ivLock = (ImageView) findViewById(R.id.imgLock);
        ivLock.setImageResource(R.mipmap.lock);
        mSwitch = (Switch) findViewById(R.id.switchLock);

        btnWifi = findImageButton_AutoBack(R.id.btnWifi, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                //如果使用lastApp而不使用lastActivity需要此设置
                aIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(aIntent);//ACTION_SETTINGS
            }
        });
        imgBattery = (ImageView) findViewById(R.id.imgBattery);

        btnRecorder = findImageButton_AutoBack(R.id.btnRecorder, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnected() == false) {
                    return;
                }
                if (mHandler != null) {
                    mHandler.removeMessages(MSG_TAKE_VIDEO);
                    mHandler.sendEmptyMessageDelayed(MSG_TAKE_VIDEO, DELAY_TIME);
                }
            }
        });
        //配合政府和地图，安全和报告成第一。
        txtRecorderTime = (TextView) findViewById(R.id.txtRecorderTime);
        txtRecorderRed = (ImageView) findViewById(R.id.txtRecorderRed);
        btnTakePhoto = findImageButton_AutoBack(R.id.btnTakePhoto, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnected() == false) {
                    return;
                }
                if (mHandler != null) {
                    mHandler.removeMessages(MSG_TAKE_PHOTO);
                    mHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO, DELAY_TIME);
                }
            }
        });
        btnHidePanel = findImageButton_AutoBack(R.id.btnHidePanel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPanelVisible();
            }
        });



        btnMore = findImageButton_AutoBack(R.id.btnMore, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow();
            }
        });
        btnGSensor = findImageButton_AutoBack(R.id.btnGSensor, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftPanel.getVisibility() != View.VISIBLE) {
                    return;
                }

                sensor();
            }
        });

        //Sensor参数设置透明面板
        btnSensorSetting = findImageButton_AutoBack(R.id.btnSensorSetting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSetVisible = centerSensorPanel.getVisibility() == View.GONE;

                if (isSetVisible == false) {
                    btnSensorSetting.setBackgroundResource(R.mipmap.main_sensor_setting_sel);


                    btnWifi.setEnabled(true);

                    btnRecorder.setEnabled(true);
                    btnTakePhoto.setEnabled(true);
                    btnHidePanel.setEnabled(true);
                    btnPhotos.setEnabled(true);

                    btnMore.setEnabled(true);
                    btnGSensor.setEnabled(true);
                    btnSettings.setEnabled(true);

                    btnLandingOrTakeoff.setEnabled(true);
                    btnFlyBack.setEnabled(true);
                    btnSpeed.setEnabled(false);

                    btnOilPoint.setEnabled(true);
                    btnGSensorPoint.setEnabled(true);

                } else {
                    //显示的时候才初始化值。

                    txtBrightnessValue.setText(mApplication.Camera_brt + "");
                    seekBarBrightness.setProgress(mApplication.Camera_brt + 128);

                    txtExposureValue.setText(mApplication.Camera_exp + "");
                    seekBarExposure.setProgress(mApplication.Camera_exp + 3);

                    txtContrastValue.setText(mApplication.Camera_ctr + "");
                    seekBarContrast.setProgress(mApplication.Camera_ctr);


                    String sText = "";
                    if (mApplication.Camera_wbl == 2) {
                        sText = getResources().getString(R.string.main_whitebalance_incandescent);
                    } else if (mApplication.Camera_wbl == 3) {
                        sText = getResources().getString(R.string.main_whitebalance_fluorescent);
                    } else if (mApplication.Camera_wbl == 5) {
                        sText = getResources().getString(R.string.main_whitebalance_auto);
                    } else if (mApplication.Camera_wbl == 0) {
                        sText = getResources().getString(R.string.main_whitebalance_sunlight);
                    } else if (mApplication.Camera_wbl == 1) {
                        sText = getResources().getString(R.string.main_whitebalance_cloundy);
                    }
                    txtWhiteBalanceValue.setText(sText);

                    int iProgress = mApplication.Camera_wbl_Default;
                    if (mApplication.Camera_wbl == 2) {
                        iProgress = 0;
                    } else if (mApplication.Camera_wbl == 3) {
                        iProgress = 1;
                    } else if (mApplication.Camera_wbl == 5) {
                        iProgress = 2;
                    } else if (mApplication.Camera_wbl == 0) {
                        iProgress = 3;
                    } else if (mApplication.Camera_wbl == 1) {
                        iProgress = 4;
                    }
                    seekBarWhiteBalance.setProgress(iProgress);

                    btnSensorSetting.setBackgroundResource(R.mipmap.main_sensor_setting);


                    btnWifi.setEnabled(false);

                    btnRecorder.setEnabled(false);
                    btnTakePhoto.setEnabled(false);
                    btnHidePanel.setEnabled(false);
                    btnPhotos.setEnabled(false);

                    btnMore.setEnabled(false);
                    btnGSensor.setEnabled(false);
                    btnSettings.setEnabled(false);

                    btnLandingOrTakeoff.setEnabled(false);
                    btnFlyBack.setEnabled(false);
                    btnSpeed.setEnabled(false);

                    btnOilPoint.setEnabled(false);
                    btnGSensorPoint.setEnabled(false);
                }
                centerSensorPanel.setVisibility(isSetVisible ? View.VISIBLE : View.GONE);
            }
        });


        btnPhotos = findImageButton_AutoBack(R.id.btnPhotos, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BrowseFileActivity.class));
            }
        });


        btnLandingOrTakeoff = findImageButton_AutoBack(R.id.btnLandingOrTakeoff, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnected() == false) {
                    return;
                }
                a.getApplication().showToastShort(R.string.one_key_fly);
                //ljw
                if (flyStatus == 2) {
                    RequestCmd aNewCmd = new RequestCmd();
                    aNewCmd.setTopic("GENERIC_CMD");
                    aNewCmd.setOperation("PUT");
                    aNewCmd.setParams(takeoffOrLandingModel.getFlyCtrlDataMap((byte) 0x02)); // 降落
                    ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                        @Override
                        public void onResponse(Integer integer) {
                            if (integer == SEND_SUCCESS) {
                                Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                            } else {
                                Log.i(tag, "GENERIC_CMD SEND_Fail");
                            }
                        }
                    });
                } else {
                    RequestCmd aNewCmd = new RequestCmd();
                    aNewCmd.setTopic("GENERIC_CMD");
                    aNewCmd.setOperation("PUT");
                    aNewCmd.setParams(takeoffOrLandingModel.getFlyCtrlDataMap((byte) 0x01)); // 起飞
                    ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                        @Override
                        public void onResponse(Integer integer) {
                            if (integer == SEND_SUCCESS) {
                                Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                            } else {
                                Log.i(tag, "GENERIC_CMD SEND_Fail");
                            }
                        }
                    });
                }

            }
        });

        btnFlyBack = findImageButton_AutoBack(R.id.btnFlyBack, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnected() == false) {
                    return;
                }
                //a.getApplication().showToastShort(R.string.one_key_return);
                flyback();
            }
        });
        btnSpeed = findImageButton_AutoBack(R.id.btnSpeed, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        speedLevel = PreferencesHelper.getSpeedLevelValue(this);

        btnSettings = findImageButton_AutoBack(R.id.btnSettings, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });

        // 获取水平仪的主组件
        //show = (PointView) findViewById(R.id.pointRight);
        // 获取传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        btnOilPoint = (ImageView) findViewById(R.id.btnOilPoint);
        imgGSensorPanel = (ImageView) findViewById(R.id.imgGSensorPanel);
        btnGSensorPoint = (ImageView) findViewById(R.id.btnGSensorPoint);

        //btnGSensorPoint.setX

        ClientManager.getClient().registerNotifyListener(onNotifyListener);

        mLeftCenterX = ViewHelper.getTranslationX(btnOilPoint);
        mLeftCenterY = ViewHelper.getTranslationY(btnOilPoint);
        leftPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (btnOilPoint.isEnabled() == false) {
                    return true;
                }
//                if (flyType == 0x06) { // 如果是跟随，则取消跟随
//                    follow();
//                }
                //获取屏幕的位置 xy值
                float tempX = event.getRawX();
                float tempY = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = tempX - mLeftManualLastX;
                        float deltaY = tempY - mLeftManualLastY;

                        float translationX = (ViewHelper.getTranslationX(btnOilPoint) + deltaX);
                        float translationY = (ViewHelper.getTranslationY(btnOilPoint) + deltaY);
                        double iDistance = getDistanceFromPoint(translationX, translationY, 0, 0);
                        if (iDistance > radius) {
                            float radian = calulateAngleLinePointA(translationX, translationY, 0, 0);
                            translationX = (float)(Math.cos(radian)*radius);
                            translationY = (float)(Math.sin(radian)*radius);
                        }

                        ViewHelper.setTranslationX(btnOilPoint, translationX);
                        ViewHelper.setTranslationY(btnOilPoint, translationY);

                        float x = ViewHelper.getTranslationX(btnOilPoint);
                        float y = ViewHelper.getTranslationY(btnOilPoint);

                        int valueX = (int)(x * 1000 / radius);
                        int valueY = (int)(y * 1000 / radius);

                        if ((valueX < kDeadValue && valueX > -kDeadValue) && (valueY > kDeadValue || valueY < -kDeadValue)) {
                            valueX = 0;
                        }
                        if ((valueY < kDeadValue && valueY > -kDeadValue) && (valueX > kDeadValue || valueX < -kDeadValue)) {
                            valueY = 0;
                        }
                        if ((valueX < kDeadValue && valueX > -kDeadValue) && (valueY < kDeadValue && valueY > -kDeadValue)) {
                            valueX = 0;
                            valueY = 0;
                        }

                        Log.d("data2", "valueX:" + valueX);
                        FlyData.rudderData[3] = valueY;
                        FlyData.rudderData[4] = valueX;

                        break;
                    case MotionEvent.ACTION_UP:
                        ViewHelper.setTranslationX(btnOilPoint, mLeftCenterX);
                        ViewHelper.setTranslationY(btnOilPoint, mLeftCenterY);

                        FlyData.rudderData[3] = 0;
                        FlyData.rudderData[4] = 0;
                        break;
                    default:
                        break;

                }
                mLeftManualLastX = tempX;
                mLeftManualLastY = tempY;

                //setBitSpecialByte_1

                return true;//false
            }
        });

        mRightCenterX = ViewHelper.getTranslationX(btnGSensorPoint);
        mRightCenterY = ViewHelper.getTranslationY(btnGSensorPoint);
        //时间，对话框，以及左边，GSensor
        rightPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (btnGSensorPoint.isEnabled() == false) {
                    return true;
                }
                //获取屏幕的位置 xy值
                float tempX = event.getRawX();
                float tempY = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isGSensor == false) {
                            float deltaX = tempX - mRightManualLastX;
                            float deltaY = tempY - mRightManualLastY;
                            float translationX = (ViewHelper.getTranslationX(btnGSensorPoint) + deltaX);
                            float translationY = (ViewHelper.getTranslationY(btnGSensorPoint) + deltaY);
                            double iDistance = getDistanceFromPoint(translationX, translationY, 0, 0);
                            if (iDistance > radius) {
                                float radian = calulateAngleLinePointA(translationX, translationY, 0, 0);
                                translationX = (float)(Math.cos(radian)*radius);
                                translationY = (float)(Math.sin(radian)*radius);
                            }
                            ViewHelper.setTranslationX(btnGSensorPoint, translationX);
                            ViewHelper.setTranslationY(btnGSensorPoint, translationY);

                            float x = ViewHelper.getTranslationX(btnGSensorPoint);
                            float y = ViewHelper.getTranslationY(btnGSensorPoint);

                            int valueX = (int)(x * 1000 / radius);
                            int valueY = (int)(y * 1000 / radius);

                            if ((valueX < kDeadValue && valueX > -kDeadValue) && (valueY > kDeadValue || valueY < -kDeadValue)) {
                                valueX = 0;
                            }
                            if ((valueY < kDeadValue && valueY > -kDeadValue) && (valueX > kDeadValue || valueX < -kDeadValue)) {
                                valueY = 0;
                            }
                            if ((valueX < kDeadValue && valueX > -kDeadValue) && (valueY < kDeadValue && valueY > -kDeadValue)) {
                                valueX = 0;
                                valueY = 0;
                            }

                            FlyData.rudderData[1] = valueX;
                            FlyData.rudderData[2] = valueY;
                            Log.i("dali2", "move:deltaX:" + valueX + ",deltaY:" + valueY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        ViewHelper.setTranslationX(btnGSensorPoint, mRightCenterX);
                        ViewHelper.setTranslationY(btnGSensorPoint, mRightCenterY);

                        FlyData.rudderData[1] = 0;
                        FlyData.rudderData[2] = 0;
                        break;
                    default:
                        break;

                }
                mRightManualLastX = tempX;
                mRightManualLastY = tempY;

                //setBitSpecialByte_1

                return true;//false
            }
        });

        centerSensorPanel = (RelativeLayout) findViewById(R.id.centerSensorPanel);


        ////亮度-128-127(0) 曝光-3-3(0) 对比度0-511(255) 白平衡0-4（0）
        seekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
        setSeekBarColor(seekBarBrightness, Color.BLACK);
        txtBrightnessValue = (TextView) findViewById(R.id.txtBrightnessValue);
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == false) {
                    return;
                }
                final int iValue = progress - 128;
                String sValue = iValue + "";
                txtBrightnessValue.setText(sValue);

                if (checkConnected() == false) {
                    return;
                }

                SettingCmd aNewCmd = new SettingCmd();
                aNewCmd.setTopic("PHOTO_BRIGHTNESS");
                aNewCmd.setOperation("PUT");
                ArrayMap aParam = new ArrayMap();
                aParam.put("brightness", sValue);
                aNewCmd.setParams(aParam);
                mApplication.Camera_brt = iValue;
                PreferencesHelper.putIntValue(a.getApplication(), "Camera_brt", iValue);
                ClientManager.getClient().tryToPut(aNewCmd, new SendResponse() {
                    @Override
                    public void onResponse(Integer integer) {
                        if (integer != SEND_SUCCESS) {
                            a.getApplication().showToastShort(R.string.setting_failed);
                        } else {
                        }
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        seekBarExposure = (SeekBar) findViewById(R.id.seekBarExposure);
        setSeekBarColor(seekBarExposure, Color.BLACK);
        txtExposureValue = (TextView) findViewById(R.id.txtExposureValue);
        seekBarExposure.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == false) {
                    return;
                }
                final int iValue = progress - 3;
                String sValue = iValue + "";
                txtExposureValue.setText(sValue);

                if (checkConnected() == false) {
                    return;
                }

                SettingCmd aNewCmd = new SettingCmd();
                aNewCmd.setTopic("PHOTO_EXP");
                aNewCmd.setOperation("PUT");
                ArrayMap aParam = new ArrayMap();
                aParam.put("exp", sValue);
                aNewCmd.setParams(aParam);
                mApplication.Camera_exp = iValue;
                PreferencesHelper.putIntValue(a.getApplication(), "Camera_exp", iValue);
                ClientManager.getClient().tryToPut(aNewCmd, new SendResponse() {
                    @Override
                    public void onResponse(Integer integer) {
                        if (integer != SEND_SUCCESS) {
                            a.getApplication().showToastShort(R.string.setting_failed);
                        } else {
                        }
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarContrast = (SeekBar) findViewById(R.id.seekBarContrast);
        setSeekBarColor(seekBarContrast, Color.BLACK);
        txtContrastValue = (TextView) findViewById(R.id.txtContrastValue);
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == false) {
                    return;
                }
                final int iValue = progress;
                String sValue = iValue + "";
                txtContrastValue.setText(sValue);

                if (checkConnected() == false) {
                    return;
                }

                SettingCmd aNewCmd = new SettingCmd();
                aNewCmd.setTopic("PHOTO_CONTRAST");
                aNewCmd.setOperation("PUT");
                ArrayMap aParam = new ArrayMap();
                aParam.put("contrast", sValue);
                aNewCmd.setParams(aParam);
                mApplication.Camera_ctr = iValue;
                PreferencesHelper.putIntValue(a.getApplication(), "Camera_ctr", iValue);
                ClientManager.getClient().tryToPut(aNewCmd, new SendResponse() {
                    @Override
                    public void onResponse(Integer integer) {
                        if (integer != SEND_SUCCESS) {
                            a.getApplication().showToastShort(R.string.setting_failed);
                        } else {
                        }
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarWhiteBalance = (SeekBar) findViewById(R.id.seekBarWhiteBalance);
        setSeekBarColor(seekBarWhiteBalance, Color.BLACK);
        txtWhiteBalanceValue = (TextView) findViewById(R.id.txtWhiteBalanceValue);
        seekBarWhiteBalance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == false) {
                    return;
                }
                int iValue = 0;
                if (progress == 0) {
                    iValue = 2;
                } else if (progress == 1) {
                    iValue = 3;
                } else if (progress == 2) {
                    iValue = 5;
                } else if (progress == 3) {
                    iValue = 0;
                } else if (progress == 4) {
                    iValue = 1;
                }

                String sValue = iValue + "";

                String sText = "";
                if (iValue == 2) {
                    sText = getResources().getString(R.string.main_whitebalance_incandescent);
                } else if (iValue == 3) {
                    sText = getResources().getString(R.string.main_whitebalance_fluorescent);
                } else if (iValue == 5) {
                    sText = getResources().getString(R.string.main_whitebalance_auto);
                } else if (iValue == 0) {
                    sText = getResources().getString(R.string.main_whitebalance_sunlight);
                } else if (iValue == 1) {
                    sText = getResources().getString(R.string.main_whitebalance_cloundy);
                }
                txtWhiteBalanceValue.setText(sText);

                if (checkConnected() == false) {
                    return;
                }

                SettingCmd aNewCmd = new SettingCmd();
                aNewCmd.setTopic("WHITE_BALANCE");
                aNewCmd.setOperation("PUT");
                ArrayMap aParam = new ArrayMap();
                aParam.put("wbl", sValue);
                aNewCmd.setParams(aParam);
                mApplication.Camera_wbl = iValue;
                PreferencesHelper.putIntValue(a.getApplication(), "Camera_wbl", iValue);
                ClientManager.getClient().tryToPut(aNewCmd, new SendResponse() {
                    @Override
                    public void onResponse(Integer integer) {
                        if (integer != SEND_SUCCESS) {
                            a.getApplication().showToastShort(R.string.setting_failed);
                        } else {
                        }
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtHeight = (TextView) findViewById(R.id.txtHeight);
        txtHeight.setTypeface(custom_font);

        if (mPublicTimer != null) {
            mPublicTimer.invalidate();
        }
        mPublicTimer = new ShareTimer(1001, 100, new ShareTimer.MainTask() {
            @Override
            public void onTimer(int iTimerCount) {
                long lNow = SystemClock.uptimeMillis();
                if (isPlaying() == false && lNow - mPlayStartTime > 2000) {
                    //判断到未正常开始播放视频流提醒或者重新尝试开始
                    if (mPlayTryCount < 5) {

                    } else {
                        //5次重试播放视频失败，请重新启动无人机电源或者APK，或者确信是符合要求的无人机。
                    }
                }
                if (mSnapshootFlashVisible && lNow - mSnapshootFlashTime > 6) {
                    mSnapshootFlashVisible = false;
                    mSnapshootFlash.setVisibility(View.GONE);
                }

                if (iTimerCount % 30 == 0) {
                    setCameraStatus();
                }

                //循环录像
                if (mIsStartRecordingLoop) {
                    if (lNow - mStartRecordingTime > Const_RecordingLoopTime) {
                        stopLocalRecording();
                        mStartRecordingTime = SystemClock.uptimeMillis();
                        startLocalRecording();
                    }
                }
            }
        });

        //监测home键
        mHomeWatcherReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeWatcherReceiver, filter);

        switchPanelVisible();//默认隐藏
        getPersimmions();

        //startGPS();
    }

    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void startGPS() {
        locationService =  ((a)getApplication()).locationService;
        locationService.registerListener(mListener);
        //注册监听
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        if (!isopen()){
            Toast.makeText(MainActivity.this, "GPS没开，请打开定位功能！", Toast.LENGTH_LONG).show();
            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(callGPSSettingIntent);
            return;
        }
        locationService.start();
    }

    private void sensor() {
        isGSensor = !isGSensor;
        if (isGSensor) {
            btnGSensor.setBackgroundResource(R.mipmap.main_gsensor_sel);
        } else {
            btnGSensor.setBackgroundResource(R.mipmap.main_gsensor);
        }
        ViewHelper.setTranslationX(btnGSensorPoint, mRightCenterX);
        ViewHelper.setTranslationY(btnGSensorPoint, mRightCenterY);

        //ljw
        FlyData.rudderData[1] = 0;
        FlyData.rudderData[2] = 0;
    }

    private void follow() {
        if (flyType == 0x06) {
            cancelFlyFollowCount++;
            stopFlyFollowTimer(); // 取消
            RequestCmd aNewCmd = new RequestCmd();
            aNewCmd.setTopic("GENERIC_CMD");
            aNewCmd.setOperation("PUT");
            aNewCmd.setParams(FlyHoverModel.getFlyCtrlDataMap()); // 悬停
            ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                @Override
                public void onResponse(Integer integer) {
                    if (integer == SEND_SUCCESS) {
                        Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                    } else {
                        Log.i(tag, "GENERIC_CMD SEND_Fail");
                    }
                }
            });
        } else {
            flyFollowCount++;
            startFlyFollowTimer();
        }
    }

    private void flyback() {
        if (flyType == 0x04) {
            cancelFlyBackCount++;
            RequestCmd aNewCmd = new RequestCmd();
            aNewCmd.setTopic("GENERIC_CMD");
            aNewCmd.setOperation("PUT");
            aNewCmd.setParams(FlyHoverModel.getFlyCtrlDataMap()); // 悬停
            ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                @Override
                public void onResponse(Integer integer) {
                    if (integer == SEND_SUCCESS) {
                        Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                    } else {
                        Log.i(tag, "GENERIC_CMD SEND_Fail");
                    }
                }
            });
        } else {
            flyBackCount++;
            RequestCmd aNewCmd = new RequestCmd();
            aNewCmd.setTopic("GENERIC_CMD");
            aNewCmd.setOperation("PUT");
            aNewCmd.setParams(takeoffOrLandingModel.getFlyCtrlDataMap((byte) 0x08)); // 一键返航
            ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                @Override
                public void onResponse(Integer integer) {
                    if (integer == SEND_SUCCESS) {
                        Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                    } else {
                        Log.i(tag, "GENERIC_CMD SEND_Fail");
                    }
                }
            });
        }
    }

    //    //监测home键1
    private HomeWatcherReceiver mHomeWatcherReceiver = null;

    public class HomeWatcherReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            Log.i("", "intentAction =" + intentAction);
            if (TextUtils.equals(intentAction, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.i("", "reason =" + reason);
                if (TextUtils.equals(SYSTEM_DIALOG_REASON_HOME_KEY, reason)) {
                    stopLocalRecording_Loop();
                    //closeRTS();
                    //MainActivity.this.finish(); //华为等认为崩溃
                }
            }
        }
    }

    //监测home键2
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int KeyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                //closeRTS();
                //this.finish();//华为等认为崩溃
                //System.gc();
                //return true;
                stopLocalRecording_Loop();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //    public void getSensorSettingValue(){
//        RequestCmd aNewCmd = new RequestCmd();
//        aNewCmd.setTopic("PHOTO_BRIGHTNESS");
//        aNewCmd.setOperation("GET");
//        ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
//            @Override
//            public void onResponse(Integer integer) {
//
//            }
//        });
//    }
    public final long Const_RecordingLoopTime = 3 * 60 * 1000;//60秒

    //setSyncSensorSetting
    public void setLastSensor() {
        if (mApplication.isCameraInit == true) {
            return;
        }
        mApplication.isCameraInit = true;

        SettingCmd aNewCmd1 = new SettingCmd();
        aNewCmd1.setTopic("PHOTO_BRIGHTNESS");
        aNewCmd1.setOperation("PUT");
        ArrayMap aParam1 = new ArrayMap();
        aParam1.put("brightness", mApplication.Camera_brt + "");
        aNewCmd1.setParams(aParam1);
        ClientManager.getClient().tryToPut(aNewCmd1, new SendResponse() {
            @Override
            public void onResponse(Integer integer) {
                if (integer != SEND_SUCCESS) {

                } else {
                    //mApplication.Camera_brt=mApplication.Camera_brt_Default;
                }
            }
        });

        SettingCmd aNewCmd2 = new SettingCmd();
        aNewCmd2.setTopic("PHOTO_EXP");
        aNewCmd2.setOperation("PUT");
        ArrayMap aParam2 = new ArrayMap();
        aParam2.put("exp", mApplication.Camera_exp + "");
        aNewCmd2.setParams(aParam2);
        ClientManager.getClient().tryToPut(aNewCmd2, new SendResponse() {
            @Override
            public void onResponse(Integer integer) {
                if (integer != SEND_SUCCESS) {

                } else {
                    //mApplication.Camera_exp=mApplication.Camera_exp_Default;
                }
            }
        });


        SettingCmd aNewCmd3 = new SettingCmd();
        aNewCmd3.setTopic("PHOTO_CONTRAST");
        aNewCmd3.setOperation("PUT");
        ArrayMap aParam = new ArrayMap();
        aParam.put("contrast", mApplication.Camera_ctr + "");
        aNewCmd3.setParams(aParam);
        ClientManager.getClient().tryToPut(aNewCmd3, new SendResponse() {
            @Override
            public void onResponse(Integer integer) {
                if (integer != SEND_SUCCESS) {

                } else {
                    //mApplication.Camera_ctr=mApplication.Camera_ctr_Default;
                }
            }
        });

        SettingCmd aNewCmd4 = new SettingCmd();
        aNewCmd4.setTopic("WHITE_BALANCE");
        aNewCmd4.setOperation("PUT");
        ArrayMap aParam4 = new ArrayMap();
        aParam4.put("wbl", mApplication.Camera_wbl + "");
        aNewCmd4.setParams(aParam4);
        ClientManager.getClient().tryToPut(aNewCmd4, new SendResponse() {
            @Override
            public void onResponse(Integer integer) {
                if (integer != SEND_SUCCESS) {

                } else {
                    //mApplication.Camera_wbl=mApplication.Camera_wbl_Default;
                }
            }
        });

        //wifi名称和密码，时间水印
        ClientManager.getClient().tryToSetTimeWatermark(mApplication.Camera_Watermark, new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != SEND_SUCCESS) {

                } else {

                }
            }
        });
    }


    //ljw
    Timer flyCtrlTimer;

    public void startFlyCtrlTimer() {
        if (flyCtrlTimer == null) {
            flyCtrlTimer = new Timer();
        }
        flyCtrlTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SettingCmd aNewCmd = new SettingCmd();
                aNewCmd.setTopic("GENERIC_CMD");
                aNewCmd.setOperation("PUT");
                aNewCmd.setParams(FlyData.getFlyCtrlDataMap());

                //Log.i(tag,"send flyCtrl");
                ClientManager.getClient().tryToPut(aNewCmd, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if (code != SEND_SUCCESS) {
                            Log.i(tag, "GENERIC_CMD SEND_Fail");
                        } else {
                            Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                        }
                    }
                });
            }
        }, 100, 50);//
    }

    public void stopFlyCtrlTimer() {
        if (flyCtrlTimer != null)
            flyCtrlTimer.cancel();
        flyCtrlTimer = null;
    }

    Timer flyFollowTimer;

    public void startFlyFollowTimer() {
        if (flyFollowTimer == null) {
            flyFollowTimer = new Timer();
        }
        flyFollowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SettingCmd aNewCmd = new SettingCmd();
                aNewCmd.setTopic("GENERIC_CMD");
                aNewCmd.setOperation("PUT");
                aNewCmd.setParams(FlyFollowModel.getFlyCtrlDataMap(latitude, longitude));

                //Log.i(tag,"send flyCtrl");
                ClientManager.getClient().tryToPut(aNewCmd, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if (code != SEND_SUCCESS) {
                            Log.i(tag, "GENERIC_CMD SEND_Fail");
                        } else {
                            Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                        }
                    }
                });
            }
        }, 100, 60);//
    }

    public void stopFlyFollowTimer() {
        if (flyFollowTimer != null)
            flyFollowTimer.cancel();
        flyFollowTimer = null;
    }


    Timer getFlyStatusTimer;

    public void startGetFlyStatusTimer() {
        if (getFlyStatusTimer == null) {
            getFlyStatusTimer = new Timer();
        }
        getFlyStatusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isPlaying()) {
                    Log.d(tag, "get fly status");
                    RequestCmd aNewCmd = new RequestCmd();
                    aNewCmd.setTopic("GENERIC_CMD");
                    aNewCmd.setOperation("PUT");
                    FlyStateModel model = new FlyStateModel();
                    int distance = (int)getDistance(latitude,longitude,longitudeB,latitudeB);
                    distance = Math.abs(distance);
                    if (distance > 300000) {
                        distance = 0;
                    }
                    aNewCmd.setParams(model.getFlyCtrlDataMap(distance * 10));
                    ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                        @Override
                        public void onResponse(Integer integer) {
                            if (integer == SEND_SUCCESS) {
                                Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                            } else {
                                Log.i(tag, "GENERIC_CMD SEND_Fail");
                            }
                        }
                    });
                }
            }
        }, 1000, 100);
    }


    private void stopGetFlyStatusTimer() {
        if (getFlyStatusTimer != null)
            getFlyStatusTimer.cancel();
        getFlyStatusTimer = null;
    }


    TextView txtHeighmHandlert;
    SeekBar seekBarBrightness, seekBarExposure, seekBarContrast, seekBarWhiteBalance;

    TextView txtBrightnessValue, txtExposureValue, txtContrastValue, txtWhiteBalanceValue;

    public void setSeekBarColor(SeekBar seekBar, int color) {
        LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
        Drawable dra = layerDrawable.getDrawable(2);
        dra.setColorFilter(color, PorterDuff.Mode.SRC);
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        seekBar.invalidate();
    }

    // dip转像素
    public int DipToPixels(Context context, int dip) {
        final float SCALE = context.getResources().getDisplayMetrics().density;

        float valueDips = dip;
        int valuePixels = (int) (valueDips * SCALE + 0.5f);

        return valuePixels;

    }

    float mLeftCenterX = 0;
    float mLeftCenterY = 0;
    private float mLeftManualLastX = 0;
    private float mLeftManualLastY = 0;
    float mRightCenterX = 0;
    float mRightCenterY = 0;
    private float mRightManualLastX = 0;
    private float mRightManualLastY = 0;
    private boolean isSpeedHigh = false;

    //计算两点距离
    public double getDistanceFromPoint(float iNewX, float iNewY, float iOldX, float iOldY) {
        float deltaX = iNewX - iOldX;
        float deltaY = iNewY - iOldY;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public boolean checkConnected() {
        if (isDeviceConnected == false) {
            a.getApplication().showToastShort(R.string.please_connect_uav);
        } else {
            //a.getApplication().showToastShort(R.string.please_connect_uav_error);
        }
        return isDeviceConnected;
    }

    boolean isDeviceConnected = false;

    private void setCameraStatus() {
        int iLevel = 0;
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                int iRssi = winfo.getRssi();
                //iLevel=calculateSignalLevel(winfo.getRssi(),5);
                if (iRssi > -25) {
                    iLevel = 4;
                } else if (iRssi > -50) {
                    iLevel = 3;
                } else if (iRssi > -75) {
                    iLevel = 2;
                } else if (iRssi > -100) {
                    iLevel = 1;
                } else {
                    iLevel = 0;
                }
            }
        }
        if (isDeviceConnected) {
            viewBackground.setVisibility(View.INVISIBLE);
            //txtStatus.setText(R.string.main_status_online);
            if (iLevel == 0) {
                btnWifi.setBackgroundResource(R.mipmap.main_wifi_0);
            } else if (iLevel == 1) {
                btnWifi.setBackgroundResource(R.mipmap.main_wifi_1);
            } else if (iLevel == 2) {
                btnWifi.setBackgroundResource(R.mipmap.main_wifi_2);
            } else if (iLevel == 3) {
                btnWifi.setBackgroundResource(R.mipmap.main_wifi_3);
            } else if (iLevel > 3) {
                btnWifi.setBackgroundResource(R.mipmap.main_wifi_4);
            }
        } else {
            mApplication.WiFiPassword = null;
            //viewBackground.setVisibility(View.VISIBLE);
            btnWifi.setBackgroundResource(R.mipmap.main_wifi_0);
            //txtStatus.setText(R.string.main_status_offline);
        }
    }

    private void tryConnectWifiDevice() {
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String ssid = WifiHelper.formatSSID(winfo.getSSID());
                //Dbug.w(tag, "onConnected: SSID=" + ssid + ", isReConnectDev=" + isReConnectDev);

                //if (!TextUtils.isEmpty(ssid) && ssid.contains(WIFI_PREFIX)) {
                String sGateWayIP = mWifiHelper.getGateWay(mApplication);
                Dbug.i(tag, "Second, connect device IP=" + sGateWayIP + ", isConnected=" + ClientManager.getClient().isConnected());
                if (!ClientManager.getClient().isConnected()) {
                    isDeviceConnected = false;
                    setCameraStatus();
                    mApplication.sendCommandToService(SERVICE_CMD_CONNECT_CTP, sGateWayIP);
                    ClientManager.getClient().registerConnectStateListener(deviceConnectStateListener);
                } else {
                    isDeviceConnected = true;
                    setCameraStatus();
                    //Play
                    openRTS();
                }
                //}else{
                //}

            }
        }
    }

    private static int MIN_RSSI = -100;
    private static int MAX_RSSI = 0;

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            int partitionSize = (MAX_RSSI - MIN_RSSI) / (numLevels - 1);
            return (rssi - MIN_RSSI) / partitionSize;
        }
    }

    private int mTryConnectCount = 0;
    private final OnConnectStateListener deviceConnectStateListener = new OnConnectStateListener() {
        @Override
        public void onStateChanged(Integer code) {
            Dbug.i(tag, "--onStateChanged-- " + Constants.getConnectDescription(code));
            dismissWaitingDialog();
            switch (code) {
                case Constants.DEVICE_STATE_CONNECTED: {
                    Dbug.i(tag, "Third, connect device success...");
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.removeMessages(MSG_STOP_RECONNECTION_DEVICE);
                    isReConnectDev = false;
                    ClientManager.getClient().tryToAccessDevice(String.valueOf(mApplication.getAppVersion()), new SendResponse() {
                        @Override
                        public void onResponse(Integer code) {
                            if (code != SEND_SUCCESS) {
                                isDeviceConnected = false;
                                Dbug.e(tag, "Send failed!!!");
                                //if (mTryConnectCount < 5) {
                                    //mTryConnectCount++;
                                    tryConnectWifiDevice();
                                //}
                            } else {
                                isDeviceConnected = true;
                                setCameraStatus();
                                //Play
                                openRTS();
                                //
                            }
                        }
                    });
                    break;
                }
                case Constants.DEVICE_STATE_CONNECTION_TIMEOUT:
                case Constants.DEVICE_STATE_EXCEPTION:
                    isDeviceConnected = false;
                    Dbug.w(tag, "error disconnected:WifiState=" + mWifiHelper.getWifiState());
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                    break;
                case Constants.DEVICE_STATE_DISCONNECTED:
                case Constants.DEVICE_STATE_UNREADY:
                    isDeviceConnected = false;
                    Dbug.e(tag, "Disconnect with device!!! Code=" + code);
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                    break;
            }
        }
    };

    private void showOpenWifiDialog() {
        if (openWifiDialog == null) {
            openWifiDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.open_wifi, R.string.dialog_exit, R.string.dialog_confirm, new NotifyDialog.OnNegativeClickListener() {
                @Override
                public void onClick() {
                    openWifiDialog.dismiss();
                    mApplication.popAllActivity();
                }
            }, new NotifyDialog.OnPositiveClickListener() {
                @Override
                public void onClick() {
                    WifiHelper.getInstance(getApplicationContext()).openWifi();
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                    openWifiDialog.dismiss();
                }
            });
        }
        if (!openWifiDialog.isShowing())
            openWifiDialog.show(mApplication.getTopActivity().getSupportFragmentManager(), "re_open_wifi");

    }

    private void showReconnectionDialog() {
        if (reconnectionDialog == null) {
            reconnectionDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.connection_timeout, R.string.comfirm, new NotifyDialog.OnConfirmClickListener() {
                @Override
                public void onClick() {
                    mHandler.removeMessages(MSG_RECONNECTION_DEVICE);
                    mHandler.sendEmptyMessageDelayed(MSG_RECONNECTION_DEVICE, 100);
                    reconnectionDialog.dismiss();
                }
            });
        }
        if (!reconnectionDialog.isShowing()) {
            reconnectionDialog.show(mApplication.getTopActivity().getSupportFragmentManager(), "re_connection");
        }
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter(ACTION_PROJECTION_STATUS);
        filter.addAction(ACTION_FORMAT_TF_CARD);
        filter.addAction(ACTION_EMERGENCY_VIDEO_STATE);
        a.getApplication().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onClick(View v) {
        if (v == mAdjustResolutionBtn) {
            if (isRtspEnable) {
                a.getApplication().showToastLong(R.string.not_supported_in_rtsp);
                return;
            }
            if (isPlaying()) {
                if (DEV_REC_DUAL.equals(mApplication.getDeviceDesc().getDevice_type()) && recordStatus == STATUS_RECORDING) {
                    a.getApplication().showToastShort(R.string.stop_recording_first);
                } else {
                    showPopupMenu(v);
                }
            } else {
                a.getApplication().showToastShort(R.string.open_rts_tip);
            }
        }
    }


    @Override
    public void onBackPressed() {
        ClientManager.getClient().unregisterNotifyListener(onNotifyListener);

        if (mHandler.hasMessages(MSG_BACEPRESSED)) {
            //MainActivity.this.finish();
            System.exit(0);
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_BACEPRESSED, 2000);
            Toast.makeText(MainActivity.this, R.string.finish_app, Toast.LENGTH_SHORT).show();
        }

    }

    //ljw
    private boolean isSnapShotValid = true;
    private int recordFlagTemp = -1;
    private int handHeldMatchFlag;
    private final OnNotifyListener onNotifyListener = new OnNotifyListener() {
        @Override
        public void onNotify(NotifyInfo data) {
            if (data == null) return;
            String topic = data.getTopic();
            if (TextUtils.isEmpty(topic) || topic.equals(Topic.KEEP_ALIVE)) return;
            if (data.getErrorType() != Code.ERROR_NONE) {
                Dbug.e(tag, "NotifyInfo:" + data + ", error:" + Code.getCodeDescription(data.getErrorType()));
                dismissWaitingDialog();
                if (recordStatus == STATUS_PREPARE) {
                    recordStatus = STATUS_NOT_RECORD;
                }
                if (isAdjustResolution) {
                    isAdjustResolution = false;
                    dismissAdjustingDialog();
                }
                switch (data.getErrorType()) {
                    case Code.ERROR_REAR_CAMERA_OFFLINE:
                        a.getApplication().showToastShort(getString(R.string.rear_camera_offline));
                        syncDeviceState();
                        return;
                    case Code.ERROR_RT_STREAM_OPEN_FAILED:
                        closeRTS();
                        return;
                    default:
                        Dbug.w(tag, "topic=" + data.getTopic() + ", reason : " + Code.getCodeDescription(data.getErrorType()));
                        return;
                }
            }
            Dbug.e(tag, "topic=" + data.getTopic());
            int port = RTS_UDP_PORT;
            switch (topic) {
                case Topic.OPEN_REAR_RTS:
                    port = RTS_UDP_REAR_PORT;
                    boolean isAutoRearCamera = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(AppUtils.getAutoRearCameraKey(mApplication.getUUID()), false);
                    if (isAutoRearCamera) {
                        PreferencesHelper.putBooleanValue(mApplication, AppUtils.getAutoRearCameraKey(mApplication.getUUID()), false);
                    }
                case Topic.OPEN_FRONT_RTS:
                    Dbug.i(tag, "Open result:" + data);
                    if (null != data.getParams()) {
                        int width, height;
                        int format = -1;
                        width = height = 0;
                        boolean isFrontCamera = port == RTS_UDP_PORT;
                        String rtsWidth = data.getParams().get(TopicKey.WIDTH);
                        String rtsHeight = data.getParams().get(TopicKey.HEIGHT);
                        String rtsFormat = data.getParams().get(TopicKey.FORMAT);
                        if (!TextUtils.isEmpty(rtsWidth) && TextUtils.isDigitsOnly(rtsWidth)) {
                            width = Integer.valueOf(rtsWidth);
                        }
                        if (!TextUtils.isEmpty(rtsHeight) && TextUtils.isDigitsOnly(rtsHeight)) {
                            height = Integer.valueOf(rtsHeight);
                        }
                        if (!TextUtils.isEmpty(rtsFormat) && TextUtils.isDigitsOnly(rtsFormat)) {
                            format = Integer.valueOf(rtsFormat);
                        }

                        int mode = mApplication.getDeviceDesc().getNetMode();
                        if (mode == Stream.Protocol.UDP_MODE || mRealtimeStream == null) {
                            createStream(Stream.Protocol.UDP_MODE, port);///Try to open UDP stream
                        }
                        if (format == DeviceClient.RTS_JPEG) {
                            mRealtimeStream.setResolution(width, height);
                        }
                        if (isFrontCamera) {
                            mRealtimeStream.setFrameRate(mApplication.getDeviceSettingInfo().getFrontRate());
                            mRealtimeStream.setSampleRate(mApplication.getDeviceSettingInfo().getFrontSampleRate());
                        } else {
                            mRealtimeStream.setFrameRate(mApplication.getDeviceSettingInfo().getRearRate());
                            mRealtimeStream.setSampleRate(mApplication.getDeviceSettingInfo().getRearSampleRate());
                        }
                        initPlayer(SDP_URL);
                        updateResolutionUI(port == RTS_UDP_REAR_PORT, width, height);

                        checkCameraType();
                    }
                    break;
                case Topic.CLOSE_PULL_RT_STREAM:
                case Topic.CLOSE_RT_STREAM:
                    if (null != data.getParams()) {
                        boolean closeRTS = TopicParam.SURE.equals(data.getParams().get(TopicKey.STATUS));
                        Dbug.w(tag, "close rt stream result : " + closeRTS + ", isRTPlaying : " + isPlaying());
                        if (closeRTS && isAdjustResolution) {
                            isAdjustResolution = false;
                            dismissWaitingDialog();
                            setCameraStatus();
                            openRTS();
                        }
                    }
                    break;
                case Topic.VIDEO_CTRL:  // video start
                    if (null == data.getParams()) {
                        return;
                    }
                    String state = data.getParams().get(TopicKey.STATUS);
                    if (!TextUtils.isEmpty(state)) {
                        boolean isRecord = TopicParam.OPEN.equals(state);
                        if (isRecord) {
                            recordStatus = STATUS_RECORDING;
                            int currentRecordLevel = getCameraLevel(mApplication.getDeviceSettingInfo().getCameraType());
                            int currentStreamLevel = AppUtils.getStreamResolutionLevel();
                            if (currentRecordLevel == RTS_LEVEL_FHD && currentStreamLevel == RTS_LEVEL_FHD) {
                                switchStreamResolution(RTS_LEVEL_HD);
                                mAdjustResolutionBtn.setImageResource(getLevelResId(RTS_LEVEL_HD));
                            }
                        } else {
                            recordStatus = STATUS_NOT_RECORD;
                        }
                        handlerVideoUI();
                        Dbug.w(tag, "state=" + state + ", dir=" + data.getParams().get(TopicKey.PATH));
                    }
                    break;
                case Topic.PHOTO_CTRL:
                    if (null == data.getParams()) {
                        return;
                    }
                    shootSound();
                    //flashScreen();
                    String photoDesc = data.getParams().get(TopicKey.DESC);
                    if (!TextUtils.isEmpty(photoDesc)) {
                        photoDesc = photoDesc.replaceAll("\\\\", "");
                        Dbug.w(tag, "-PHOTO_CTRL- photoDesc = " + photoDesc);
                    }
                    break;
                case Topic.VIDEO_CONTENT_THUMBNAILS:
                    Dbug.e(tag, "topic->" + "VIDEO_CONTENT_THUMBNAILS->" + "create");
                case Topic.TF_STATUS:
                    if (null == data.getParams()) {
                        return;
                    }

                    if (recordStatus == STATUS_RECORDING) {
                        hideVideoRecordUI();
                    }
                    stopLocalRecording_Loop();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissWaitingDialog();
                        }
                    }, 1000);
                    break;
                case Topic.PULL_VIDEO_STATUS:
                    if (null != data.getParams()) {
                        if (mHandler != null) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mApplication.getDeviceSettingInfo().isExistRearView()) {
                                        Dbug.w(tag, "is playing " + isPlaying() + ", mCameraType=" + mCameraType);
                                        if (isPlaying() && mCameraType == DeviceClient.CAMERA_REAR_VIEW) {
                                            closeRTS();
                                            if (mHandler != null) {
                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setCameraStatus();
                                                        openRTS();
                                                    }
                                                }, 250);
                                            }
                                        }
                                    }
                                    syncDeviceState();
                                }
                            }, 100);
                        }
                    }
                    break;
                case Topic.COLLISION_DETECTION_VIDEO:
                    if (null != data.getParams()) {
                        String widthStr = data.getParams().get(TopicKey.WIDTH);
                        String heightStr = data.getParams().get(TopicKey.HEIGHT);
                        String fpsStr = data.getParams().get(TopicKey.FRAME_RATE);
                        String durationStr = data.getParams().get(TopicKey.DURATION);
                        if (!TextUtils.isEmpty(widthStr) && !TextUtils.isEmpty(heightStr)
                                && !TextUtils.isEmpty(fpsStr) && !TextUtils.isEmpty(durationStr)) {
                            closeRTS();
                        }
                    }
                    break;
                case "GENERIC_CMD": {//设置回应命令的数据
                    ArrayMap<String, String> dParams = data.getParams();
                    if (dParams != null) {
                        String sRespone = dParams.get("status");
                        String cmd = dParams.get("D3");
                        if (cmd == null) {
                            return;
                        }
                        if (Integer.valueOf(cmd) == 0x65) {
                            String h1str = dParams.get("D16");
                            String h2str = dParams.get("D17");
                            int h1 = 0;
                            int h2 = 0;
                            if (!TextUtils.isEmpty(h1str) && TextUtils.isDigitsOnly(h1str)) {
                                h1 = Integer.valueOf(h1str);
                                h1 = h1 & 0x000000ff;
                            }
                            if (!TextUtils.isEmpty(h2str) && TextUtils.isDigitsOnly(h2str)) {
                                h2 = Integer.valueOf(h2str);
                                h2 = h2 << 8;
                                h2 = h2 & 0x0000ff00;
                            }
                            int height = h1 | h2 ;
                            if (height >= 20000) {
                                height = 0;
                            }
                            float fHeight = (float) (height * 0.1);
                            txtHeight.setText(String.format("%.1f",fHeight));
                            String d1Str = dParams.get("D13");
                            String d2Str = dParams.get("D14");
                            int d1 = 0;
                            int d2 = 0;
                            if (!TextUtils.isEmpty(d1Str) && TextUtils.isDigitsOnly(d1Str)) {
                                d1 = Integer.valueOf(d1Str);
                                d1 = d1 & 0x000000ff;
                            }
                            if (!TextUtils.isEmpty(d2Str) && TextUtils.isDigitsOnly(d2Str)) {
                                d2 = Integer.valueOf(d2Str);
                                d2 = d2 << 8;
                                d2 = d2 & 0x0000ff00;
                            }
                            int distance = d1 | d2;
                            String speedStr = dParams.get("D18");
                            int speedV = 0;
                            if (!TextUtils.isEmpty(speedStr) && TextUtils.isDigitsOnly(speedStr)) {
                                speedV = Integer.valueOf(speedStr);
                                speedV = speedV & 0x000000ff;
                            }
                            byte sV = (byte)speedV;
                            float svFloat = (float) (((int)sV) * 0.1);
                            tvSpeedV.setText(String.format("%.1f", svFloat));
                            String speedHstr = dParams.get("D15");
                            int speedH = 0;
                            if (!TextUtils.isEmpty(speedHstr) && TextUtils.isDigitsOnly(speedHstr)) {
                                speedH = Integer.valueOf(speedHstr);
                                speedH = speedH & 0x000000ff;
                            }
                            float speedHFloat = (float) (speedH * 0.1 * 3.6);
                            tvSpeedH.setText(String.format("%.1f",speedHFloat));
                            int planet = Integer.valueOf(dParams.get("D19"));
                            tvPlanet.setText("" + planet);
                            if (planet < 8) {
                                iv_top_left_bg.setImageResource(R.mipmap.red);
                            } else {
                                iv_top_left_bg.setImageResource(R.mipmap.green);
                            }
                            String d25Str = dParams.get("D25");
                            String d26Str = dParams.get("D26");
                            String d27Str = dParams.get("D27");
                            String d28Str = dParams.get("D28");
                            int d25 = 0;
                            int d26 = 0;
                            int d27 = 0;
                            int d28 = 0;
                            if (!TextUtils.isEmpty(d25Str) && TextUtils.isDigitsOnly(d25Str)) {
                                d25 = Integer.valueOf(d25Str);
                                d25 = d25 & 0x000000ff;
                            }
                            if (!TextUtils.isEmpty(d26Str) && TextUtils.isDigitsOnly(d26Str)) {
                                d26 = Integer.valueOf(d26Str);
                                d26 = d26 & 0x000000ff;
                                d26 = d26 << 8;
                            }
                            if (!TextUtils.isEmpty(d27Str) && TextUtils.isDigitsOnly(d27Str)) {
                                d27 = Integer.valueOf(d27Str);
                                d27 = d27 & 0x000000ff;
                                d27 = d27 << 16;
                            }
                            if (!TextUtils.isEmpty(d28Str) && TextUtils.isDigitsOnly(d28Str)) {
                                d28 = Integer.valueOf(d28Str);
                                d28 = d28 & 0x000000ff;
                                d28 = d28 << 24;
                                d28 = d28 & 0xff000000;
                            }
                            latitudeB = d25 | d26 | d27 | d28;
                            //tvE.setText("E: " + String.format("%.2f", latitudeB * 0.0000001));
                            String d21Str = dParams.get("D21");
                            String d22Str = dParams.get("D22");
                            String d23Str = dParams.get("D23");
                            String d24Str = dParams.get("D24");
                            int d21 = 0;
                            int d22 = 0;
                            int d23 = 0;
                            int d24 = 0;
                            if (!TextUtils.isEmpty(d21Str) && TextUtils.isDigitsOnly(d21Str)) {
                                d21 = Integer.valueOf(d21Str);
                                d21 = d21 & 0x000000ff;
                            }
                            if (!TextUtils.isEmpty(d22Str) && TextUtils.isDigitsOnly(d22Str)) {
                                d22 = Integer.valueOf(d22Str);
                                d22 = d22 & 0x000000ff;
                                d22 = d22 << 8;
                            }
                            if (!TextUtils.isEmpty(d23Str) && TextUtils.isDigitsOnly(d23Str)) {
                                d23 = Integer.valueOf(d23Str);
                                d23 = d23 & 0x000000ff;
                                d23 = d23 << 16;
                            }
                            if (!TextUtils.isEmpty(d24Str) && TextUtils.isDigitsOnly(d24Str)) {
                                d24 = Integer.valueOf(d24Str);
                                d24 = d24 & 0x000000ff;
                                d24 = d24 << 24;
                            }
                            longitudeB = d21 | d22 | d23 | d24;
                            //tvN.setText("N: " + String.format("%.2f", longitudeB * 0.0000001));
//                            int distance = (int)getDistance(latitude,longitude,longitudeB,latitudeB);
//                            distance = Math.abs(distance);
//                            if (distance > 300000) {
//                                distance = 0;
//                            }
                            tvDistance.setText(String.format("%.1f",distance * 0.1));
                            int roll = Integer.valueOf(dParams.get("D6")) | (Integer.valueOf(dParams.get("D7")) << 8);
                            //tvRoll.setText("Roll: " + String.format("%.1f", roll * 0.1));
                            int pitch = Integer.valueOf(dParams.get("D8")) | (Integer.valueOf(dParams.get("D9")) << 8);
                            //tvPatch.setText("Pitch: " + String.format("%.1f", pitch * 0.1));
                            String yaw1Str = dParams.get("D10");
                            String yaw2Str = dParams.get("D11");
                            int yaw1 = 0;
                            int yaw2 = 0;
                            if (!TextUtils.isEmpty(yaw1Str) && TextUtils.isDigitsOnly(yaw1Str)) {
                                yaw1 = Integer.valueOf(yaw1Str);
                                yaw1 = yaw1 & 0x000000ff;
                            }
                            if (!TextUtils.isEmpty(yaw2Str) && TextUtils.isDigitsOnly(yaw2Str)) {
                                yaw2 = Integer.valueOf(yaw2Str);
                                yaw2 = yaw2 & 0x000000ff;
                                yaw2 = yaw2 << 8;
                                yaw2 = yaw2 & 0x0000ff00;
                            }
                            int yaw = yaw1 | yaw2;
                            short sYaw = (short) yaw;
                            //tvYaw.setText("Yaw: " + String.format("%.1f", yaw * 0.1));

                            String typeAndStatusStr = dParams.get("D12");
                            int typeAndStatus = 0;
                            if (!TextUtils.isEmpty(typeAndStatusStr) && TextUtils.isDigitsOnly(typeAndStatusStr)) {
                                typeAndStatus = Integer.valueOf(typeAndStatusStr);
                                typeAndStatus = typeAndStatus & 0x000000ff;
                            }
                            int lock = typeAndStatus & 0x0000000f;
                            flyType = (typeAndStatus >> 4) & 0x0000000f;
                            flyStatus = typeAndStatus & 0x0000000f;
                            String[] arrType = new String[]{"", "悬停模式", "起飞模式", "降落模式", "返航模式", "航点模式", "跟随模式", "环绕模式", "自稳模式"};
                            String[] arrStatus = new String[]{"已上锁", "已解锁未起飞", "已解锁已起飞", "失控返航", "一级返航", "二级返航", "一键返航", "低压降落", "一键降落", "一键起飞", "陀螺仪校准", "磁力计校准"};
                            //txtStatus.setText("状态：" + arrType[flyType] + " " + arrStatus[flyStatus]);
                            if (lock == 0) {
                                ivLock.setImageResource(R.mipmap.lock);
                            } else {
                                ivLock.setImageResource(R.mipmap.lock1);
                            }

                            if (flyStatus == 0x02) {
                                btnLandingOrTakeoff.setBackgroundResource(R.mipmap.main_landing);
                            } else if (flyStatus == 0x00) {
                                btnLandingOrTakeoff.setBackgroundResource(R.mipmap.main_takeoff_landing);
                            }

                            //tvTemp.setText("" + lock + " " + longitudeB * 0.0000001 + " " + latitudeB * 0.0000001 + " " + sYaw + " " + latitude * 0.0000001 + " " + longitude * 0.0000001);

                            if (flyBackCount > 0) {
                                if (flyType == 0x04) { // 返航模式
                                    flyBackCount = 0;
                                } else {
                                    if (flyBackCount > 3) {
                                        flyBackCount = 0;
                                        return;
                                    }
                                    flyback();
                                }
                            }
                            if (cancelFlyBackCount > 0) {
                                if (flyType == 0x04) { // 返航模式
                                    if (cancelFlyBackCount > 3) {
                                        cancelFlyBackCount = 0;
                                        return;
                                    }
                                    flyback();
                                } else {
                                    cancelFlyBackCount = 0;
                                }
                            }

                            if (flyType == 0x04) {
                                btnFlyBack.setBackgroundResource(R.mipmap.main_voyage_home01);
                            } else {
                                btnFlyBack.setBackgroundResource(R.mipmap.main_voyage_home);
                            }

                            if (flyFollowCount > 0) { // 跟随数量判断
                                if (flyType == 0x06) { // 跟随模式
                                    flyFollowCount = 0; // 清空跟随数量计数
                                } else { // 非跟随模式
                                    if (flyFollowCount > 3) { // 如果跟随数量计数大于3
                                        flyFollowCount = 0; // 清空跟随数量计数
                                        return;
                                    } // 如果小于3，则再发一次跟随
                                    follow(); // 执行跟随方法
                                }
                            }
                            if (cancelFlyFollowCount > 0) {
                                if (flyType == 0x06) { // 跟随模式
                                    if (cancelFlyFollowCount > 3) {
                                        cancelFlyFollowCount = 0;
                                        return;
                                    }
                                    follow();// 取消跟随
                                } else {
                                    cancelFlyFollowCount = 0;
                                }
                            }

                            if (flyType == 0x06) {
                                btnMore.setBackgroundResource(R.mipmap.main_hide_views01);
                            } else {
                                btnMore.setBackgroundResource(R.mipmap.main_hide_views);
                            }

                            if (latitude != 0 && longitude != 0) {
                                double angle = getAngle1(latitude * 0.0000001, longitude * 0.0000001, longitudeB * 0.0000001, latitudeB * 0.0000001);
                                int i = (int)(angle / (180.0 / 8));
                                i = -i;
                                float angle1 = (float) (i * (180.0 / 8) - 90);
                                if (angle1 <= -360) {
                                    angle1 = angle1 + 360;
                                }
                                if (angle1 >= 360) {
                                    angle1 = angle1 - 360;
                                }
                                rl_comprass.setRotation(angle1);
                                float angle2 = (float) (-(sYaw / 1800.0) * 180.0 - angle1);
                                if (angle2 <= 360) {
                                    angle2 = angle2 + 360;
                                }
                                if (angle2 >= 360) {
                                    angle2 = angle2 - 360;
                                }
                                iv_arrow.setRotation(angle2);
                                tvTemp.setText("" + angle1 + " " + angle2);
                            }

                            String sLowU = dParams.get("D29");
                            if (sLowU != null && sLowU.length() > 0) {
                                int iU = Integer.parseInt(sLowU);
                                if (iU <= 1) {
                                    if (isRecordPrepared) {
                                        //电量低自动停止录像
                                        a.getApplication().showToastShort(R.string.main_low_power);
                                    }
                                    stopLocalRecording_Loop();

                                    imgBattery.setBackgroundResource(R.mipmap.main_battery_0);
                                } else if (iU < 36) {
                                    imgBattery.setBackgroundResource(R.mipmap.main_battery_1);
                                } else if (iU < 40) {
                                    imgBattery.setBackgroundResource(R.mipmap.main_battery_2);
                                } else {
                                    imgBattery.setBackgroundResource(R.mipmap.main_battery_3);
                                }
                            }
                            if (gpsStarting) {
                                return;
                            }
                            gpsStarting = true;
                            startGPS(); // 启动定位功能
                            return;
                        } else if (Integer.valueOf(cmd) == 0xa0) {
                            int value = Integer.valueOf(dParams.get("D6"));
                            if (value == 0x01) {
                                //遥控器拍照
                                isSnapShotValid = false;
                                delayHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        isSnapShotValid = true;
                                    }
                                }, 1000);
                                btnTakePhoto.callOnClick();
                                Log.d(tag, "snapshot...");
                            } else if (value == 0x02) {
                                if (isRecordPrepared) {
                                    stopLocalRecording_Loop();
                                } else {
                                    startLocalRecording_Loop();
                                }
                            } else if (value == 0x04) {
                                if (isRecordPrepared) {
                                    stopLocalRecording_Loop();
                                } else {
                                    startLocalRecording_Loop();
                                }
                            }
                            return;
                        }


                        //ljw
                        String sByte11 = dParams.get("D11");
                        if (sByte11 != null) {
                            int status3 = Integer.parseInt(sByte11);
                            ////手持遥控器对码 Flag = 1
                            handHeldMatchFlag = status3 >> 1 & 1;
                            Log.d(tag, "handHeldMatchFlag:" + handHeldMatchFlag);
                        }
                        String sByte9HexString = dParams.get("D9");//接收遥控器的录像和拍照命令
                        if (sByte9HexString != null) {

                            int status1 = Integer.parseInt(sByte9HexString);
                            Log.d(tag, "status1:" + status1);
                            int snapshotFlag = status1 >> 1 & 1;

                            int recordFlag = status1 >> 2 & 1;
                            Log.d(tag, "snapshotFlag:" + snapshotFlag +
                                    " recordFlag:" + recordFlag);
                            //遥控器拍照
                            if (snapshotFlag == 1 && isSnapShotValid) {
                                isSnapShotValid = false;
                                delayHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        isSnapShotValid = true;
                                    }
                                }, 1000);
                                btnTakePhoto.callOnClick();
                                Log.d(tag, "snapshot...");
                            }


                            //录像方式2
                            if (recordFlagTemp == -1) {
                                recordFlagTemp = recordFlag;
                            }
                            if (recordFlag != recordFlagTemp) {
                                //排除因遥控关机复位收到的错误信号
                                if (handHeldMatchFlag == 0)
                                    return;
                                if (isRecordPrepared) {
                                    stopLocalRecording_Loop();
                                } else {
                                    startLocalRecording_Loop();
                                }
                                recordFlagTemp = recordFlag;
                            }

                        }

                    }
                    break;
                }
                case "PHOTO_BRIGHTNESS": {
                    if (data.getParams() != null) {
                        String sValue = data.getParams().get("brightness");
                        if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
                            Integer iValue = Integer.valueOf(sValue);
                            if (iValue > 127) {
                                iValue = 127;
                            }
                            if (iValue < -128) {
                                iValue = -128;
                            }
                            //mApplication.Camera_brt=iValue;
                        }
                    }
                    break;
                }
                case "PHOTO_EXP":
                    if (data.getParams() != null) {
                        String sValue = data.getParams().get(TopicKey.EXP);
                        if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
                            Integer iValue = Integer.valueOf(sValue);
                            if (iValue > 3) {
                                iValue = 3;
                            }
                            if (iValue < -3) {
                                iValue = -3;
                            }
                            //mApplication.getDeviceSettingInfo().setPhotoExp(iValue);
                            //mApplication.Camera_exp = iValue;//曝光
                        }
                    }
                    break;
                case "PHOTO_CONTRAST": {
                    if (data.getParams() != null) {
                        String sValue = data.getParams().get("contrast");
                        if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
                            Integer iValue = Integer.valueOf(sValue);
                            if (iValue > 512) {
                                iValue = 512;
                            }
                            if (iValue < 0) {
                                iValue = 0;
                            }
                            //mApplication.Camera_ctr=iValue;//对比度
                        }
                    }
                    break;
                }
                case "WHITE_BALANCE": {
                    if (data.getParams() != null) {
                        String sValue = data.getParams().get("wbl");
                        if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
                            Integer iValue = Integer.valueOf(sValue);
                            if (iValue > 4) {
                                iValue = 4;
                            }
                            if (iValue < 0) {
                                iValue = 0;
                            }
                            //mApplication.Camera_wbl=iValue;//白平衡
                        }
                    }
                    break;
                }
                case "UAV_HEIGHT": {//无人机高度
                    if (data.getParams() != null) {
                        String sValue = data.getParams().get("height");
                        if (!TextUtils.isEmpty(sValue) && TextUtils.isDigitsOnly(sValue)) {
                            //mApplication.UAV_height=Integer.valueOf(sValue);
                            //txtHeight.setText(getString(R.string.main_height)+sValue+"m");
                        }
                    }
                    break;
                }
                case "AP_SSID_INFO": {
                    if (data.getParams() != null) {
                        String sStatus = data.getParams().get("status");//是否成功
                        String sName = data.getParams().get("ssid");
                        String sPass = data.getParams().get("pwd");
                        mApplication.WiFiPassword = sPass;
                        if (!TextUtils.isEmpty(sPass)) {

                        }
                    }
                    break;
                }
            }
        }
    };

    /**
     * 将byte转换为一个长度为8的boolean数组（每bit代表一个boolean值）
     */
    public static boolean[] getBooleanArray(byte b) {
        boolean[] array = new boolean[8];
        for (int i = 7; i >= 0; i--) { // 对于byte的每bit进行判定
            array[i] = (b & 1) == 1; // 判定byte的最后一位是否为1，若为1，则是true；否则是false
            b = (byte) (b >> 1); // 将byte右移一位
        }
        return array;
    }

    private void showPopupMenu(View view) {
        Map<Integer, Integer> resMap = new HashMap<>();
        String[] levels;
        if (mCameraType == DeviceClient.CAMERA_REAR_VIEW) {
            levels = mApplication.getDeviceDesc().getRear_support();
        } else {
            levels = mApplication.getDeviceDesc().getFront_support();
        }
        if (levels != null) {
            int currentLevel = AppUtils.getStreamResolutionLevel();
            int currentRecordLevel = getCameraLevel(mApplication.getDeviceSettingInfo().getCameraType());
            Dbug.e(tag, "======currentLevel====== " + currentLevel);
            for (String str : levels) {
                if (!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
                    int rtsLevel = Integer.valueOf(str);
                    if (recordStatus != STATUS_RECORDING) {
                        if (rtsLevel != currentLevel) {
                            resMap.put(rtsLevel, getLevelResId(rtsLevel));
                        }
                    } else {
                        if (currentRecordLevel == RTS_LEVEL_FHD && rtsLevel < RTS_LEVEL_FHD) {//Record:1080
                            if (rtsLevel != currentLevel) {
                                resMap.put(rtsLevel, getLevelResId(rtsLevel));
                            }
                        } else if (currentRecordLevel < RTS_LEVEL_FHD) {//Record: vga/720
                            if (rtsLevel != currentLevel) {
                                resMap.put(rtsLevel, getLevelResId(rtsLevel));
                            }
                        }
                    }
                }
            }
            dismissPopMenu();
            popupMenu = new PopupMenu(mApplication, resMap);
            popupMenu.setOnPopItemClickListener(mOnPopItemClickListener);
            popupMenu.showAsRight(view);
        }
    }

    private void dismissPopMenu() {
        if (popupMenu != null) {
            if (popupMenu.isShowing()) {
                popupMenu.dismiss();
            }
            popupMenu = null;
        }
    }

    private PopupMenu.OnPopItemClickListener mOnPopItemClickListener = new PopupMenu.OnPopItemClickListener() {
        @Override
        public void onItemClick(final int level, final Integer resId, int index) {
            switchStreamResolution(level);
        }
    };

    private void switchStreamResolution(int level) {
        isAdjustResolution = true;
        AppUtils.saveStreamResolutionLevel(level);
        if (isPlaying()) {
            closeRTS();
        }
        showWaitingDialog();
    }

    private void showWaitingDialog() {
        if (!isDestroyed()) {
            if (mWaitingDialog == null) {
                mWaitingDialog = new WaitingDialog();
                mWaitingDialog.setNotifyContent(getString(R.string.deleting_tip));
            }
            if (!mWaitingDialog.isShowing()) {
                mWaitingDialog.show(getSupportFragmentManager(), "waiting_dialog");
            }
        }
    }

    private void dismissWaitingDialog() {
        if (mWaitingDialog != null) {
            if (mWaitingDialog.isShowing() && !isDestroyed()) {
                mWaitingDialog.dismiss();
            }
            mWaitingDialog = null;
        }
    }

    private void dismissAdjustingDialog() {
        if (mAdjustingDialog != null) {
            if (mAdjustingDialog.isShowing() && !isDestroyed()) {
                mAdjustingDialog.dismiss();
            }
            mAdjustingDialog = null;
        }
    }

    private void showErrorDialog(String errMsg) {
        if (!TextUtils.isEmpty(errMsg)) {
            mErrorDialog = NotifyDialog.newInstance(getString(R.string.dialog_tips), errMsg, R.string.dialog_ok,
                    new NotifyDialog.OnConfirmClickListener() {
                        @Override
                        public void onClick() {
                            dismissErrorDialog();
                            onBackPressed();
                        }
                    });
            if (!mErrorDialog.isShowing()) {
                mErrorDialog.show(getSupportFragmentManager(), "ViewDialog");
            }
        }
    }

    private void dismissErrorDialog() {
        if (mErrorDialog != null) {
            if (mErrorDialog.isShowing() && !isDestroyed()) {
                mErrorDialog.dismiss();
            }
            mErrorDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCaptureBusying = false;
        isCaptureBusying_Main = false;

        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);


        //wifichange-event
        syncDeviceState();
        if (!isPlaying()) {
            //openRTS();
            tryConnectWifiDevice();
        } else {
            setCameraStatus();
        }

        startGetFlyStatusTimer();
    }

    @Override
    protected void onPause() {

        // 取消注册
        mSensorManager.unregisterListener(this);

        closeRTS();
        if (mRealtimeStream != null) {
            mRealtimeStream.unregisterStreamListener(realtimePlayerListener);
            mRealtimeStream.release();
            mRealtimeStream = null;
        }

        mTryConnectCount = 0;

        stopGetFlyStatusTimer();

        super.onPause();
    }

    @Override
    protected void onStop() {
        // 取消注册
        mSensorManager.unregisterListener(this);

        a.getApplication().unregisterReceiver(mReceiver);

        super.onStop();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //ljw
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isGSensor == false) {
            return;
        }
        float[] values = event.values;
        // 获取触发event的传感器类型 重力感应
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ORIENTATION:
                // 获取与Y轴的夹角
                float yAngle = values[1];
                // 获取与Z轴的夹角
                float zAngle = values[2];

                Log.i("SensorAngle:", "yAngle:" + yAngle + ",zAngle:" + zAngle);

                // 气泡位于中间时（水平仪完全水平），气泡的X、Y座标
                float iMinX = (imgGSensorPanel.getWidth() - btnGSensorPoint.getWidth()) / 2;
                float iMinY = (imgGSensorPanel.getHeight() - btnGSensorPoint.getHeight()) / 2;

                //float radian = iMinX < iMinX ? iMinX : iMinY;


                float xPercent = 0f;
                float yPercent = 0f;


                if (yAngle >= MAX_ANGLE) {
                    xPercent = 1.0f;
                } else if (yAngle <= -MAX_ANGLE) {
                    xPercent = -1.0f;
                } else {
                    xPercent = yAngle / MAX_ANGLE;
                }

                if (zAngle >= MAX_ANGLE) {
                    yPercent = 1.0f;
                } else if (zAngle <= -MAX_ANGLE) {
                    yPercent = -1.0f;
                } else {
                    yPercent = zAngle / MAX_ANGLE;
                }

                float offsetX = xPercent * iMinX;
                float offsetY = yPercent * iMinY;

                Log.i("Percent:", "xPercent:" + xPercent + ",yPercent:" + yPercent);

                float iNewX = mRightCenterX - offsetX;
                float iNewY = mRightCenterY + offsetY;
                int distance = getLength(mRightCenterX, mRightCenterY, iNewX, iNewY);

                int rawX;
                int rawY;

                if (distance > radius) {
                    Point borderPoint = getBorderPoint((int) iNewX, (int) iNewY, (int) mRightCenterX, (int) mRightCenterY, (int) radius);
                    ViewHelper.setTranslationX(btnGSensorPoint, borderPoint.x);
                    ViewHelper.setTranslationY(btnGSensorPoint, borderPoint.y);

                    rawX = borderPoint.x;
                    rawY = borderPoint.y;


                } else {
                    ViewHelper.setTranslationX(btnGSensorPoint, iNewX);
                    ViewHelper.setTranslationY(btnGSensorPoint, iNewY);

                    rawX = (int) iNewX;
                    rawY = (int) iNewY;
                }

                int x = 0;
                int y = 0;

                x = (int)(ViewHelper.getTranslationX(btnGSensorPoint));
                y = (int)(ViewHelper.getTranslationY(btnGSensorPoint));

                int valueX = (int)(x * 1000 / radius);
                int valueY = (int)(y * 1000 / radius);

                if ((valueX < kDeadValue && valueX > -kDeadValue ) &&(valueY < kDeadValue && valueY > -kDeadValue) ) {
                    valueX = 0;
                    valueY = 0;
                }

                FlyData.rudderData[1] = valueX;
                FlyData.rudderData[2] = valueY;

                Log.d(tag, "gsensor x=" + valueX + " y=" + valueY);

                break;
        }
    }

    public static Point getBorderPoint(int x1, int y1, int startx, int starty, int HRudder) {
        Point point = new Point();
        double radian = getRadian(x1, y1, startx, starty);
        point.x = (int) ((double) startx + (((double) HRudder) * Math.cos(radian)));
        point.y = (int) ((double) starty + (((double) HRudder) * Math.sin(radian)));
        return point;
    }

    // 获取水平线夹角弧度
    public static float getRadian(int x2, int y2, int x, int y) {
        float lenA = x2 - x;
        float lenB = y2 - y;
        float lenC = (float) Math.sqrt(lenA * lenA + lenB * lenB);
        float ang = (float) Math.acos(lenA / lenC);
        ang = ang * (y2 < y ? -1 : 1);
        return ang;
    }


    // 获取两点间直线距离
    public static int getLength(float x, float y, float x2, float y2) {
        return (int) Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2));
    }

    // 计算x、y点的气泡是否处于水平仪的仪表盘内
    private boolean isContain(float x, float y) {
        // 计算气泡的圆心座标X、Y
        float bubbleCx = x + btnGSensorPoint.getWidth() / 2;
        float bubbleCy = y + btnGSensorPoint.getWidth() / 2;
        // 计算水平仪仪表盘的圆心座标X、Y
        float backCx = imgGSensorPanel.getWidth() / 2;
        float backCy = imgGSensorPanel.getWidth() / 2;
        // 计算气泡的圆心与水平仪仪表盘的圆心之间的距离。
        double distance = Math.sqrt((bubbleCx - backCx) * (bubbleCx - backCx) + (bubbleCy - backCy) * (bubbleCy - backCy));
        // 若两个圆心的距离小于它们的半径差，即可认为处于该点依然位于仪表盘内
        if (distance < (imgGSensorPanel.getWidth() - btnGSensorPoint.getWidth()) / 2) {
            return true;
        } else {
            return false;
        }
    }

    //https://blog.csdn.net/ouyang_peng/article/details/46849223


    @Override
    protected void onDestroy() {
        //Clear_onDestroy();
        //监测home键
        if (mHomeWatcherReceiver != null) {
            try {
                unregisterReceiver(mHomeWatcherReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mPublicTimer != null) {
            mPublicTimer.invalidate();
            mPublicTimer = null;
        }

        dismissPopMenu();

        closeRTS();

        dismissErrorDialog();
        dismissWaitingDialog();
        dismissAdjustingDialog();
        super.onDestroy();
        //Dbug.i(tag, "==================onDestroy===============");
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (mFrameCodec != null) {
            mFrameCodec.setOnFrameCodecListener(null);
            mFrameCodec.destroy();
            mFrameCodec = null;
        }
        ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void Clear_onDestroy() {
        //监测home键
        if (mHomeWatcherReceiver != null) {
            try {
                unregisterReceiver(mHomeWatcherReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mPublicTimer != null) {
            mPublicTimer.invalidate();
            mPublicTimer = null;
        }

        dismissPopMenu();

        closeRTS();

        dismissErrorDialog();
        dismissWaitingDialog();
        dismissAdjustingDialog();
        //Dbug.i(tag, "==================onDestroy===============");
        if (null != wakeLock && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (mFrameCodec != null) {
            mFrameCodec.setOnFrameCodecListener(null);
            mFrameCodec.destroy();
            mFrameCodec = null;
        }
        ClientManager.getClient().unregisterNotifyListener(onNotifyListener);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public boolean isCamera = false;

    //WIFI网络是否可用 //必要时可配合t.startListen(new NetChangeBroadcast() 使用
    public boolean isConnectedWifi() {
        //if (context != null) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null && mWiFiNetworkInfo.isAvailable() && mWiFiNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
            //System.out.println(mWiFiNetworkInfo.getState()+"----"+mWiFiNetworkInfo.getDetailedState()+"-----");
//                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                if(wifiManager.isWifiEnabled()){
//                    WifiInfo aWifiInfo=wifiManager.getConnectionInfo();
//                    if(aWifiInfo.getNetworkId() != -1){
//                        return true;// 是否网络连接
//                    }
//                }
            return true;
        }
        //}
        return false;
    }

    public String getWifiName() {
        String sWifiName;
        if (isConnectedWifi()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            sWifiName = wifiInfo.getSSID();
            if (sWifiName != null && sWifiName.length() > 1 && sWifiName.substring(0, 1).equals("\"") && sWifiName.substring(sWifiName.length() - 1).equals("\"")) {
                sWifiName = sWifiName.substring(1, sWifiName.length() - 1);
            }
        } else {
            return null;
        }
        return sWifiName;
    }

    private long mPlayStartTime = 0;
    private int mPlayTryCount = 0;

    private void openRTS() {
        Dbug.i(tag, "openRTS:::................isRtspEnable-" + isRtspEnable);
        //请求获取WiFi密码
        RequestCmd aNewCmd = new RequestCmd();
        aNewCmd.setTopic("AP_SSID_INFO");
        aNewCmd.setOperation("GET");
        ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
            @Override
            public void onResponse(Integer integer) {
                if (integer == SEND_SUCCESS) {

                } else {

                }
            }
        });
        setLastSensor();

        mPlayStartTime = SystemClock.uptimeMillis();
        if (isPlaying()) {
            Dbug.e(tag, "rts is playing, please stop it first.");
            return;
        }

        mPlayTryCount++;
        boolean isDebugOpen = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false);
        if (isDebugOpen) {
            if (mMoreHub != null) {
                if (mMoreHub.getVisibility() != View.VISIBLE) {
                    mMoreHub.setVisibility(View.VISIBLE);
                    mStreamView.setHudView(mMoreHub);
                }
            }
            startDebug();
        } else {
            if (mMoreHub != null) {
                if (mMoreHub.getVisibility() != View.GONE) {
                    fps = 0;
                    if (mHandler != null) {
                        mHandler.removeMessages(MSG_FPS_COUNT);
                    }
                    mMoreHub.setVisibility(View.GONE);
                    mStreamView.setHudView(null);
                }
            }
        }
        int cameraType = mApplication.getDeviceSettingInfo().getCameraType();
        if (isRtspEnable) {
            Dbug.i(tag, "@OpenRTS-Rtsp-Init-22");
            String strRtsp;
            if (cameraType == DeviceClient.CAMERA_REAR_VIEW) {
                strRtsp = String.format(RTSP_URL_REAR, ClientManager.getClient().getAddress());
            } else {
                strRtsp = String.format(RTSP_URL, ClientManager.getClient().getAddress());
            }
            initPlayer(strRtsp);
            return;
        }
        mStartTime = ShareTimer.getTick();
        Dbug.i(tag, "@OpenRTS-Init1");

        int mode = mApplication.getDeviceDesc().getNetMode();
        if (mode == Stream.Protocol.TCP_MODE) {
            Dbug.i(tag, "@OpenRTS-CreateStream2");
            createStream(mode, VIDEO_SERVER_PORT);//For TCP stream mode
        }
        Dbug.i(tag, "@OpenRTS-3");
        int level = AppUtils.getStreamResolutionLevel();
        int[] resolution = AppUtils.getRtsResolution(level);
        //默认720P
        resolution[0] = 1280;//width = 1280;
        resolution[1] = 720;//height = 720;
        int mRTSType = getRtsFormat();//请求的视频传输类型
        long lTime = ShareTimer.getTick() - mStartTime;
        Dbug.i(tag, "@OpenRTS-3-Start-" + lTime);
        ClientManager.getClient().tryToOpenRTStream(cameraType, mRTSType, resolution[0], resolution[1], getVideoRate(cameraType), new SendResponse() {
            @Override
            public void onResponse(Integer code) {
                if (code != SEND_SUCCESS) {
                    Dbug.i(tag, "@OpenRTS-4-fail");
                    Dbug.e(tag, "Send failed!!!");
                } else {
                    long lTime = ShareTimer.getTick() - mStartTime;
                    Dbug.i(tag, "@OpenRTS-5-" + lTime);
                }
            }
        });
    }

    boolean isBusying_closeRTS = false;//synchronized 会让时间会过。图传变黑。

    private void closeRTS() {
        if (isRecordPrepared) {
            stopLocalRecording_Loop();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Dbug.i(tag, "closeRTS:::................");
        deinitPlayer();

        boolean isDebugOpen = PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false);
        if (isDebugOpen) closeDebug();
        if (!isRtspEnable) {
            if (isPlaying()) {
                int cameraTyp = mApplication.getDeviceSettingInfo().getCameraType();
                ClientManager.getClient().tryToCloseRTStream(cameraTyp, new SendResponse() {
                    @Override
                    public void onResponse(Integer code) {
                        if (code != SEND_SUCCESS) {
                            Dbug.e(tag, "Send failed!!!");
                        }
                    }
                });
            }
        }


        if (mVideoCapture != null) {
            mVideoCapture.destroy();
            mVideoCapture = null;
        }

        if (mRealtimeStream != null) {
            mRealtimeStream.close();
            mRealtimeStream = null;
        }

        if (progressBarLoading != null && progressBarLoading.getVisibility() != View.GONE) {
            progressBarLoading.setVisibility(View.GONE);
        }
        if (btnPlay != null && btnPlay.getVisibility() != View.VISIBLE) {
            //btnPlay.setVisibility(View.VISIBLE);
        }

        //viewBackground.setVisibility(View.VISIBLE);
        mApplication.UAV_height = 0;
    }

    private long mStartTime = 0;

    private void initPlayer(String videoPath) {
        if (mStreamView != null && !TextUtils.isEmpty(videoPath)) {
            final Uri uri = Uri.parse(videoPath);
            // init player
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            isIJKPlayerOpen = true;
            mStreamView.setRealtime(true);
            mStreamView.setVideoURI(uri);

            long lTime = ShareTimer.getTick() - mStartTime;
            Dbug.e(tag, "@OpenRTS-PlayStart-6-" + lTime);
            mStreamView.start();

            btnPlay.setVisibility(View.GONE);
            progressBarLoading.setVisibility(View.VISIBLE);
        } else {
            Dbug.e(tag, "init player fail");
        }
    }

    private void deinitPlayer() {
        Dbug.w(tag, "deinit player");
        if (mStreamView != null) {
            mStreamView.stopPlayback();
            mStreamView.release(true);
            mStreamView.stopBackgroundPlay();
            //mStreamView=null;
        }
        if (isIJKPlayerOpen) IjkMediaPlayer.native_profileEnd();
        isIJKPlayerOpen = false;
    }

    /**
     * 拍照声效
     */
    private void shootSound() {
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        int volume = 0;
//        if (audioManager != null) volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
//        Dbug.i(tag, "volume=:" + volume);
//        if (volume != 0) {
        MediaPlayer mMediaPlayer = MediaPlayer.create(this, (R.raw.snapshot_sounds));
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    mp.stop();
                    mp.release();
                }
            }
        });
        mMediaPlayer.start();
        //}
    }

    private void handlerVideoUI() {
        if (recordStatus == STATUS_RECORDING) {
            showVideoRecordUI();
        } else {
            hideVideoRecordUI();
        }
    }

    private void showVideoRecordUI() {
        mRecorderTimeCount = SystemClock.uptimeMillis();
        txtRecorderTime.setVisibility(View.VISIBLE);
        btnRecorder.setBackgroundResource(R.mipmap.main_recorder_sel);

        //禁止点击设置和本地图片
        btnPhotos.setEnabled(false);
        btnSettings.setEnabled(false);

        //闪烁显隐按钮
        txtRecorderRed.setVisibility(View.VISIBLE);
        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        txtRecorderRed.startAnimation(alphaAnimation);

        if (mRecorderTimer != null) {
            mRecorderTimer.invalidate();
        }
        mRecorderTimer = new ShareTimer(1677, 200, new ShareTimer.MainTask() {
            @Override
            public void onTimer(int iTimerCount) {
                long lUseTime = SystemClock.uptimeMillis() - mRecorderTimeCount;
                String sTime = DiffMillsToHours(lUseTime);
                txtRecorderTime.setText(sTime);
            }
        });
    }

    public static String DiffMillsToHours(long lDiffMills) {
        lDiffMills = lDiffMills / 1000;
        int iHour = (int) lDiffMills / (60 * 60);
        int iRemainMills = (int) lDiffMills - iHour * 60 * 60;
        int iMin = iRemainMills / 60;
        int iSecond = iRemainMills - iMin * 60;
        String sTime = fit2(iMin) + ":" + fit2(iSecond);
        return sTime;
    }

    //int转为固定长度字符串,如时间时经常使用固定2位值
    public static String fit2(int iValue) {
        String sValue = String.valueOf(iValue);
        if (iValue > 99) {
            return sValue.substring(sValue.length() - 2);
        } else if (iValue > 9) {
            return sValue;
        } else {
            return "0" + sValue;
        }
    }

    private void hideVideoRecordUI() {
        //恢复允许点击设置和本地图片
        btnPhotos.setEnabled(true);
        btnSettings.setEnabled(true);

        txtRecorderTime.setVisibility(View.GONE);
        if (mRecorderTimer != null) {
            mRecorderTimer.invalidate();
            mRecorderTimer = null;
        }
//        if (null != wakeLock && wakeLock.isHeld()) {
//            wakeLock.release();
//        }
        txtRecorderRed.setVisibility(View.GONE);
        txtRecorderRed.clearAnimation();
        btnRecorder.setBackgroundResource(R.mipmap.main_recorder);
    }

    private void switchPanelVisible() {
        int setVisible;
        if (leftPanel.getVisibility() != View.VISIBLE) {
            temSwitchHiden = false;
            setVisible = View.VISIBLE;
            leftPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.left_enter));
            rightPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.right_enter));
            btnHidePanel.setBackgroundResource(R.mipmap.main_joystick_switch_sel);
            stopFlyFollowTimer();
            startFlyCtrlTimer();//ljw
        } else {
            setVisible = View.INVISIBLE;
            leftPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.left_exit));
            rightPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.right_exit));
            btnHidePanel.setBackgroundResource(R.mipmap.main_joystick_switch);
            stopFlyCtrlTimer();//ljw
            if (isGSensor) {
                sensor();
            }
            RequestCmd aNewCmd = new RequestCmd();
            aNewCmd.setTopic("GENERIC_CMD");
            aNewCmd.setOperation("PUT");
            aNewCmd.setParams(FlyHoverModel.getFlyCtrlDataMap()); // 悬停
            ClientManager.getClient().tryToGet(aNewCmd, new SendResponse() {
                @Override
                public void onResponse(Integer integer) {
                    if (integer == SEND_SUCCESS) {
                        Log.i(tag, "GENERIC_CMD SEND_SUCCESS");
                    } else {
                        Log.i(tag, "GENERIC_CMD SEND_Fail");
                    }
                }
            });
            temSwitchHiden = true;
            mSwitch.setChecked(false);
        }
        leftPanel.setVisibility(setVisible);
        rightPanel.setVisibility(setVisible);
    }

    //隐藏工具栏动画
    private void switchUIBarVisible() {
        int setVisible;
        if (leftControlBar.getVisibility() != View.VISIBLE) {
            setVisible = View.VISIBLE;
            leftControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.left_enter));
            rightControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.right_enter));
            topControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_enter));
            bottomControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_enter));

            leftPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.left_enter));
            rightPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.right_enter));
        } else {
            setVisible = View.INVISIBLE;
            leftControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.left_exit));
            rightControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.right_exit));
            topControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_exit));
            bottomControlBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_exit));

            leftPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.left_exit));
            rightPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.right_exit));
        }
        leftControlBar.setVisibility(setVisible);
        rightControlBar.setVisibility(setVisible);
        topControlBar.setVisibility(setVisible);
        bottomControlBar.setVisibility(setVisible);

        leftPanel.setVisibility(setVisible);
        rightPanel.setVisibility(setVisible);
    }

    /**
     * 同步设备状态
     */
    private void syncDeviceState() {
        DeviceSettingInfo deviceSettingInfo = mApplication.getDeviceSettingInfo();
        if (deviceSettingInfo != null) {
            if (recordStatus != deviceSettingInfo.getRecordState()) {
                recordStatus = deviceSettingInfo.getRecordState();
                handlerVideoUI();
            }
            int currentLevel = AppUtils.getStreamResolutionLevel();
            mAdjustResolutionBtn.setImageResource(getLevelResId(currentLevel));
        }
    }


    private void checkCameraType() {
        int cameraType = mApplication.getDeviceSettingInfo().getCameraType();
        boolean isSwitchCamera = (mCameraType != cameraType);
        if (!isSwitchCamera) {
            return;
        }
        mCameraType = cameraType;
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int rtsLevel = getCameraLevel(mCameraType);
                    mAdjustResolutionBtn.setImageResource(getLevelResId(rtsLevel));
                }
            }, 200);
        }
    }

    private IMediaController iMediaController = new IMediaController() {
        @Override
        public void hide() {
        }

        @Override
        public boolean isShowing() {
            return (rightControlBar != null && rightControlBar.getVisibility() == View.VISIBLE);
        }

        @Override
        public void setAnchorView(View view) {

        }

        @Override
        public void setEnabled(boolean enabled) {
            Dbug.i(tag, "setEnabled : " + enabled);
            if (enabled) {
                if (progressBarLoading != null && progressBarLoading.getVisibility() != View.GONE) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl player) {

        }

        @Override
        public void show(int timeout) {
        }

        @Override
        public void show() {

        }

        @Override
        public void showOnce(View view) {

        }
    };

    //显示隐藏工具栏
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        private int startX;
        private int startY;
        private boolean isClick = true;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final float x = event.getX();
            final float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    startX = (int) x;
                    startY = (int) y;
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if (Math.abs(x - startX) > threshold || Math.abs(y - startY) > threshold) {
                        isClick = false;
                    }
                    if (isClick) {
                        if (leftControlBar.getVisibility() != View.VISIBLE) {
                            switchUIBarVisible();
                        }
                    }
                    isClick = true;
                    break;
                }
            }
            return true;
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, final int framework_err, final int impl_err) {
            Dbug.e(tag, "Error: framework_err=" + framework_err + ",impl_err=" + impl_err);
            if (framework_err == -10000) {
                progressBarLoading.setVisibility(View.GONE);
                return true;
            }
            closeRTS();
            showErrorDialog(getString(R.string.fail_to_play));
            return true;
        }
    };

    /**
     * Listen RTSP state
     */
    private final IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            progressBarLoading.setVisibility(View.GONE);
            //setLastSensor();
        }
    };

    /**
     * Listen RTSP state
     */
    private final IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            //btnPlay.setVisibility(View.VISIBLE);
        }
    };

    private OnRealTimeListener realtimePlayerListener = new OnRealTimeListener() {
        @Override
        public void onVideo(int t, int channel, byte[] data, long sequence, long timestamp) {
            if (isStartDebug && PreferencesHelper.getSharedPreferences(mApplication).getBoolean(IConstant.DEBUG_SETTINGS, false)) {
                fps++;
            }
            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(t, data))
                    Dbug.e(tag, "Write video failed");
            }
            if (mVideoCapture != null && isCaptureBusying == false && mTakePictureCount > 0) {
                isCaptureBusying = true;
                mVideoCapture.capture(data);
                isCaptureBusying = false;
            }
        }

        @Override
        public void onAudio(int t, int channel, byte[] data, long sequence, long timestamp) {
            if (mRecordVideo != null && isRecordPrepared) {
                if (!mRecordVideo.write(t, data))
                    Dbug.e(tag, "Write audio failed");
            }
        }

        @Override
        public void onStateChanged(int state) {
            Dbug.i(tag, "onStateChanged:state=" + state);

            if (state == Stream.Status.STOP) {
                stopLocalRecording_Loop();
                if (btnPlay.getVisibility() != View.VISIBLE) {
                    //btnPlay.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onError(int code, String message) {
            Dbug.e(tag, "code=" + code + ", message=" + message);
//            mApplication.showToastShort("player has a error, message : "+ message);
            closeRTS();
        }
    };

    @Override
    public void onError(int code) {

    }

    private int getLevelResId(int level) {
        int res;
        switch (level) {
            case RTS_LEVEL_SD:
                res = R.drawable.drawable_resolution_sd;
                break;
            case RTS_LEVEL_FHD:
                res = R.drawable.drawable_resolution_fhd;
                break;
            default:
                res = R.drawable.drawable_resolution_hd;
                break;
        }
        return res;
    }

    private int getCameraLevel(int cameraType) {
        int level = IConstant.RTS_LEVEL_HD;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if (settingInfo != null) {
            if (cameraType == DeviceClient.CAMERA_REAR_VIEW) {
                level = settingInfo.getRearLevel();
            } else {
                level = settingInfo.getFrontLevel();
            }
        }
        return level;
    }

    private int getRtsFormat() {
        int format = DeviceClient.RTS_H264;
        DeviceDesc settingInfo = a.getApplication().getDeviceDesc();
        if (settingInfo != null) {
            format = settingInfo.getVideoType();
        }
        return format;
    }

    private int getVideoRate(int cameraType) {
        int rate = 30;
        DeviceSettingInfo settingInfo = a.getApplication().getDeviceSettingInfo();
        if (settingInfo != null) {
            if (cameraType == DeviceClient.CAMERA_REAR_VIEW) {
                rate = settingInfo.getRearRate();
            } else {
                rate = settingInfo.getFrontRate();
            }
        }
        return rate;
    }

    private void updateResolutionUI(boolean isRearCamera, int width, int height) {
        if (isAdjustResolution) {
            Dbug.w(tag, "adjust resolution step 006. isRear " + isRearCamera + ", w " + width + ", h " + height);
            int rtsLevel = AppUtils.adjustRtsResolution(width, height);
            if (rtsLevel != getCameraLevel(mApplication.getDeviceSettingInfo().getCameraType())) {
                if (isRearCamera) {
                    mApplication.getDeviceSettingInfo().setRearLevel(rtsLevel);
                } else {
                    mApplication.getDeviceSettingInfo().setFrontLevel(rtsLevel);
                }
            }
            isAdjustResolution = false;
            dismissAdjustingDialog();
        }
        int currentLevel = AppUtils.getStreamResolutionLevel();
        mAdjustResolutionBtn.setImageResource(getLevelResId(currentLevel));
    }

    private void createStream(int mode, int port) {
        Dbug.i(tag, "createStream==========mode=" + mode);
        if (mode == Stream.Protocol.TCP_MODE || mode == Stream.Protocol.UDP_MODE) {
            if (mRealtimeStream == null) {
                mRealtimeStream = new RealtimeStream(mode);
                mRealtimeStream.useDeviceTimestamp(true);
                mRealtimeStream.registerStreamListener(realtimePlayerListener);
            }
            Dbug.i(tag, "Net mode=" + mode + ", is receiving " + mRealtimeStream.isReceiving());
            if (!mRealtimeStream.isReceiving()) {
                if (mode == Stream.Protocol.TCP_MODE) {
                    mRealtimeStream.create(port, ClientManager.getClient().getAddress());
                } else {
                    mRealtimeStream.create(port);
                }
                mRealtimeStream.configure(RTP_VIDEO_PORT1, RTP_AUDIO_PORT1);
            } else Dbug.w(tag, "stream not receiving");
        } else {
            Dbug.e(tag, "Create stream failed!!!");
        }
    }

    private boolean mIsStartRecordingLoop = false;
    private long mStartRecordingTime = 0;

    private void startLocalRecording_Loop() {
        mIsStartRecordingLoop = true;
        mStartRecordingTime = SystemClock.uptimeMillis();
        startLocalRecording();
    }

    private void startLocalRecording() {
        if (mRecordVideo == null) {
            mRecordVideo = new VideoRecord();
            mRecordVideo.prepare(new OnRecordStateListener() {
                @Override
                public void onPrepared() {
                    //a.getApplication().showToastShort(R.string.start_recording);
                    isRecordPrepared = true;
                    showVideoRecordUI();
                }

                @Override
                public void onStop() {
                    isRecordPrepared = false;
                    hideVideoRecordUI();
                }

                @Override
                public void onError(String message) {
                    Dbug.e(tag, "Record error:" + message);
                    mRecordVideo = null;
                    isRecordPrepared = false;
                    hideVideoRecordUI();
                }
            });
        }
    }

    private void stopLocalRecording_Loop() {
        mIsStartRecordingLoop = false;
        mStartRecordingTime = 0;
        stopLocalRecording();
    }

    private void stopLocalRecording() {
        if (mRecordVideo != null) {
            //a.getApplication().showToastShort(R.string.stop_recording);
            isRecordPrepared = false;
            mRecordVideo.close();
            mRecordVideo = null;
        }
    }

    private boolean isPlaying() {
        if (isRtspEnable) return mStreamView != null && mStreamView.isPlaying();

        return mRealtimeStream != null && mRealtimeStream.isReceiving();
    }


    private void startDebug() {
        fps = 0;
        isStartDebug = true;
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_FPS_COUNT);
        }
        if (mDebugHelper == null) {
            mDebugHelper = new DebugHelper();
            mDebugHelper.registerDebugListener(mIDebugListener);
        }
        mDebugHelper.startDebug();
    }

    private void closeDebug() {
        if (mDebugHelper != null) {
            isStartDebug = false;
            fps = 0;
            if (mHandler != null) {
                mHandler.removeMessages(MSG_FPS_COUNT);
            }
            mDebugHelper.unregisterDebugListener(mIDebugListener);
            mDebugHelper.closeDebug();
            mDebugHelper = null;
        }
    }

    private void updateDebug(int dropCount, int dropSum) {
        if (mStreamView != null) {
            InfoHudViewHolder mHudViewHolder = mStreamView.getHudView();
            if (mHudViewHolder != null) {
                mHudViewHolder.setRowValue(R.string.drop_packet_count, dropCount + "");
                mHudViewHolder.setRowValue(R.string.drop_packet_sum, dropSum + "");
            }
        }
    }

    private void updateDebugFps(int fps) {
        if (mStreamView != null) {
            InfoHudViewHolder mHudViewHolder = mStreamView.getHudView();
            if (mHudViewHolder != null) {
                mHudViewHolder.setRowValue(R.string.fps, fps + "");
            }
        }
    }

    private IDebugListener mIDebugListener = new IDebugListener() {
        @Override
        public void onStartDebug(String remoteIp, int sendDataLen, int sendDataInterval) {

        }

        @Override
        public void onDebugResult(int dropCount, int dropSum) {
            updateDebug(dropCount, dropSum);
        }

        @Override
        public void onError(int code, String message) {
            Dlog.w(tag, message);
        }
    };

    private boolean isopen(){
        LocationManager locationManager = (LocationManager)getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                if (location.getLatitude() == 0.0 || location.getLongitude() == 0.0) {
                    return;
                }
                latitude = (int)((location.getLatitude() + 0.002598) * 10000000);
                longitude = (int)((location.getLongitude() - 0.005) * 10000000);
                //LatLng ll = convertBaiduToGPS(new LatLng(latitude * 0.0000001, longitude * 0.0000001));
                //latitude = (int)(ll.latitude * 10000000);
                //longitude = (int)(ll.longitude * 10000000);
                Log.d("location", "lat: " + latitude + " long: " + longitude);
            } else {
                Log.d("location","定位失败");
            }

        }

    };

    private float calulateAngleLinePointA(float pointAX, float pointAY, float pointOX, float pointOY){
        int deltaX = (int)(pointAX-pointOX);
        int deltaY = (int)(pointAY-pointOY);
        double distance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
        double radian = Math.acos(deltaX / distance);
        return (float)(radian) * (pointAY < pointOY ?  -1 : 1);
    }

    public double getDistance(LatLng start, LatLng end) {

        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;

        // 地球半径
        double R = 6371;

        // 两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1))
                * R;

        return d;
    }

    public double getDistance(int lat, int lng, int latB, int lngB) {
        LatLng stare = new LatLng(lat * 0.0000001, lng * 0.0000001);
        LatLng end = new LatLng(latB * 0.0000001, lngB * 0.0000001);
        double double_distance = getDistance(stare, end);
        return double_distance * 1000;
    }

    private static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    /**
     * 将百度坐标转变成火星坐标
     *
     * @param lngLat_bd 百度坐标（百度地图坐标）
     * @return 火星坐标(高德、腾讯地图等)
     */
    public static LatLng baidu_to_gaode(LatLng lngLat_bd) {
        double x = lngLat_bd.longitude - 0.0065, y = lngLat_bd.latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        return new LatLng(dataDigit(6, z * Math.sin(theta)), dataDigit(6, z * Math.cos(theta)));
    }

    static double dataDigit(int digit, double in) {
        return new BigDecimal(in).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();

    }

    public static LatLng convertBaiduToGPS(LatLng sourceLatLng) {
        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        double latitude = 2 * sourceLatLng.latitude - desLatLng.latitude;
        double longitude = 2 * sourceLatLng.longitude - desLatLng.longitude;
        BigDecimal bdLatitude = new BigDecimal(latitude);
        bdLatitude = bdLatitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        BigDecimal bdLongitude = new BigDecimal(longitude);
        bdLongitude = bdLongitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        return new LatLng(bdLatitude.doubleValue(), bdLongitude.doubleValue());
    }

    public static double getAngle1(double lat_a, double lng_a, double lat_b, double lng_b) {

        double y = Math.sin(lng_b-lng_a) * Math.cos(lat_b);
        double x = Math.cos(lat_a)*Math.sin(lat_b) - Math.sin(lat_a)*Math.cos(lat_b)*Math.cos(lng_b-lng_a);
        double bearing = Math.atan2(y, x);

        bearing = Math.toDegrees(bearing);
        if(bearing < 0){
            bearing = bearing + 360;
        }
        return bearing;

    }

}



