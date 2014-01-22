package com.freshplanet.nativeExtensions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotificationActivity extends Activity {

	private static String TAG = "NotifActivity";
	
	public static String notifParams;
	public static Boolean isComingFromNotification;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "create notif activity");

		
		isComingFromNotification = false;
		
		Bundle values = this.getIntent().getExtras();
		
		if (C2DMBroadcastReceiver.USE_MULTI_MSG && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) 
		{
			if (values.getString("allclean").equals("true"))
				{
					MultiMsgNotification msg = MultiMsgNotification.Instance(this);
					msg.remove();
				}
		}

		if (values.getString("params") != null)
		{
			Log.d(TAG, "notif has params: " + values.getString("params"));
			
			isComingFromNotification = true;
			notifParams = values.getString("params");
			
			if (Extension.context != null)
			{
				Log.d(TAG, "context exists "+notifParams);
				Extension.context.dispatchStatusEventAsync("APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION", notifParams);
				isComingFromNotification = false;
				notifParams = null;
			}
			
		}
		
		Intent intent;
		try {
			intent = new Intent(this, Class.forName(this.getPackageName() + ".AppEntry"));
			startActivity(intent);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "destroying activity");
		finish();
	}
	
	
}
