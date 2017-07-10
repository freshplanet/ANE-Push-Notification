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

import java.util.HashMap;
import java.util.Map;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;

public class ExtensionContext extends FREContext {

	public ExtensionContext() {

    }
	
	@Override
	public void dispose() {
		Extension.context = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions() {

		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		
		functionMap.put("registerPush", new C2DMRegisterFunction());
		functionMap.put("setBadgeNb", new SetBadgeValueFunction());
		functionMap.put("sendLocalNotification", new LocalNotificationFunction());
		functionMap.put("setIsAppInForeground", new SetIsAppInForegroundFunction());
		functionMap.put("fetchStarterNotification", new FetchStarterNotificationFunction());
		functionMap.put("cancelLocalNotification", new CancelLocalNotificationFunction());
		functionMap.put("cancelAllLocalNotifications", new CancelLocalNotificationFunction());
		functionMap.put("getNotificationsEnabled", new GetNotificationsEnabledFunction());
		functionMap.put("openDeviceNotificationSettings", new GoToNotifSettingsFunction());
		functionMap.put("getCanSendUserToSettings", new GetCanSendUserToSettings());
		functionMap.put("storeNotifTrackingInfo", new StoreNotifTrackingInfo());

		return functionMap;
	}
}