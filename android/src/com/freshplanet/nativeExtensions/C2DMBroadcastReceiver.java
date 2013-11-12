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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import android.widget.RemoteViews;
import android.widget.TextView;

import com.adobe.fre.FREContext;
import com.distriqt.extension.util.Resources;

public class C2DMBroadcastReceiver extends BroadcastReceiver {

	private static String TAG = "c2dmBdcastRcvr";

	private static int notificationIcon;
	private static int customLayout;
	private static int customLayoutTitle;
	private static int customLayoutDescription;
	private static int customLayoutImageContainer;
	private static int customLayoutImage;
	
	private static C2DMBroadcastReceiver instance;
	
	private static final boolean USE_MULTI_MSG = false;
	
	public C2DMBroadcastReceiver() {
		
		Log.d(TAG, "Broadcast receiver started!!!!!");
	}

	public static C2DMBroadcastReceiver getInstance()
	{
		return instance != null ? instance : new C2DMBroadcastReceiver();
	}
	
	/**
	 * When a cd2m intent is received by the device.
	 * Filter the type of intent.
	 * <ul>
	 * <li>REGISTRATION : get the token and send it to the AS</li>
	 * <li>RECEIVE : create a notification with the intent parameters</li>
	 * </ul>
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				"com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals(
				"com.google.android.c2dm.intent.RECEIVE")) { 
			handleMessage(context, intent);//display the notification only when in background
		}
	}

	/**
	 * Check if there is a registration_id, and pass it to the AS.
	 * Send a TOKEN_FAIL event if there is an error.
	 * @param context 
	 * @param intent
	 */
	private void handleRegistration(Context context, Intent intent) {
		FREContext freContext = C2DMExtension.context;
		String registration = intent.getStringExtra("registration_id");

		if (intent.getStringExtra("error") != null) {
			String error = intent.getStringExtra("error");
			Log.d(TAG, "Registration failed with error: " + error);
			if (freContext != null) {
				freContext.dispatchStatusEventAsync("TOKEN_FAIL", error);
			}
		} else if (intent.getStringExtra("unregistered") != null) {
			Log.d(TAG, "Unregistered successfully");
			if (freContext != null) {
				freContext.dispatchStatusEventAsync("UNREGISTERED",
						"unregistered");
			}
		} else if (registration != null) {
			Log.d(TAG, "Registered successfully");
			if (freContext != null) {
				freContext.dispatchStatusEventAsync("TOKEN_SUCCESS", registration);
			}
		}
	}
	
	
	private static int NotifId = 1;
	private static Map<String,String> chatList = new HashMap<String,String>();
		
