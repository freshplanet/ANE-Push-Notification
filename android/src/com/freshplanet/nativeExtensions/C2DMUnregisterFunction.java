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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

/**
 * Register the app for push notification
 * 
 * @author titi
 *
 */
public class C2DMUnregisterFunction implements FREFunction {

	private static String TAG = "c2dmUnregister";

	
	public FREObject call(FREContext context, FREObject[] args) 
	{
		if(Build.MANUFACTURER.equals("Amazon")) {
			Log.d(TAG, "push notifications disabled on amzon devices, ignoring register");
			return null;
		}
		
		Context appContext = context.getActivity().getApplicationContext();
		Log.d(TAG, "C2DMUnregisterFunction.call");
		try {
			
			Intent unregistrationIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
			unregistrationIntent.putExtra("app", PendingIntent.getBroadcast(appContext, 0, new Intent(), 0));
			appContext.startService(unregistrationIntent);

		} catch (Exception e) {
			Log.e(TAG, "Error sending unregistration intent.", e);
		}
		return null;
	}

	
}
