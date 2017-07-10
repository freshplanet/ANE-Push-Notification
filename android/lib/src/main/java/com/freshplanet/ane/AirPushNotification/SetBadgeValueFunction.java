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
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

import me.leolin.shortcutbadger.ShortcutBadger;



/**
 * Set the bage value around the app icon
 * Used only in IOS.
 * This method doesnt do anything.
 * @author titi
 *
 */
public class SetBadgeValueFunction implements FREFunction {

	private static String TAG = "setBadgeNb";

	public FREObject call(FREContext context, FREObject[] args) {
		// TODO Auto-generated method stub

		if (args == null || args.length == 0)
		{
			Log.e(TAG, "No badge number provided. Cannot set badge number.");
			return null;
		}
		int badgeNumber = 0;
		try {
			badgeNumber = args[0].getAsInt();
		} catch (Exception e) {
			Log.e(TAG, "Wrong object passed for badge number. Object expected : int. Cannot set badge number.");
			return null;
		}

		Context appContext = context.getActivity().getApplicationContext();

		if(badgeNumber == 0)
		{
			ShortcutBadger.removeCount(appContext);
		}
		else
		{
			ShortcutBadger.applyCount(appContext, badgeNumber);
		}

		return null;
	}

}
