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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.AsyncTask;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.distriqt.extension.util.Resources;

public class CreateNotificationTask extends AsyncTask<Void, Void, Boolean>
{

	private Context _context;
	private Intent _intent;
	private Bitmap _picture;

	private String _notifSender;
	private String _notifTrackingType;
	
	public CreateNotificationTask(Context context, Intent intent)
	{
		super();
		_context = context;
		_intent = intent;
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		// Try to download the picture
		try
		{
			// Get picture URL from parameters
			String jsonParameters = _intent.getStringExtra("parameters");
			String pictureUrl = null;
			
			_notifSender = _intent.getStringExtra("sender");
			_notifTrackingType = _intent.getStringExtra("type");
			
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
			if (pictureUrl == null) return false;
			
			// Download picture
			HttpURLConnection connection = (HttpURLConnection)(new URL(pictureUrl)).openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap rawPicture = BitmapFactory.decodeStream(input);
			
			
			
			// Center-crop the picture as a square
			if (rawPicture.getWidth() >= rawPicture.getHeight())
			{
				_picture = Bitmap.createBitmap(
						rawPicture, 
						rawPicture.getWidth()/2 - rawPicture.getHeight()/2, 0,
						rawPicture.getHeight(), rawPicture.getHeight());

			}
			else
			{
				_picture = Bitmap.createBitmap(
						rawPicture,
						0, rawPicture.getHeight()/2 - rawPicture.getWidth()/2,
						rawPicture.getWidth(), rawPicture.getWidth() );
			}
			

			float density = _context.getResources().getDisplayMetrics().density;
			int finalSize = Math.round(100*density);
			
			if (rawPicture.getWidth() < finalSize)
			{
				_picture = Bitmap.createScaledBitmap(_picture, finalSize, finalSize, false);
			}
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean downloadSuccess)
	{
		if (_context == null || _intent == null)
		{
			Extension.log("Couldn't create push notification: _context or _intent was null (CreateNotificationTask.onPostExecute)");
			return;
		}


		
		// Notification texts
		CharSequence contentTitle = _intent.getStringExtra("contentTitle");
		if (contentTitle.length() > 22)
		{
			contentTitle = contentTitle.subSequence(0, 20) + "...";
		}
		CharSequence contentText = _intent.getStringExtra("contentText");
		CharSequence tickerText = _intent.getStringExtra("tickerText");

		String largeIconResourceId = _intent.getStringExtra("largeIconResourceId");
		String groupId = _intent.getStringExtra("groupId");
		if(groupId != null && groupId.equals("")) {
			groupId = null;
		}

		String categoryId = _intent.getStringExtra("android_channel_id");
		if(categoryId == null || categoryId.equals("")) {
			categoryId = _context.getString(Resources.getResourseIdByName(_context.getPackageName(), "string", "notification_channel_id_default"));
		}

		String categoryName = null;

		if(categoryId != null) {
			int categoryNameResourceId = Resources.getResourseIdByName(_context.getPackageName(), "string", "notification_channel_name_"+categoryId);
			categoryName = _context.getString(categoryNameResourceId);
		}

		// Notification images
		int smallIconId = Resources.getResourseIdByName(_context.getPackageName(), "drawable", "status_icon");
		int largeIconId = Resources.getResourseIdByName(_context.getPackageName(), "drawable", "app_icon");
		if (largeIconResourceId != null)
		{
			largeIconId = Resources.getResourseIdByName(_context.getPackageName(), "drawable", largeIconResourceId);
		}

		Bitmap largeIcon;
		if (downloadSuccess)
		{
			largeIcon = _picture;
		}
		else
		{
			largeIcon = BitmapFactory.decodeResource(_context.getResources(), largeIconId);
		}

		// rounded picture for lollipop
		if (largeIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			largeIcon = getCircleBitmap(largeIcon);
		}

		// Notification sound
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


		Intent notificationIntent = new Intent(_context, NotificationActivity.class);;
		notificationIntent.putExtra("params", Extension.getParametersFromIntent(_intent));
		PendingIntent contentIntent = PendingIntent.getActivity(_context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Dispatch notification
		NotificationManager notifManager = (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder;

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && categoryId != null && categoryName != null){
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel notificationChannel = new NotificationChannel(categoryId, categoryName ,importance);
			notificationChannel.enableLights(true);
			notificationChannel.enableVibration(true);
			notifManager.createNotificationChannel(notificationChannel);

			builder = new NotificationCompat.Builder(_context, categoryId);
		}
		else {
			builder = new NotificationCompat.Builder(_context);
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
				.setContentIntent(contentIntent);


		int notificationId = Extension.getNotificationID(_context);

		Notification notification = builder.build();
		notifManager.notify(notificationId, notification);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && groupId != null) {

			ArrayList<StatusBarNotification> groupedNotifications = new ArrayList<>();
			for (StatusBarNotification sbn : notifManager.getActiveNotifications()) {
				// add any previously sent notifications with a group that matches our RemoteNotification
				// and exclude any previously sent stack notifications

				if (groupId.equals(sbn.getNotification().getGroup())) {
					groupedNotifications.add(sbn);
				}
			}

			if (groupedNotifications.size() > 1) {
				NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
				for (StatusBarNotification activeSbn : groupedNotifications) {
					String stackNotificationLine = (String)activeSbn.getNotification().extras.get(NotificationCompat.EXTRA_TITLE);
					if (stackNotificationLine != null) {
						inbox.addLine(stackNotificationLine);
					}
				}

				inbox.setSummaryText(categoryName);

				builder = new NotificationCompat.Builder(_context, categoryId);
				builder.setContentTitle(contentTitle);
				builder.setContentText(contentText);
				builder.setStyle(inbox)
						.setSmallIcon(smallIconId)
						.setLargeIcon(largeIcon)
						.setSound(soundUri)
						.setWhen(System.currentTimeMillis())
						.setAutoCancel(true)
						.setColor(0xFF2DA9F9)
						.setGroup(groupId)
						.setGroupSummary(true);

				notifManager.notify(Extension.getNotificationID(_context), builder.build());

			}

		}


		trackNotification();

	}

	private void trackNotification()
	{

		// retrieve stored url
		SharedPreferences settings = _context.getSharedPreferences(Extension.PREFS_NAME, Context.MODE_PRIVATE);
		String trackingUrl = settings.getString(Extension.PREFS_KEY, null);
		if (trackingUrl != null)
		{
			
			String linkParam = "/?source_type=notif&source_ref="+_notifTrackingType;
			if (_notifSender != null)
			{
				linkParam += "&source_userId="+_notifSender;
			}
			
			try {
				trackingUrl += "&link="+URLEncoder.encode(linkParam, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
			PingUrlTask task = new PingUrlTask();
			task.execute(trackingUrl);
			
		} else
		{
			Extension.log("couldn't find stored tracking url");
		}
	}
	
	private Bitmap getCircleBitmap(Bitmap bitmap) 
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
	
}
