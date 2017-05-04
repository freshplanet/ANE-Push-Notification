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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.distriqt.extension.util.Resources;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.RemoteViews;

public class CreateNotificationTask_multiMsg extends AsyncTask<URL, Integer, Long>
{

	//
	// private int _notifyId;
	// private int _customLayoutImageContainer;
	private Notification _notification;
	private NotificationManager _nm;
	private Context _context;
	private String _imagecontainer;
	private RemoteViews _remoteviews;
	private Bitmap _bitmap;

	public CreateNotificationTask_multiMsg()
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
	protected Long doInBackground(URL... urls)
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) urls[0].openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			_bitmap = BitmapFactory.decodeStream(input);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onPostExecute(Long result)
	{
		_remoteviews.setImageViewBitmap(Resources.getResourseIdByName(_context.getPackageName(), "id", _imagecontainer), _bitmap);

		_notification.bigContentView = _remoteviews;
		_nm.notify(0, _notification);

	}
}
