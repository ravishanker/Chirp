package com.fovea.chirp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class StatusData {
	static final String TAG = StatusData.class.getSimpleName();
	
	static final int VERSION = 1;
	static final String DATABASE = "timeline.db";
	static final String TABLE = "timeline";
	static final String C_ID = BaseColumns._ID;
	static final String C_CREATED_AT = "created_at";
	static final String C_SOURCE = "source";
	static final String C_TEXT = "txt";
	static final String C_USER = "user";
	
	private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";
	
	private static final String[] MAX_CREATED_AT_COLUMNS = { "max("
		+ StatusData.C_CREATED_AT + ")" 
	};
	
	private static final String[] DB_TEXT_COLUMNS = { C_TEXT };
	
	class DbHelper extends SQLiteOpenHelper {
		
		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
			//this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database: " + DATABASE); 
			
			db.execSQL(
				"create table " + TABLE + " (" + C_ID + " int primary key, "
				+ C_CREATED_AT + " int, "+ C_SOURCE + " text, " + C_USER + " text, " 
						+ C_TEXT + " text)"	);
			

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Typically do ALTER TABLE statements, but ... we're just in dev
			db.execSQL("drop table if exists " + TABLE);
			this.onCreate(db);

		}
	} // DbHelper
	
	private final DbHelper dbHelper;
	
	public StatusData(Context context) {
		this.dbHelper = new DbHelper(context);
		Log.i(TAG, "Initialized data");
	}
	
	public void close() {
		this.dbHelper.close();
	}
	
	public void insertOrIgnore(ContentValues values) {
		Log.d(TAG, "insertOrIgnore on " + values);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		
		try {
			db.insertWithOnConflict(TABLE, null, values, 
					SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public Cursor getStatusUpdates() { //
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
		
	}
	
	// return Timestamp of the latest status we have it in the db
	public long getLatestStatusCreatedAtTime() { //
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, null, null,
					null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}
	
	public String getStatusTextById(long id) { // 
		SQLiteDatabase db = this.dbHelper.getReadableDatabase(); 
		try {
			Cursor cursor = db.query(TABLE, DB_TEXT_COLUMNS, C_ID + 
					"=" + id, null, null, null, null);
		
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;
			} finally {
				cursor.close(); 
			}
		} finally {
			db.close(); 
		}
	}
	
	public void delete() {
	    // Open Database
	    SQLiteDatabase db = dbHelper.getWritableDatabase();

	    // Delete the data
	    db.delete(TABLE, null, null);

	    // Close Database
	    db.close();
	  }
	
	
	
}

