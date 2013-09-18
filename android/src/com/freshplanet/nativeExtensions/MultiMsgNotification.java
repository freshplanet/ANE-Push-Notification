
package com.freshplanet.nativeExtensions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.distriqt.extension.util.Resources;


public class MultiMsgNotification{
	
	private String TAG = "MultiMsgNotification";
	
	private Map<String,String> chatList;
	private int nbChat;
	private int nbMsg;
	private String senderID = "";

	private CharSequence temp1,temp2,temp3,temp4;
	Notification notification;
	private RemoteViews singleBigNotifView;
	private RemoteViews multiBigNotifView;
	private NotificationManager nm;

	private Context mContext;
	private static MultiMsgNotification instance;

	//Singleton
	public static MultiMsgNotification Instance(Context context)
	{
		if( instance == null )
			instance = new MultiMsgNotification(context);
		return instance;
	}
	
	public MultiMsgNotification(Context context)
	{
		this.mContext = context;
		
		this.initialize();
	}

	public void remove()
	{
		this.initialize();
		nm.cancelAll();
	}
	
	public void initialize() 
	{
		chatList = new HashMap<String,String>();
		chatList.clear();
		nbChat = 0;
		nbMsg = 0;
		senderID="";
		multiBigNotifView = new RemoteViews(mContext.getPackageName(), Resources.getResourseIdByName(mContext.getPackageName(), "layout", "multibignotif"));
		singleBigNotifView = new RemoteViews(mContext.getPackageName(), Resources.getResourseIdByName(mContext.getPackageName(), "layout", "singlebignotif"));
		
		nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(mContext.getPackageName(), "id", "chat1"), View.GONE);
		multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(mContext.getPackageName(), "id", "chat2"), View.GONE);
		multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(mContext.getPackageName(), "id", "chat3"), View.GONE);
		multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(mContext.getPackageName(), "id", "chat4"), View.GONE);
	}

	public boolean existInNotifList(Intent intent)
	{
		int nbMsgInChat;
		nbChat = chatList.size();
		senderID = intent.getStringExtra("contentTitle");
		if (chatList.containsKey(senderID))
		{
			nbMsgInChat = Integer.parseInt(chatList.get(senderID));
			nbMsgInChat++;
			chatList.put(senderID, nbMsgInChat+"");
			nbMsg++;
			return true;
		}
		else
		{
			chatList.put(senderID, "1");
			nbMsg++;
			nbChat++;
			return false;
		}

	}
	
	public void publishImage(Context context, RemoteViews remoteviews, String imagecontainer, String pictureUrl)
	{
		if (pictureUrl != null)
		{		
			Log.d(TAG, "pictureUrl not null");
			NewCreateNotificationTask cNT = new NewCreateNotificationTask();
			cNT.setParams(context, remoteviews, imagecontainer, notification, nm);
			URL url;
			try {
				url = new URL(pictureUrl);
				cNT.execute(url);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else
		{
			remoteviews.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", imagecontainer), Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon72"));
		}
	}

	public void makeBigNotif(Context context, Intent intent, String parameters)
	{
		boolean existornot = this.existInNotifList(intent);
		
		android.text.format.DateFormat df = new android.text.format.DateFormat();
		String when = (String) df.format("h:mmaa", new java.util.Date());
		
		JSONObject object = null;
		String pictureUrl = null;
		if (parameters != null)
		{
			try
			{
				object = (JSONObject) new JSONTokener(parameters).nextValue();
				if (object != null)
				{
					if(object.has("pictureUrl"))
					{
						pictureUrl = object.getString("pictureUrl");
					}
					else if(object.has("facebookId"))
					{
						pictureUrl = "http://graph.facebook.com/"+object.getString("facebookId")+"/picture?type=normal";
					}
				}

			} catch (Exception e)	
			{
				Log.d(TAG, "cannot parse the object");
			}
		}

		CharSequence tickerText = intent.getStringExtra("tickerText");
		CharSequence contentTitle = intent.getStringExtra("contentTitle");
		CharSequence contentText = intent.getStringExtra("contentText");

		notification=new Notification.Builder(context)
		.setTicker(tickerText)
		.setContentTitle(nbMsg+" HelloPop Messages")
		.setContentText(nbChat+" HelloPop Chats")
		.setNumber(nbMsg)
		.setSmallIcon(Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon36"))
		.setLights(Color.BLUE, 500, 500)
		.build();
		notification.ledARGB = Color.BLUE;
		Intent notificationIntent = null;
		PendingIntent contentIntent = null;
		notificationIntent = new Intent(context, NotificationActivity.class);
		notificationIntent.putExtra("params", parameters);
		notificationIntent.putExtra("allclean","true");
		contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		notification.contentIntent = contentIntent;
		
		if (nbChat == 1)
		{
			
			
			publishImage(context, singleBigNotifView, "singleimage", pictureUrl);	
			singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singletext"), contentText);
			singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singletitle"), contentTitle);
			singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singlenbmessage"), chatList.get(contentTitle));
			singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singletime"), when);
			
			multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text2"), contentText);
			multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title2"), contentTitle);
			multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage2"), chatList.get(contentTitle));
			
			temp1 = contentText;
			temp2 = contentTitle;
			temp3 = chatList.get(contentTitle);
			temp4 = pictureUrl;

			Log.i("nbChat=1","!!!!!!!!!!!!!!!!!");
			
			//notification.bigContentView = singleBigNotifView;
			
		} 
		if (nbChat > 1)
		{
			multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text1"), nbChat+" HelloPop Chats");
			multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title1"), nbMsg+" HelloPop Messages");
			multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage1"), when);
			multiBigNotifView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "image1"), Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon72"));

			if ( temp2.equals(contentTitle) )
			{
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text2"), contentText);
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage2"), chatList.get(contentTitle));
				temp1 = contentText;
				temp2 = contentTitle;
				temp3 = chatList.get(contentTitle);
				temp4 = pictureUrl;
			}
			else 
			{	
				multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat1"), View.VISIBLE);
				multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat2"), View.VISIBLE);
				multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat3"), View.VISIBLE);
				
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text3"), temp1);
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title3"), temp2);
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage3"), temp3);
				publishImage(context, multiBigNotifView, "image3", (String) temp4);
				
				temp1 = contentText;
				temp2 = contentTitle;
				temp3 = chatList.get(contentTitle);
				temp4 = pictureUrl;
				
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text2"), contentText);
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title2"), contentTitle);
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage2"), chatList.get(contentTitle));
				publishImage(context, multiBigNotifView, "image2", pictureUrl);
				
			}
			if (nbChat > 2)
			{
				multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "emit"), nbChat+"");
				multiBigNotifView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "image4"), Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon36"));
				multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat4"), View.VISIBLE);
			}
			//notification.bigContentView=multiBigNotifView;		
		}
		//nm.notify(0, notification);
	}


}
