package com.example.cs285final;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class TextingView extends Activity {
	ListView textList;
	EditText textField;
	EditText contactName;
	Button sendButton;
	ArrayAdapter<String> textAdapter;
	String number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_texting_view);
		// Show the Up button in the action bar.
		setupActionBar();
		number = (String) getIntent().getExtras().get("number");
		textList = (ListView) findViewById(R.id.listView1);
		textField = (EditText) findViewById(R.id.editText1);
		contactName = (EditText) findViewById(R.id.editText2);
		
		

		
		populatePastTexts();
		
	
		/*
		 * Grabs the current texts in the content provider and populates the  
		 */
		
		sendButton = (Button) findViewById(R.id.button1);
		
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// when press send, populate the listView with the text
				
				Editable message = textField.getText();
				//textAdapter.add(message.toString());
				//textList.setAdapter(textAdapter);
				
				textField.setText("");
				EncryptAndSend(message.toString());
				populatePastTexts();
			}

			private void EncryptAndSend(String message) {
				/*
				 * TODO ENCRYPTION GOES HERE
				 */
				message = "\"" + message + "\"";
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(number, "6318852193", message, null, null);
				
			}
			
		});
	}

	// TODO
	public void addUserKeyInfo(String phoneNumber, String key) {
		ContentValues values = new ContentValues();
		values.put(KeyProvider.NUMBER, phoneNumber);
		values.put(KeyProvider.KEY, key);
		Uri uri = getContentResolver().insert(KeyProvider.CONTENT_URI, values);
	}

	// TODO
	public String getKey(String number) {
		String URL = "content://com.example.contentprovidertest.Keys/users";
		Uri users = Uri.parse(URL);
		Cursor c = getContentResolver()
				.query(users, null, null, null, "number");
		if (!c.moveToFirst()) {
			return "";
		} else {
			do {
				if (c.getString(c.getColumnIndex(KeyProvider.NUMBER))
						.equalsIgnoreCase(number)) {
					return c.getString(c.getColumnIndex(KeyProvider.KEY));
				}
			} while (c.moveToNext());
			return "";
		}
	}
	private void populatePastTexts() {
		List<String> prevTexts = new ArrayList<String>();
		prevTexts = getPrevTexts((String) getIntent().getExtras().get("number"));
	//	ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
    //            this, 
    //            /*not sure, need to talk to erin*/R.layout.text,
    //            prevTexts );
		textAdapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.text, prevTexts);
		textList.setAdapter(textAdapter);
		
	}

	private List<String> getPrevTexts(String number) {
		ArrayList<Pair<String, Long>> result = new ArrayList<Pair<String, Long>>();

		Cursor c = getContentResolver().query(
				Uri.parse("content://sms/inbox"), null,
				null, null, null);
		/* while (c.moveToNext()) {
		        for (int i = 0; i < c.getColumnCount(); i++) {
		            Log.d(c.getColumnName(i) + "", i + " " + c.getString(i));
		        }
		        Log.d("One row finished",
		                "**************************************************");
		    }*/
		
		while (c.moveToNext()) {
			if(c.getString(2).equals(number)){
				String toAdd = c.getString(12);
				if(toAdd.charAt(0)=='\"'){
					// TODO DECRYPT
					toAdd = toAdd.substring(1,toAdd.length()-1);
				}
				result.add(new Pair<String, Long>(number + ": " + toAdd,c.getLong(4)));
				Log.d("TEXTVIEW", c.getString(12));
			}
		}
		c.close();
		
		Cursor d = getContentResolver().query(
				Uri.parse("content://sms/sent"), null,
				null, null, null);
		
		while (d.moveToNext()) {
			if(d.getString(2).equals(number)){
				String toAdd = d.getString(12);
				if(toAdd.charAt(0)=='\"'){
					//TODO DECRYPT
					toAdd = toAdd.substring(1,toAdd.length()-1);
				}
				result.add(new Pair<String, Long>("6318852193: " +toAdd,d.getLong(4)));
				Log.d("TEXTVIEW", d.getString(12));
			}
		}
		Collections.sort(result, new Comparator<Pair<String, Long>>() {
	        @Override
	        public int compare(Pair<String, Long> p1, Pair<String, Long>  p2)
	        {
	            return  p1.second.compareTo(p2.second);
	        }
	    });
		c.close();
		List<String> sortedResults = new ArrayList<String>();
		for(Pair<String, Long> p : result){
			sortedResults.add(p.first);
		}
		return sortedResults;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.texting_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
