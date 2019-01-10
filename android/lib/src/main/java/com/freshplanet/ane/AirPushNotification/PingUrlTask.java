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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class PingUrlTask extends AsyncTask<String, Void, Boolean> {

	@Override
	protected Boolean doInBackground(String... urls) {
				
		String trackingUrl = urls[0];
		
		Extension.log("start tracking "+trackingUrl);

		try {
			URL url = new URL(trackingUrl);

			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestMethod("GET");
			urlc.setRequestProperty("User-Agent", "Android");
			urlc.setRequestProperty("Connection", "close");
			urlc.setDoInput(true);
			urlc.setConnectTimeout(1000 * 10);
			urlc.connect();

			switch (urlc.getResponseCode()) {
				case 200:
				case 201:
					BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line+"\n");
					}
					br.close();

					Extension.log("Notification tracking response: " + sb.toString());
			}

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Boolean downloadSuccess)
	{
		Extension.log("tracking complete");

	}

}
