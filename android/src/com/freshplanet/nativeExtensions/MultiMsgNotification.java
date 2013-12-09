package com.freshplanet.nativeExtensions;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.distriqt.extension.util.Resources;

public class MultiMsgNotification //extends Activity
{

	private String TAG = "MultiMsgNotification";

	/**
	 * chatList is the storage of all the chats already displayed in the
	 * notification center.
	 * 
	 * key: chatID - String value: the number of unread messages correspondent
	 * to the chatID - Integer
	 * 
	 */
	private Map<String, ChatListItem> _chatList;
	private class ChatListItem
	{
		public String contentText;
		public String contentTitle;
		public int nbMsgInChat;
		public String pictureUrl;
		public String timeOfMsg;
		public int orderInList;
		ArrayList<String> msgList = new ArrayList<String>();
	}
	
	private Notification _notification;
	private RemoteViews _contentView;
	private RemoteViews _bigContentView;
	private NotificationManager _nm;

	private Context _mContext;
	private static MultiMsgNotification _instance;

	public static MultiMsgNotification Instance(Context context)
	{
		if (_instance == null)
			_instance = new MultiMsgNotification(context);
		return _instance;
	}

	public MultiMsgNotification(Context context)
	{
		this._mContext = context;
		_nm = (NotificationManager) _mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		this.initialize();
	}

	public void remove()
	{
		this.initialize();
		_nm.cancelAll();
	}

