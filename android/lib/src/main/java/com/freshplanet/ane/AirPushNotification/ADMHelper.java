/*
 * [ADMHelper.java]
 *
 * (c) 2019, Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */
package com.freshplanet.ane.AirPushNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * The ADMHelper class ia a helper class.
 *
 * @version Revision: 1, Date: 11/20/2019
 */
public class ADMHelper {

    public static final String ADM_CLASSNAME = "com.amazon.device.messaging.ADM";
    public static final String ADMV2_HANDLER = "com.amazon.device.messaging.ADMMessageHandlerJobBase";
    public static final int JOB_ID = 1324124;
    public static final String CHANNEL_ID = "notification_channel";
    public static final String CHANNEL_NAME = "Notification Channel";

    public static final boolean IS_ADM_AVAILABLE;
    public static final boolean IS_ADM_V2;

    private static boolean isClassAvailable(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static {
        IS_ADM_AVAILABLE = isClassAvailable(ADM_CLASSNAME);
        IS_ADM_V2 = IS_ADM_AVAILABLE ? isClassAvailable(ADMV2_HANDLER) : false;
    }
}
