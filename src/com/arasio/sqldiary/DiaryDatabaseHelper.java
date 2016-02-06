package com.arasio.sqldiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DiaryDatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_TABLE = "diary";
	private static final String DATABASE_NAME = "com.arasio.sqldiary.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String KEY_ID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_DATE = "date";
	
	private static final String SQL_QUERY = "CREATE TABLE diary (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, date TEXT); ";
	
	public DiaryDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// create table in database
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_QUERY);
	}
	
	// delete table and recreate it
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		onCreate(db);
	}
	
	public void insertRow(SQLiteDatabase db, ContentValues values) {
		db.insertWithOnConflict(DiaryDatabaseHelper.DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	public void updateRow(SQLiteDatabase db, long row, ContentValues values) {
		String whereClause = KEY_ID + "=?";
		String [] whereArgs = new String[] { String.valueOf(row) };
	    db.update(DATABASE_TABLE, values, whereClause, whereArgs);
	}
	
	public void deleteRow(SQLiteDatabase db, long row) {
		// google recommended way of deleting rows, protects against SQL injections
		String whereClause = KEY_ID + "=?";
		String [] whereArgs = new String[] { String.valueOf(row) };
	    db.delete(DATABASE_TABLE, whereClause, whereArgs);
	}

}
