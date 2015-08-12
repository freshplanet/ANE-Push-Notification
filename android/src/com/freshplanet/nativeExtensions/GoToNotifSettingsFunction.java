package com.freshplanet.nativeExtensions;

import android.app.Activity;
import android.content.Intent;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

/**
 * Created by peter on 5/15/15.
 */
public class GoToNotifSettingsFunction implements FREFunction
{
    @Override
    public FREObject call(FREContext freContext, FREObject[] freObjects) {
        Activity activity = freContext.getActivity();
        activity.startActivity(new Intent(activity.getApplicationContext(), SettingsLauncherActivity.class));
        return null;
    }
}
