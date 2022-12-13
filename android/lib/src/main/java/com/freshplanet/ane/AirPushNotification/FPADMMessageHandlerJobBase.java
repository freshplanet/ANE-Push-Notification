package com.freshplanet.ane.AirPushNotification;

import android.content.Context;
import android.content.Intent;

import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazon.device.messaging.ADMMessageHandlerJobBase;

public class FPADMMessageHandlerJobBase extends ADMMessageHandlerJobBase {

	public FPADMMessageHandlerJobBase()
	{
		super();
	}

	@Override
	protected void onMessage(final Context context, final Intent intent) {

		// Extract message content from set of extras from 
		// com.amazon.device.messaging.intent.RECEIVE intent
		
		Extension.logToAIR("on message");

		
		try
		{
			String params = Extension.getParametersFromIntent(intent);

			
			Extension.logToAIR("Received push notification with parameters: " + params);
			
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
		}

	}

	@Override
	protected void onRegistered(final Context context, final String registrationId) {

		Extension.logToAIR("Registered successfully with token: " + registrationId);
		if (Extension.context == null) return;
		
		Extension.context.dispatchStatusEventAsync("TOKEN_SUCCESS", registrationId);
	}

	@Override
	protected void onRegistrationError(final Context context, final String string) {
		
		Extension.logToAIR("Error registering " + string);
		Extension.context.dispatchStatusEventAsync("TOKEN_FAIL", string);
	}

	@Override
	protected void onUnregistered(final Context context, final String registrationId) {
	
		Extension.logToAIR("Unregistered successfully");
		Extension.context.dispatchStatusEventAsync("UNREGISTERED", registrationId);
	}

}
