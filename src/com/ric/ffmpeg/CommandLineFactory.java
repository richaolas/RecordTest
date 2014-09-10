package com.ric.ffmpeg;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class CommandLineFactory {

	public static List<String> spliteAV(String avPath, String avName, String destPath)
	{			
		String ffmpegInstallPath = FFmpegTools.getmFfmpegInstallPath();
		
		List<String> cmd = new LinkedList<String>();
		cmd.add(ffmpegInstallPath);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(avPath+ File.separator + avName);
		cmd.add("-vcodec");
		cmd.add("copy");
		cmd.add("-an");
		cmd.add(destPath);
		
		return cmd;
	}
	
	public static List<String> mixVideoAndAudio(String videoPath, String videoName, String audioPath, String audioName, String destPath)
	{

		String ffmpegInstallPath = FFmpegTools.getmFfmpegInstallPath();
		List<String> cmd = new LinkedList<String>();
		cmd.add(ffmpegInstallPath);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(videoPath+ File.separator + videoName);
		cmd.add("-i");
		cmd.add(audioPath+ File.separator + audioName);
		cmd.add("-vcodec");
		cmd.add("copy");
		cmd.add("-acodec");
		cmd.add("copy");
		//-absf aac_adtstoasc
		cmd.add("-absf");
		cmd.add("aac_adtstoasc");
		cmd.add(destPath);	
		
		return cmd;
	}
	
}
