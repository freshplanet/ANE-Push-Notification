package com.freshplanet.nativeExtensions;

import android.app.Activity;
import android.content.SharedPreferences;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class StoreNotifTrackingInfo implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		Extension.log("start storing notif tracking");

		if (arg1.length > 0)
		{
			String url = null;
			try {
				url = arg1[0].getAsString();
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
			
			if (url != null) {
				storeUrl(arg0.getActivity(), url);
			} else
			{
				Extension.log("url tracking is null");
			}
			
		} else
		{
			Extension.log("no arguments providing to store tracking url");
		}
		
		return null;
	}

	private void storeUrl(Activity act, String url) {
		
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = act.getSharedPreferences(Extension.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Extension.PREFS_KEY, url);
	
	    // Commit the edits!
		editor.commit();

	}
	
	
}
