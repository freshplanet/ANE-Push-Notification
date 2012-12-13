package com.freshplanet.nativeExtensions;

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

public class FetchStarterNotificationFunction implements FREFunction {

	private static String TAG = "NotifActivity";

	
	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		if (NotificationActivity.isComingFromNotification)
		{
			String params = NotificationActivity.notifParams;
			params = params == null ? "" : params;
			Log.d(TAG, "coming from notif :"+params);

			arg0.dispatchStatusEventAsync("APP_STARTING_FROM_NOTIFICATION", params);
		} else
		{
			Log.d(TAG, "not coming from notif");
		}
		return null;
	}

}
