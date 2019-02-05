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

import android.support.annotation.NonNull;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class ExtensionContext extends FREContext {

	public static FCMMessagingService messagingService;

	public ExtensionContext() {

    }
	
	@Override
	public void dispose() {
		Extension.context = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions() {

		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		
		functionMap.put("registerPush", regPushFunc);
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
		functionMap.put("createNotificationChannel", new CreateNotificationChannel());
		functionMap.put("checkNotificationChannelEnabled", new CheckNotificationChannelEnabled());

		return functionMap;
	}

	private final FREFunction regPushFunc = new FREFunction() {
		@Override
		public FREObject call(FREContext freContext, FREObject[] freObjects) {
		try {
				Log.i("firebase", "firebase will try");
				Task<InstanceIdResult> task = FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {

					@Override
					public void onComplete(@NonNull Task<InstanceIdResult> task) {
						if (!task.isSuccessful()) {
							Log.w("firebase", "getInstanceId failed", task.getException());
							return;
						}
						// Get new Instance ID token
						String token = task.getResult().getToken();

						Log.i("firebase", "got token: " + token);
					}
				});

				Boolean complete = task.isComplete();
				Boolean canceled = task.isCanceled();
				Boolean successful = task.isSuccessful();
			} catch (Exception e) {
				Log.e("firebase", "error", e);
			}
			return null;
		}
	};
}