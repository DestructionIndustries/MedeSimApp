package com.medesim.medesimapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.medesim.medesimapp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;



public class OptionsMenu extends Activity{

	private EditText inputAdress;
	private String urlString = "";
	private Button saveButton, testButton;//, scanButton;
	private ImageButton favIm [] = new ImageButton [3];
	private Button favButt [] = new Button[3];
	private int favButtRes [] = {R.id.fav1Button, R.id.fav2Button, R.id.fav3Button};
	private int favImRes [] = {R.id.favImageButton1, R.id.favImageButton2, R.id.favImageButton3};
	private final String TAG = "Options Menu";
	private HttpPost post;
	private SharedPreferences shP;
	private SharedPreferences.Editor ed;
	private final String [] FAV_KEYS = {"fav1", "fav2", "fav3"}; 	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.config_layout);
		
		inputAdress = (EditText) findViewById(R.id.editText1);
		
		shP = getSharedPreferences(MainActivity.SERV_FILE, MODE_PRIVATE);
		ed = shP.edit();
		
		String extUSRI = shP.getString(MainActivity.SERV_URL, MainActivity.NULL_POST);
		
		if(extUSRI.equals(MainActivity.NULL_POST)){
			
			inputAdress.setText("http://");
			
		}else{
			
			inputAdress.setText(extUSRI);
		}
		
	//	scanButton = (Button)findViewById(R.id.scan_button);
	/*	scanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent nSInt = new Intent("android.intent.action.NETSCANNER");
				startActivity(nSInt);
			}
		});*/
		
		
		saveButton = (Button)findViewById(R.id.saveButton);
		testButton = (Button)findViewById(R.id.testButton);
		
		for(int i = 0; i <favButt.length; i++){
			
			
			favButt[i] = (Button)findViewById(favButtRes[i]);
			favIm[i] = (ImageButton)findViewById(favImRes[i]);
			
			String favString = shP.getString(FAV_KEYS[i], MainActivity.NULL_POST);
			
			if(!favString.equals(MainActivity.NULL_POST)){
				
				favButt[i].setText(favString);
			}
			
			
			favIm[i].setTag(i);
			
			favIm[i].setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					favButt[(Integer) v.getTag()].setText(inputAdress.getText().toString());
					
					ed.putString(FAV_KEYS[(Integer) v.getTag()], inputAdress.getText().toString());
					ed.commit();
				}
			});
			
			favButt[i].setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					Button b = (Button)v;
					
					if(! b.getText().toString().startsWith("Favori")){
						
						inputAdress.setText(b.getText().toString());
						
						
					}else{
						
						Toast.makeText(OptionsMenu.this, "Aucun favori sauvé dans cet emplacement", Toast.LENGTH_SHORT).show();
						
					}				
					
				}
			});
			
		}
		
	
		
		testButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			//	boolean allOK = isURLValid();
				
				if(isURLValid()){
					
					prepareHTTPPost("test", 0);
					
					
				}
				
				
			}
		});
		
		
		saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			//	boolean allOK = isURLValid();
				
				Intent orInt = getIntent();				
				
				if(isURLValid()){
					
					
					shP = getSharedPreferences(MainActivity.SERV_FILE, MODE_PRIVATE);
					ed.putString(MainActivity.SERV_URL, urlString);
					ed.commit();
					
					
					orInt.putExtra(MainActivity.SERV_URL, urlString);

					setResult(RESULT_OK, orInt);
					finish();
					
				}
					
			}
		});
				
	}
	
	private Boolean isURLValid(){
		
		boolean allOK = true;
		
		urlString = inputAdress.getText().toString();
		
		urlString = urlString.replaceAll(" ", "");
		
		
	    HttpURLConnection connection = null;
	    
	    try{         
	        URL myurl = new URL(urlString);        
	        connection = (HttpURLConnection) myurl.openConnection(); 
	        //Set request to header to reduce load as Subirkumarsao said.       
	        connection.setRequestMethod("HEAD");         
	        int code = connection.getResponseCode();        
	        System.out.println("" + code); 
	    } catch (IllegalArgumentException e){
			Toast.makeText(OptionsMenu.this,"Veuillez entrer une adresse valide",Toast.LENGTH_LONG).show();
			allOK = false;
			e.printStackTrace();
	    } catch (MalformedURLException e) {
	    	Toast.makeText(OptionsMenu.this,"Veuillez entrer une adresse valide",Toast.LENGTH_LONG).show();
			allOK = false;
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(OptionsMenu.this,"Veuillez entrer une adresse valide",Toast.LENGTH_LONG).show();
			allOK = false;
			e.printStackTrace();
		} catch (NetworkOnMainThreadException e){
			
			e.printStackTrace();
			
		}
		
		if(!urlString.startsWith("http://")){
			
			Toast.makeText(OptionsMenu.this,"L'adresse doit commencer par 'http://'",Toast.LENGTH_LONG).show();
			allOK = false;
		}
		
		
		return allOK;
		
	}
	
	public void prepareHTTPPost(String key, long tg) {

		ConnectivityManager connMgr = (ConnectivityManager) OptionsMenu.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {

			post = new HttpPost(urlString);
			
				// new HTTPSendTask().execute((int) tg);
				new HTTPSendTask().executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR, key, Long.toString(tg));
			

		} else {

			Toast.makeText(OptionsMenu.this,
					"Aucune connexion au serveur n'a pu Ítre Ètablie.",
					Toast.LENGTH_SHORT).show();
		}

	}
	private class HTTPSendTask extends AsyncTask<String, String, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			boolean connection = false;

			JSONObject pair = new JSONObject();
			try {

				pair.put(params[0], params[1]);

				post.setEntity(new StringEntity(pair.toString()));

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {

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
				Toast.makeText(OptionsMenu.this,
						"Communication avec le serveur ÈchouÈe.",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				connection = false;
				e.printStackTrace();

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
						OptionsMenu.this,
						"Communication avec le serveur " + post.getURI()
								+ " réussie. ", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(OptionsMenu.this, "L'adresse du serveur ne répond pas.",
						Toast.LENGTH_SHORT).show();

			}
		}
	}
	
}
