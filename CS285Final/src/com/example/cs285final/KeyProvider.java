package com.example.cs285final;

import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class KeyProvider extends ContentProvider{
	//fields for provider
	static final String PROVIDER_NAME = "com.example.contentprovidertest.Keys";
	static final String URL = "content://" + PROVIDER_NAME + "/users";
	static final Uri CONTENT_URI = Uri.parse(URL);
	
	//fields for database
	static final String ID = "id";
	static final String NUMBER = "number";
	static final String KEY = "key";
	
	//integer values used in ContentURI
	static final int USERS = 1;
	static final int USERS_ID = 2;

	DBHelper dbHelper;
	
	//Projection map for a query
	//TODO are these the right types??
	private static HashMap<String, String> KeyMap;
	
	// maps content URI "patterns" to the integer values that were set above
	static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "users", USERS);
		uriMatcher.addURI(PROVIDER_NAME, "users/#", USERS_ID);
	}
	
	//Database declarations
	private SQLiteDatabase database;
	static final String DATABASE_NAME = "SecureTextingDB";
	static final String TABLE_NAME = "allTheKeys";
	static final int DATABASE_VERSION = 1;
	static final String CREATE_TABLE =
			" CREATE TABLE " + TABLE_NAME +
			" (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			" number TEXT NOT NULL, " +
			" key TEXT NOT NULL);";

	// class that creates and manages the provider's database
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DBHelper.class.getName(), "Upgrading - Old data will be destroyed");
			db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
			onCreate(db);
		}

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)){
		case USERS:
			//delete everything in table
			count = database.delete(TABLE_NAME, selection, selectionArgs);
			break;
		case USERS_ID:
			String id = uri.getLastPathSegment(); //get the id
			count = database.delete( TABLE_NAME, ID +  " = " + id +
					(!TextUtils.isEmpty(selection) ? " AND (" +
				    selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		case USERS:
			return "vnd.android.cursor.dir/vnd.example.users";
		case USERS_ID:
			return "vnd.android.cursor.item/vnd.example.users";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long row = database.insert(TABLE_NAME, "", values);
		if(row > 0) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		} else {
			throw new SQLException("Fail to add a new record into " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		dbHelper = new DBHelper(context);
		// permissions to be writable
		database = dbHelper.getWritableDatabase();

		return !(database == null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TABLE_NAME);
		
		switch (uriMatcher.match(uri)) {
		//maps all column names
		case USERS:
			queryBuilder.setProjectionMap(KeyMap);
			break;
		case USERS_ID:
			queryBuilder.appendWhere( ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);	
		}
		
		if (sortOrder == null || sortOrder == ""){
			//no sorting --> default sorts by NUMBER (phone number)
			sortOrder = NUMBER;
		}
		Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		
		switch (uriMatcher.match(uri)){
		case USERS:
			count = database.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		case USERS_ID:
			count = database.update(TABLE_NAME, values, ID +
					" = " + uri.getLastPathSegment() +
					(!TextUtils.isEmpty(selection) ? " AND (" +
					selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI " + uri );	
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
		

}
