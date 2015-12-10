package com.freshplanet.nativeExtensions;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
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
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;

import com.distriqt.extension.util.Resources;

public class CreateNotificationTask extends AsyncTask<Void, Void, Boolean>
{
	private static int NOTIFICATION_ID = 1;
	
	private Context _context;
	private Intent _intent;
	private Bitmap _picture;

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
		
		// Notification action
		Intent notificationIntent = new Intent(_context, NotificationActivity.class);;
		notificationIntent.putExtra("params", Extension.getParametersFromIntent(_intent));
		PendingIntent contentIntent = PendingIntent.getActivity(_context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Create notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(_context)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setTicker(tickerText)
			.setSmallIcon(smallIconId)
			.setLargeIcon(largeIcon)
			.setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
			.setWhen(System.currentTimeMillis())
			.setAutoCancel(true)
			.setColor(0xFF2DA9F9)
			.setContentIntent(contentIntent);
		
		Notification notification = builder.build();
		
		
		// Dispatch notification
		NotificationManager notifManager = (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(NOTIFICATION_ID, notification);
		NOTIFICATION_ID++;
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
