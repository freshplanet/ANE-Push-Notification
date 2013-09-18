package com.freshplanet.nativeExtensions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.RemoteViews;

public class CreateNotificationTask extends AsyncTask<URL, Integer, Long> {

	
	
	private int _notifyId;
	private int _customLayoutImageContainer;
	private Notification _notif;
	private NotificationManager _nm;
	private RemoteViews _contentView;
	private Bitmap _bitmap;
	
	public CreateNotificationTask()
	{
		super();
	}
	
	public void setParams(int customLayoutImageContainer, int notifyId, NotificationManager _notifMan, Notification notif, RemoteViews contentView )
	{
		_customLayoutImageContainer = customLayoutImageContainer;
		_notifyId = notifyId;
		_notif = notif;
		_nm = _notifMan;
		_contentView = contentView;
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
    	int h = 100;
    	int w = 100;
    	Bitmap scaledBitmap = Bitmap.createScaledBitmap(_bitmap, w, h, true);
		_contentView.setImageViewBitmap(_customLayoutImageContainer, scaledBitmap);
		_notif.contentView = _contentView;
		_nm.notify(_notifyId, _notif);
	}


}
