package com.example.recordtest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.ric.ffmpeg.CommandLineFactory;
import com.ric.ffmpeg.FFmpegTools;
import com.ric.ffmpeg.FileIOUtils;
import com.ric.ffmpeg.SDCardUtility;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

	private static final String TAG = "CAMERA_TUTORIAL";
	private FFmpegTools mFFmpegTools = null;

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	private boolean previewRunning;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//install ffmpeg
		mFFmpegTools = new FFmpegTools(this);
		mFFmpegTools.installFfmpeg();
		
		//set record path
		sdcardPath = SDCardUtility.createSDCardDir(this.getPackageName(), this);
		recordPath = sdcardPath; 
	    SDCardUtility.copyAssetsDir(this, "music", sdcardPath); 
		
		//surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        Button startBtn = (Button)this.findViewById(R.id.button1);
        startBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Toast.makeText(MainActivity.this, "Will auto stop at 10s.", Toast.LENGTH_LONG).show();
				MainActivity.this.startRecording();
				((Button)arg0).setEnabled(false);
			}
        });
        
        Button startEnd = (Button)this.findViewById(R.id.button2);
        startEnd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				stopRecording();
				Toast.makeText(MainActivity.this, "Will auto stop at 10s.", Toast.LENGTH_LONG).show();
			}
		});
       // startEnd.setEnabled(false);
        
        Button genBtn = (Button)this.findViewById(R.id.button3);
        genBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				List<String> cmdLine1 = CommandLineFactory.spliteAV(recordPath, curRawVideoName, recordPath + File.separator + "tmp.3gp");
				
				MainActivity.this.mFFmpegTools.execCommandLine(cmdLine1);
				
				List<String> cmdLine2 = CommandLineFactory.mixVideoAndAudio(recordPath, "tmp.3gp", 
						recordPath + File.separator + "music", "audio_001.aac", 
						recordPath + File.separator + "result.3gp");
				
				MainActivity.this.mFFmpegTools.execCommandLine(cmdLine2);
			}
		}); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (previewRunning){
	         camera.stopPreview();
	    }
		
	   // Camera.Parameters p = camera.getParameters();
	   // p.setPreviewSize(width, height);
	   // camera.setParameters(p);

	    try {
	        camera.setPreviewDisplay(holder);
	        camera.startPreview();
	        previewRunning = true;
	    }
	    catch (IOException e) {
	        Log.e(TAG,e.getMessage());
	        e.printStackTrace();
	        }
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		camera = Camera.open();
        if (camera != null){
            Camera.Parameters params = camera.getParameters();
            camera.setParameters(params);
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		 camera.stopPreview();
         previewRunning = false;
         camera.release();
	}
	
	
	// MediaRecorder context
	private MediaRecorder mediaRecorder;
	private final int maxDurationInMs = (1000*10);

	private File tempFile = null;
	private String recordPath = null; 
	private String curRawVideoName = null;
	private String sdcardPath = null;
	
	public boolean startRecording() {
		try {

			camera.unlock();

			mediaRecorder = new MediaRecorder();
			
			mediaRecorder.setOnErrorListener(new android.media.MediaRecorder.OnErrorListener() {
		        public void onError(MediaRecorder mediarecorder1, int k, int i1)
		        {
		            Log.e(TAG,String.format("Media Recorder error: k=%d, i1=%d", k, i1));
		        }

		    });

			mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {  

	            public void onInfo(MediaRecorder mr, int what, int extra) {  

	                if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {  

	                    System.out.println("已经达到最长录制时间");  

	                    if (mediaRecorder!=null) {  

	                    	mediaRecorder.stop();  
	                    	mediaRecorder.release();  
	                    	mediaRecorder=null;  
	                    	
	                    	camera.lock();
	                    	
	                    	Toast.makeText(MainActivity.this, "Record completed!", Toast.LENGTH_LONG).show();
	                    	
	                    	((Button)MainActivity.this.findViewById(R.id.button1)).setEnabled(true);
	                    }  
	                } else {
	                	
	                	 // do nothing
	                }  
	                
	            }  
	        });  

			mediaRecorder.setCamera(camera);
			
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); 
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //OK

			//mediaRecorder.setMaxDuration(maxDurationInMs);
			//mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
			
			CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			
			mediaRecorder.setOutputFormat(mProfile.fileFormat);
	    	mediaRecorder.setAudioEncoder(mProfile.audioCodec);
	    	mediaRecorder.setVideoEncoder(mProfile.videoCodec);
	    	//mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/001.3gp");
	    	//mediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
	    	mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
	    	mediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
	    	mediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
	    	mediaRecorder.setAudioChannels(mProfile.audioChannels);
	    	mediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);

			curRawVideoName = FileIOUtils.getRecordFileName();
			String recordFile = recordPath + "/" + curRawVideoName;
			
			tempFile = new File( recordFile );
			mediaRecorder.setOutputFile(tempFile.getPath());
			
			 mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

			
			Log.i(TAG, recordFile);

			try {
				
				mediaRecorder.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mediaRecorder.start();

			return true;
			
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		} 
		
		
	}

	// 停止拍摄，则：
	public void stopRecording() {
		mediaRecorder.stop();
		camera.lock();
	}
	

}
