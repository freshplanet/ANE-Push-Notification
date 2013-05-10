package com.freshplanet.nativeExtensions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.distriqt.extension.util.Resources;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class NewCreateNotificationTask extends AsyncTask<URL, Integer, Long> {

//	
//	private int _notifyId;
//	private int _customLayoutImageContainer;
	private Notification _notification;
	private NotificationManager _nm;
	private Context _context;
	private String _imagecontainer;
	private RemoteViews _remoteviews;
	private Bitmap _bitmap;
	
	public NewCreateNotificationTask()
	{
		super();
	}

	public void setParams(Context context, RemoteViews remoteviews, String imagecontainer, Notification notification, NotificationManager nm)
	{
		_context = context;
		_remoteviews = remoteviews;
		_imagecontainer = imagecontainer;
		_notification = notification;
		_nm = nm;
	}
		
	@Override
	protected Long doInBackground(URL... urls) {
        HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) urls[0].openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        _bitmap = BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
    protected void onPostExecute(Long result) {
		_remoteviews.setImageViewBitmap(Resources.getResourseIdByName(_context.getPackageName(), "id", _imagecontainer), _bitmap);
		
		_notification.bigContentView = _remoteviews;
		NotificationManager nm = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, _notification);
		
	}
}
