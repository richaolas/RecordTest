/*
 * Copyright (C) 2012,2013 Qianliang Zhang, Shawn Van Every, Samuel Audet
 *
 * IMPORTANT - Make sure the AndroidManifest.xml file looks like this:
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <manifest xmlns:android="http://schemas.android.com/apk/res/android"
 *     package="org.bytedeco.javacv.recordactivity"
 *     android:versionCode="1"
 *     android:versionName="1.0" >
 *     <uses-sdk android:minSdkVersion="4" />
 *     <uses-permission android:name="android.permission.CAMERA" />
 *     <uses-permission android:name="android.permission.INTERNET"/>
 *     <uses-permission android:name="android.permission.RECORD_AUDIO"/>
 *     <uses-permission android:name="android.permission.WAKE_LOCK"/>
 *     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 *     <uses-feature android:name="android.hardware.camera" />
 *     <application android:label="@string/app_name">
 *         <activity
 *             android:name="RecordActivity"
 *             android:label="@string/app_name"
 *             android:screenOrientation="landscape">
 *             <intent-filter>
 *                 <action android:name="android.intent.action.MAIN" />
 *                 <category android:name="android.intent.category.LAUNCHER" />
 *             </intent-filter>
 *         </activity>
 *     </application>
 * </manifest>
 *
 * And the res/layout/main.xml file like this:
 *
 * <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:tools="http://schemas.android.com/tools"
 *     android:id="@+id/record_layout"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" >
 * 
 *     <TextView
 *         android:id="@+id/textView1"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:layout_centerHorizontal="true"
 *         android:layout_centerVertical="true"
 *         android:padding="8dp"
 *         android:text="@string/app_name"
 *         tools:context=".RecordActivity" />
 *
 *     <Button
 *         android:id="@+id/recorder_control"
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:layout_above="@+id/textView1"
 *         android:layout_alignRight="@+id/textView1"
 *         android:layout_marginRight="70dp"
 *         android:text="Button" />
 *
 * </LinearLayout>
 */

package com.example.recordtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.ric.ffmpeg.CommandLineFactory;
import com.ric.ffmpeg.FFmpegTools;
import com.ric.ffmpeg.FileIOUtils;
import com.ric.ffmpeg.SDCardUtility;

//import org.bytedeco.javacv.FFmpegFrameRecorder;

//import static org.bytedeco.javacpp.opencv_core.*;

public class RecordActivity extends Activity implements OnClickListener {

    private final static String CLASS_LABEL = "RecordActivity";
    private final static String LOG_TAG = CLASS_LABEL;

    private PowerManager.WakeLock mWakeLock;

    private String ffmpeg_link = "/mnt/sdcard/stream.3gp";

    long startTime = 0;
    boolean recording = false;

    private volatile FFmpegFrameRecorder recorder;

    private boolean isPreviewOn = false;

    private int sampleAudioRateInHz = 44100;
   
    private int imageWidth = 960;//640;//960;
    private int imageHeight = 720;//480;//720;
//    private int imageWidth = 320;
//    private int imageHeight = 240;
    
    private int frameRate = 30;

    /* audio data getting thread */
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    volatile boolean runAudioThread = true;

    /* video data getting thread */
    private Camera cameraDevice;
    private CameraView cameraView;

    private IplImage yuvIplimage = null;

    /* layout setting */
    private final int bg_screen_bx = 232;
    private final int bg_screen_by = 128;
    private final int bg_screen_width = 700;
    private final int bg_screen_height = 500;
    private final int bg_width = 1123;
    private final int bg_height = 715;
    
    private final int live_width = 640;
    private final int live_height = 480;
    
    
    private int screenWidth, screenHeight;
    private Button btnRecorderControl;
    
    
    private FFmpegTools mFFmpegTools = null;
    private String recordPath = null; 
	private String curRawVideoName = null;
	private String sdcardPath = null;
	
	static final int MSG_RECORD_END = 0;
	
