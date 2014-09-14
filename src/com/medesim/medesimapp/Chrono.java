package com.medesim.medesimapp;

import android.content.Context;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.util.Log;

public class Chrono {

	private boolean running = true;
	private AutoFitTextView tV;
	private long base = 0;
	private long interv = 0;
	private long l = 0;
	private boolean doing = true;
	private long pauseInterv = 0;
	private long progress = 0;
	private ChronoUpdate cU;
	private boolean sBQueued = false;
	private TagsView parentTV;
	
	public Chrono(Context context, TagsView parentTV, Boolean go) {

		if(this.parentTV != null)this.parentTV = null;
		
		this.parentTV = parentTV;
		
		if (tV != null)tV = null;
		
		tV = new AutoFitTextView(context);
		interv = 0;
		l = 0;
		pauseInterv = 0;
		progress = 0;
		
		base = System.currentTimeMillis();
		if(cU != null)cU =null;
		
		if(!go)pauseChro();
		
		if(cU != null){
			cU.cancel(true);
			cU = null;
		}
		
		cU = new ChronoUpdate();
		cU.execute(base);
		

	}

	public void displayChrono(long tg) {

		tV.setText(String.format("%02d",
				(int) (Math.floor((double) ((tg / 3600000) % 60))))
				+ " : "
				+ String.format("%02d",
						(int) (Math.floor((double) ((tg / 60000) % 60))))
				+ " : "
				+ (String.format("%02d",
						(int) Math.floor((double) (tg / 1000) % 60))));

		parentTV.setSBProg(tg, TagsView.FROM_CHRO);

	//	Log.d("Chrono", Long.toString(progress));

	}

	public AutoFitTextView gettV() {
		return tV;
	}

	public void settV(AutoFitTextView tV) {
		this.tV = tV;
	}
	
	public long getTime() {
		return l;
	}

	public void setTime(long l) {
		this.l = l;
		tV.setText(String.format("%02d",
				(int) (Math.floor((double) ((l / 3600000) % 60))))
				+ " : "
				+ String.format("%02d",
						(int) (Math.floor((double) ((l / 60000) % 60))))
				+ " : "
				+ (String.format("%02d",
						(int) Math.floor((double) (l / 1000) % 60))));
		
	}
	

	public void setProg(long bM){
		
		progress = bM;
		
	}
	
	public void setQueuedSB(boolean bol){
		
		sBQueued = bol;
		
	}

	public void pauseChro() {

		if (running){
			running = false;
			interv = System.currentTimeMillis();
		}
			

	}

	public void resumeChro() {
		if (!running){
			
			running = true;
			pauseInterv += System.currentTimeMillis()-interv;
			
		}
	}

	public void stopChro(){
		
		if(running)running = false;
		if(doing)doing = false;
		cU.cancel(true);
		
		cU = null;

	}
	
	protected class ChronoUpdate extends AsyncTask<Long, Long, Result> {

		@Override
		protected Result doInBackground(Long... lo) {
			// TODO Auto-generated method stub
			

			while (doing) {

				if (running) {

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					l = System.currentTimeMillis() - (base + pauseInterv - progress);

					publishProgress(l);

				}else{
					
					if(sBQueued){
						
						
						publishProgress(l);
						
					}
					
				}			

			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Long... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			
			
			if(sBQueued){
				
				parentTV.setSeekBar();
				sBQueued = false;
				
				
				
			}else{
			//	Log.d("chrono", "updating");
				displayChrono(values[0]);
			}
			

		}

	}

}
