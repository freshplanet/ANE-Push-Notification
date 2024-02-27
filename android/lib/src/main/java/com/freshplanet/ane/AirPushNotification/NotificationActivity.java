/**
 * Copyright 2017 FreshPlanet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freshplanet.ane.AirPushNotification;

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

		if (values != null && values.getString("params") != null)
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
		
		boolean appStarted = tryStartApp("AIRAppEntry");
		if (!appStarted) {
			Log.d(TAG, "Failed to start activity, falling back to legacy activity name");
			tryStartApp("AppEntry");
		}

		Log.d(TAG, "destroying activity");
		finish();
	}

	private boolean tryStartApp(String mainClass) {
		Intent intent;
		try {
			intent = new Intent(this, Class.forName(this.getPackageName() + "." + mainClass));
			startActivity(intent);
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