	private Handler mHander = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			if (msg.arg1 == MSG_RECORD_END)
			{
				stopRecording();
				Log.w(LOG_TAG, "Stop Button Pushed");
	            btnRecorderControl.setText("录制");
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	
	public static Intent getVideoFileIntent(File file)
    {
      Intent intent = new Intent("android.intent.action.VIEW");
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("oneshot", 0);
      intent.putExtra("configchange", 0);
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "video/*");
      return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //setContentView(R.layout.activity_main);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL); 
        mWakeLock.acquire(); 

        
        //sdcardPath = SDCardUtility.createSDCardDir(this.getPackageName(), this);
		//recordPath = sdcardPath; 
		
		//set record path
				sdcardPath = SDCardUtility.createSDCardDir(this.getPackageName(), this);
				recordPath = sdcardPath; 
			    SDCardUtility.copyAssetsDir(this, "music", sdcardPath); 
        
        
        initLayout2();
        initRecorder();
        
      //install ffmpeg
      		mFFmpegTools = new FFmpegTools(this);
      		mFFmpegTools.installFfmpeg();
      		
      		 /* add control button: start and stop */
            btnRecorderControl = (Button) findViewById(R.id.button1);
            //btnRecorderControl.setText("Start");
            btnRecorderControl.setOnClickListener(this);
            
      
        
        Button genBtn = (Button)this.findViewById(R.id.button3);
        genBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				List<String> cmdLine1 = CommandLineFactory.spliteAV(recordPath, curRawVideoName, recordPath + File.separator + "tmp.mp4");
				
				RecordActivity.this.mFFmpegTools.execCommandLine(cmdLine1);
				
				List<String> cmdLine2 = CommandLineFactory.mixVideoAndAudio(recordPath, "tmp.mp4", 
						recordPath + File.separator + "music", "audio_001.aac", 
						recordPath + File.separator + "result.mp4");
				
				RecordActivity.this.mFFmpegTools.execCommandLine(cmdLine2);
			}
		}); 
        
        Button watchBtn = (Button)this.findViewById(R.id.watch);
        watchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				String filePath = recordPath + File.separator + "result.mp4";
				Intent intent = RecordActivity.getVideoFileIntent(new File(filePath));
				RecordActivity.this.startActivity(intent);
			}
		}); 
        
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mWakeLock == null) {
           PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
           mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
           mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recording = false;

        if (cameraView != null) {
            cameraView.stopPreview();
        }

        if(cameraDevice != null) {
           cameraDevice.stopPreview();
           cameraDevice.release();
           cameraDevice = null;
        }

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void initLayout2()
    {
    	Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();
		
		System.out.println("---------------------------" + screenWidth + " " + screenHeight);

		RelativeLayout.LayoutParams layoutParam = null;
		LayoutInflater myInflate = null;
		myInflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout rootViewLayout = (RelativeLayout) myInflate.inflate(
				R.layout.activity_main, null);

		int l = 20, t = 20, r = 20, b = 20;
		int w = 4, h = 3; //640:480 64:48 -> 8:6 ->4 : 3

		int h2 = screenHeight - t - b;
		int w2 = (int)(w * 1.0 / h * h2);
		
		
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				w2,
				h2);
		
		lp1.setMargins(screenWidth-w2-r, t, r, b);
		
		cameraDevice = Camera.open();
        Log.i(LOG_TAG, "cameara open");
        cameraView = new CameraView(this, cameraDevice);
		
		rootViewLayout.addView(cameraView, lp1);

		
		/*LinearLayout subLayout = (LinearLayout) myInflate.inflate(
				R.layout.sub, null);
		//subLayout.set
		
		RelativeLayout.LayoutParams lp2 = 
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lp1.setMargins(0, 20, 20, 0);
		
		rootViewLayout.addView(subLayout, lp2);
		*/
		
		

		setContentView(rootViewLayout);	
		
		
    	
    }

    private void initLayout() {

        /* get size of screen */
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        
        RelativeLayout.LayoutParams layoutParam = null; 
        LayoutInflater myInflate = null; 
        myInflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        RelativeLayout topLayout = new RelativeLayout(this);
        setContentView(topLayout);
        
        LinearLayout preViewLayout = (LinearLayout) myInflate.inflate(R.layout.activity_main, null);
        layoutParam = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        topLayout.addView(preViewLayout, layoutParam);

        /* add control button: start and stop */
        btnRecorderControl = (Button) findViewById(R.id.button1);
        btnRecorderControl.setText("Start");
        btnRecorderControl.setOnClickListener(this);

        /* add camera view */
        int display_width_d = (int) (1.0 * bg_screen_width * screenWidth / bg_width);
        int display_height_d = (int) (1.0 * bg_screen_height * screenHeight / bg_height);
        int prev_rw, prev_rh;
        if (1.0 * display_width_d / display_height_d > 1.0 * live_width / live_height) {
            prev_rh = display_height_d;
            prev_rw = (int) (1.0 * display_height_d * live_width / live_height);
        } else {
            prev_rw = display_width_d;
            prev_rh = (int) (1.0 * display_width_d * live_height / live_width);
        }
        layoutParam = new RelativeLayout.LayoutParams(prev_rw, prev_rh);
        layoutParam.topMargin = (int) (1.0 * bg_screen_by * screenHeight / bg_height);
        layoutParam.leftMargin = (int) (1.0 * bg_screen_bx * screenWidth / bg_width);

        
        
        
        cameraDevice = Camera.open();
        Log.i(LOG_TAG, "cameara open");
        cameraView = new CameraView(this, cameraDevice);
        
        
        topLayout.addView(cameraView, layoutParam);
        Log.i(LOG_TAG, "cameara preview start: OK");
    }

    //---------------------------------------
    // initialize ffmpeg_recorder
    //---------------------------------------
    private void initRecorder() {

    	if (cameraDevice!=null){
    	//Camera cam = Camera.open();
    	Size s = UtilityTools.SetCameraPreviewSize(cameraDevice);
    	imageWidth = s.width;
    	imageHeight = s.height;
    	//cam.release();
    	//cam = null;
    	}
    	
		Log.w(LOG_TAG, "init recorder");

		if (yuvIplimage == null) {

			yuvIplimage = IplImage.create(imageWidth, imageHeight,
					com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U, 2);
			Log.i(LOG_TAG, "create yuvIplimage");
		}

		Log.i(LOG_TAG, "ffmpeg_url: " + ffmpeg_link);

		// 文件位置
		curRawVideoName = FileIOUtils.getRecordFileName();
		curRawVideoName = FileIOUtils.getRecordFileNameMP4();
		String recordFile = recordPath + "/" + curRawVideoName;

		recorder = new FFmpegFrameRecorder(recordFile, imageWidth, imageHeight,
				1);
		recorder.setFormat("mp4");
		recorder.setVideoQuality(1);
		recorder.setSampleRate(sampleAudioRateInHz);
		// Set in the surface changed method
		recorder.setFrameRate(frameRate);

		// recorder.set

		Log.i(LOG_TAG, "recorder initialize success");

		audioRecordRunnable = new AudioRecordRunnable();
		audioThread = new Thread(audioRecordRunnable);
		runAudioThread = true;
    }

    public void startRecording() {

        try {
        	
        	if (recorder == null)
        		initRecorder();
        	
            recorder.start();
            startTime = System.currentTimeMillis();
            recording = true;
            audioThread.start();
            
            
            //设置定时器
            Timer t = new Timer();
            t.schedule(new TimerTask() {
				
				@Override
				public void run() {
					Message msg = mHander.obtainMessage();
					msg.arg1 = MSG_RECORD_END;
					msg.sendToTarget();
				}
			}, 1000 * 10);

        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {

        runAudioThread = false;
        try {
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioRecordRunnable = null;
        audioThread = null;

        if (recorder != null && recording) {
            recording = false;
            Log.v(LOG_TAG,"Finishing recording, calling stop and release on recorder");
            try {
                recorder.stop();
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            recorder = null;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (recording) {
                stopRecording();
            }

            finish();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz, 
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz, 
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = new short[bufferSize];

            Log.d(LOG_TAG, "audioRecord.startRecording()");
            audioRecord.startRecording();

            /* ffmpeg_audio encoding loop */
            while (runAudioThread) {
                //Log.v(LOG_TAG,"recording? " + recording);
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
                if (bufferReadResult > 0) {
                    //Log.v(LOG_TAG,"bufferReadResult: " + bufferReadResult);
                    // If "recording" isn't true when start this thread, it never get's set according to this if statement...!!!
                    // Why?  Good question...
                    if (recording) {
                        try {
                        	//记录音频
                            recorder.record(ShortBuffer.wrap(audioData, 0, bufferReadResult));
                            //Log.v(LOG_TAG,"recording " + 1024*i + " to " + 1024*i+1024);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(LOG_TAG,e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.v(LOG_TAG,"AudioThread Finished, release audioRecord");

            /* encoding finish, release recorder */
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v(LOG_TAG,"audioRecord released");
            }
        }
    }

    //---------------------------------------------
    // camera thread, gets and encodes video data
    //---------------------------------------------
    class CameraView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraView(Context context, Camera camera) {
            super(context);
            Log.w("camera","camera view");
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(CameraView.this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCamera.setPreviewCallback(CameraView.this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                stopPreview();
                //mHolder = getHolder();
               //mHolder.addCallback(CameraView.this);
              //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
              
                mCamera.setPreviewCallback(CameraView.this);
                
                mCamera.setPreviewDisplay(holder);
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.v(LOG_TAG,"Setting imageWidth: " + imageWidth + " imageHeight: " + imageHeight + " frameRate: " + frameRate);
            Log.v(LOG_TAG, "###" + width + "  " + height);
            
            try {
            Camera.Parameters camParams = mCamera.getParameters();
            camParams.setPreviewSize(960, 720);
            //camParams.setPreviewSize(960, 720);
            camParams.setPictureSize(imageWidth, imageHeight);
            Log.v(LOG_TAG,"Preview Framerate: " + camParams.getPreviewFrameRate());
            //camParams.setPictureSize(width, height);
            camParams.setPreviewFrameRate(frameRate);
            mCamera.setParameters(camParams);
            
            camParams = mCamera.getParameters();
            Size s = camParams.getPreviewSize();
            
            Log.v(LOG_TAG, "#######" + s.width + "  " + s.height);
            } catch (Exception e)
            {
            	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            	Camera.Parameters camParams = mCamera.getParameters();
                //camParams.setPreviewSize(imageWidth, imageHeight);
            	
                camParams.setPreviewSize(imageWidth, imageHeight);
                camParams.setPictureSize(imageWidth, imageHeight);
                Log.v(LOG_TAG,"Preview Framerate: " + camParams.getPreviewFrameRate());
                //camParams.setPictureSize(width, height);
                camParams.setPreviewFrameRate(frameRate);
                mCamera.setParameters(camParams);
                
                camParams = mCamera.getParameters();
                Size s = camParams.getPreviewSize();
                
                Log.v(LOG_TAG, "#######" + s.width + "  " + s.height);
            }
            
            
            startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                //mHolder.addCallback(null); //添加这句 再次恢复会崩溃，可以再activity 结束时候调用
                mCamera.setPreviewCallback(null);
            } catch (RuntimeException e) {
                // The camera has probably just been released, ignore.
            }
        }

        public void startPreview() {
            if (!isPreviewOn && mCamera != null) {
                isPreviewOn = true;
                mCamera.startPreview();
            }
        }

        public void stopPreview() {
            if (isPreviewOn && mCamera != null) {
                isPreviewOn = false;
                mCamera.stopPreview();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
        	
        	//System.out.println(data.length);
            /* get video data */
        	//Log.v(LOG_TAG, ""+data.length);
            if (yuvIplimage != null && recording) {
            	System.out.println(data.length);
            	//Log.v(LOG_TAG, "before getByteBuffer");
                yuvIplimage.getByteBuffer().put(data); //内存必须够用
                //Log.v(LOG_TAG, "end getByteBuffer");
        
                //Log.v(LOG_TAG,"Writing Frame");
                try {
                    long t = 1000 * (System.currentTimeMillis() - startTime);
                    if (t > recorder.getTimestamp()) {
                        recorder.setTimestamp(t);
                    }
                    recorder.record(yuvIplimage); //记录当前帧
                } catch (FFmpegFrameRecorder.Exception e) {
                    Log.v(LOG_TAG,e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!recording) {
            startRecording();
            Log.w(LOG_TAG, "Start Button Pushed");
            btnRecorderControl.setText("10s停止");
            
        } else {
            // This will trigger the audio recording loop to stop and then set isRecorderStart = false;
            stopRecording();
            Log.w(LOG_TAG, "Stop Button Pushed");
            btnRecorderControl.setText("录制");
        }
    }
    
    
    
//	void test() {
//		// 获取摄像头的所有支持的分辨率
//		List<Camera.Size> resolutionList = Util.getResolutionList(mCamera);
//		if (resolutionList != null && resolutionList.size() > 0) {
//			Collections.sort(resolutionList, new Util.ResolutionComparator());
//			Camera.Size previewSize = null;
//			if (defaultScreenResolution == -1) {
//				boolean hasSize = false;
//				// 如果摄像头支持640*480，那么强制设为640*480
//				for (int i = 0; i < resolutionList.size(); i++) {
//					Size size = resolutionList.get(i);
//					if (size != null && size.width == 640 && size.height == 480) {
//						previewSize = size;
//						hasSize = true;
//						break;
//					}
//				}
//				// 如果不支持设为中间的那个
//				if (!hasSize) {
//					int mediumResolution = resolutionList.size() / 2;
//					if (mediumResolution >= resolutionList.size())
//						mediumResolution = resolutionList.size() - 1;
//					previewSize = resolutionList.get(mediumResolution);
//				}
//			} else {
//				if (defaultScreenResolution >= resolutionList.size())
//					defaultScreenResolution = resolutionList.size() - 1;
//				previewSize = resolutionList.get(defaultScreenResolution);
//			}
//			// 获取计算过的摄像头分辨率
//			if (previewSize != null) {
//				previewWidth = previewSize.width;
//				previewHeight = previewSize.height;
//				cameraParameters.setPreviewSize(previewWidth, previewHeight);
//				if (videoRecorder != null) {
//					videoRecorder.setImageWidth(previewWidth);
//					videoRecorder.setImageHeight(previewHeight);
//				}
//
//			}
//		}
//
//	}
}
