package com.ric.ffmpeg;

import java.io.File;
import java.text.SimpleDateFormat;

import android.os.Environment;

public class FileIOUtils {
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ��Ŀ¼
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
