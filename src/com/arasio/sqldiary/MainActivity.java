package com.arasio.sqldiary;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	
	private DiaryDatabaseHelper databaseHelper;
	private SQLiteDatabase db;
	private ListAdapter listAdapter;
	
	// 0 = sort by date
	// 1 = sort by title
	private int sortOrder;
	private String sortArg;
	
	// onCreate is called when the activity is first created
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.sortOrder = 0;
		setContentView(R.layout.activity_main);
		refreshUI();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info;
		info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		
		// set context menu title to "<entry title> @ <creation date>"
		menu.setHeaderTitle("\"" + cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_TITLE)) +
				"\"" + " @ " + cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_DATE)));
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		
		switch (item.getItemId()) {
			case R.id.editEntry:
				int rowToEdit = cursor.getInt(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_ID));
				editEntry(rowToEdit);
				return true;
			case R.id.deleteEntry:
				// delete the desired entry, choosing it via row
				databaseHelper.deleteRow(db, cursor.getLong(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_ID)));
				
				// display a toast notification
				Toast.makeText(getApplicationContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
				
				// refresh the user interface
				refreshUI();
				return true;
				
			default:
				return super.onContextItemSelected(item);
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
			case R.id.action_new_entry:
				newEntry();
				return true;
			
			// change sort order modifiers and refresh user interface
			case R.id.action_menu_sortByDate:
				this.sortOrder = 0;
				refreshUI();
				return true;
			case R.id.action_menu_sortByTitle:
				this.sortOrder = 1;
				refreshUI();
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	private void refreshUI() {
		databaseHelper = new DiaryDatabaseHelper(MainActivity.this);
		db = databaseHelper.getReadableDatabase();
		
		// columns and views for data binding
		String[] columns = new String[] { DiaryDatabaseHelper.KEY_ID, DiaryDatabaseHelper.KEY_TITLE, 
				DiaryDatabaseHelper.KEY_DATE };
		int[] to = new int[] { R.id.entryID, R.id.entryTitle, R.id.entryDate };
		
		// sort order handling
		// 0 = sort by date
		// 1 = sort by title
		switch(this.sortOrder) {
			case 0:
				this.sortArg = DiaryDatabaseHelper.KEY_DATE + " DESC";
				break;
			case 1:
				// ignore case when sorting
				this.sortArg = DiaryDatabaseHelper.KEY_TITLE + " COLLATE NOCASE";
				break;
			default:
				this.sortArg = DiaryDatabaseHelper.KEY_DATE + " DESC";
				break;
		}

		Cursor cursor = db.query(DiaryDatabaseHelper.DATABASE_TABLE, columns, null, null, null, null, sortArg);
		startManagingCursor(cursor);

		listAdapter = new SimpleCursorAdapter(this, R.layout.list_item, cursor, columns, to, 0 );
		
		this.setListAdapter(listAdapter);
		registerForContextMenu(getListView());
		
		// behaviour for list item clicks
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				Intent i = new Intent(getApplicationContext(), DiaryEntryActivity.class);
				i.putExtra("ID", cursor.getLong(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_ID)));
				startActivity(i);
			}
		});
		
	}
	
	private void newEntry() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		final EditText titleInputField = new EditText(this);
		final EditText descriptionInputField = new EditText(this);
		LinearLayout layout = new LinearLayout(this);
		
		layout.setOrientation(LinearLayout.VERTICAL);
		
		// set text hints for empty EditText-fields
		titleInputField.setHint("Title");
		descriptionInputField.setHint("What's on your mind?");
		
		// set EditText-fields to capitalize first letter of every sentence
		titleInputField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		descriptionInputField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		
		// set title edittext to single line mode (just in case)
		titleInputField.setSingleLine();
		
		// vertical scrolling
		descriptionInputField.setVerticalScrollBarEnabled(true);
		// make edittext multiline and display scrollbar after five lines of text
		descriptionInputField.setSingleLine(false);
		descriptionInputField.setMaxLines(5);
		descriptionInputField.setMovementMethod(new ScrollingMovementMethod());
		// no horizontal scrolling -> word wrap
		descriptionInputField.setHorizontallyScrolling(false);
		
		// add fields to layout
		layout.addView(titleInputField);
		layout.addView(descriptionInputField);
		
		// assign layout to dialog + set title
		dialogBuilder.setView(layout);
		dialogBuilder.setTitle("New entry");
		
		// confirm button + behaviour
		dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// get strings from the user input fields
				String titleText = titleInputField.getText().toString();
				String descriptionText = descriptionInputField.getText().toString();
				
				// generate timestamp
				String timestamp = (DateFormat.format("yyyy.MM.dd", new java.util.Date()).toString());
				
				// create database (or re-open if exists)
				databaseHelper = new DiaryDatabaseHelper(MainActivity.this);
				db = databaseHelper.getWritableDatabase();
				
				// store the input data
				ContentValues values = new ContentValues();
				values.put(DiaryDatabaseHelper.KEY_TITLE, titleText);
				values.put(DiaryDatabaseHelper.KEY_DESCRIPTION, descriptionText);
				values.put(DiaryDatabaseHelper.KEY_DATE, timestamp);
				
				// insert data into database table
				databaseHelper.insertRow(db, values);
				
				// display toast notification
				Toast.makeText(getApplicationContext(), "New entry created", Toast.LENGTH_SHORT).show();
				
				// refresh UI so we can see the changes made
				refreshUI();
			}
		});
		
		// set cancel button
		dialogBuilder.setNegativeButton("Cancel", null);
		
		// finally show the dialog
		dialogBuilder.create().show();
	}
	
	private void editEntry(final long row_id_input) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		final EditText titleInputField = new EditText(this);
		final EditText descriptionInputField = new EditText(this);
		LinearLayout layout = new LinearLayout(this);
		
		layout.setOrientation(LinearLayout.VERTICAL);
		
		String[] columns = new String[] { DiaryDatabaseHelper.KEY_TITLE, DiaryDatabaseHelper.KEY_DATE, 
				DiaryDatabaseHelper.KEY_DESCRIPTION };
		final String row_id = DiaryDatabaseHelper.KEY_ID + "=" + row_id_input;
		
		Cursor cursor = db.query(DiaryDatabaseHelper.DATABASE_TABLE, columns, row_id, null, null, null, null);
		cursor.moveToFirst();
		
		// save the timestamp, we won't touch it
		final String date = cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_DATE));
		
		// set title edittext to single line mode (just in case)
		titleInputField.setSingleLine();
		
		// vertical scrolling
		descriptionInputField.setVerticalScrollBarEnabled(true);
		// make edittext multiline and display scrollbar after five lines of text
		descriptionInputField.setSingleLine(false);
		descriptionInputField.setMaxLines(5);
		descriptionInputField.setMovementMethod(new ScrollingMovementMethod());
		// no horizontal scrolling -> word wrap
		descriptionInputField.setHorizontallyScrolling(false);
		
		// fill EditText-fields with existing entry data
		titleInputField.setText(cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_TITLE)));
		descriptionInputField.setText(cursor.getString(cursor.getColumnIndex(DiaryDatabaseHelper.KEY_DESCRIPTION)));
		
		// set EditText-fields to capitalize first letters
		titleInputField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		descriptionInputField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		
		// add fields to layout
		layout.addView(titleInputField);
		layout.addView(descriptionInputField);
		
		// assign layout to dialog + set title
		dialogBuilder.setView(layout);
		dialogBuilder.setTitle("Edit entry");
		
		// confirm button + behaviour
		dialogBuilder.setPositiveButton("Update entry", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// get strings from the user input fields
				String titleText = titleInputField.getText().toString();
				String descriptionText = descriptionInputField.getText().toString();
				
				// open database
				databaseHelper = new DiaryDatabaseHelper(MainActivity.this);
				db = databaseHelper.getWritableDatabase();
				
				// store the input data
				ContentValues values = new ContentValues();
				values.put(DiaryDatabaseHelper.KEY_TITLE, titleText);
				values.put(DiaryDatabaseHelper.KEY_DESCRIPTION, descriptionText);
				values.put(DiaryDatabaseHelper.KEY_DATE, date);
				
				// update row with new data
				databaseHelper.updateRow(db, row_id_input, values);

				// refresh UI so we can see the changes made
				refreshUI();
				
				// display toast notification
				Toast.makeText(getApplicationContext(), "Entry updated", Toast.LENGTH_SHORT).show();
			}
		});
		
		// set cancel button
		dialogBuilder.setNegativeButton("Cancel", null);
		
		// finally show the dialog
		dialogBuilder.create().show();
	}
	
}
