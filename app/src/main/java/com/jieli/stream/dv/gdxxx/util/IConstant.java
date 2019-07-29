package com.jieli.stream.dv.gdxxx.util;

import android.os.Environment;

import com.jieli.stream.dv.gdxxx.ui.a;

/**
 * 常量定义
 * date : 2017/3/6
 */
public interface IConstant {

    /**
     * func settings
     */
    boolean isWifiP2pEnable = true; //是否开启P2P直连

    /**
     * Device IP & Port
     */
    String DEFAULT_DEV_IP = "192.168.1.1";//The default gateway of device, some is NOT
    int CTP_TCP_PORT = 3333;//Use for sending TCP commands
    int CTP_UDP_PORT = 2228;//Use for sending UDP commands

    int RTS_TCP_PORT = 2223;//Use for request device's playback stream, download either.

    int RTS_UDP_PORT = 2224;//Use for request device's front camera real time stream
    int RTS_UDP_REAR_PORT = 2225;//Use for request device's rear camera real tiem stream

    int THUMBNAIL_TCP_PORT = 2226;//Use for request video cover

    int VIDEO_SERVER_PORT = 2229;
    int DEFAULT_HTTP_PORT = 8080;
    int FRONT_EMERGENCY_VIDEO_PORT = 10000;
    int REAR_EMERGENCY_VIDEO_PORT = 10001;

    /**
     * Generic Value
     */
    String KEY_FRAGMENT_TAG = "key_fragment_tag";
    String WIFI_PREFIX = a.isFactoryMode ? "" : "wifi_camera_";
    String CURRENT_WIFI_SSID = "current_wifi_ssid";
    String RECONNECT_TYPE = "reconnect_type"; //重连类型 （ 0 ： Wifi , 1 : WifiDirect)
    String KEY_DATA = "key_data";
    String KEY_PATH_LIST = "path_list";
    String KEY_POSITION = "position";
    String SOS_PREFIX = "SOS";
    String REC_PREFIX = "REC";
    String JPG_PREFIX = "JPG";
    String AUD_DEFAULT_NAME = "AUD_RECORD.pcm";
    int PAGE_LIMIT_COUNT = 18;

    /*重连类型*/
    int RECONNECT_TYPE_WIFI_DIRECT = 1;

    //AP搜索模式
    int AP_SEARCH_MODE = 0;
    //STA搜索模式
    int STA_SEARCH_MODE = 1;

    /*Device Mode*/
    int DEV_AP_MODE = 0;
    int DEV_STA_MODE = 1;

    /*Device Type*/
    String DEV_REC_SINGLE = "0";
    String DEV_REC_DUAL = "1";

    String SUPPORT_BUMPING = "1";
    String SUPPORT_PROJECTION = "1";

    String RTS_TYPE_JPEG = "0";
    String RTS_TYPE_H264 = "1";

    String RTS_NET_TYPE_TCP = "0";
    String RTS_NET_TYPE_UDP = "1";

    /*resolution level*/
    int RTS_LEVEL_SD = 0;
    int RTS_LEVEL_HD = 1;
    int RTS_LEVEL_FHD = 2;

    /*Frame Type*/
    int FRAME_TYPE_I = 0xa1a1;
    int FRAME_TYPE_P = 0xc2c2;
    int FRAME_TYPE_B = 0xb3b3;
    int FRAME_TYPE_UNKNOWN = 0;  //错误帧

    /*Fragment type*/
    int ADD_DEVICE_FRAGMENT = 0x0001;
    int BROWSE_FILE_FEAGMENT = 0x0002;
    int DEVICE_LIST_FRAGMENT = 0x0003;
    int SETTING_FRAGMENT = 0x0004;
    int UPGRADE_FRAGMENT = 0x0005;
    int DEV_PHOTO_FRAGMENT = 0x0006;
    int PHOTO_VIEW_FRAGMENT = 0x0007;
    int VIDEO_PLAYER_FRAGMENT = 0x0008;
    int HELP_FRAGMENT = 0x0010;
    int ABOUT_FRAGMENT = 0x0009;