	public void initialize()
	{
		_chatList = new HashMap<String, ChatListItem>();

		_bigContentView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
				"layout", "bigcontentview"));
		_contentView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
				"layout", "contentview"));

	}

	/**
	 * Possible value in intent - { "type": "chat", "sender": "1230056",
	 * "sentAt": "1354219799.120", "parameters": {"pictureUrl": "http://..."}} -
	 * { "type": "chat", "sender": "1230056", "sentAt": "1354219799.120"} - {
	 * "type": "chat", "sender": "1230056", "sentAt": "1354219799.120", "group":
	 * "330012"}
	 * 
	 * @param intent
	 * @return boolean value to indicate if it's already existed in chatList
	 */
	private void addToChatList(Intent intent)
	{
		String chatID = "";
		ChatListItem chatListItem = new ChatListItem();

		if (intent.hasExtra("group"))
		{
			chatID = "group_" + intent.getStringExtra("group");
		} else
		{
			chatID = "duo_" + intent.getStringExtra("sender");
		}

		if (_chatList.containsKey(chatID))
		{
			chatListItem = _chatList.get(chatID);
			chatListItem.contentText = intent.getStringExtra("contentText").toString();
			chatListItem.contentTitle = intent.getStringExtra("contentTitle").toString();
			chatListItem.nbMsgInChat++;
			chatListItem.pictureUrl = getPictureUrl(intent);
			chatListItem.timeOfMsg = getTimeFromMessage(intent);
			chatListItem.msgList.add(chatListItem.contentText);

			// increase the order of other chats
			for (String tempKey : _chatList.keySet())
			{
				ChatListItem tempValue = _chatList.get(tempKey);
				if (tempValue.orderInList < chatListItem.orderInList)
					tempValue.orderInList++;
				_chatList.put(tempKey, tempValue);
			}
		} else
		{
			chatListItem.contentText = intent.getStringExtra("contentText").toString();
			chatListItem.contentTitle = intent.getStringExtra("contentTitle").toString();
			chatListItem.nbMsgInChat = 1;
			chatListItem.pictureUrl = getPictureUrl(intent);
			chatListItem.timeOfMsg = getTimeFromMessage(intent);
			chatListItem.msgList.add(chatListItem.contentText);

			// increase the order of other chats
			for (String tempKey : _chatList.keySet())
			{
				ChatListItem tempValue = _chatList.get(tempKey);
				tempValue.orderInList++;
				_chatList.put(tempKey, tempValue);
			}
		}

		chatListItem.orderInList = 1;
		_chatList.put(chatID, chatListItem);
	}

	private String getTimeFromMessage(Intent intent)
	{
		String when = "";
		if (intent.hasExtra("sentAt"))
		{
			String timeString = intent.getStringExtra("sentAt");
			double timeValue = Double.parseDouble(timeString) * 1000;
			Timestamp timeStamp = new Timestamp((long) timeValue);
			Date date = new Date(timeStamp.getTime());
			when = DateFormat.format("h:mmaa", date).toString();
		} else
		{
			when = (String) DateFormat.format("h:mmaa", new Date());
		}
		return when;
	}

	private void publishImage(Context context, RemoteViews remoteviews, String imagecontainer, String pictureUrl)
	{
		if (pictureUrl != null)
		{
			Log.d(TAG, "pictureUrl not null");
			CreateNotificationTask_multiMsg cNT = new CreateNotificationTask_multiMsg();
			cNT.setParams(context, remoteviews, imagecontainer, _notification, _nm);
			URL url;
			try
			{
				url = new URL(pictureUrl);
				cNT.execute(url);

			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		} else
		{
			remoteviews.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", imagecontainer),
					Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon72"));
		}
	}

	private String getPictureUrl(Intent intent)
	{
		String params = intent.getStringExtra("parameters");
		JSONObject object = null;
		String pictureUrl = null;

		if (params != null)
		{
			try
			{
				object = (JSONObject) new JSONTokener(params).nextValue();
				if (object != null)
				{
					if (object.has("pictureUrl"))
					{
						pictureUrl = object.getString("pictureUrl");
					} else if (object.has("facebookId"))
					{
						pictureUrl = "http://graph.facebook.com/" + object.getString("facebookId") + "/picture?type=normal";
					}
				}

			} catch (Exception e)
			{
				Log.d(TAG, "cannot parse the object");
			}
		}
		return pictureUrl;
	}

	private int getNbTotalMsg()
	{
		int nbTotalMsg = 0;
		for (ChatListItem temp : _chatList.values())
		{
			nbTotalMsg += temp.nbMsgInChat;
		}
		return nbTotalMsg;
	}

	private int getNbTotalChat()
	{
		return _chatList.size();
	}
	


	public void makeBigNotif(Context context, Intent intent)
	{
		addToChatList(intent);

		CharSequence tickerText = intent.getStringExtra("tickerText");
		Bitmap largeicon = BitmapFactory.decodeResource(context.getResources(), Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon72"));
		largeicon = Bitmap.createScaledBitmap(largeicon, largeicon.getWidth()/2, largeicon.getHeight()/2, true);
		
		Intent notificationIntent = null;
		PendingIntent contentIntent = null;
		notificationIntent = new Intent(context, NotificationActivity.class);
		notificationIntent.putExtra("params", LocalNotificationService.getFullJsonParams(intent));
		notificationIntent.putExtra("allclean", "true");
		contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		int nbTotalChat = getNbTotalChat();
		int nbTotalMsg = getNbTotalMsg();

		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		Vibrator v = (Vibrator) _mContext.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100); // vibrate 100 ms

		Collection<ChatListItem> chatListItemCollection = _chatList.values();
		
		if (nbTotalChat == 1)
		{
			ChatListItem item1 = new ChatListItem();
			for (ChatListItem temp : chatListItemCollection)
			{
				item1 = temp;
			}
			
			if (nbTotalMsg == 1)
			{
				_contentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "contentviewtext"),
						item1.contentText);
				_contentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "contentviewtitle"),
						item1.contentTitle);
				_contentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "contentviewtime"),
						item1.timeOfMsg);
				_contentView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "contentviewsmalllogo"),
						Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon24"));
				
				_notification = new NotificationCompat.Builder(context).setTicker(tickerText).setContentTitle(item1.contentTitle)
						.setContentText(item1.contentText)
						.setLargeIcon(largeicon)
						.setSmallIcon(Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon24"))
						.setLights(Color.BLUE, 500, 500).setContentIntent(contentIntent).setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
						.build();
				
				publishImage(context, _contentView, "contentviewimage", item1.pictureUrl);
			}
			else
			{
				_bigContentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtitle"),
						item1.contentTitle);
				_bigContentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtime"),
						item1.timeOfMsg);
				_bigContentView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewsmalllogo"),
						Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon24"));
				
				_bigContentView.removeAllViews(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtext"));
				for (int i=item1.msgList.size()-1;i>=0;i--)
				{
					RemoteViews inflateView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
							"layout", "inflatelayout"));
					inflateView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "inflateChatName"),
							View.GONE);
					inflateView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "inflateText"),
							item1.msgList.get(i));
					
					_bigContentView.addView(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtext"), inflateView);
				}
				
				_notification = new NotificationCompat.Builder(context).setTicker(tickerText).setContentTitle(item1.contentTitle)
						.setContentText(nbTotalMsg+" new messages")
						.setLargeIcon(largeicon)
						.setSmallIcon(Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon24"))
						.setLights(Color.BLUE, 500, 500).setContentIntent(contentIntent).setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
						.build();
				
				publishImage(context, _bigContentView, "bigcontentviewimage", item1.pictureUrl);
			}

		} else if (nbTotalChat > 1)
		{
			_bigContentView.removeAllViews(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtext"));
			
			ChatListItem item[] = new ChatListItem[10];
			for (ChatListItem temp : chatListItemCollection)
			{
				item[temp.orderInList] = temp;
			}
			for (int i=1;i<=nbTotalChat;i++)
			{
				if (item[i].nbMsgInChat==1) 
				{
					RemoteViews inflateView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
							"layout", "inflatelayout"));
					inflateView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "inflateChatName"),
							item[i].contentTitle);
					inflateView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "inflateText"),
							item[i].msgList.get(0));
					
					_bigContentView.addView(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtext"), inflateView);
				}
				else
				{
					RemoteViews inflateView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
							"layout", "inflatelayout"));
					inflateView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "inflateChatName"),
							item[i].contentTitle);
					inflateView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "inflateText"),
							item[i].nbMsgInChat+" messages");
					
					_bigContentView.addView(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtext"), inflateView);
				}
			}
			
			_bigContentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtitle"),
					nbTotalMsg+" new HelloPop messages");
			_bigContentView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewtime"),
					item[1].timeOfMsg);
			_bigContentView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "bigcontentviewsmalllogo"),
					Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon24"));
		
			_notification = new NotificationCompat.Builder(context).setTicker(tickerText).setContentTitle(nbTotalMsg+" new HelloPop messages")
					.setContentText(nbTotalChat+" new chats")
					.setLargeIcon(largeicon)
					.setSmallIcon(Resources.getResourseIdByName(context.getPackageName(), "drawable", "status_icon24"))
					.setLights(Color.BLUE, 500, 500).setContentIntent(contentIntent).setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
					.build();
			
			publishImage(context, _bigContentView, "bigcontentviewimage", item[1].pictureUrl);
		
		}
	}

}
