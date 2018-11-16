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

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Extension implements FREExtension {

	private static String TAG = "AirPushNotification";

	public static final String PREFS_NAME = "UrlPrefFile";
	public static final String PREFS_KEY = "trackUrl";

	public static final String PREFS_NOTIFICATION_NAME = "notifIdsFile";
	public static final String PREFS_NOTIFICATION_ID = "notificationId";


	private static AtomicInteger atomicInt;
	public static ExtensionContext context;
	
	public static boolean isInForeground = false;

	public static int getNotificationID(Context context) {

		SharedPreferences settings = context.getSharedPreferences(Extension.PREFS_NOTIFICATION_NAME, Context.MODE_PRIVATE);
		if(atomicInt == null) {
			String notificationIdString = settings.getString(Extension.PREFS_NOTIFICATION_ID, null);
			if(notificationIdString != null) {
				atomicInt = new AtomicInteger(Integer.valueOf(notificationIdString));
			}
			else{
				atomicInt = new AtomicInteger(1);
			}

		}

		int id = atomicInt.incrementAndGet();
		SharedPreferences.Editor editor = settings.edit();
		if(id > 1000000) { // do not grow too much
			id = 0;
		}
		editor.putString(Extension.PREFS_NOTIFICATION_ID, Integer.toString((id+1)));
		editor.commit();
		return id;
	}

	public static int getSummaryID(Context context, String groupId) {

		SharedPreferences settings = context.getSharedPreferences(Extension.PREFS_NOTIFICATION_NAME, Context.MODE_PRIVATE);
		// check saved notification ID based on groupId
		String summaryIdString = settings.getString(groupId, null);

		// if it exists - return that one
		if(summaryIdString != null) {
			return Integer.valueOf(summaryIdString);
		}
		// if none exists - get new one and save it
		int newID = getNotificationID(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(groupId, Integer.toString((newID)));
		editor.commit();

		return newID;
	}

	public static Boolean isSummaryID(Context context, String groupId, int notificationId) {

		SharedPreferences settings = context.getSharedPreferences(Extension.PREFS_NOTIFICATION_NAME, Context.MODE_PRIVATE);
		// check saved notification ID based on groupId
		String summaryIdString = settings.getString(groupId, null);
		return summaryIdString != null && Integer.valueOf(summaryIdString) == notificationId;
	}

	public FREContext createContext(String extId)
	{
		context = new ExtensionContext();
		return context;
	}

	public void dispose()
	{
		context = null;
	}
	
	public void initialize() {

	}
	
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
				String convertedToString = "" + bundle.get(key);
				paramsJson.put(key, convertedToString);
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