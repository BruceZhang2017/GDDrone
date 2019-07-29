package com.jieli.stream.dv.gdxxx.util;

import android.util.Log;

import com.jieli.stream.dv.gdxxx.BuildConfig;

public class Dbug {
	private static boolean IS_DEBUG = BuildConfig.DEBUG;

	public static void openOrCloseDebug(boolean isOpen) {
		IS_DEBUG = isOpen;
	}

	public static void v(String tag, String msg) {
		if(IS_DEBUG){
			Log.v(tag, msg);
		}
	}
	public static void d( String msg) {
		if(IS_DEBUG){
			Log.d("Debug", msg);
		}
	}
	public static void d(String tag, String msg) {
		if(IS_DEBUG){
			Log.d(tag, msg);
		}
	}
	public static void i(String tag, String msg) {
		if(IS_DEBUG){
			Log.i(tag, msg);
		}
	}
	public static void w(String tag, String msg) {
		if(IS_DEBUG){
			Log.w(tag, msg);
		}
	}
	public static void e(String tag, String msg) {
		if(IS_DEBUG){
			Log.e(tag, msg);
		}
	}
}

