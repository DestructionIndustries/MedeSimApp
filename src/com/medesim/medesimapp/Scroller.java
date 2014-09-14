package com.medesim.medesimapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.medesim.medesimapp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Scroller extends ScrollView {

	private Context context;
	private LinearLayout lL;
	private ImageView playV;
	private TagsView tV;

	public Scroller(Context context) {
		super(context);

		this.context = context;		
		
		setBackgroundColor(Color.argb(255,179,179,179));

		lL = new LinearLayout(context);
		lL.setOrientation(LinearLayout.VERTICAL);
		lL.setGravity(Gravity.CENTER);

		addView(lL);

		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();

		// TODO Auto-generated constructor stub
	}

	public void addTag(final long tg, int id) {

		AutoFitTextView t = new AutoFitTextView(context);

		t.setText(String.format("%02d",
				(int) (Math.floor((double) ((tg / 3600000) % 60))))
				+ " : "
				+ String.format("%02d",
						(int) (Math.floor((double) ((tg / 60000) % 60))))
				+ " : "
				+ (String.format("%02d",
						(int) Math.floor((double) (tg / 1000) % 60))));
		t.setTextColor(Color.BLACK);
		t.setGravity(Gravity.CENTER);

		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0,
				MainActivity.HEIGHT/20);
		textParams.weight = 2;


		ImageView tagV = new ImageView(context);
		LinearLayout.LayoutParams iVParams = new LinearLayout.LayoutParams(0,
				MainActivity.HEIGHT/25);
		iVParams.weight = 1;
	
		
		playV = new ImageView(context);
		LinearLayout.LayoutParams playVParams = new LinearLayout.LayoutParams(
				0, MainActivity.HEIGHT/20);
		playVParams.weight = 1;
		
		Bitmap pIBM = BitmapFactory.decodeResource(getResources(), R.drawable.play_icon);
		playV.setImageBitmap(pIBM);
	
		playV.setOnTouchListener(new OnTouchListener() {
			float lastX=0;
			float lastY=0;
			
		
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub
				
				if(TagsView.stopped){
					
					switch (e.getAction()) {
					case MotionEvent.ACTION_DOWN:
						
						v.setBackgroundColor(Color.WHITE);
						lastX = e.getX();
						lastY = e.getY();
						
						tV.pauseChrono();
						
						break;
						
					case MotionEvent.ACTION_MOVE:
						
						if(Math.abs((double)(e.getX() - lastX)) >=10 ||
						Math.abs((double)(e.getY() - lastY)) >=10){
						
							v.setBackgroundColor(Color.argb(255, 242, 242, 242));
						
						}
						
						
						break;

					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.argb(255, 242, 242, 242));
						
						tV.setSBProg(tg, TagsView.FROM_SCRO_PLAY);
						
					//	HttpPreparer.prepareHTTPPost(tg, context);
						
						
						break;
						
					default:
						break;
					}		
					
				}
				return true;
				
			}
		});
			

		switch (id) {
		case 0:
			tagV.setBackgroundColor(Color.argb(255, 118, 167, 250));
			break;
		case 1:
			tagV.setBackgroundColor(Color.argb(255, 229, 115, 104));
			break;
		case 2:
			tagV.setBackgroundColor(Color.argb(255, 251, 203, 67));
			break;

		default:
			break;
		}

		LinearLayout horL = new LinearLayout(context);
		horL.setOrientation(LinearLayout.HORIZONTAL);
		horL.setGravity(Gravity.CENTER);
		
		horL.addView(tagV, iVParams);
		horL.addView(t, textParams);
		horL.addView(playV, playVParams);
		

		LinearLayout.LayoutParams horLParams = new LinearLayout.LayoutParams(
				19*MainActivity.WIDTH/20, MainActivity.HEIGHT/13);

		horLParams.setMargins(0, MainActivity.HEIGHT/400, 0, MainActivity.HEIGHT/400);
		horL.setBackgroundColor(Color.argb(255, 242, 242, 242));

		TranslateAnimation tA = new TranslateAnimation(-MainActivity.WIDTH, 0, 0, 0);
		tA.setDuration(350);
		horL.setAnimation(tA);
		
		
		horL.setTag(R.string.horl_tag, tg);
		
		int ind = 0;
		
		
	//	System.out.println(lL.getChildCount());
		
		if (lL.getChildCount() != 0){
			
			for(int i = 0; i < lL.getChildCount(); i++){
				
				if( ((Long) lL.getChildAt(i).getTag(R.string.horl_tag)).longValue() < tg ){
				//	System.out.println(i);
					ind = i+1;
				}
				
			}
				
		}
		
		horL.setOnTouchListener(new OnTouchListener() {
			float lastX=0;
			float lastY=0;
			
		
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub
				
				if(TagsView.stopped){
					
					switch (e.getAction()) {
					case MotionEvent.ACTION_DOWN:
						
						requestDisallowInterceptTouchEvent(true);
						
						v.setBackgroundColor(Color.WHITE);
						lastX = e.getX();
						lastY = e.getY();
						
						tV.pauseChrono();
						
						break;
						
					case MotionEvent.ACTION_MOVE:
						
						if(Math.abs((double)(e.getX() - lastX)) >=3 ||
						Math.abs((double)(e.getY() - lastY)) >=3){
					
							v.setBackgroundColor(Color.argb(255, 242, 242, 242));
							requestDisallowInterceptTouchEvent(false);
						}
						
						
						break;

					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.argb(255, 242, 242, 242));
						
						tV.setSBProg(tg, TagsView.FROM_SCRO_TAG);
						
				//		HttpPreparer.prepareHTTPPost(tg, context);
						
						
						break;
						
					default:
						break;
					}		
					
				}
				return true;
				
			}
		});

		lL.addView(horL, ind, horLParams);
		
	//	lL.addView(horL, horLParams);
	

		postDelayed(new Runnable() // waits until the system adds the view
									// before forcing scroll down
				{
					public void run() {
						fullScroll(View.FOCUS_DOWN);
					}
				}, 150);

	}
	

	
	/*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 // When DeviceListActivity returns with a device to connect
		
		
		switch (requestCode) {
				
		case MainActivity.REQUEST_OPTIONS_MENU:
			
			if (resultCode == Activity.RESULT_OK) {
				
				HttpPreparer.post = new HttpPost(data.getExtras().getString(MainActivity.SERV_URL));

			}
			break;

		default:
			break;
			}

	}	*/

	public void setTagsView (TagsView tv){
		
		tV = tv;
		
	}
	

}
