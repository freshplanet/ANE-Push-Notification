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

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;

public class C2DMExtensionContext extends FREContext {

	private static String TAG = "c2dmContext";
	
	public C2DMExtensionContext() {
		Log.d(TAG, "C2DMExtensionContext.C2DMExtensionContext");
	}
	
	@Override
	public void dispose() {
		Log.d(TAG, "C2DMExtensionContext.dispose");
		C2DMExtension.context = null;
	}

	/**
	 * Registers AS function name to Java Function Class
	 */
	@Override
	public Map<String, FREFunction> getFunctions() {
		Log.d(TAG, "C2DMExtensionContext.getFunctions");
		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		functionMap.put("registerPush", new C2DMRegisterFunction());
		functionMap.put("unregisterPush", new C2DMUnregisterFunction());
		functionMap.put("setBadgeNb", new SetBadgeValueFunction());
		functionMap.put("sendLocalNotification", new LocalNotificationFunction());
		functionMap.put("setIsAppInForeground", new SetIsAppInForegroundFunction());
		return functionMap;	
	}

}
