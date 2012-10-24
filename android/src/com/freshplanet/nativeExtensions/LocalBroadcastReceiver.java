package com.freshplanet.nativeExtensions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class LocalBroadcastReceiver extends BroadcastReceiver {

	private static String TAG = "LclBrRec";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (C2DMExtension.isInForeground)
		{
			return; // display the notification only when in background
		}
		
		Log.d(TAG, "receive local notification");

		Bundle bundle = intent.getExtras();
		String title = bundle.getString("contentTitle");
		String message = bundle.getString("contentText");

		Intent newIntent = new Intent(context, LocalNotificationService.class);
		newIntent.putExtra("contentTitle", title);
		newIntent.putExtra("contentText", message);
		context.startService(newIntent);
	}

	
	
}
