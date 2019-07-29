package com.jieli.stream.dv.gdxxx.util;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * 时间格式工具类
 */
public class TimeFormate {
	public static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.getDefault());
	public static final SimpleDateFormat yyyyMMdd_format = new SimpleDateFormat(
			"yyyyMMdd", Locale.getDefault());
	public static final SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm", Locale.getDefault());
	public static final SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	public static final SimpleDateFormat yyyyMMdd_HHmmss =  new SimpleDateFormat(
			"yyyyMMddHHmmss", Locale.getDefault());

	public static String formatYMD(long time) {
		return formatYMD(new Date(time));
	}

	public static String formatYMD(Date date) {
		return yyyyMMdd.format(date);
	}

	public static String formatYMD(Calendar calendar) {
		return formatYMD(calendar.getTime());
	}

	public static String formatToYMD(long time) {
		return formatToYMD(new Date(time));
	}

	public static String formatToYMD(Date date) {
		return yyyyMMdd_format.format(date);
	}

	public static String formatToYMD(Calendar calendar) {
		return formatToYMD(calendar.getTime());
	}

	public static String formatYMDHM(long time) {
		return formatYMDHM(new Date(time));
	}

	public static String formatYMDHM(Date date) {
		return yyyyMMddHHmm.format(date);
	}

	public static String formatYMDHM(Calendar calendar) {
		return formatYMDHM(calendar.getTime());
	}

	public static String formatYMDHMS(long time) {
		return formatYMDHMS(new Date(time));
	}

	public static String formatYMDHMS(Date date) {
		return yyyyMMddHHmmss.format(date);
	}

	public static String formatYMD_HMS(long time) {
		return formatYMD_HMS(new Date(time));
	}

	public static String formatYMD_HMS(Date date) {
		return yyyyMMdd_HHmmss.format(date);
	}

	public static String formatYMDHMS(Calendar calendar) {
		return formatYMDHMS(calendar.getTime());
	}

	public static String getTimeFormatValue(int time) {
		int hour = time / 60 / 60 % 24;
		int min = time /60 % 60;
		int sec = time % 60;
		if(hour == 0){
			return MessageFormat.format("{0,number,00}:{1,number,00}", min, sec);
		}else{
			return MessageFormat.format("{0,number,00}:{1,number,00}:{2,number,00}", hour, min, sec);
		}
	}

	public static String getHHMMSSFormatValue(int time) {
		long t = time / 1000;
		return MessageFormat.format("{0,number,00}:{1,number,00}:{2,number,00}", t / 60 / 60, t / 60 % 60, t % 60);
	}

	/**
	 * timeZoneOffset表示时区，如中国一般使用东八区，因此timeZoneOffset就是8
	 * @param timeZoneOffset
	 */
	public static String getFormatedDateString(int timeZoneOffset){
		if (timeZoneOffset > 13 || timeZoneOffset < -12) {
			timeZoneOffset = 0;
		}
		TimeZone timeZone;
		String[] ids = TimeZone.getAvailableIDs(timeZoneOffset * 60 * 60 * 1000);
		if (ids.length == 0) {
			// if no ids were returned, something is wrong. use default TimeZone
			timeZone = TimeZone.getDefault();
		} else {
			timeZone = new SimpleTimeZone(timeZoneOffset * 60 * 60 * 1000, ids[0]);
		}
		SimpleDateFormat sdf = yyyyMMddHHmmss;
		sdf.setTimeZone(timeZone);
		return sdf.format(new Date());
	}

	public enum DateType{
		YEAR,MONTH,DAY,HOUR,MIN,SEC,TIME
	}

	public static long getDateMillTime(String date, SimpleDateFormat simpleDateFormat){
		if(date == null || date.isEmpty() || simpleDateFormat == null){
			return 0;
		}
		long time = 0;
		Date newDate = null;
		try{
			newDate = simpleDateFormat.parse(date);
		}catch (ParseException e){
			e.printStackTrace();
		}
		if(newDate != null){
			time = newDate.getTime();
		}
		return time;
	}

	public static String getDateTime(String date, SimpleDateFormat simpleDateFormat, DateType type){
		String result = null;
		if(date == null || date.isEmpty() || simpleDateFormat == null){
			return null;
		}
		Date newDate = null;
		try{
			newDate = simpleDateFormat.parse(date);
		}catch (ParseException e){
			e.printStackTrace();
		}
		if(newDate != null){
			switch (type){
				case YEAR:
					result = String.valueOf(newDate.getYear()+1900);
					break;
				case MONTH:
					result = String.valueOf(newDate.getMonth() + 1);
					break;
				case DAY:
					result = String.valueOf(newDate.getDate());
					break;
				case HOUR:
					result = String.valueOf(newDate.getHours());
					break;
				case MIN:
					result = String.valueOf(newDate.getMinutes());
					break;
				case SEC:
					result = String.valueOf(newDate.getSeconds());
					break;
				case TIME:
					result = getInt2TwoByte(newDate.getHours()) + ":" + getInt2TwoByte(newDate.getMinutes())+ ":" + getInt2TwoByte(newDate.getSeconds());
					break;
			}
		}
		return result;
	}


	public static String getFormatedDateTime(SimpleDateFormat format, long dateTime) {
		if(format == null || dateTime < 0){
			return null;
		}
		return format.format(new Date(dateTime));
	}

	private static  String getInt2TwoByte(int num){
		String str = String.valueOf(num);
		if(num < 10){
			str = "0" + num;
		}
		return str;
	};
}
