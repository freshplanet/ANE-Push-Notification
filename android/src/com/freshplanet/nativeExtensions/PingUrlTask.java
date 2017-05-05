package com.freshplanet.nativeExtensions;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class PingUrlTask extends AsyncTask<String, Void, Boolean> {

	@Override
	protected Boolean doInBackground(String... urls) {
				
		String trackingUrl = urls[0];
		
		Extension.log("start tracking "+trackingUrl);

		
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)(new URL(trackingUrl)).openConnection();
			connection.setDoInput(true);
			connection.connect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
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
