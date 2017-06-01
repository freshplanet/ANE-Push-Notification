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

import android.content.Intent;
import android.provider.Settings;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.adobe.fre.FREWrongThreadException;

/**
 * TODO placeholder code, don't use
 */
public class GetCanSendUserToSettings implements FREFunction
{
    private Boolean checkedCanSend = false;
    private Boolean canSend = false;
    @Override
    public FREObject call(FREContext freContext, FREObject[] freObjects) {
        if(!checkedCanSend) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            canSend = intent.resolveActivity(freContext.getActivity().getPackageManager()) != null;
            checkedCanSend = true;
        }
        try {
            return FREObject.newObject(canSend);
        } catch (FREWrongThreadException e) {
            e.printStackTrace();
        }
        return null;
    }
}
