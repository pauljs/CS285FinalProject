package com.example.cs285final;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends Activity {
    ListView list;
    LinearLayout ll;
    Button loadBtn;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ll = (LinearLayout) findViewById(R.id.LinearLayout1);

        list = (ListView) findViewById(R.id.listView1);
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view,
					final int position, final long id) {
				// TODO Auto-generated method stub
				final String info = adapter.getItem(position);
//				Intent intent = new Intent(getApplicationContext(), MessagingActivity.class).putExtra("contact_info", "info");
//				startActivity(intent);
			}
		});
        loadBtn = (Button) findViewById(R.id.button1);
        loadBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final LoadContactsAyscn lca = new LoadContactsAyscn();
                lca.execute();
            }
        });

    }
    
    //STORAGE IS ONE STRING SEPARATING CONTACTS BY SEMICOLONS AND 
    // INFORMATION WITHIN THE CONTACT BY COMMAS
    public String getStorage() {
    	final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	//storage exists so get it
    	if(sp.contains("storage")) {
    		return sp.getString("storage", "");
    	} else {//storage does not exist (AKA first time opening application)
    		Editor edit = sp.edit();
    		edit.putString("storage", "");
    		edit.commit();
    		return "";
    	}
	}
    
    //FORMAT: storage is really one string
    // contacts are separated by semicolons
    // within a contact, the phoneNumber is first then a comma to separate it from the key
    public void addToStorage(final String phoneNumber, final String key) {
    	String storage = getStorage();
    	storage = storage + ";" + phoneNumber + "," + key;
    	final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	Editor edit = sp.edit();
    	edit.putString("storage", storage);
    	while(!edit.commit()) {};
    }

    class LoadContactsAyscn extends AsyncTask<Void, Void, ArrayList<String>> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pd = ProgressDialog.show(MainActivity.this, "Loading Contacts",
                    "Please Wait");
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            // TODO Auto-generated method stub
            final ArrayList<String> contacts = new ArrayList<String>();

            final Cursor c = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    null, null, null);
            while (c.moveToNext()) {

                final String contactName = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                final String phNumber = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contacts.add(contactName + ":" + phNumber);

            }
            c.close();

            return contacts;
        }

        @Override
        protected void onPostExecute(ArrayList<String> contacts) {
            // TODO Auto-generated method stub
            super.onPostExecute(contacts);

            pd.cancel();

            ll.removeView(loadBtn);

            adapter = new ArrayAdapter<String>(
                    getApplicationContext(), R.layout.text, contacts);

            list.setAdapter(adapter);

        }

    }
    
    
    
}
 
