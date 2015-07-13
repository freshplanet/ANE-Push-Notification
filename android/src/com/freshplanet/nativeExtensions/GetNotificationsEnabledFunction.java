package com.freshplanet.nativeExtensions;

import android.support.v4.app.NotificationManagerCompat;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.adobe.fre.FREWrongThreadException;

import java.util.Set;

/**
 * TODO this may not do what I think it does, it's temp code
 */
public class GetNotificationsEnabledFunction implements FREFunction
{
	private static final String TAG = "GetNotificationsEnabledFunction";
	
    @Override
    public FREObject call(FREContext freContext, FREObject[] freObjects) {        
        Boolean notifsEnabled = true;
        try {
            return FREObject.newObject(notifsEnabled);
        } catch (FREWrongThreadException e) {
            e.printStackTrace();
        }
        return null;
    }
}