    /*Permissions Code*/
    int CHECK_GPS_CODE = 0x1104;
    int PERMISSION_LOCATION_CODE = 110;
    int PERMISSION_STORAGE_CODE = 111;
    int PERMISSION_SETTING_CODE = 112;
    int PERMISSION_CONTACTS_CODE = 113;
    int PERMISSION_MICROPHONE_CODE = 114;

    /**
     * Browse file
     */
    int CODE_BROWSE_FILE = 0x1025;
    int CODE_SHARE_FILES = 0x1026;
    int CODE_PLAYBACK = 0x1027;
    String TAG_BROWSE_FILE = "browse_file";
    String KEY_VIDEO_LIST = "video_list";
    String MEDIA_TASK = "media_task";
    String KEY_FILE_INFO = "file_info";

    /*File Type*/
    int FILE_TYPE_PIC = 1;
    int FILE_TYPE_VIDEO = 2;
    int FILE_TYPE_UNKNOWN = 0;

    /*File Source*/
    int COME_FORM_DEV = 0;
    int COME_FORM_LOCAL = 1;

    /*Browse operation*/
    String KEY_BROWSE_OPERATION = "browse_operation";
    int OP_ENTER_EDIT_MODE = 0x00a1;
    int OP_EXIT_EDIT_MODE = 0x00a2;
    int OP_DOWNLOAD_FILES = 0x00a3;
    int OP_DELETE_FILES = 0x00a4;
    int OP_SELECT_ALL = 0x00a5;
    int OP_CANCEL_SELECT_ALL = 0x00a6;
    int OP_SHARE_FILES = 0x00a7;
    int OP_CANCEL_TASK = 0x00a8;

    int RESULT_DOWNLOAD_FILE = 0x0053;
    int RESULT_DELETE_FILE = 0x0054;
    int DOWNLOAD_FILE_PROGRESS = 0x0055;

    /*Operation Result*/
    int RESULT_FALSE = 0;
    int RESULT_SUCCESS = 1;
    int RESULT_FILE_EXIST = 2;
    int RESULT_CANCEL = 3;


    /*Select Files*/
    String KEY_SELECT_FILES_NUM = "select_files_num";
    String KEY_STATE_TYPE = "state_type";
    String KEY_SELECT_STATE = "select_state";

    /*state type*/
    int TYPE_EDIT = 1;
    int TYPE_SELECT_ALL = 2;

    /**
     * document
     */
    String DEFAULT_PATH = "null";
    String ROOT_PATH = Environment.getExternalStorageDirectory().getPath(); //手机内存路径
    String DIR_FRONT = "FMedia";
    String DIR_REAR = "RMedia";
    String DIR_RECORD = "Record";
    String DIR_DOWNLOAD = "Download";
    String DIR_THUMB = ".thumbnail";
    String VERSION = "version";
    String UPGRADE = "upgrade";
    String DEV_WORKSPACE_FRONT = "/DCIM/1";//Device front camera workspace
    String DEV_WORKSPACE_REAR = "/DCIM/2";//Device rear camera workspace

    String VERSION_JSON = "version.json";

    String UPDATE_PATH = "update_path";
    String UPDATE_TYPE = "update_type";
    int UPGRADE_APK_TYPE = 1;
    int UPGRADE_SDK_TYPE = 2;

    String FIRMWARE_DIR = "firmware";
    String ANDROID_DIR = "android";

    int MSG_UPDATE_DOWNLOAD_PROGRESS = 0x5566;
    int MSG_UPDATE_UPLOAD_PROGRESS = 0x5567;

    /*device camera type*/
    String CAMERA_TYPE_FRONT = "0";
    String CAMERA_TYPE_REAR = "1";

    /**
     * App
     */
    String DEV_TYPE = "dev_type";
    String DEV_LIST = "dev_list";

