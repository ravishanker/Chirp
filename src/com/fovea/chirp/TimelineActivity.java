package com.fovea.chirp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineActivity extends BaseActivity {
	Cursor cursor;
	ListView listTimeline;
	SimpleCursorAdapter adapter;
	static final String[] FROM = {
		DbHelper.C_CREATED_AT, DbHelper.C_USER, DbHelper.C_TEXT
	};
	static final int[] TO = {
		R.id.textCreatedAt, R.id.textUser, R.id.textText
	};
	
	TimelineReceiver receiver;
	IntentFilter filter;
	
	static final String SENE_TIMELINE_NOTIFICATIONS = 
			"com.fovea.chirp.SEND_TIMELINE_NOTIFICATIONS";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		
		if (chirp.getPrefs().getString("username", null) == null) {
			startActivity(new Intent(this, PrefsActivity.class));
			Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show(); 
		}
		
		listTimeline = (ListView) findViewById(R.id.listTimeline);
		
		filter = new IntentFilter("com.fovea.chirp.NEW_STATUS");
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		chirp.getStatusData().close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.setupList();
		
		registerReceiver(receiver, filter, SENE_TIMELINE_NOTIFICATIONS, null);
		
	}
	
	
	@SuppressWarnings("deprecation")
	private void setupList() {
		cursor = chirp.getStatusData().getStatusUpdates();
		startManagingCursor(cursor);
		
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listTimeline.setAdapter(adapter);
	}
	
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() != R.id.textCreatedAt) 
				return false;
			
			long timestamp = cursor.getLong(columnIndex);
			
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(
					view.getContext(), timestamp );
			
			((TextView) view).setText(relTime);
			
			return true;			
		}
	};
	
	class TimelineReceiver extends BroadcastReceiver {

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			cursor.requery();
			adapter.notifyDataSetChanged();
			Log.d("TimelineReceiver", "onReceived");
		}
		
	}
	
}
