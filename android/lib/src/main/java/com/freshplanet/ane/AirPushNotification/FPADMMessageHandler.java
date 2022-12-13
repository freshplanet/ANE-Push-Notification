package com.freshplanet.ane.AirPushNotification;

import android.content.Context;
import android.content.Intent;

import com.amazon.device.messaging.ADMMessageHandlerBase;

public class FPADMMessageHandler extends ADMMessageHandlerBase {

    public FPADMMessageHandler() {
        super(FPADMMessageHandler.class.getName());
    }

    public FPADMMessageHandler(final String className) {
        super(className);
    }
	
	
	@Override
	protected void onMessage(Intent intent) {

		// Extract message content from set of extras from 
		// com.amazon.device.messaging.intent.RECEIVE intent
		
		Extension.logToAIR("on message");

		
		try
		{
			String params = Extension.getParametersFromIntent(intent);
			
			Context context = getApplicationContext();
			
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
	protected void onRegistered(String token) {

		Extension.logToAIR("Registered successfully with token: " + token);
		if (Extension.context == null) return;
		
		Extension.context.dispatchStatusEventAsync("TOKEN_SUCCESS", token);
	}

	@Override
	protected void onRegistrationError(String errorId) {
		
		Extension.logToAIR("Error registering " + errorId);
		Extension.context.dispatchStatusEventAsync("TOKEN_FAIL", errorId);
	}

	@Override
	protected void onUnregistered(String token) {
	
		Extension.logToAIR("Unregistered successfully");
		Extension.context.dispatchStatusEventAsync("UNREGISTERED", token);
	}

}
