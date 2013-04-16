package com.fovea.chirp;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	static final String TAG = "UpdaterService";
	
	static final int DELAY = 60000; // a minute
	private boolean runFlag = false;
	private Updater updater;
	private ChirpApplication chirp;
	
	DbHelper dbHelper;
	SQLiteDatabase db;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.chirp = (ChirpApplication) getApplication();
		this.updater = new Updater();
		
		dbHelper = new DbHelper(this);
		
		Log.d(TAG, "onCreated");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		this.chirp.setServiceRunning(false);
		
		Log.d(TAG, "onDestroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		this.runFlag = true;
		this.updater.start();
		this.chirp.setServiceRunning(true);
		
		Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
