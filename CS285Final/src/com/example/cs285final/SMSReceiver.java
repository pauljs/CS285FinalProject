package com.example.cs285final;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyAgreement;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.*;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = "Message recieved";

	@Override
	public void onReceive(Context context, Intent intent) {
		//if (intent.equals("SMS_RECEIVED"))
		abortBroadcast(); // stops message from going to receiver's text screen

		final Bundle bundle = intent.getExtras();
		final Object[] pdus = (Object[]) bundle.get("pdus");
		final SmsMessage[] messages = new SmsMessage[pdus.length];
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < pdus.length; i++) {
			messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			sb.append(messages[i].getMessageBody());
		}
		final String sender = messages[0].getOriginatingAddress();
		final String message = sb.toString();
		
		DHKeyAgreement2 cryptographyHelper = new DHKeyAgreement2();
		try{
			if(message.substring(0, 4).equals(MainActivity.RECIEVE_INITAL)){
				Transfer t = cryptographyHelper.receiveInitialHandShakePart1(message.substring(4).getBytes());
				//TODO: DOES THIS WORK?
				addUserKeyInfo(sender, new String(t.getAliceKeyAgree().generateSecret("DES").toString()), context);
				byte[] toSend = cryptographyHelper.receiveInitialHandShakePart2(t.getAliceKpair());
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(sender, "",MainActivity.COMPLETE_HANDSHAKE + new String(toSend), null, null);
			} else if(message.substring(0, 4).equals(MainActivity.COMPLETE_HANDSHAKE)){
				//TODO: THIS DOES NOT WORK
				//KeyAgreement temp = null;//KeyAgreement(getKey(sender, context).getBytes());
				//KeyAgreement k = cryptographyHelper.completeHandshake(message.substring(4).getBytes(), temp);
				KeyAgreement k = cryptographyHelper.completeHandshake(message.substring(4).getBytes(), MainActivity.currentKeyAgreement);
				addUserKeyInfo(sender, new String(k.generateSecret("DES").toString()), context);
			}
		}
		//To robust four U
		catch (Exception e){}
		
		
		/*
		// need to create a for loop because if message is too long it will be
		// broken up into a few messages
		final SmsMessage[] messages = new SmsMessage[pdus.length];
		final StringBuilder sb = new StringBuilder();

		sb.append("\"");
		for (int i = 0; i < pdus.length; i++) {
			messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			sb.append(messages[i].getMessageBody());
		}
		sb.append("\"");

		final String sender = messages[0].getOriginatingAddress();
		final String message = sb.toString();

		Toast.makeText(context, "SMS Received : " + message, Toast.LENGTH_LONG)
				.show();

		final TelephonyManager tMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String myPhoneNumber = tMgr.getLine1Number();
		Log.i(TAG, "My Phone Number: " + myPhoneNumber);
		Toast.makeText(context, "My Number: " + myPhoneNumber,
				Toast.LENGTH_LONG).show();
		//
		// String myPhoneNumber = "4805771280";
		// Toast.makeText(context, "My Number: " + myPhoneNumber,
		// Toast.LENGTH_LONG).show();
		//
		*/
		
	/*  INFINTE LOOOP HERE
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(myPhoneNumber, sender, message, null, null);
*/
		
		
		// Map<String, String> regMap = generateMap();
		//
		// Log.i(TAG, messages.getMessageBody());
		// if(regMap.containsKey(sender)){
		// String message = messages.getDisplayMessageBody();
		// String tag = message.substring(0, 5);
		// if(tag.equals("encry")){
		// message = message.substring(5);
		// String plainText = decrypt(message, sender, regMap.get(sender));
		// sendInternal(message, sender);
		// }
		// sendInternal(message, sender);
		// }
	}


	/***
	 * Create a Map from the shared preferences location not sure how to do this
	 * 
	 * @return return a map will all of the registered phone numbers and keys
	 */
	private Map<String, String> generateMap() {

		// TODO : THIS IS A PROBLEM
		final SharedPreferences sp = null;
		// final SharedPreferences sp =
		// PreferenceManager.getDefaultSharedPreferences(this);
		String toParse = "";
		if (sp.contains("storage")) {
			toParse = sp.getString("storage", "");
		}
		String[] contacts = toParse.split(";");
		Map<String, String> result = new ConcurrentHashMap<String, String>();
		for (String contact : contacts) {
			String[] val = contact.split(",");
			result.put(val[0], val[1]);
		}
		return result;
	}

	/**
	 * Resends the intent that message was received so that the default
	 * application can catch it and behave like it is natural
	 * 
	 * @param message
	 *            : message to be sent
	 * @param sender
	 *            : string of the phone number that initially sent sent it
	 */
	private void sendInternal(String message, String sender) {
		// TODO Auto-generated method stub
	}
	
	// TODO
		public void addUserKeyInfo(String phoneNumber, String key, Context context) {
			ContentValues values = new ContentValues();
			values.put(KeyProvider.NUMBER, phoneNumber);
			values.put(KeyProvider.KEY, key);
			Uri uri = context.getContentResolver().insert(KeyProvider.CONTENT_URI, values);
		}

		// TODO
		public String getKey(String number, Context context) {
			String URL = "content://com.example.contentprovidertest.Keys/users";
			Uri users = Uri.parse(URL);
			Cursor c = context.getContentResolver()
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
}