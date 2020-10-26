package com.freshplanet.ane.AirPushNotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.distriqt.extension.util.Resources;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class FCMMessagingService extends FirebaseMessagingService {

    public static final String TAG = "FCMMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + data);
            if (!Extension.isInForeground) {
                displayMessage(getApplicationContext(), downloadImage(getApplicationContext(), data), data);
                Extension.trackNotification(getApplicationContext(), data);
            } else if (Extension.context != null) {
                Extension.context.dispatchStatusEventAsync("NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND", Extension.getParametersFromMessage(data));
            }
        }
        // What is this used for?
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    public static Bitmap downloadImage(Context context, Map<String, String> messageData)
    {
        // Try to download the picture
        try
        {
            // Get picture URL from parameters
            String jsonParameters = messageData.get("parameters");
            String pictureUrl = null;

            if (jsonParameters != null)
            {
                try
                {
                    JSONObject parameters = (JSONObject)new JSONTokener(jsonParameters).nextValue();
                    if (parameters != null)
                    {
                        if (parameters.has("pictureUrl"))
                        {
                            pictureUrl = parameters.getString("pictureUrl");
                        }
                        else if (parameters.has("facebookId"))
                        {
                            pictureUrl = "http://graph.facebook.com/"+parameters.getString("facebookId")+"/picture?type=normal";
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (pictureUrl == null) {
                pictureUrl = messageData.get("pictureUrl");
                if(pictureUrl == null) {
                    return null;
                }
            }


            // Download picture
            HttpURLConnection connection = (HttpURLConnection)(new URL(pictureUrl)).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap rawPicture = BitmapFactory.decodeStream(input);
            Bitmap picture;

            // Center-crop the picture as a square
            if (rawPicture.getWidth() >= rawPicture.getHeight())
            {
                picture = Bitmap.createBitmap(
                        rawPicture,
                        rawPicture.getWidth()/2 - rawPicture.getHeight()/2, 0,
                        rawPicture.getHeight(), rawPicture.getHeight());

            }
            else
            {
                picture = Bitmap.createBitmap(
                        rawPicture,
                        0, rawPicture.getHeight()/2 - rawPicture.getWidth()/2,
                        rawPicture.getWidth(), rawPicture.getWidth() );
            }

            float density = context.getResources().getDisplayMetrics().density;
            int finalSize = Math.round(100*density);

            if (rawPicture.getWidth() < finalSize)
            {
                picture = Bitmap.createScaledBitmap(picture, finalSize, finalSize, false);
            }

            return picture;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    protected static void displayMessage(Context context, Bitmap picture, Map<String, String> messageData)
    {
        if (context == null || messageData == null)
        {
            Extension.logToAIR("Couldn't create push notification: _context or _intent was null (CreateNotificationTask.onPostExecute)");
            return;
        }

        // Notification texts
        CharSequence contentTitle = messageData.get("contentTitle");
        CharSequence contentText = messageData.get("contentText");
        CharSequence tickerText = messageData.get("tickerText");

        String largeIconResourceId = messageData.get("largeIconResourceId");
        String groupId = messageData.get("groupId");
        if(groupId != null && groupId.equals("")) {
            groupId = null;
        }

        String categoryId = messageData.get("android_channel_id");
        if(categoryId == null || categoryId.equals("")) {
            try{
                categoryId = context.getString(Resources.getResourseIdByName(context.getPackageName(), "string", "notification_channel_id_default"));
            }
            catch (Exception e) {
                Log.d("AirPushNotification", "Unable to retrieve default category id");
            }
        }

        String categoryName = null;

        if(categoryId != null) {
            try {
                int categoryNameResourceId = Resources.getResourseIdByName(context.getPackageName(), "string", "notification_channel_name_" + categoryId);
                categoryName = context.getString(categoryNameResourceId);
            }
            catch (Exception e) {
                Log.d("AirPushNotification", "Unable to retrieve category name");
            }
        }

        // Notification images
        int smallIconId = Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon");
        int largeIconId = Resources.getResourseIdByName(context.getPackageName(), "drawable", "app_icon");
        if (largeIconResourceId != null)
        {
            largeIconId = Resources.getResourseIdByName(context.getPackageName(), "drawable", largeIconResourceId);
        }

        Bitmap largeIcon;
        if (picture != null)
        {
            largeIcon = picture;
        }
        else
        {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), largeIconId);
        }


        boolean makeSquare = false;//_intent.getBooleanExtra("makeSquare", false);

        // rounded picture for lollipop
        if (!makeSquare && largeIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            largeIcon = getCircleBitmap(largeIcon);
        }

        // Notification sound
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Intent notificationIntent = new Intent(context, NotificationActivity.class);;
        notificationIntent.putExtra("params", Extension.getParametersFromMessage(messageData));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Dispatch notification
        NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && categoryId != null && categoryName != null){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(categoryId, categoryName ,importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notifManager.createNotificationChannel(notificationChannel);

            builder = new NotificationCompat.Builder(context, categoryId);
        }
        else {
            builder = new NotificationCompat.Builder(context);
        }

        NotificationCompat.InboxStyle inbox;
        CharSequence summaryArg = messageData.get("summaryArg");
        String summaryText = null;
        int summaryId = -1;
        if(categoryId != null) {
            try{
                summaryId = Resources.getResourseIdByName(context.getPackageName(), "plurals", categoryId);
            }
            catch (Exception e) {
                Log.d("AirPushNotification", "Unable to retrieve summary text");
            }

        }

        // Create notification
        builder.setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(tickerText)
                .setSmallIcon(smallIconId)
                .setLargeIcon(largeIcon)
                .setSound(soundUri)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setColor(0xFF2DA9F9)
                .setGroup(groupId)
                .setContentIntent(contentIntent)
                .setGroupSummary(false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));


        int notificationId = Extension.getNotificationID(context);

        Notification notification = builder.build();
        notifManager.notify(notificationId, notification);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && groupId != null) {

            int numNotifs = 0;
            for (StatusBarNotification sbn : notifManager.getActiveNotifications()) {
                // and exclude any previously sent stack notifications
                if (groupId.equals(sbn.getNotification().getGroup()) && !Extension.isSummaryID(context, groupId, sbn.getId())) {
                    numNotifs++;
                }
            }

            inbox = new NotificationCompat.InboxStyle();
            try {
                if(summaryId >= 0) {
                    summaryText = context.getResources().getQuantityString(summaryId, numNotifs, numNotifs, summaryArg);
                    inbox.setSummaryText(summaryText);
                }
            }
            catch (Exception e) {
                Log.d("AirPushNotification", "Unable to retrieve summary text");
            }


            builder = new NotificationCompat.Builder(context, categoryId);
            builder.setContentTitle(contentTitle);
            builder.setContentText(contentText);
            builder
                    .setStyle(inbox)
                    .setSmallIcon(smallIconId)
                    .setLargeIcon(largeIcon)
                    .setSound(soundUri)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setColor(0xFF2DA9F9)
                    .setGroup(groupId)
                    .setGroupSummary(true);

            notifManager.notify(Extension.getSummaryID(context, groupId), builder.build());
        }
    }

    private static Bitmap getCircleBitmap(Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    @Override
    public void onNewToken(String s) {
        if(Extension.context != null)
            Extension.context.dispatchStatusEventAsync("TOKEN_SUCCESS", s);
    }
}