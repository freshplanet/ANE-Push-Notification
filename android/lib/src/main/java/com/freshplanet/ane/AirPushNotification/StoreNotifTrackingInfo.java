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
