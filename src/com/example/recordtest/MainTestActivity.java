package com.example.recordtest;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainTestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main_test);
		
		
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        
//        RelativeLayout.LayoutParams layoutParam = null; 
//        LayoutInflater myInflate = null; 
//        myInflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        
//       // RelativeLayout topLayout = new RelativeLayout(this);
//      //  setContentView(topLayout);
//        
//        RelativeLayout rootViewLayout = (RelativeLayout) myInflate.inflate(R.layout.activity_main_test, null);
//        layoutParam = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
//        
//        Button btn = new Button(this);
//        btn.setText("Hello Button");
//        rootViewLayout.addView(btn, layoutParam);
        
        //topLayout.addView(preViewLayout, layoutParam);
        
      //  RelativeLayout rl = new RelativeLayout(this);  
              
        RelativeLayout.LayoutParams layoutParam = null;
      LayoutInflater myInflate = null; 
      myInflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout rootViewLayout = (RelativeLayout) myInflate.inflate(R.layout.activity_main_test, null);
        
               Button btn1 = new Button(this);  
              btn1.setText("----------------------");  
              btn1.setId(1);  
                 
               RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
            		   ViewGroup.LayoutParams.WRAP_CONTENT, 
            		   ViewGroup.LayoutParams.WRAP_CONTENT);  
              lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);  
              lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);  
              // btn1 位于父 View 的顶部，在父 View 中水平居中  
              rootViewLayout.addView(btn1, lp1 );  
              
              int l = 20,t=20,r = 0,b=20;
              int w = 640, h = 480;
              
              //screenHeight -
        
              

              setContentView(rootViewLayout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_test, menu);
		return true;
	}

}
