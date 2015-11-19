package com.freshplanet.nativeExtensions;

import android.content.Context;
import android.content.Intent;

import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazon.device.messaging.ADMMessageReceiver;

public class ADMBroadcastReceiver extends ADMMessageHandlerBase {

	public static class Receiver extends ADMMessageReceiver
	{
		public Receiver()
		{
			super(ADMBroadcastReceiver.class);
		}
	}
	
    public ADMBroadcastReceiver() {
        super(ADMBroadcastReceiver.class.getName());
    }

    public ADMBroadcastReceiver(final String className) {
        super(className);
    }
	
	
	@Override
	protected void onMessage(Intent intent) {

		// Extract message content from set of extras from 
		// com.amazon.device.messaging.intent.RECEIVE intent
		
		Extension.log("on message");

		
		try
		{
			String params = Extension.getParametersFromIntent(intent);
			
			Context context = getApplicationContext();
			
			Extension.log("Received push notification with parameters: " + params);
			
			// Build the notification if the app is not in foreground, otherwise just dispatch an event
			if (!Extension.isInForeground)
			{
				if (C2DMBroadcastReceiver.USE_MULTI_MSG && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
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

	@Override
	protected void onRegistered(String token) {

		Extension.log("Registered successfully with token: " + token);
		if (Extension.context == null) return;
		
		Extension.context.dispatchStatusEventAsync("TOKEN_SUCCESS", token);
	}

	@Override
	protected void onRegistrationError(String errorId) {
		
		Extension.log("Error registering " + errorId);
		Extension.context.dispatchStatusEventAsync("TOKEN_FAIL", errorId);
	}

	@Override
	protected void onUnregistered(String token) {
	
		Extension.log("Unregistered successfully");
		Extension.context.dispatchStatusEventAsync("UNREGISTERED", token);
	}

}
