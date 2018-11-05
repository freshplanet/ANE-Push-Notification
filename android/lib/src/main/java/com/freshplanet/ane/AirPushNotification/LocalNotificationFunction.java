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


package com.freshplanet.ane.AirPushNotification;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class LocalNotificationFunction implements FREFunction {

	private static String TAG = "LocalNotificationFunction"; 
	
	private static int RECURRENCE_NONE = 0;
	private static int RECURRENCE_DAY = 1;
	private static int RECURRENCE_WEEK = 2;
	private static int RECURRENCE_MONTH = 3;
	private static int RECURRENCE_YEAR = 4;
	
	private static int DEFAULT_NOTIFICATION_ID = 192837;
	
	/**
	 * Doesn't do anything for now. just to ensure iOS compatibility. 
	 *
	 */
	public FREObject call(FREContext arg0, FREObject[] arg1) {

		if(Build.MANUFACTURER.equals("Amazon")) {
			Log.d(TAG, "push notifications disabled on amazon devices, ignoring local notification");
			return null;
		}
		
		String message = null;
		long timestamp = 0;
		String title = null;
		int recurrenceType = RECURRENCE_NONE;
		int notificationId = DEFAULT_NOTIFICATION_ID;
		String largeIconResourceId = null;
		String groupId = null;
		String categoryId = null;
		try {
			message = arg1[0].getAsString();
			timestamp = (long) arg1[1].getAsInt();
			title = arg1[2].getAsString();
			
			if (arg1.length >= 4)
			{
				recurrenceType = arg1[3].getAsInt();
			}
			
			if (arg1.length >= 5)
			{
				notificationId = arg1[4].getAsInt();
			}
			
			if (arg1.length >= 7 && arg1[6] != null)
			{
				largeIconResourceId = arg1[6].getAsString();
			}

			if (arg1.length >= 8 && arg1[7] != null)
			{
				groupId = arg1[7].getAsString();
			}

			if (arg1.length >= 9 && arg1[8] != null)
			{
				categoryId = arg1[8].getAsString();
			}
			
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FRETypeMismatchException e) {
			e.printStackTrace();
		} catch (FREInvalidObjectException e) {
			e.printStackTrace();
		} catch (FREWrongThreadException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ((groupId == null || groupId.equals("")) && categoryId != null) {
			groupId = categoryId;
		}

		
		if (message != null && timestamp > 0)
		{
			Context appContext = arg0.getActivity().getApplicationContext();
			timestamp *= 1000; // convert in ms
			Calendar cal = Calendar.getInstance();
			if (cal.getTimeInMillis() > timestamp)
			{
				Log.d(TAG, "timestamp is older than current time");
				return null;
			}
			
			Intent intent = new Intent(appContext, LocalBroadcastReceiver.class);
			
			if (title != null)
			{
				intent.putExtra("contentTitle", title);
			}
			intent.putExtra("contentText", message);
			
			if (largeIconResourceId != null)
			{
				intent.putExtra("largeIconResourceId", largeIconResourceId);
			}
			if (groupId != null)
			{
				intent.putExtra("groupId", groupId);
			}

			if (categoryId != null)
			{
				intent.putExtra("categoryId", categoryId);
			}

			
			PendingIntent sender = PendingIntent.getBroadcast(appContext, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
			
			am.cancel(sender);

			if (recurrenceType == RECURRENCE_DAY)
			{
				am.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, 24*3600*1000, sender);
			} else if (recurrenceType == RECURRENCE_WEEK) {
				am.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, 7*24*3600*1000, sender);
			} else if (recurrenceType == RECURRENCE_MONTH) {
				am.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, 30*7*24*3600*1000, sender);

			} else if (recurrenceType == RECURRENCE_YEAR) {
				am.setRepeating(AlarmManager.RTC_WAKEUP, timestamp, 365*7*24*3600*1000, sender);

			} else
			{
				am.set(AlarmManager.RTC_WAKEUP, timestamp, sender);
			}
			Log.d(TAG, "setting params to run at "+Long.toString(timestamp));

		} else
		{
			Log.d(TAG, "cannot set local notification, not enough params");
		}
		
		 
		return null;
	}

}
