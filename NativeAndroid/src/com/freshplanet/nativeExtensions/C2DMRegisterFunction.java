package com.freshplanet.nativeExtensions;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
public class C2DMRegisterFunction implements FREFunction {

	
	public FREObject call(FREContext context, FREObject[] args) 
	{
		
		if (args == null || args.length == 0)
		{
			Log.e("as3c2dm", "no email adress provided. Cannot register the device.");
			return null;
		}
		String emailAdress;
		try {
			emailAdress = args[0].getAsString();
		} catch (Exception e) {
			Log.e("as3c2dm", "Wrong object passed for email adress. Object expected : String. Cannot register the device.");
			return null;
		}
		
		if (emailAdress == null)
		{
			Log.e("as3c2dm", "emailAdress is null. Cannot register the device.");
			return null;
		}
		
		Context appContext = context.getActivity().getApplicationContext();
		Log.d("as3c2dm", "C2DMRegisterFunction.call");
		try {
			
			Intent registrationIntent = new Intent(
					"com.google.android.c2dm.intent.REGISTER");
			registrationIntent.putExtra("app",
					PendingIntent.getBroadcast(appContext, 0, new Intent(), 0));
			registrationIntent.putExtra("sender", emailAdress);
			appContext.startService(registrationIntent);
			context.dispatchStatusEventAsync("REGISTERING", "success");

		} catch (Exception e) {
			context.dispatchStatusEventAsync("REGISTERING", "error "+e.toString());

			Log.e("as3c2dm", "Error sending registration intent.", e);
		}
		return null;
	}

	
}
