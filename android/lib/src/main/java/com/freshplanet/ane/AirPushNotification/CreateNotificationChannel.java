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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class CreateNotificationChannel implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		Extension.logToAIR("start storing notif tracking");




		try {

			String channelId = arg1[0].getAsString();
			String channelName = arg1[1].getAsString();
			int importance = arg1[2].getAsInt();
			boolean enableLights = arg1[3].getAsBool();
			boolean enableVibration = arg1[4].getAsBool();

			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
				Context appContext = arg0.getActivity().getApplicationContext();
				NotificationManager notifManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName ,importance);
				notificationChannel.enableLights(enableLights);
				notificationChannel.enableVibration(enableVibration);
				notifManager.createNotificationChannel(notificationChannel);
			}
			else {
				Extension.logToAIR("Notification channels available only on Android API >= 26");
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FRETypeMismatchException e) {
			e.printStackTrace();
		} catch (FREInvalidObjectException e) {
			e.printStackTrace();
		} catch (FREWrongThreadException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}



		return null;
	}

	private void storeUrl(Activity act, String url) {
		
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = act.getSharedPreferences(Extension.PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Extension.PREFS_KEY, url);
	
	    // Commit the edits!
		editor.commit();

	}
	
	
}
