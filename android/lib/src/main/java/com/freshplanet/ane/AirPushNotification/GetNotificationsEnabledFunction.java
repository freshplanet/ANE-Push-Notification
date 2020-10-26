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

import androidx.core.app.NotificationManagerCompat;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.adobe.fre.FREWrongThreadException;

/**
 * TODO this may not do what I think it does, it's temp code
 */
public class GetNotificationsEnabledFunction implements FREFunction
{
	private static final String TAG = "GetNotificationsEnabledFunction";
	
    @Override
    public FREObject call(FREContext freContext, FREObject[] freObjects) {
        Context appContext = freContext.getActivity().getApplicationContext();
        Extension.context.dispatchStatusEventAsync("GET_NOTIFICATIONS_ENABLED_RESULT", (NotificationManagerCompat.from(appContext).areNotificationsEnabled() ? "true" : "false"));
        return null;
    }
}
