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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocalBroadcastReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			String params = Extension.getParametersFromIntent(intent);
			
			Extension.logToAIR("Received local notification with parameters: " + params);
			
			// Build the notification if the app is not in foreground, otherwise just dispatch an event
			if (!Extension.isInForeground)
			{
				new CreateNotificationTask(context, intent).execute();
			}
			else if (Extension.context != null)
			{
				Extension.context.dispatchStatusEventAsync("NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND", params);
			}
		}
		catch (Exception e)
		{

			e.printStackTrace();
			if (Extension.context != null)
			{
				Extension.context.dispatchStatusEventAsync("LOG_ISSUE", "LocalBroadcastReceiver: Unable to process local notification data : "+ e.getLocalizedMessage());
			}
		}
	}
}