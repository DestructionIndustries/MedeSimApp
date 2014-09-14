package com.medesim.medesimapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

@SuppressLint("ViewConstructor")
public class TagsView extends LinearLayout {

	private Context context;
	private boolean started = false;
	private AutoFitTextView startText;
	private LinearLayout panel, controls;
	private Scroller sV;
	private ImageView button[] = new ImageView[3];
	private LinearLayout.LayoutParams bLP;
	private boolean paused = false;
	private boolean sBPaused = false;
	private Chrono ch;
	private long tg, playProg, recordedL, accPlayProg;
	public SeekBar sB;
	public static boolean stopped = false;
	private ImageView pauseIV, stopIV;
	private Bitmap playIBM, pauseIBM;
	private final int panelColor = Color.argb(255, 230, 230, 230);
	private int or = 0;
	private int second = 0;
	public static final int FROM_CHRO = 1;
	public static final int FROM_SCRO_PLAY = 2;
	public static final int FROM_SCRO_TAG = 3;

	private final String TAG = "TagsView";

	public static final String RES_PLAYBACK = "resumePB";
	public static final String PAUSE_PLAYBACK = "pausePB";
	public static final String RES_REC = "resumeRec";
	public static final String PAUSE_REC = "pauseRec";
	public static final String STOP_REC = "stopRec";
	public static final String START_REC = "startRec";
	public static final String TEST = "test";
	private HttpPost post;
	private boolean jumped = false;
	private boolean doubleStarted = false;

	private JSONArray tagArr = new JSONArray();
	private SharedPreferences shP;
	private SharedPreferences.Editor ed;

//hey juan, suck it

	public TagsView(Context context) {
		super(context);
	}

	public TagsView(Context context, Scroller sV) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.sV = sV;

		
		
		shP = context.getSharedPreferences(MainActivity.SERV_FILE, MainActivity.MODE_PRIVATE);
		ed = shP.edit();
		
		or = 0;
		tg = 0;
		second = 0;
		playProg = 0;
		recordedL = 0;
		accPlayProg = 0;
		jumped = false;
		
		stopped = false;

		if (sB != null) sB = null;
			
		if (post != null)post = null;
			
		if (ch != null)ch = null;

		setBackgroundColor(panelColor);
		setOrientation(LinearLayout.VERTICAL);

		controls = new LinearLayout(context);

		controls.setOrientation(LinearLayout.HORIZONTAL);
		controls.setGravity(Gravity.CENTER);

		panel = new LinearLayout(context);
		LinearLayout.LayoutParams panelLP = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		panel.setOrientation(LinearLayout.HORIZONTAL);
		panel.setGravity(Gravity.CENTER);

