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
			// 创建一个文件夹对象，赋值为外部存储器的目录
			File sdcardDir = Environment.getExternalStorageDirectory();
			// 得到一个路径，内容是sdcard的文件夹路径和名字
			path = sdcardDir.getPath() + File.separator + dirName;

			File dir = new File(path);

			if (!dir.exists()) {
				dir.mkdirs();
			}	
		}
		
		return path;
	}
	
	/**  

     * 通过包名获取应用程序的名称。  

	    * @param context  

	     *            Context对象。  

      * @param packageName  

	      *            包名。  

	      * @return 返回包名所对应的应用程序的名称。  

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
	 * 复制assets中的文件到指定目录下
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
				//如果是文件，则直接拷贝，如果是文件夹，就会抛出异常，捕捉后递归拷贝
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
