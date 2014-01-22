package com.freshplanet.nativeExtensions;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class LocalNotificationService extends Service
{
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		handleMessage(this, intent);
	      
		// If we get killed, after returning from here, restart
	    return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
	      // We don't provide binding, so return null
	      return null;
	}
	
	/** Get the parameters from the message and create a notification from it. */
	public void handleMessage(Context context, Intent intent)
	{
		try
		{
			new CreateNotificationTask(context, intent).execute();
			
			if (Extension.context != null)
			{
				Extension.context.dispatchStatusEventAsync("COMING_FROM_NOTIFICATION", LocalNotificationService.getFullJsonParams(intent));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getFullJsonParams(Intent intent)
	{
		JSONObject paramsJson = new JSONObject();
		Bundle bundle = intent.getExtras();
		String parameters = intent.getStringExtra("parameters");
		try
		{
			for (String key : bundle.keySet())
			{
				paramsJson.put(key, bundle.getString(key));
			}
			
			if(parameters != null)
			{
				paramsJson.put("parameters", new JSONObject(parameters));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return paramsJson.toString();
	}
}