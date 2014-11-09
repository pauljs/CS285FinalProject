package com.example.cs285final;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.*;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

 private static final String TAG = "Message recieved";

 @Override
 public void onReceive(Context context, Intent intent) {    
     Bundle bundle = intent.getExtras();
     Object[] pdus = (Object[]) bundle.get("pdus");
     SmsMessage messages =SmsMessage.createFromPdu((byte[]) pdus[0]); 
     Toast.makeText(context, "SMS Received : "+messages.getOriginatingAddress(),
    		 Toast.LENGTH_LONG).show();
     String sender = messages.getOriginatingAddress();
     Map<String, String> regMap = generateMap();

    		 Log.i(TAG,  messages.getMessageBody());
     if(regMap.containsKey(sender)){
    	 String message = messages.getDisplayMessageBody();
    	 String tag = message.substring(0, 5);
    	 if(tag.equals("encry")){
    		 message = message.substring(5);
    		 String plainText = decryt(message, sender, regMap.get(sender));
    		 sendInternal(message, sender);
    	 }
    	 sendInternal(message, sender);
     }
  }

 /***
  * Create a Map from the shared preferences location not sure how to do this
  * @return return a map will all of the registered phone numbers and keys
  */
 private Map<String, String> generateMap() {
	 
	 // TODO : THIS IS A PROBLEM
	 final SharedPreferences sp = null;
//	 final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 	 String toParse = "";
 	if(sp.contains("storage")) {
 		 toParse = sp.getString("storage", "");
 	}
 	String[] contacts = toParse.split(";");
 	Map<String, String> result = new ConcurrentHashMap<String, String>();
 	for(String contact : contacts){
 		String[] val = contact.split(",");
 		result.put(val[0],val[1]);
 	}
 	return result;	
}

/**
  * Resends the intent that message was received so that the default
  * application can catch it and be have like it is natural
  * @param message : message to be sent
  * @param sender : string of the phone number that initially sent sent it
  */
 private void sendInternal(String message, String sender) {
	// TODO Auto-generated method stub
	
}

/**
  * looks up the key associated with the sender and decrypts the message
  * 
  * @param message : encrypted text message
  * @param sender : string of the senders phone number
  */
 private String decryt(String message, String sender, String key) {
	// TODO Auto-generated method stub
	 return null;
 }
	
}