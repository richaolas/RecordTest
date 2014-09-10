package com.ric.ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import com.example.recordtest.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class FFmpegTools {
	
	private Context mContext;
	
	private static String mFfmpegInstallPath;
	
	public static String getmFfmpegInstallPath() {
		return mFfmpegInstallPath;
	}

	public static void setmFfmpegInstallPath(String mFfmpegInstallPath) {
		FFmpegTools.mFfmpegInstallPath = mFfmpegInstallPath;
	}

	public FFmpegTools(Context ctx)
	{
		mContext = ctx;
	}
	
	public void execCommandLine(final List<String> cmdLine)
	{	
		final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Loading", "Please wait.", 
				true, false);
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				
				final ProcessBuilder pb = new ProcessBuilder(cmdLine);
				new ProcessRunnable(pb).run();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();
				Toast.makeText(mContext, "Ffmpeg job complete.", Toast.LENGTH_SHORT).show();
			}
			
		}.execute();
		
	}
	
	public void installFfmpeg() {
		
		File ffmpegFile = new File(mContext.getCacheDir(), "ffmpeg");
		mFfmpegInstallPath = ffmpegFile.getAbsolutePath();
		
		Log.d(TAG, "ffmpeg install path: " + mFfmpegInstallPath);
		
		if (!ffmpegFile.exists()) {
			try {
				ffmpegFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "Failed to create new file!", e);
			}
			this.installBinaryFromRaw(R.raw.ffmpeg, ffmpegFile);
		}
		
		ffmpegFile.setExecutable(true);
	}
	
	private static final String TAG = "Utils";
	private static final int IO_BUFFER_SIZE = 32256;
	
	public static final String SHELL_CMD_CHMOD = "chmod";	
	public static final int CHMOD_EXEC_VALUE = 700;
	
	public static void doChmod(File file, int chmodValue) {
		final StringBuilder sb = new StringBuilder();
		sb.append(SHELL_CMD_CHMOD);
		sb.append(' ');
		sb.append(chmodValue);
		sb.append(' ');
		sb.append(file.getAbsolutePath());
		
		try {
			Runtime.getRuntime().exec(sb.toString());
		} catch (IOException e) {
			Log.e(TAG, "Error performing chmod", e);
		}
	}
	
	
	private void installBinaryFromRaw(int resId, File file) {
		final InputStream rawStream = mContext.getResources().openRawResource(resId);
		final OutputStream binStream = getFileOutputStream(file);
		
		if (rawStream != null && binStream != null) {
			pipeStreams(rawStream, binStream);
			
			try {
				rawStream.close();
				binStream.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to close streams!", e);
			}		
			
			//doChmod(file, CHMOD_EXEC_VALUE);
		}		
	}
	
	public static void checkFilePerms(File file) {
		try {
			Process proc = Runtime.getRuntime().exec("ls -l " + file.toString());
			logInputStream(proc.getInputStream());
		} catch (IOException e) {
			Log.e(TAG, "Error checking file permissions.", e);
		}
	}
	
	public static void logInputStream(InputStream stream) {
		StringBuilder sb = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			Log.e(TAG, "Error reading inputstream.", e);
		}
		
		Log.d(TAG, sb.toString());
	}
	
	public static OutputStream getFileOutputStream(File file) {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found attempting to stream file.", e);
		}
		return null;
	}
	
	public static void pipeStreams(InputStream is, OutputStream os) {
		byte[] buffer = new byte[IO_BUFFER_SIZE];
		int count;
		try {
			while ((count = is.read(buffer)) > 0) {
				os.write(buffer, 0, count);
			}
		} catch (IOException e) {
			Log.e(TAG, "Error writing stream.", e);
		}
	}
	
}
