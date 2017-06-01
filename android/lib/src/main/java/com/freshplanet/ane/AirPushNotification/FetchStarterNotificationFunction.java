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

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

public class FetchStarterNotificationFunction implements FREFunction {

	private static String TAG = "NotifActivity";

	
	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		if(NotificationActivity.isComingFromNotification == null) {
			Log.e(TAG, "fetchStarterNotificationFunction called before NotificationActivity was created - not supposed to happen");
			NotificationActivity.isComingFromNotification = false;
		}
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
