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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class CreateNotificationTask extends AsyncTask<Void, Void, Boolean>
{

	private Context _context;
	private Map<String, String> _messageData;
	private Bitmap _picture;

	public CreateNotificationTask(Context context, Intent intent)
	{
		super();
		_context = context;
		Map<String, String> messageData = new HashMap<>();
		Bundle bundle = intent.getExtras();
		for (String key : bundle.keySet())
		{
			String convertedToString = "" + bundle.get(key);
			messageData.put(key, convertedToString);
		}

		_messageData = messageData;
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		// Try to download the picture
		try
		{
			_picture = FCMMessagingService.downloadImage(_context, _messageData);
			return _picture != null;
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
		if (_context == null || _messageData == null)
		{
			Extension.logToAIR("Couldn't create push notification: _context or _intent was null (CreateNotificationTask.onPostExecute)");
			return;
		}
		FCMMessagingService.displayMessage(_context, _picture, _messageData);
	}
}
