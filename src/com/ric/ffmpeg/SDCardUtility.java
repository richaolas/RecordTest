package com.ric.ffmpeg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class SDCardUtility 
{	
	public static String createSDCardDir(String dirName, Context context) {
		
		//String appName = getProgramNameByPackageName(context);
		String path = null;
		
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
			File sdcardDir = Environment.getExternalStorageDirectory();
			// �õ�һ��·����������sdcard���ļ���·��������
			path = sdcardDir.getPath() + File.separator + dirName;

			File dir = new File(path);

			if (!dir.exists()) {
				dir.mkdirs();
			}	
		}
		
		return path;
	}
	
	/**  

     * ͨ��������ȡӦ�ó�������ơ�  

	    * @param context  

	     *            Context����  

      * @param packageName  

	      *            ������  

	      * @return ���ذ�������Ӧ��Ӧ�ó�������ơ�  

	     */ 

	public static String getProgramNameByPackageName(Context context) {

		String packageName = context.getPackageName();
		
		PackageManager pm = context.getPackageManager();

		String name = null;

		try {

			name = pm.getApplicationLabel(

			pm.getApplicationInfo(packageName,

			PackageManager.GET_META_DATA)).toString();

		} catch (NameNotFoundException e) {

			e.printStackTrace();

		}

		return name;

	}

	/**
	 * ����assets�е��ļ���ָ��Ŀ¼��
	 * @param context
	 * @param assetsFileName
	 * @param targetPath
	 * @return
	 */
	public static boolean copyAssetData(Context context, String assetsFileName, String targetPath) {
		try {
			
			String destPath = targetPath + File.separator + assetsFileName;
			File destFile = new File(destPath);

			if (!destFile.exists()) {
				InputStream inputStream = context.getAssets().open(assetsFileName);
				FileOutputStream output = new FileOutputStream(destPath);

				byte[] buf = new byte[1024 * 4];
				int count = 0;
				while ((count = inputStream.read(buf)) > 0) {
					output.write(buf, 0, count);
				}
				output.close();
				inputStream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	public static void copyAssetsDir(Context context, String dirName, String targetFolder) {
		try {
			File f = new File(targetFolder + File.separator + dirName);
			if(!f.exists() && !f.isDirectory())
				f.mkdirs();

			String[] filenames = context.getAssets().list(dirName);
			InputStream inputStream = null;
			for(String filename : filenames) {
				String name = null;
				if (dirName.length() != 0)
				{
					name = dirName + File.separator + filename;
				} else {
					name = filename;
				}
				//������ļ�����ֱ�ӿ�����������ļ��У��ͻ��׳��쳣����׽��ݹ鿽��
				try {
					inputStream = context.getAssets().open(name);
					inputStream.close();
					copyAssetData(context, name, targetFolder);
				} catch (Exception e) {
					copyAssetsDir(context, name, targetFolder);
				} finally {
					inputStream = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	

}
