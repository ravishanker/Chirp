package com.fovea.chirp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	public static final String NEW_STATUS_INTENT = "com.fovea.chirp.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	static final String TAG = "UpdaterService";
	
	static final int DELAY = 60000; // a minute
	private boolean runFlag = false;
	private Updater updater;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.updater = new Updater();
		
		Log.d(TAG, "onCreated");
	}

	
	@Override
	  public int onStartCommand(Intent intent, int flag, int startId) {
	    if (!runFlag) {
	      this.runFlag = true;
	      this.updater.start();
	      ((ChirpApplication) super.getApplication()).setServiceRunning(true);

	      Log.d(TAG, "onStarted");
	    }
	    return Service.START_STICKY;
	  }

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		//this.chirp.setServiceRunning(false);
		((ChirpApplication) super.getApplication()).setServiceRunning(false);
		
		Log.d(TAG, "onDestroyed");
	}

	
	private class Updater extends Thread {
		
		public Updater() {
			super("UpdaterService-Updater");
		}
		
		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;
			while (updaterService.runFlag) {
				Log.d(TAG, "Running background thread");
				try {
					// Get the time-line from the cloud
					ChirpApplication chirp = (ChirpApplication) updaterService
							.getApplication();
					int newUpdates = chirp.fetchStatusUpdates();
					if (newUpdates > 0) {
						Log.d(TAG, "We have new status");
					}
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}				
			}
		}
	} // Updater

}
