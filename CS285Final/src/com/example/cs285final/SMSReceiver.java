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
				KeyProvider.addUserKeyInfo(sender, t.getAliceKeyAgree().generateSecret("DES"), context);
				byte[] toSend = cryptographyHelper.receiveInitialHandShakePart2(t.getAliceKpair());
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(sender, "",MainActivity.COMPLETE_HANDSHAKE + new String(toSend), null, null);
			} else if(message.substring(0, 4).equals(MainActivity.COMPLETE_HANDSHAKE)){
				KeyAgreement k = cryptographyHelper.completeHandshake(message.substring(4).getBytes(), MainActivity.currentKeyAgreement);
				KeyProvider.addUserKeyInfo(sender, k.generateSecret("DES"), context);
			}
		}
		catch (Exception e){}
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
	}	
}