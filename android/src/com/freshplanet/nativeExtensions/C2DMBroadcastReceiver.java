//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

package com.freshplanet.nativeExtensions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class C2DMBroadcastReceiver extends BroadcastReceiver
{	
	public static final boolean USE_MULTI_MSG = false;
	
	public C2DMBroadcastReceiver() {}
	
	/**
	 * When a cd2m intent is received by the device.
	 * Filter the type of intent.
	 * <ul>
	 * <li>REGISTRATION : get the token and send it to the AS</li>
	 * <li>RECEIVE : create a notification with the intent parameters</li>
	 * </ul>
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION"))
		{
			handleRegistration(context, intent);
		}
		else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
		{ 
			handleMessage(context, intent); // display the notification only when in background
		}
		setResultCode(Activity.RESULT_OK);
	}

	/** Check if there is a registration_id, and pass it to the AS. Send a TOKEN_FAIL event if there is an error. */
	private void handleRegistration(Context context, Intent intent)
	{
		if (Extension.context == null) return;
		
		String error = intent.getStringExtra("error");
		String unregistered = intent.getStringExtra("unregistered");
		String token = intent.getStringExtra("registration_id");
		
		if (error != null)
		{
			Extension.log("Registration failed with error: " + error);
			Extension.context.dispatchStatusEventAsync("TOKEN_FAIL", error);
		}
		else if (unregistered != null)
		{
			Extension.log("Unregistered successfully");
			Extension.context.dispatchStatusEventAsync("UNREGISTERED", unregistered);
		}
		else if (token != null)
		{
			Extension.log("Registered successfully with token: " + token);
			Extension.context.dispatchStatusEventAsync("TOKEN_SUCCESS", token);
		}
	}
	
	/** Get the parameters from the message and create a notification from it. */
	public void handleMessage(Context context, Intent intent)
	{
		try
		{
			String params = Extension.getParametersFromIntent(intent);
			
			Extension.log("Received push notification with parameters: " + params);
			
			// Build the notification if the app is not in foreground, otherwise just dispatch an event
			if (!Extension.isInForeground)
			{
				if (USE_MULTI_MSG && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
				{
					MultiMsgNotification msg = MultiMsgNotification.Instance(context);
					msg.makeBigNotif(context, intent);
				}
				else
				{
					new CreateNotificationTask(context, intent).execute();
				}
			}
			else if (Extension.context != null)
			{
				Extension.context.dispatchStatusEventAsync("NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND", params);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}