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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class LocalNotificationFunction implements FREFunction {

	private static String TAG = "LocalNotificationFunction"; 
	
	/**
	 * Doesn't do anything for now. just to ensure iOS compatibility. 
	 *
	 */
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		String message = null;
		long timestamp = 0;
		String title = null;
		
		try {
			message = arg1[0].getAsString();
			timestamp = (long) arg1[1].getAsInt();
			title = arg1[2].getAsString();
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
			PendingIntent sender = PendingIntent.getBroadcast(appContext, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, timestamp, sender);
			Log.d(TAG, "setting params to run at "+Long.toString(timestamp));

		} else
		{
			Log.d(TAG, "cannot set local notification, not enough params");
		}
		
		 
		return null;
	}

}
