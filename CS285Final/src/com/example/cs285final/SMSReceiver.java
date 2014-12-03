package com.example.cs285final;

import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import android.content.*;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
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
				Toast.makeText(context,KeyProvider.getKey(sender, context).getEncoded().toString(), 
		                Toast.LENGTH_LONG).show();
			} else if(message.startsWith(MainActivity.COMPLETE_HANDSHAKE)){
				Log.d("SMSRECIEVER", "started with:" + MainActivity.COMPLETE_HANDSHAKE);
				KeyAgreement k = cryptographyHelper.completeHandshake(convertToBytes(message.substring(4)), MainActivity.currentKeyAgreement);
				KeyProvider.addUserKeyInfo(sender, k.generateSecret("DES"), context);
				Toast.makeText(context,KeyProvider.getKey(sender, context).getEncoded().toString(), 
		                Toast.LENGTH_LONG).show();
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
}