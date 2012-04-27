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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.adobe.fre.FREContext;

public class C2DMBroadcastReceiver extends BroadcastReceiver {

	private static String TAG = "c2dmBdcastRcvr";

	public C2DMBroadcastReceiver() {
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
			handleMessage(context, intent);
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

	/**
	 * Get the parameters from the message and create a notification from it.
	 * @param context
	 * @param intent
	 */
	private void handleMessage(Context context, Intent intent) {
		try {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			// icon is required for notification.
			// @see http://developer.android.com/guide/practices/ui_guidelines/icon_design_status_bar.html
			int icon = R.drawable.icon_status;
			long when = System.currentTimeMillis();

			String parameters = intent.getStringExtra("parameters");
			CharSequence tickerText = intent.getStringExtra("tickerText");
			CharSequence contentTitle = intent.getStringExtra("contentTitle");
			CharSequence contentText = intent.getStringExtra("contentText");
						
			Intent notificationIntent = new Intent(context, 
					Class.forName(context.getPackageName() + ".AppEntry"));
			notificationIntent.setData(Uri.parse(parameters));

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);

			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			nm.notify(1, notification);
		} catch (Exception e) {
			Log.e(TAG, "Error activating application:", e);
		}
	}
}