package com.example.recordtest;

import android.hardware.Camera;
import android.hardware.Camera.Size;

public class UtilityTools {
	public static Size SetCameraPreviewSize(Camera camera)
	{
		try {
            Camera.Parameters camParams = camera.getParameters();
            camParams.setPreviewSize(960, 720);
            camera.setParameters(camParams);

            } catch (Exception e1)
            {
            	try{
	            	Camera.Parameters camParams = camera.getParameters();
	                camParams.setPreviewSize(640, 480);
	                camera.setParameters(camParams);
            	}catch (Exception e2)
            	{
            		try{
            			Camera.Parameters camParams = camera.getParameters();
    	                camParams.setPreviewSize(320, 240);
    	                camera.setParameters(camParams);
            		}catch (Exception e3)
            		{
            			
            		}
            	}
                
            }		
		
		Camera.Parameters camParams = camera.getParameters();
        return camParams.getPreviewSize();
	}
}
