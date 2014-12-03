package com.example.cs285final;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
		final TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String myPhoneNumber = tMgr.getLine1Number();
		
		final Bundle bundle = intent.getExtras();
		final Object[] pdus = (Object[]) bundle.get("pdus");
		final SmsMessage[] messages = new SmsMessage[pdus.length];
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < pdus.length; i++) {
			messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			sb.append(messages[i].getMessageBody());
		}
		final String sender = messages[0].getOriginatingAddress();
		Log.d("SMSRECIEVER", "notDecoded: "+ sb.toString());
		final String message = (sb.toString());
		Log.d("SMSRECIEVER", "Decoded: " + message);
		DHKeyAgreement2 cryptographyHelper = new DHKeyAgreement2();
		try{
			Log.d("SMSRECIEVER", "entered try");
			if(message.startsWith(MainActivity.RECIEVE_INITAL)){
				Log.d("SMSRECIEVER", "started with:" + MainActivity.RECIEVE_INITAL);
				Transfer t = cryptographyHelper.receiveInitialHandShakePart1(convertToBytes(message.substring(4)));
				Log.d("BLAH 1", "BLAH 1");
				KeyProvider.addUserKeyInfo(sender, t.getAliceKeyAgree().generateSecret("DES"), context);
				Log.d("BLAH 2", "BLAH 2");

				byte[] toSend = cryptographyHelper.receiveInitialHandShakePart2(t.getAliceKpair());
				Log.d("BLAH 3", "BLAH 3");

				SmsManager sms = SmsManager.getDefault();
				Log.d("THE SENDER NUMBER IS ", myPhoneNumber);
				ArrayList<String> msgArray=sms.divideMessage((MainActivity.COMPLETE_HANDSHAKE+ Arrays.toString(toSend)));
				sms.sendMultipartTextMessage(sender, myPhoneNumber, msgArray, null, null);
			} else if(message.startsWith(MainActivity.COMPLETE_HANDSHAKE)){
				Log.d("SMSRECIEVER", "started with:" + MainActivity.COMPLETE_HANDSHAKE);
				KeyAgreement k = cryptographyHelper.completeHandshake(convertToBytes(message.substring(4)), MainActivity.currentKeyAgreement);
				KeyProvider.addUserKeyInfo(sender, k.generateSecret("DES"), context);
			}
			Log.d("SMSRECIEVER", "finished the if");
			
		}
		catch (Exception e){}
	}


	private byte[] convertToBytes(String str) {
		String[] byteValues = str.substring(1, str.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];

        for (int i=0, len=bytes.length; i<len; i++) {
           bytes[i] = Byte.parseByte(byteValues[i].trim());     
        }
        return bytes;
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