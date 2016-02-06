package com.arasio.sqldiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class DiaryEntryActivity extends Activity {

	Context ctx;
	
	public DiaryEntryActivity() {
		super();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diary_entry);
		
		// show up-button on the action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// fetch the sql row id via getExtra
		Intent i = getIntent();
		final String row_id = DiaryDatabaseHelper.KEY_ID + "=" + i.getLongExtra("ID", 1);
		
		DiaryDatabaseHelper databaseHelper = new DiaryDatabaseHelper(DiaryEntryActivity.this);
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		TextView entryTitle = (TextView) findViewById(R.id.diaryEntryActivityTitle);
		TextView entryDescription = (TextView) findViewById(R.id.diaryEntryActivityDescription);
		
		String[] columns = new String[] { DiaryDatabaseHelper.KEY_ID, DiaryDatabaseHelper.KEY_TITLE, 
				DiaryDatabaseHelper.KEY_DATE, DiaryDatabaseHelper.KEY_DESCRIPTION };
		
		Cursor cursor = db.query(DiaryDatabaseHelper.DATABASE_TABLE, columns, row_id, null, null, null, null);
		cursor.moveToFirst();
		
		String title = cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_TITLE));
		String date = cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_DATE));
		String description = cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_DESCRIPTION));
		
		entryTitle.setText(title);
		getActionBar().setTitle("Entry on " + date);
		entryDescription.setText(description);
		
	}
	
	// add behaviour for action bar up-button
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home: 
            onBackPressed();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
}
