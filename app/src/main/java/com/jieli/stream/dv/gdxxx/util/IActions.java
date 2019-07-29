package com.jieli.stream.dv.gdxxx.util;

/**
 *
 * @author zqjasonZhong
 * date : 2017/3/8
 */
public interface IActions {
    String ACTION_PREFIX = "com.jieli.dv.running2_";

    String ACTION_DEVICE_CAPACITY = ACTION_PREFIX + "device_capacity";
    String KEY_DEVICE_STORAGE_AVAILABLE = "device_storage_available";
    String KEY_DEVICE_STORAGE_TOTAL = "device_storage_total";

    String ACTION_DEV_ACCESS = ACTION_PREFIX + "dev_access";
    String KEY_ALLOW_ACCESS = "allow_access";

    String ACTION_CONNECT_DEVICE = ACTION_PREFIX + "connect_device";
    String ACTION_CONNECT_INTERNET = ACTION_PREFIX + "connect_internet";
    String ACTION_UPGRADE_FILE = ACTION_PREFIX + "upgrade_file";
    String ACTION_SDK_UPGRADE_SUCCESS = ACTION_PREFIX + "sdk_upgrade_success";

    String ACTION_TF_CARD_STATE = ACTION_PREFIX + "sdcard_state";
    String KEY_TF_STATE = "TF_state";
    String ACTION_FORMAT_TF_CARD = ACTION_PREFIX + "format_sdcard";

    String ACTION_BROWSE_FILE_OPERATION = ACTION_PREFIX + "browse_file_operation";
    String ACTION_SELECT_FILES = ACTION_PREFIX + "select_files";
    String ACTION_SELECT_STATE_CHANGE = ACTION_PREFIX + "select_state_change";
    String ACTION_LANGUAAGE_CHANGE= ACTION_PREFIX + "language_change";
    String ACTION_PROJECTION_STATUS = ACTION_PREFIX + "projection_status";
    String ACTION_ACCOUT_CHANGE = ACTION_PREFIX + "account_change";
    String ACTION_EMERGENCY_VIDEO_STATE = ACTION_PREFIX + "emergency_video_state";
    String ACTION_KEY_VIDEO_STATE = "video_state";
    String ACTION_KEY_ERROR_CODE = "error_code";
    String ACTION_KEY_EMERGENCY_MSG = "emergency_msg";
}