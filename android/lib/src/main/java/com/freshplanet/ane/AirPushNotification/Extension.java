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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Extension implements FREExtension {

	private static String TAG = "AirPushNotification";

	public static final String PREFS_NAME = "UrlPrefFile";
	public static final String PREFS_KEY = "trackUrl";

	public static final String PREFS_NOTIFICATION_NAME = "notifIdsFile";
	public static final String PREFS_NOTIFICATION_ID = "notificationId";

	private static String storedToken;
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
		if (storedToken != null) {
			context.dispatchStatusEventAsync("TOKEN_SUCCESS", storedToken);
			storedToken = null;
		}
		return context;
	}

	public void dispose()
	{
		context = null;
	}
	
	public void initialize() {

	}
	
	public static void logToAIR(String message)
	{
		Log.d(TAG, message);
		
		if (context != null)
		{
			context.dispatchStatusEventAsync("LOGGING", message);
		}
	}

	public static String getParametersFromMessage(Map<String, String> messageData)
	{
		JSONObject paramsJson = new JSONObject();
		String parameters = messageData.get("parameters");
		try
		{
			for (Map.Entry<String, String> entry: messageData.entrySet())
			{
				paramsJson.put(entry.getKey(), entry.getValue());
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

	public static void setToken(String token)
	{
		// This can happen before the context is created
		if (context != null) {
			context.dispatchStatusEventAsync("TOKEN_SUCCESS", token);
		} else {
			storedToken = token;
		}
	}

	public static void trackNotification(Context context, Map<String, String> messageData)
	{
		// retrieve stored url
		SharedPreferences settings = context.getSharedPreferences(Extension.PREFS_NAME, Context.MODE_PRIVATE);
		String trackingUrl = settings.getString(Extension.PREFS_KEY, null);
		if (trackingUrl != null)
		{
			String notifTrackingType = messageData.get("type");
			String notifSender = messageData.get("sender");
			String category = messageData.get("android_channel_id");
			String trackingId = messageData.get("trackingId");

			String linkParam = "/?source_type=notif&source_ref="+notifTrackingType;
			if (notifSender != null)
			{
				linkParam += "&source_userId="+notifSender;
			}

			if(Extension.isInForeground) {
				linkParam += "&appState=active";
			}
			else {
				linkParam += "&appState=inactive";
			}

			if(category != null) {
				linkParam += "&category="+category;
			}

			if(trackingId != null) {
				linkParam += "&trackingId="+trackingId;
			}

			try {
				trackingUrl += "&link="+ URLEncoder.encode(linkParam, "UTF-8");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			PingUrlTask task = new PingUrlTask();
			task.execute(trackingUrl);

		} else
		{
			Extension.logToAIR("couldn't find stored tracking url");
		}
	}
}