		panel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!started) {
					prepareHTTPPost(TEST, 0);

					/*panel.removeAllViews();
					started = true;

					addButtons();
					addControls();*/

				}else{
					
					
					doubleStarted = true;
					
					LinearLayout.LayoutParams controlLP = new LayoutParams(
					LayoutParams.MATCH_PARENT, MainActivity.HEIGHT / 15);
			
					LinearLayout.LayoutParams panelLP = new LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					
					panel.removeAllViews();
					
					removeAllViews();
					
					addView(controls, controlLP);
					addView(panel, panelLP);
					
					addButtons();
					addControls();
					
					prepareHTTPPost(START_REC, 0);
					
					
				}

			}

		});

		startText = new AutoFitTextView(context);
		startText.setText("Connecter avec le serveur");
		startText.setGravity(Gravity.CENTER);

		panel.addView(startText);

	//	addView(controls, controlLP);
		addView(panel, panelLP);

	}

	private void addControls() {

		ch = new Chrono(context, this, true);
		ch.gettV().setTextColor(Color.BLACK);
		ch.gettV().setVisibility(VISIBLE);
		ch.gettV().setGravity(Gravity.CENTER);
		ch.gettV().setTextSize(MainActivity.WIDTH / 25);

		LinearLayout.LayoutParams chParams = new LinearLayout.LayoutParams(0,
				MainActivity.HEIGHT / 20);
		chParams.weight = 1;

		pauseIV = new ImageView(context);
		LinearLayout.LayoutParams pauseVParams = new LinearLayout.LayoutParams(
				0, MainActivity.HEIGHT / 20);
		pauseVParams.weight = 1;

		pauseIBM = BitmapFactory.decodeResource(getResources(),
				R.drawable.pause_icon);
		playIBM = BitmapFactory.decodeResource(getResources(),
				R.drawable.play_icon);
		final Bitmap stopIBM = BitmapFactory.decodeResource(getResources(),
				R.drawable.stop_icon);

		pauseIV.setImageBitmap(pauseIBM);

		pauseIV.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub

				switch (e.getAction()) {
				case MotionEvent.ACTION_DOWN:

					((ImageView) v).setBackgroundColor(Color.WHITE);

					break;

				case MotionEvent.ACTION_MOVE:

					((ImageView) v).setBackgroundColor(Color.TRANSPARENT);

					break;

				case MotionEvent.ACTION_UP:

					((ImageView) v).setBackgroundColor(Color.TRANSPARENT);

					if (paused) {
						pauseIV.setImageBitmap(pauseIBM);
						paused = false;
						ch.resumeChro();

						if (stopped) {
							prepareHTTPPost(RES_PLAYBACK, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(RES_PLAYBACK,
							// tg);

						} else {
							prepareHTTPPost(RES_REC, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(RES_REC,
							// tg);
						}

					} else {
						pauseIV.setImageBitmap(playIBM);
						paused = true;
						ch.pauseChro();

						if (stopped) {

							prepareHTTPPost(PAUSE_PLAYBACK, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(PAUSE_PLAYBACK,
							// tg);

						} else {

							prepareHTTPPost(PAUSE_REC, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(PAUSE_REC,
							// tg);
						}

					}

					break;

				default:
					break;
				}

				return true;
			}
		});

		stopIV = new ImageView(context);
		LinearLayout.LayoutParams stopVParams = new LinearLayout.LayoutParams(
				0, MainActivity.HEIGHT / 20);
		stopVParams.weight = 1;

		stopIV.setImageBitmap(stopIBM);

		stopIV.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub

				switch (e.getAction()) {
				case MotionEvent.ACTION_DOWN:

					((ImageView) v).setBackgroundColor(Color.WHITE);

					break;

				case MotionEvent.ACTION_MOVE:

					((ImageView) v).setBackgroundColor(Color.TRANSPARENT);

					break;

				case MotionEvent.ACTION_UP:

					((ImageView) v).setBackgroundColor(Color.TRANSPARENT);
					ch.pauseChro();
					ch.setQueuedSB(true);
					pauseIV.setImageBitmap(playIBM);
					paused = true;

					prepareHTTPPost(STOP_REC, tg);
					// new HttpPreparer(context).prepareHTTPPost(STOP_REC, tg);

					break;

				default:
					break;
				}

				return true;
			}
		});

		TranslateAnimation controlAnim = new TranslateAnimation(
				MainActivity.WIDTH, 0, 0, 0);
		controlAnim.setDuration(400);
		ch.gettV().setAnimation(controlAnim);
		pauseIV.setAnimation(controlAnim);
		stopIV.setAnimation(controlAnim);

		controls.addView(ch.gettV(), chParams);
		controls.addView(pauseIV, pauseVParams);
		controls.addView(stopIV, stopVParams);
	}

	private void addButtons() {
		// TODO Auto-generated method stub

		bLP = new LinearLayout.LayoutParams(0, 2 * panel.getHeight() / 4);
		bLP.weight = 1;

		final Bitmap pIBM[] = {
				BitmapFactory.decodeResource(getResources(),
						R.drawable.blue_button),
				BitmapFactory.decodeResource(getResources(),
						R.drawable.red_button),
				BitmapFactory.decodeResource(getResources(),
						R.drawable.yellow_button) };
		final Bitmap clickedBM = BitmapFactory.decodeResource(getResources(),
				R.drawable.gray_button);

		for (int i = 0; i < button.length; i++) {

			button[i] = new ImageView(context);

			TranslateAnimation tA = new TranslateAnimation(0, 0,
					panel.getHeight(), 0);
			tA.setDuration(350);
			button[i].setAnimation(tA);

			button[i].setImageBitmap(pIBM[i]);

			button[i].setId(i);

			button[i].setOnTouchListener(new OnTouchListener() {

				float lastX = 0;
				float lastY = 0;

				@Override
				public boolean onTouch(View v, MotionEvent e) {
					// TODO Auto-generated method stub

					switch (e.getAction()) {
					case MotionEvent.ACTION_DOWN:

						lastX = e.getX();
						lastY = e.getY();

						((ImageView) v).setImageBitmap(clickedBM);

						break;

					case MotionEvent.ACTION_MOVE:

						if (Math.abs((double) (e.getX() - lastX)) >= 5
								|| Math.abs((double) (e.getY() - lastY)) >= 5) {

							((ImageView) v).setImageBitmap(pIBM[v.getId()]);

						}

						break;

					case MotionEvent.ACTION_UP:

						((ImageView) v).setImageBitmap(pIBM[v.getId()]);
						
						JSONObject jTag = new JSONObject();
						
						try {
							jTag.put("time", Long.toString(ch.getTime()));
							jTag.put("id", v.getId());
							
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						
						tagArr.put(jTag);
						
						ed.putString(MainActivity.SAVE_SLOT[0], tagArr.toString());
						ed.commit();
						
						
						sV.addTag(ch.getTime(), v.getId());

						break;

					default:
						break;
					}

					return true;
				}
			});

			panel.addView(button[i], bLP);

		}

	}
	
	public void saveState(int itemN){
						
		ed.putString(MainActivity.SAVE_SLOT[itemN], tagArr.toString());
		
		
		
		if (!jumped) {
			ed.putString(MainActivity.SAVE_SLOT_RECL[itemN],
					Integer.toString(second + 1));
		}
		ed.putString(MainActivity.SAVE_SLOT_DATE[itemN], DateFormat.getDateTimeInstance().format(new Date()));
		
		ed.commit();
		
		
	}

	public void setChrono(boolean b) {

		if (b) {

			ch.resumeChro();
		} else {

			tg = 0;
			playProg = 0;
			if(ch!=null)ch.stopChro();
			
			
		}

	}

	public void pauseChrono() {

		if (!paused) {
			sBPaused = true;
			paused = true;
			ch.pauseChro();
		}

	}

	public void setTagArr(JSONArray jjaa){
		
		tagArr = jjaa;	
		
	}
	
	public void setSBProg(long p, int orig) {

		

		if (sB != null) { //Means stop button has been pressed
			or = orig;
			if (p * 100 / recordedL < 100) {
				sB.setProgress((int) (p * 100 / recordedL));

				if (or == FROM_SCRO_PLAY || or == FROM_SCRO_TAG) {
					tg = p;

					accPlayProg += playProg;
					if (!jumped) {
						ch.setProg((tg - recordedL) - accPlayProg);
					}else{
						ch.setProg((tg) - accPlayProg);
						
					}
					playProg = 0; // make sure the progress doesn't accumulate
									// when doing several progress changes
									// during pause.
					ch.setTime(tg);

					if (or == FROM_SCRO_PLAY
							|| (or == FROM_SCRO_TAG && sBPaused)) {

						sBPaused = false;
						paused = false;
						ch.resumeChro();
						pauseIV.setImageBitmap(pauseIBM);

						prepareHTTPPost(RES_PLAYBACK, tg);
						// new
						// HttpPreparer(context).prepareHTTPPost(RES_PLAYBACK,
						// tg);

					}

				}

			} else {
				paused = true;
				pauseIV.setImageBitmap(playIBM);
				ch.pauseChro();
			}

		}else{		
			
			if (!jumped) {
				if ((int) (p / 1000) > second) {

					second = (int) (p / 1000);

					ed.putString(MainActivity.SAVE_SLOT_RECL[0],
							Integer.toString(second + 1));
					ed.commit();

					//	Log.d(TAG, Integer.toString(second));

				}
			}
			
		}

	}
	
	public void jumpToPlayback(String rec){
		
		stopped = true;
		jumped = true;
		doubleStarted = true;
		
		ch = new Chrono(context, this, false);
		
		
		ch.gettV().setTextColor(Color.BLACK);
		ch.gettV().setVisibility(VISIBLE);
		ch.gettV().setGravity(Gravity.CENTER);
		ch.gettV().setTextSize(MainActivity.WIDTH / 25);
		

		LinearLayout.LayoutParams chParams = new LinearLayout.LayoutParams(0,
				MainActivity.HEIGHT / 20);
		chParams.weight = 1;

		pauseIV = new ImageView(context);
		LinearLayout.LayoutParams pauseVParams = new LinearLayout.LayoutParams(
				0, MainActivity.HEIGHT / 20);
		pauseVParams.weight = 1;

		pauseIBM = BitmapFactory.decodeResource(getResources(),
				R.drawable.pause_icon);
		playIBM = BitmapFactory.decodeResource(getResources(),
				R.drawable.play_icon);

		pauseIV.setImageBitmap(pauseIBM);

		pauseIV.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub

				switch (e.getAction()) {
				case MotionEvent.ACTION_DOWN:

					((ImageView) v).setBackgroundColor(Color.WHITE);

					break;

				case MotionEvent.ACTION_MOVE:

					((ImageView) v).setBackgroundColor(Color.TRANSPARENT);

					break;

				case MotionEvent.ACTION_UP:

					((ImageView) v).setBackgroundColor(Color.TRANSPARENT);

					if (paused) {
						pauseIV.setImageBitmap(pauseIBM);
						paused = false;
						ch.resumeChro();

						if (stopped) {
							prepareHTTPPost(RES_PLAYBACK, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(RES_PLAYBACK,
							// tg);

						} else {
							prepareHTTPPost(RES_REC, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(RES_REC,
							// tg);
						}

					} else {
						pauseIV.setImageBitmap(playIBM);
						paused = true;
						ch.pauseChro();

						if (stopped) {

							prepareHTTPPost(PAUSE_PLAYBACK, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(PAUSE_PLAYBACK,
							// tg);

						} else {

							prepareHTTPPost(PAUSE_REC, tg);
							// new
							// HttpPreparer(context).prepareHTTPPost(PAUSE_REC,
							// tg);
						}

					}

					break;

				default:
					break;
				}

				return true;
			}
		});

		

		TranslateAnimation controlAnim = new TranslateAnimation(
				MainActivity.WIDTH, 0, 0, 0);
		controlAnim.setDuration(400);
		
		ch.gettV().setAnimation(controlAnim);
		pauseIV.setAnimation(controlAnim);

		controls.addView(ch.gettV(), chParams);
		controls.addView(pauseIV, pauseVParams);
		
		removeAllViews();
		
		LinearLayout.LayoutParams controlLP = new LayoutParams(
				LayoutParams.MATCH_PARENT, MainActivity.HEIGHT / 15);
		
		pauseIV.setImageBitmap(playIBM);
		paused = true;		
		addView(controls, controlLP);
		panel.removeAllViews();
		addButtons();

		ch.setTime(1000*Long.parseLong(rec));
		ch.setQueuedSB(true);
		
	}

	public void setSeekBar() {


		recordedL = ch.getTime(); // keep memory of final length of video

		
		if (!jumped) {
			ch.setProg(- recordedL); // reset progress
		}
		
		sB = new SeekBar(context);
		LinearLayout.LayoutParams sBLP = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		sBLP.setMargins(MainActivity.WIDTH / 20, 0, MainActivity.WIDTH / 20, 0);
		sB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

				accPlayProg += playProg; // add all the time spent playing
				if (!jumped) {
					ch.setProg((tg - recordedL) - accPlayProg); // update the
																// progress variable
																// with the seekbar
																// progress minus
																// the accumulated
																// time spent
																// playing
				}else{
					
					ch.setProg((tg) - accPlayProg);
					
				}
				playProg = 0;

				if (sBPaused) {

					sBPaused = false;
					paused = false;
					ch.resumeChro();

					prepareHTTPPost(RES_PLAYBACK, tg);
					// new HttpPreparer(context).prepareHTTPPost(RES_PLAYBACK,
					// tg);
				}

			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if (!paused) {

					paused = true;
					sBPaused = true;
					ch.pauseChro();
				}

			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				if (fromUser) {
					tg = progress * recordedL / 100;
					ch.setTime(tg);

				} else {

					if (!paused) { // avoid changing playProg because of latency
									// of asynctasc
						playProg = (progress * recordedL / 100 - tg);
					}

				}

			}
		});

		TranslateAnimation sBAnim = new TranslateAnimation(-MainActivity.WIDTH,
				0, 0, 0);
		sBAnim.setDuration(350);
		sB.setAnimation(sBAnim);
	
		
		if (stopIV != null) {
			
			TranslateAnimation panelMoveAnim = new TranslateAnimation(0, 0,
					-MainActivity.HEIGHT / 50, 0);
			panelMoveAnim.setDuration(350);
			panel.setAnimation(panelMoveAnim);
			TranslateAnimation stopOutAnim = new TranslateAnimation(0,
					MainActivity.WIDTH, 0, 0);
			stopOutAnim.setDuration(350);
			stopIV.setAnimation(stopOutAnim);
			
			TranslateAnimation pauseMoveAnim = new TranslateAnimation(
					-MainActivity.WIDTH / 4, 0, 0, 0);
			pauseMoveAnim.setDuration(350);
			pauseIV.setAnimation(pauseMoveAnim);

			TranslateAnimation chroMoveAnim = new TranslateAnimation(
					-MainActivity.WIDTH / 12, 0, 0, 0);
			chroMoveAnim.setDuration(350);
			ch.gettV().setAnimation(chroMoveAnim);

			stopped = true;
			removeView(panel);
		}
			
		addView(sB, sBLP);
		addView(panel);
		
		if (stopIV != null) {
			
			controls.removeView(stopIV);
		}

	}

	public void setPostURI(String u) {

		post = new HttpPost(u);

	}

	public boolean getStarted(){
		
		return started;
		
	}
	
	public boolean getStopped(){
		
		return stopped;
		
	}
	
	public boolean getDoubleStarted(){
		
		return doubleStarted;
	}

	private void prepareRec(){
		
		panel.removeAllViews();

		started = true;
		
		ImageView recV = new ImageView(context); 
		Bitmap recBM = BitmapFactory.decodeResource(getResources(), R.drawable.rec_icon);

		TranslateAnimation tA = new TranslateAnimation(0, 0, panel.getHeight(), 0);
		tA.setDuration(350);
		recV.setAnimation(tA);

		LinearLayout.LayoutParams recVParams = 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						3*panel.getHeight()/4);

		recV.setImageBitmap(recBM);
		panel.addView(recV, recVParams);
	
	}

	
	
	public void prepareHTTPPost(String key, long tg) {

		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {

			if (post == null) {

				Intent optionsMenu = new Intent(
						"android.intent.action.OPTIONMENU");

				((Activity) context).startActivityForResult(optionsMenu,
						MainActivity.REQUEST_OPTIONS_MENU);

			} else {
				// new HTTPSendTask().execute((int) tg);
				new HTTPSendTask().executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR, key, Long.toString(tg));
			}

		} else {

			Toast.makeText(context,
					"Aucune connexion au serveur n'a pu Ítre Ètablie.",
					Toast.LENGTH_SHORT).show();
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// When DeviceListActivity returns with a device to connect

		switch (requestCode) {

		case MainActivity.REQUEST_OPTIONS_MENU:

			if (resultCode == Activity.RESULT_OK) {

				post = new HttpPost(data.getExtras().getString(
						MainActivity.SERV_URL));

				if (MainActivity.aBMenu != null) {

					MainActivity.aBMenu.findItem(R.id.url_icon).setTitle(
							"  " + data.getExtras().getString("servurl"));

				} else {

					Log.d(TAG, "aBMenu is null");

				}
				// sURL = data.getExtras().getString(MainActivity.SERV_URL);
			}
			break;

		default:
			break;
		}

	}

	private class HTTPSendTask extends AsyncTask<String, String, Boolean> {

		String par;
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			par = params[0];
			
			boolean connection = false;

			/*
			 * List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			 * pairs.add(new BasicNameValuePair("key1", Long
			 * .toString(params[0])));
			 */
			JSONObject pair = new JSONObject();
			try {

				pair.put(params[0], params[1]);

				// post.setEntity(new UrlEncodedFormEntity(pair));
				post.setEntity(new StringEntity(pair.toString()));

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {

				// HttpResponse response = client.execute(post);

				HttpResponse response = new DefaultHttpClient().execute(post);

				if (response != null) {
					connection = true;

				} else {
					connection = false;
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connection = false;
				Toast.makeText(context,
						"Communication avec le serveur ÈchouÈe.",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				connection = false;
				e.printStackTrace();
				// Toast.makeText(LogScroll.this,
				// "Communication avec le serveur ÔøΩchouÔøΩe.",
				// Toast.LENGTH_SHORT).show();
			}

			// TODO Auto-generated method stub
			return connection;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result) {
				Toast.makeText(
						context,
						"Communication avec le serveur " + post.getURI()
								+ " réussie. ", Toast.LENGTH_SHORT).show();
				
				
				if(par.equals(TEST)){
					
					prepareRec();					
				}else{
					
					Log.d(TAG, par);
					
				}
				
			} else {
				Toast.makeText(context, "L'adresse du serveur ne répond pas.",
						Toast.LENGTH_SHORT).show();

			}
		}
	}
}
