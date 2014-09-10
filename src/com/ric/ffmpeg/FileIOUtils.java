package com.ric.ffmpeg;

import java.io.File;
import java.text.SimpleDateFormat;

import android.os.Environment;

public class FileIOUtils {
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.getAbsolutePath();
	}

	public static String getRecordFileName() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyyMMdd_hhmmss");
		return "R_V_" + sDateFormat.format(new java.util.Date()) + ".3gp";
	}
	
	
	public static String getRecordFileNameMP4() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyyMMdd_hhmmss");
		return "R_V_" + sDateFormat.format(new java.util.Date()) + ".mp4";
	}
}
