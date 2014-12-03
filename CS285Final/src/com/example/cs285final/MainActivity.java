package com.example.cs285final;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends Activity {
	public static final String RECIEVE_INITAL = "i5d3";
	public static final String COMPLETE_HANDSHAKE = "n5s2";
	ListView list;
	LinearLayout ll;
	Button loadBtn;
	ArrayAdapter<String> adapter;
	final String PARENT = "com.example.cs285final";
	final String LOG = "MainActivity: ";
	public static KeyAgreement currentKeyAgreement;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final TelephonyManager tMgr = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String myPhoneNumber = tMgr.getLine1Number();
		Log.i(LOG, "My Phone Number: " + myPhoneNumber);
		
		ll = (LinearLayout) findViewById(R.id.LinearLayout1);

		list = (ListView) findViewById(R.id.listView1);
		
		// On long click start handshake
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(LOG, "Item long click");
				String info = adapter.getItem(position);
				
				final String[] t = info.split(":");
				final String phoneNumber = t[1];
				final String name = t[0];
				Log.i(LOG, "#: " + phoneNumber + "Name: " + name);
				if (!info.contains("*")) {
					info = info.concat("*");
					adapter.insert(info, position);
					DHKeyAgreement2 temp = new DHKeyAgreement2();
					Transfer transfer = null;
					try {
						transfer = temp.startHandshakePart1();
					} catch (Exception e) {
						e.printStackTrace();
					}
					MainActivity.currentKeyAgreement = transfer.getAliceKeyAgree();
					byte[] alicePubKeyEnc = temp.startHandshakePart2(transfer.getAliceKpair());
					
					SmsManager sms = SmsManager.getDefault();
					ArrayList<String> msgArray=sms.divideMessage((MainActivity.RECIEVE_INITAL+ Arrays.toString(alicePubKeyEnc)));
					sms.sendMultipartTextMessage(phoneNumber, myPhoneNumber, msgArray, null, null);
				}
				
				// Switch to texting view
				final Intent intent = new Intent(MainActivity.this, TextingView.class);
				intent.putExtra(PARENT, "test");
				intent.putExtra("number", myPhoneNumber); 
				intent.putExtra("contactNumber", phoneNumber);
				intent.putExtra("contactName", name);
				startActivity(intent);
				return false;
			}
			
		});
		
		// On normal click, go to texting view
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				Log.i(LOG, "Item short click");
				
				String info = adapter.getItem(position);
				
				final String[] t = info.split(":");
				final String phoneNumber = t[1];
				final String name = t[0];
				
				// Switch to texting view
				final Intent intent = new Intent(MainActivity.this, TextingView.class);
				intent.putExtra(PARENT, "test");
				intent.putExtra("number", myPhoneNumber); 
				intent.putExtra("contactNumber", phoneNumber);
				intent.putExtra("contactName", name);
				startActivity(intent);
			}
		});
		
		loadBtn = (Button) findViewById(R.id.button1);
		loadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final LoadContactsAyscn lca = new LoadContactsAyscn();
				lca.execute();
			}
		});
		
	}

	// STORAGE IS ONE STRING SEPARATING CONTACTS BY SEMICOLONS AND
	// INFORMATION WITHIN THE CONTACT BY COMMAS
	public String getStorage() {
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		// storage exists so get it
		if (sp.contains("storage")) {
			return sp.getString("storage", "");
		} else {// storage does not exist (AKA first time opening application)
			Editor edit = sp.edit();
			edit.putString("storage", "");
			edit.commit();
			return "";
		}
	}

	// FORMAT: storage is really one string
	// contacts are separated by semicolons
	// within a contact, the phoneNumber is first then a comma to separate it
	// from the key
	public void addToStorage(final String phoneNumber, final String key) {
		String storage = getStorage();
		storage = storage + ";" + phoneNumber + "," + key;
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit = sp.edit();
		edit.putString("storage", storage);
		while (!edit.commit()) {
		}
		;
	}

	class LoadContactsAyscn extends AsyncTask<Void, Void, ArrayList<String>> {
		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = ProgressDialog.show(MainActivity.this, "Loading Contacts",
					"Please Wait");
		}

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			final ArrayList<String> contacts = new ArrayList<String>();

			final Cursor c = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);
			while (c.moveToNext()) {

				final String contactName = c
						.getString(c
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				String phNumber = c
						.getString(c
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				phNumber = parseNumber(phNumber);

				contacts.add(contactName + ":" + phNumber);

			}
			c.close();

			return contacts;
		}

		private String parseNumber(String phNumber) {
			phNumber = phNumber.replaceAll("[()-]", "");
			phNumber = phNumber.replaceAll("\\s", "");
			Log.i(LOG, "Number: " + phNumber);
			return phNumber;
		}

		@Override
		protected void onPostExecute(ArrayList<String> contacts) {
			super.onPostExecute(contacts);

			pd.cancel();

			ll.removeView(loadBtn);

			adapter = new ArrayAdapter<String>(getApplicationContext(),
					R.layout.text, contacts);

			list.setAdapter(adapter);

		}

	}

}
