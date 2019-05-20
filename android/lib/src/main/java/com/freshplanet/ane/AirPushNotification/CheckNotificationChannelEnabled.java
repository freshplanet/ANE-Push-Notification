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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class CheckNotificationChannelEnabled implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		

		if (arg1.length > 0)
		{
			String channelId = null;
			try {
				channelId = arg1[0].getAsString();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (FRETypeMismatchException e) {
				e.printStackTrace();
			} catch (FREInvalidObjectException e) {
				e.printStackTrace();
			} catch (FREWrongThreadException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Context appContext = arg0.getActivity().getApplicationContext();
			
			

			boolean isEnabled = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				if(!TextUtils.isEmpty(channelId)) {
					NotificationManager manager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
					NotificationChannel channel = manager.getNotificationChannel(channelId);
					isEnabled = channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
				}
				else
					isEnabled = false;
			} else {
				isEnabled = NotificationManagerCompat.from(appContext).areNotificationsEnabled();
			}

			try {
				return FREObject.newObject(isEnabled);
			}
			catch (FREWrongThreadException e) {
				Extension.logToAIR("unable to return value for channel enabled");
			}

		}

		return null;
	}


	
}
