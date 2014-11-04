package com.example.cs285final;

import android.content.*;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = "Message recieved";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Object[] pdus = (Object[]) bundle.get("pdus");
		SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
		Toast.makeText(context,
				"SMS Received : " + messages.getOriginatingAddress(),
				Toast.LENGTH_LONG).show();
//		Log.i(TAG, messages.getMessageBody());
		// Toast.makeText(context, "SMS Received : "+messages.getMessageBody(),
		// Toast.LENGTH_LONG).show();
	}
}