    /*Device Desc*/
    String DEV_UUID = "uuid";
    String DEV_PRODUCT = "product_type";
    String DEV_MATCH_APP = "match_app_type";
    String DEV_FIRMWARE_VERSION = "firmware_version";
    String DEV_APP_LIST = "app_list";
    String DEV_MATCH_ANDROID_VER = "match_android_ver";
    String DEV_DEVICE_TYPE = "device_type";
    String DEV_SUPPORT_BUMPING = "support_bumping";
    String DEV_SUPPORT_PROJECTION = "support_projection";
    String DEV_FRONT_SUPPORT = "forward_support";
    String DEV_REAR_SUPPORT = "behind_support";
    String DEV_RTS_TYPE = "rts_type";//support h264 or jpeg
    String DEV_RTS_NET_TYPE = "net_type";//TCP or UDP transport stream from device

    /* Upgrade */
    int MSG_UPGRADE_FILE = 0x0051;
    int CODE_UPGRADE_APK = 0x1008;

    /*File type*/
    int FILE_DESC_TXT = 1;
    int FILE_TYPE_UPGRADE = 2;

    /**
     * Device
     */
    int CODE_ADD_DEVICE = 0x1036;
    int CODE_DEVICE_LIST = 0x1037;
    int CODE_UPGRADE = 0x1038;
    int REQUEST_MEDIA_PROJECTION = 0x1049;

    /*record status*/
    int STATUS_PREPARE = 0;
    int STATUS_RECORDING = 1;
    int STATUS_NOT_RECORD = 2;

    /**
     * Service
     */
    String SERVICE_CMD = "service_command";
    String SCREEN_ORIENTATION = "screen_orientation";
    int SERVICE_CMD_CONNECT_CTP = 1;
    int SERVICE_CMD_DISCONNECT_CTP = 2;
    int SERVICE_CMD_OPEN_SCREEN_TASK = 3;
    int SERVICE_CMD_CLOSE_SCREEN_TASK = 4;
    int SERVICE_CMD_SCREEN_CHANGE = 5;

    String KEY_PROJECTION_STATUS = "projection_status";
    String KEY_CONNECT_IP = "connect_ip";

    /**
     * Setting
     */
    String DEVICE_DESCRIPTION = "dev_desc.txt";

    int DEFAULT_CACHE_SIZE = 200 * 1024 * 1024; //200 MB

    /**
     * Wifi Constant
     */
    /*Wifi State*/
    int WIFI_CONNECTING = 0;
    int WIFI_CONNECTED = 1;
    int WIFI_CONNECT_FAILED = 2;
    int WIFI_UNKNOWN_ERROR = -1;

    /*Wifi Type*/
    String KEY_WPA = "WPA_PSK";
    String KEY_NONE = "NONE";

    /*Wifi Error Code*/
    int ERROR_NETWORK_INFO_EMPTY = 0xeef0;
    int ERROR_NETWORK_TYPE_NOT_WIFI = 0xeef1;
    int ERROR_WIFI_INFO_EMPTY = 0xeef2;
    int ERROR_WIFI_PWD_NOT_MATCH = 0xeef3;
    int ERROR_NETWORK_NOT_OPEN = 0xeef4;
    int ERROR_WIFI_IS_CONNECTED = 0xeef5;

    /*Wifi key*/
    String KEY_WIFI_SSID = "wifi_ssid";
    String KEY_WIFI_PWD = "wifi_pwd";
    String KEY_SEARCH_MODE = "search_mode";
    /**
     * Device type
     */
    int FILE_TYPE_INVALID = 0;//无效文件
    int FILE_TYPE_NORMAL = 1;            //正常录像视频
    int FILE_TYPE_SOS = 2;            //保护/紧急视频
    int FILE_TYPE_LATENCY = 3;          //延时拍摄视频

    /**
     * 图片显示分页大小
     */
    int PAGE_SIZE = 10;

    /**
     * FTP
     */
    //ftp parameters
    int DEFAULT_FTP_PORT = 21;
    //outer net ftp
    String FTP_HOST_NAME = "cam.jieli.net";
//    String FTP_HOST_NAME = "120.24.210.62";

