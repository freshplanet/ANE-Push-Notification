package com.freshplanet.nativeExtensions;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.adobe.fre.FREContext;
import com.distriqt.extension.util.Resources;

public class LocalNotificationService extends Service {

	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		FREContext context = C2DMExtension.context;
		if (context != null)
		{
			Log.d("LocalNService", "context not null");
		} else
		{
			Log.d("LocalNService", "context is null");
		}

		handleMessage(this, intent);
	      
		// If we get killed, after returning from here, restart
	    return START_STICKY;
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	}
	  
	  @Override
	  public void onDestroy() {
	  }

	private static String TAG = "c2dmBdcastRcvrLcl";

	private static int notificationIcon;
	private static int customLayout;
	private static int customLayoutTitle;
	private static int customLayoutDescription;
	private static int customLayoutImageContainer;
	private static int customLayoutImage;

	
	private static int NotifId = 1;
	
	private void registerResources(Context context)
	{
		notificationIcon = Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon_status");
		customLayout = Resources.getResourseIdByName(context.getPackageName(), "layout", "notification");
		customLayoutTitle = Resources.getResourseIdByName(context.getPackageName(), "id", "title");
		customLayoutDescription = Resources.getResourseIdByName(context.getPackageName(), "id", "text");
		customLayoutImageContainer = Resources.getResourseIdByName(context.getPackageName(), "id", "image");
		customLayoutImage = Resources.getResourseIdByName(context.getPackageName(), "drawable", "app_icon");
	}
	
	
	/**
	 * Get the parameters from the message and create a notification from it.
	 * @param context
	 * @param intent
	 */
	public void handleMessage(Context context, Intent intent) {
		try {
			
			Log.d("LocalNService", "registering resources");
			registerResources(context);
			
			Log.d("LocalNService", "extract colors");
			extractColors(context);
			
			FREContext ctxt = C2DMExtension.context;
			
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Log.d("LocalNService", "getting nm");

			// icon is required for notification.
			// @see http://developer.android.com/guide/practices/ui_guidelines/icon_design_status_bar.html
			
			int icon = notificationIcon;
			long when = System.currentTimeMillis();

			
			// json string
			Log.d("LocalNService", "getting extra params");

			String parameters = intent.getStringExtra("parameters");
			String facebookId = null;
			JSONObject object = null;
			if (parameters != null)
			{
				try
				{
					object = (JSONObject) new JSONTokener(parameters).nextValue();
				} catch (Exception e)	
				{
					Log.d(TAG, "cannot parse the object");
				}
			}
			if (object != null && object.has("facebookId"))
			{
				facebookId = object.getString("facebookId");
			}
			
			CharSequence tickerText = intent.getStringExtra("tickerText");
			CharSequence contentTitle = intent.getStringExtra("contentTitle");
			CharSequence contentText = intent.getStringExtra("contentText");
					
			Log.d("LocalNService", "creating intent");

			Intent notificationIntent = new Intent(context, 
					Class.forName(context.getPackageName() + ".AppEntry"));
			Log.d("LocalNService", "getting penging intent");


			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);

			Log.d("LocalNService", "getting notif");

			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			Log.d("LocalNService", "creating remove view");

			RemoteViews contentView = new RemoteViews(context.getPackageName(), customLayout);
			
			if (facebookId != null)
			{		
				Log.d(TAG, "bitmap not null");
				String src = "http://graph.facebook.com/"+facebookId+"/picture";
				URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
				contentView.setImageViewBitmap(customLayoutImageContainer, myBitmap);
			} else
			{
				Log.d(TAG, "bitmap null");
				contentView.setImageViewResource(customLayoutImageContainer, customLayoutImage);
			}
			
			
			contentView.setTextViewText(customLayoutTitle, contentTitle);
			contentView.setTextViewText(customLayoutDescription, contentText);

			contentView.setTextColor(customLayoutTitle, notification_text_color);
			contentView.setFloat(customLayoutTitle, "setTextSize", notification_text_size);
			contentView.setTextColor(customLayoutDescription, notification_text_color);
			contentView.setFloat(customLayoutDescription, "setTextSize", notification_text_size);

			notification.contentView = contentView;
			Log.d("LocalNService", "notifying");

			nm.notify(NotifId, notification);
			NotifId++;
			
			if (ctxt != null)
			{
				parameters = parameters == null ? "" : parameters;
				ctxt.dispatchStatusEventAsync("COMING_FROM_NOTIFICATION", parameters);
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Error activating application:", e);
		}
	}
	
	
	
	private static Integer notification_text_color = null;
	private static float notification_text_size = 11;
	private static final String COLOR_SEARCH_RECURSE_TIP = "SOME_SAMPLE_TEXT";

	private boolean recurseGroup(Context context, ViewGroup gp)
	{
	    final int count = gp.getChildCount();
	    for (int i = 0; i < count; ++i)
	    {
	        if (gp.getChildAt(i) instanceof TextView)
	        {
	            final TextView text = (TextView) gp.getChildAt(i);
	            final String szText = text.getText().toString();
	            if (COLOR_SEARCH_RECURSE_TIP.equals(szText))
	            {
	                notification_text_color = text.getTextColors().getDefaultColor();
	                notification_text_size = text.getTextSize();
	                DisplayMetrics metrics = new DisplayMetrics();
	                WindowManager systemWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	                systemWM.getDefaultDisplay().getMetrics(metrics);
	                notification_text_size /= metrics.scaledDensity;
	                return true;
	            }
	        }
	        else if (gp.getChildAt(i) instanceof ViewGroup)
	            return recurseGroup((Context) context, (ViewGroup) gp.getChildAt(i));
	    }
	    return false;
	}

	private void extractColors(Context context)
	{
	    if (notification_text_color != null)
	        return;

	    try
	    {
	        Notification ntf = new Notification();
	        ntf.setLatestEventInfo(context, COLOR_SEARCH_RECURSE_TIP, "Utest", null);
	        LinearLayout group = new LinearLayout(context);
	        ViewGroup event = (ViewGroup) ntf.contentView.apply(context, group);
	        recurseGroup(context, event);
	        group.removeAllViews();
	    }
	    catch (Exception e)
	    {
	        notification_text_color = android.R.color.black;
	    }
	}

}
