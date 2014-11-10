package com.example.cs285final;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_texting_view);
		// Show the Up button in the action bar.
		setupActionBar();
		
		textList = (ListView) findViewById(R.id.listView1);
		textField = (EditText) findViewById(R.id.editText1);
		contactName = (EditText) findViewById(R.id.editText2);
		textAdapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.text);
		sendButton = (Button) findViewById(R.id.button1);
		
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// when press send, populate the listView with the text
				Editable message = textField.getText();
				textAdapter.add(message.toString());
				textList.setAdapter(textAdapter);
				textField.setText("");
			}
			
		});
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