    //测试账号
    String FTP_USER_NAME = "wifi@baidu.com";
    String FTP_PASSWORD = "wifi123456";

    //intranet ftp
    String INSIDE_FTP_USER_NAME = "FTPX";
    String INSIDE_FTP_PASSWORD = "12345678";

    //无人机设置参数key
   // String KEY_VIDEO_MIC="video_mic";
    String KEY_VOLUME ="volume";
    //String KEY_TF_STORAGE_LEFT="tf_storage_left";
    //String KEY_TF_STORAGE_TOTAL="tf_storage_total";
  //  String KEY_PHOTO_QUALITY_INDEX="photo_quality_index";

    //APP设置参数KEY
    String KEY_OPEN_DEBUG="open_debug";
    String KEY_ALLOW_SAVE_DRIVING_DATA="allow_save_driving_data";
    String KEY_TIME_FORMAT="time_format";
    String KEY_SAVE_PICTURE="save_picture";
    String KEY_AUTO_DOWNLOAG_PICTURE="auto_download_picture";
    String KEY_APP_LANGUAGE_CODE ="language_code";
    String KEY_HARD_CODEC = "hard_codec";
    String KEY_FRONT_RES_LEVEL = "rt_front_res_level";
    //String KEY_FRONT_RES_HEIGHT = "rt_front_res_height";
    String KEY_REAR_RES_LEVEL = "rt_rear_res_level";
    String KEY_HAS_AGREED = "has_agreed_with_agreement";

    int ARGS_SHOW_DIALOG = 0;
    int ARGS_DISMISS_DIALOG = 1;
    String KEY_DIR_PATH = "key_dir_path";
    String KEY_ROOT_PATH_NAME = "key_root_path_name";

    /*碰撞视频会封装状态*/
    int STATE_START = 0x001;
    int STATE_PROGRESS = 0x002;
    int STATE_END = 0x003;

    /*视频封装错误码*/
    int ERROR_INIT_MOV = 0xee1;
    int ERROR_CLOSE_MOV =0xee2;
    int ERROR_NETWORK = 0xee3;
    int ERROR_DEVICE_OFFLINE = 0xee4;
    int ERROR_DATA_IS_NULL = 0xee5;
    int ERROR_STORAGE = 0xee6;

    /*回放自动播放时间*/
    int AUTO_TIME=10000;

    int AUDIO_CHANNEL = 1;
    int AUDIO_FORMAT = 16;
    int AUDIO_SAMPLE_RATE_DEFAULT = 8000;

    int VIDEO_FRAME_RATE_DEFAULT = 30;

    int RES_HD_WIDTH = 1280;
    int RES_HD_HEIGHT = 720;

    //SDP info
    int SDP_PORT = 6789;
    String SDP_URL = "tcp://127.0.0.1:" + SDP_PORT;
    //The port for the second Stream
    int SDP_PORT2 = 6880;
    String SDP_URL2 = "tcp://127.0.0.1:" + SDP_PORT2;

    //RTP info
    int RTP_VIDEO_PORT1 = 6666;
    int RTP_AUDIO_PORT1 = 1234;
    int RTP_VIDEO_PORT2 = 6668;
    int RTP_AUDIO_PORT2 = 1236;

    //使用RTSP方式播放实时流
    String KEY_RTSP = "rtsp_state";
    String STR_RTSP = "RTSP";
    String RTSP_URL = "rtsp://%s:554/264_rt/XXX.sd";
    String RTSP_URL_REAR = "rtsp://%s:554/264_rt/rear.sd";
    String RTSP_FRONT_JPEG_URL = "rtsp://%s:554/avi_rt/front.sd";
    String RTSP_REAR_JPEG_URL = "rtsp://%s:554/avi_rt/rear.sd";

    String VIDEO_PATH = "video_path";
    String VIDEO_CREATE_TIME = "video_create_time";
    String VIDEO_OFFSET = "video_offset";

    /*Debug settings*/
    String DEBUG_SETTINGS = "debug_settings";
}
