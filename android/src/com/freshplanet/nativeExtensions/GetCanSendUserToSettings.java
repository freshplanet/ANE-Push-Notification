package com.freshplanet.nativeExtensions;

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
