package com.jieli.stream.dv.gdxxx.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Set;

public class PreferencesHelper {
	/** Shared Preferences name */
    private static String PREFERENCES_NAME = "wifi_camera_tab";

    public void setPreferencesName(String preferencesName){
        PREFERENCES_NAME = preferencesName;
    }

    public static void putIntValue(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void putLongValue(Context context, String key, long value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void putStringValue(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    public static void putBooleanValue(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    public static void putStringSetValue(Context context, String key, Set<String> value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }
    
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }
    
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    //ljw
    private static String SPEED_LEVEL_NAME = "speed_level";
    public static final int SPEED_LEVEL_L = 0;
    public static final int SPEED_LEVEL_M = 1;
    public static final int SPEED_LEVEL_H = 2;
    /**
     *
     * @param context
     * @param speedLevel 0 1 2
     */
    public static void putSpeedLevelValue(Context context, int speedLevel){
        SharedPreferences sp = context.getSharedPreferences(SPEED_LEVEL_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt("speedLevel", speedLevel).commit();
    }

    public static int getSpeedLevelValue(Context context){
        SharedPreferences sp = context.getSharedPreferences(SPEED_LEVEL_NAME, Context.MODE_PRIVATE);
        return sp.getInt("speedLevel", SPEED_LEVEL_L);
    }

}
