package com.medesim.medesimapp;

import java.text.DateFormat;
import java.util.Date;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends Activity {

	/** Called when the activity is first created. */
	private static final String TAG = "MainActivity";
	private long bckPrssLapse = 0;
	private long resetPrssLapse = 0;
	private RelativeLayout relL;
	private TagsView tV;
	private Scroller sV;
	public static int WIDTH, HEIGHT;
	public static final int REQUEST_OPTIONS_MENU = 2;
	public static final String SERV_FILE = "post_uri";
	public static final String SERV_URL = "servurl";
//	public static final String AUTO_SAVE = "autoSave";
//	public static final String AUTO_SAVE_RECORDEDL = "autoSaveRecordedLength";
	
	public static final String [] SAVE_SLOT = {"SS0", "SS1", "SS2", "SS3"};
	public static final String [] SAVE_SLOT_RECL = {"SSL0", "SSL1", "SSL2", "SSL3"};
	public static final String [] SAVE_SLOT_DATE = {"SSD0", "SSD1", "SSD2", "SSD3"};
	public static final String NULL_POST = "no post saved";
	public static Menu aBMenu;
	
	private SharedPreferences shPServ;//, shPTag ;
	public static Context cont;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		WIDTH = getWindowManager().getDefaultDisplay().getWidth();
		HEIGHT = getWindowManager().getDefaultDisplay().getHeight();
		
		shPServ = getSharedPreferences(SERV_FILE, MODE_PRIVATE);
	//	shPTag = getSharedPreferences(AUTO_SAVE, MODE_PRIVATE);
		
		int ABH = getActionBarHeight() + getStatusBarHeight();

	//	Log.d(TAG, "status bar = " +  Integer.toString(getStatusBarHeight()));
		
		cont = this;
		
		relL = new RelativeLayout(this);
		
		sV = new Scroller(this);
		tV = new TagsView(this,sV);
		sV.setTagsView(tV);
		
		RelativeLayout shadow = new RelativeLayout(this);
		
		GradientDrawable gD = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {Color.argb(0, 0, 0, 0),Color.argb(128, 0, 0, 0)});
		shadow.setBackgroundDrawable(gD);
		
		RelativeLayout.LayoutParams shadLP = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, MainActivity.HEIGHT / 100);
		shadLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		shadLP.setMargins(0, 3 * HEIGHT / 4 - shadLP.height - ABH, 0, 0);

		RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, 3 * HEIGHT / 4 - ABH);

		RelativeLayout.LayoutParams tagParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		tagParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		tagParams.setMargins(0, 3 * HEIGHT / 4 - ABH, 0, 0);
		

		relL.addView(sV, scrollParams);
		relL.addView(shadow, shadLP);
		relL.addView(tV, tagParams);

		setContentView(relL);
		
		setSavedParams();
		
		
		

	}
	
	

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater infl = getMenuInflater();
		infl.inflate(R.menu.action_bar_menu, menu);
		
		
		String extUSRI = shPServ.getString(SERV_URL, NULL_POST);
		
		
		
		aBMenu = menu;
		if(extUSRI.equals(NULL_POST)){
			
		//	menu.getItem(0).setTitle("Http://");
			
			aBMenu.findItem( R.id.url_icon).setTitle("Http://");

			
			
		}else{
			
			aBMenu.findItem( R.id.url_icon).setTitle("  " + extUSRI);
		}

		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case R.id.url_icon:
			
		//	Toast.makeText(this, "config", Toast.LENGTH_SHORT).show();
			
			Intent optionsMenu = new Intent ("android.intent.action.OPTIONMENU");
			
			startActivityForResult(optionsMenu, REQUEST_OPTIONS_MENU);
		
			break;
		
			
		case R.id.reset:
			
			reset();
			
			break;
			
		case R.id.save_icon:
						
			if (tV.getDoubleStarted()) {
				PopupMenu popup = new PopupMenu(this,
						findViewById(R.id.save_icon));
				popup.getMenuInflater().inflate(R.menu.pop_up_menu,
						popup.getMenu());
				for (int i = 0; i < popup.getMenu().size(); i++) {

					String d = shPServ.getString(SAVE_SLOT_DATE[i+1], NULL_POST);
					if (!d.equals(NULL_POST)) {
						popup.getMenu().getItem(i).setTitle(d);

					}
				}
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {


						tV.saveState(item.getOrder()+1);

						return true;
					}
				});
				popup.show();
			}
			break;
			
			
		case R.id.load_icon:
			
			if(tV.getStarted() && !tV.getDoubleStarted()){
				
				
				PopupMenu popup = new PopupMenu(this,
						findViewById(R.id.load_icon));
				popup.getMenuInflater().inflate(R.menu.pop_up_menu_load,
						popup.getMenu());
				
				
				if (!shPServ.getString(SAVE_SLOT[0], NULL_POST).equals(NULL_POST)) {
					popup.getMenu().getItem(0).setTitle("Dernière Sauvegarde Automatique");

				}
				
				
				for (int i = 1; i < popup.getMenu().size(); i++) {

					String d = shPServ.getString(SAVE_SLOT_DATE[i], NULL_POST);
					if (!d.equals(NULL_POST)) {
						popup.getMenu().getItem(i).setTitle(d);

					}
				}
				
				
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						
						String tags = shPServ.getString(SAVE_SLOT[item.getOrder()], NULL_POST);
						
						if(tags.equals(NULL_POST)){
							
						//	menu.getItem(0).setTitle("Http://");
							
						//	aBMenu.findItem( R.id.url_icon).setTitle("Http://");
						
						}else{

							
							JSONArray jA;
							try {
								jA = new JSONArray(tags);
								
								tV.setTagArr(jA);
								
								for (int i = 0; i < jA.length(); i ++){
									
									JSONObject t = jA.getJSONObject(i);
									
									
									sV.addTag(t.getLong("time"), t.getInt("id"));
									
								}
								
								
								tV.jumpToPlayback(shPServ.getString(SAVE_SLOT_RECL[item.getOrder()], NULL_POST));
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
							
						//	sV.addTag(tg, id);
						}
						
					//	break;
						

						//tV.saveState(item.getOrder());

						return true;
					}
				});
				popup.show();
				
				
				
				
			}
			
			
		}

		return true;
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 // When DeviceListActivity returns with a device to connect
		
		
		switch (requestCode) {
				
		case REQUEST_OPTIONS_MENU:
			
			if (resultCode == Activity.RESULT_OK) {
					tV.setPostURI(data.getExtras().getString("servurl"));
					
					if(aBMenu != null){
						
						aBMenu.findItem(R.id.url_icon).setTitle(data.getExtras().getString("servurl"));
						
					}
					
			//	HttpPreparer.post = new HttpPost(data.getExtras().getString("servurl"));
			//	HttpPreparer.sURL = data.getExtras().getString("servurl");
			}
			break;

		default:
			break;
			}

	}
	
	public void reset(){
		
		
		if (System.currentTimeMillis() - resetPrssLapse > 3000) {

			Toast.makeText(
					this,
					"Appuyez sur l'icone 'Recommencer' à nouveau pour redémarrer l'application",
					Toast.LENGTH_SHORT).show();

			resetPrssLapse = System.currentTimeMillis();

		} else {	
			
			tV.setChrono(false);
			
			Intent myself = getIntent();
			finish();
			startActivity(myself);
		}
		
		
		
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	
		
		if (System.currentTimeMillis() - bckPrssLapse > 3000) {

			Toast.makeText(
					this,
					"Appuyez sur la touche 'retour' à nouveau pour quitter l'application",
					Toast.LENGTH_SHORT).show();

			bckPrssLapse = System.currentTimeMillis();

		} else {	
			
			
			tV.setChrono(false);
			finish();
		}

	}

	protected void onSizeChanged (int w, int h, int oldw, int oldh){
		
		System.out.println("oldh = " + oldh + "new h = " + h);
	}
	
	private void setSavedParams(){
		
		String extUSRI = shPServ.getString(SERV_URL, NULL_POST);
		
		if(extUSRI.equals(NULL_POST)){
			
			Log.d(TAG, "nothing");
			
			Intent optionsMenu = new Intent ("android.intent.action.OPTIONMENU");
		
			startActivityForResult(optionsMenu, REQUEST_OPTIONS_MENU);
			
		}else{
			
			Log.d(TAG, extUSRI);
	
			tV.setPostURI(extUSRI);
		//	HttpPreparer.post = new HttpPost(extUSRI);
		//	HttpPreparer.sURL = extUSRI;
		}
			
	}
	
	public int getActionBarHeight() {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
	//	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
					true))
				actionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, getResources().getDisplayMetrics());


		return actionBarHeight;
	}
	
	public int getStatusBarHeight() {
	      int result = 0;
	      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = getResources().getDimensionPixelSize(resourceId);
	      }
	      return result;
	}
	
	
}
