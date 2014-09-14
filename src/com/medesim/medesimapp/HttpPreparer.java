package com.medesim.medesimapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class HttpPreparer {

	public static final String TAG = "HttpPreparer";
	private HttpPost post;
	public static String sURL;
	private static HttpClient client = new DefaultHttpClient();
	private Context context;
	
	public HttpPreparer(Context context){
		this.context = context;
		
		
	}
	
	public void setPost(String u){
		
		post = new HttpPost(u);
		
	}
	
	public void prepareHTTPPost(String key, long tg){
		
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();	
		
		
		if (networkInfo != null && networkInfo.isConnected()) {
			
			
			
			if(sURL == null){

				Intent optionsMenu = new Intent ("android.intent.action.OPTIONMENU");
				
				((Activity) context).startActivityForResult(optionsMenu, MainActivity.REQUEST_OPTIONS_MENU);
				
			}else{
				
				post = new HttpPost(sURL);
				new HTTPSendTask().execute(key, Long.toString(tg));
			}			

		} else {

			Toast.makeText(context,
					"Aucune connexion avec le serveur n'a pu être êtablie.",
					Toast.LENGTH_SHORT).show();
		}
				
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 // When DeviceListActivity returns with a device to connect
		
		
		switch (requestCode) {
				
		case MainActivity.REQUEST_OPTIONS_MENU:
			
			if (resultCode == Activity.RESULT_OK) {
				
			//	post = new HttpPost(data.getExtras().getString(MainActivity.SERV_URL));
				sURL = data.getExtras().getString(MainActivity.SERV_URL);
			}
			break;

		default:
			break;
			}

	}
	
	private class HTTPSendTask extends AsyncTask<String, String, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			boolean connection = false;
			Log.d(TAG, "url : " + sURL);
		/*	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("key1", Long
					.toString(params[0])));*/
			
			
			JSONObject pair = new JSONObject();
			
			try {
				pair.put(params[0], params[1]);
				
			//	StringEntity sE = pair.
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				
				post.setEntity(new StringEntity(pair.toString()));			
				
			} catch (UnsupportedEncodingException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {

				HttpResponse response = client.execute(post);

				if (response != null) {
					connection = true;

				} else {
					connection = false;
				}
				
			/*	int status=response.getStatusLine().getStatusCode();

				   if(status == HttpStatus.SC_OK){
				    	
			        HttpEntity e = response.getEntity();
			        String data = EntityUtils.toString(e);
			        Log.d(TAG, "Success : " + data);

			    }else{
				    	
			    	Log.d(TAG, "failure : " + Integer.toString(status));
			    }*/
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connection = false;
				 Toast.makeText(MainActivity.cont,
				 "Communication avec le serveur échouée.",
				 Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				connection = false;
				e.printStackTrace();
				// Toast.makeText(LogScroll.this,
				// "Communication avec le serveur a √©chou√©e.",
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
				Toast.makeText(context,
						"Communication avec le serveur " + post.getURI() + " réussie." ,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context,
						"Communication avec le serveur réussie.",
						Toast.LENGTH_SHORT).show();

			}
		}
	}
}
