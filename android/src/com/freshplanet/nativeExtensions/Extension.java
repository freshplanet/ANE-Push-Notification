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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class Extension implements FREExtension {

	private static String TAG = "AirPushNotification";

	public static FREContext context;
	
	public static boolean isInForeground = false;
	
	public FREContext createContext(String extId)
	{
		return context = new ExtensionContext();
	}

	public void dispose()
	{
		context = null;
	}
	
	public void initialize() {}
	
	public static void log(String message)
	{
		Log.d(TAG, message);
		
		if (context != null)
		{
			context.dispatchStatusEventAsync("LOGGING", message);
		}
	}
	
	public static String getParametersFromIntent(Intent intent)
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