	public static void registerResources(Context context)
	{
		notificationIcon = Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon36");
		customLayout = Resources.getResourseIdByName(context.getPackageName(), "layout", "notification");
		customLayoutTitle = Resources.getResourseIdByName(context.getPackageName(), "id", "title");
		customLayoutDescription = Resources.getResourseIdByName(context.getPackageName(), "id", "text");
		customLayoutImageContainer = Resources.getResourseIdByName(context.getPackageName(), "id", "image");
		customLayoutImage = Resources.getResourseIdByName(context.getPackageName(), "drawable", "app_icon");
	}
	
	
	/**
	 * Get the parameters from the message and create a notification from it.
	 * @param context
	 * @param intent
	 */
	public void handleMessage(Context context, Intent intent) {
		try {
			
			Log.d(TAG, "handleMessage");
			// json string
			String parameters = intent.getStringExtra("parameters");
			
			if (!C2DMExtension.isInForeground)
			{
				Log.d(TAG, "display notif");
				extractColors(context);
				registerResources(context);
				if (USE_MULTI_MSG && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
				{
					MultiMsgNotification msg = MultiMsgNotification.Instance(context);
					msg.makeBigNotif(context, intent, parameters);
				}
				else
				{
					createNotificationMessage(context, intent, parameters);
				}
			}
			
			FREContext ctxt = C2DMExtension.context;

			if (ctxt != null)
			{
				parameters = parameters == null ? "" : parameters;
				if (C2DMExtension.isInForeground)
				{
					Log.d(TAG, "dispatch event notif in foreground "+parameters);
					ctxt.dispatchStatusEventAsync("NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND", parameters);
				} else
				{
					Log.d(TAG, "dispatch event logging");
					ctxt.dispatchStatusEventAsync("LOGGING", parameters);
				}
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Error handling message:", e);
		}
	}
	
	private void createNotificationMessage(Context context, Intent intent, String parameters)
	{
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// icon is required for notification.
		// @see http://developer.android.com/guide/practices/ui_guidelines/icon_design_status_bar.html

		int icon = notificationIcon;
		long when = System.currentTimeMillis();

		
		JSONObject object = null;
		String pictureUrl = null;
		if (parameters != null)
		{
			try
			{
				object = (JSONObject) new JSONTokener(parameters).nextValue();
				if (object != null)
				{
					if(object.has("pictureUrl"))
					{
						pictureUrl = object.getString("pictureUrl");
					}
					else if(object.has("facebookId"))
					{
						pictureUrl = "http://graph.facebook.com/"+object.getString("facebookId")+"/picture?type=normal";
					}
				}

			} catch (Exception e)	
			{
				Log.d(TAG, "cannot parse the object");
			}
		}

		CharSequence tickerText = intent.getStringExtra("tickerText");
		CharSequence contentTitle = intent.getStringExtra("contentTitle");
		CharSequence contentText = intent.getStringExtra("contentText");
		
		Intent notificationIntent = null;
		PendingIntent contentIntent = null;
		notificationIntent = new Intent(context, NotificationActivity.class);
		Log.d(TAG, "Creating NotificationActivity intent with parameters: " + parameters);
		notificationIntent.putExtra("params", parameters);
		contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		RemoteViews contentView = new RemoteViews(context.getPackageName(), customLayout);
		contentView.setTextViewText(customLayoutTitle, contentTitle);
		contentView.setTextViewText(customLayoutDescription, contentText);
		if (notification_text_color != null)
		{
			contentView.setTextColor(customLayoutTitle, notification_text_color);
		}
		
		contentView.setFloat(customLayoutTitle, "setTextSize", notification_title_size_factor*notification_text_size);
		if (notification_text_color != null)
		{
			contentView.setTextColor(customLayoutDescription, notification_text_color);
		}
		contentView.setFloat(customLayoutDescription, "setTextSize", notification_description_size_factor*notification_text_size);
		
		existInNotifList(intent);
		if (pictureUrl != null)
		{
			CreateNotificationTask cNT = new CreateNotificationTask();
			cNT.setParams(customLayoutImageContainer, NotifId, nm, notification, contentView);
			URL url;
			try {
				url = new URL(pictureUrl);
				cNT.execute(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else
		{
			contentView.setImageViewResource(customLayoutImageContainer, customLayoutImage);
			notification.contentView = contentView;
			nm.notify(NotifId, notification);
		}
	}

	private static int notifIdCursor = 1;
	public boolean existInNotifList(Intent intent)
	{
		String senderID = intent.getStringExtra("contentTitle");
		if (chatList.containsKey(senderID))
		{
			NotifId = Integer.parseInt(chatList.get(senderID));
			return true;
		}
		else
		{
			notifIdCursor++;
			NotifId = notifIdCursor;
			chatList.put(senderID, notifIdCursor+"");
			return false;
		}

	}
	
	
	private static Integer notification_text_color = null;
	private static float notification_text_size;
	private static float notification_title_size_factor = (float) 1.0;
	private static float notification_description_size_factor = (float) 0.8;
	private static final String COLOR_SEARCH_RECURSE_TIP = "SOME_SAMPLE_TEXT";

	private boolean recurseGroup(Context context, ViewGroup gp)
	{
	    final int count = gp.getChildCount();
	    for (int i = 0; i < count; ++i)
	    {
	        if (gp.getChildAt(i) instanceof TextView)
	        {
	            final TextView text = (TextView) gp.getChildAt(i);
	            final String szText = text.getText().toString();
	            if (COLOR_SEARCH_RECURSE_TIP.equals(szText))
	            {
	                notification_text_color = text.getTextColors().getDefaultColor();
	                notification_text_size = text.getTextSize();
	                DisplayMetrics metrics = new DisplayMetrics();
	                WindowManager systemWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	                systemWM.getDefaultDisplay().getMetrics(metrics);
	                notification_text_size /= metrics.scaledDensity;
	                return true;
	            }
	        }
	        else if (gp.getChildAt(i) instanceof ViewGroup)
	        {
	        	boolean value = recurseGroup((Context) context, (ViewGroup) gp.getChildAt(i));
	        	if (value)
	        	{
	        		return true;
	        	}
	        }
	    }
	    return false;
	}

	private void extractColors(Context context)
	{
	    if (notification_text_color != null)
	        return;

	    try
	    {
	        Notification ntf = new Notification();
	        ntf.setLatestEventInfo(context, COLOR_SEARCH_RECURSE_TIP, "Utest", null);
	        LinearLayout group = new LinearLayout(context);
	        ViewGroup event = (ViewGroup) ntf.contentView.apply(context, group);
	        recurseGroup(context, event);
	        group.removeAllViews();
	    }
	    catch (Exception e)
	    {
	        notification_text_color = android.R.color.black;
	        Log.e(TAG, "Error extracting colors: ", e);
	    }
	}
	
}
