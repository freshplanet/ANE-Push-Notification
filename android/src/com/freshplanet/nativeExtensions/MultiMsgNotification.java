package com.freshplanet.nativeExtensions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.distriqt.extension.util.Resources;
import android.net.Uri;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Vibrator;

public class MultiMsgNotification
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
	}
	
	private Notification _notification;
	private RemoteViews _singleBigNotifView;
	private RemoteViews _multiBigNotifView;
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

		_multiBigNotifView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
				"layout", "multibignotif"));
		_singleBigNotifView = new RemoteViews(_mContext.getPackageName(), Resources.getResourseIdByName(_mContext.getPackageName(),
				"layout", "singlebignotif"));

		_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(_mContext.getPackageName(), "id", "chat1"), View.GONE);
		_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(_mContext.getPackageName(), "id", "chat2"), View.GONE);
		_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(_mContext.getPackageName(), "id", "chat3"), View.GONE);
		_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(_mContext.getPackageName(), "id", "chat4"), View.GONE);
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

	private void displayNotifFromChatList(Context context, Intent intent)
	{
		int nbTotalChat = getNbTotalChat();
		int nbTotalMsg = getNbTotalMsg();
		Collection<ChatListItem> chatListItemCollection = _chatList.values();

		if (nbTotalChat == 1)
		{
			ChatListItem item1 = new ChatListItem();
			for (ChatListItem temp : chatListItemCollection)
			{
				item1 = temp;
			}
			_singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singletext"),
					item1.contentText);
			_singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singletitle"),
					item1.contentTitle);
			_singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singlenbmessage"),
					String.valueOf(item1.nbMsgInChat));
			_singleBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "singletime"),
					item1.timeOfMsg);
			publishImage(context, _singleBigNotifView, "singleimage", item1.pictureUrl);
		} else if (nbTotalChat == 2)
		{
			ChatListItem item1 = new ChatListItem();
			ChatListItem item2 = new ChatListItem();
			for (ChatListItem temp : chatListItemCollection)
			{
				if (temp.orderInList == 1)
					item1 = temp;
				else if (temp.orderInList == 2)
					item2 = temp;
			}
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat1"), View.VISIBLE);
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat2"), View.VISIBLE);
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat3"), View.VISIBLE);

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text1"), nbTotalChat
					+ " HelloPop Chats");
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title1"), nbTotalMsg
					+ " HelloPop Messages");
			_multiBigNotifView
					.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage1"), item1.timeOfMsg);
			_multiBigNotifView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "image1"),
					Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon72"));

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text2"), item1.contentText);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title2"), item1.contentTitle);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage2"),
					String.valueOf(item1.nbMsgInChat));

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text3"), item2.contentText);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title3"), item2.contentTitle);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage3"),
					String.valueOf(item2.nbMsgInChat));
			publishImage(context, _multiBigNotifView, "image2", item1.pictureUrl);
			publishImage(context, _multiBigNotifView, "image3", item2.pictureUrl);
		} else if (nbTotalChat > 2)
		{
			ChatListItem item1 = new ChatListItem();
			ChatListItem item2 = new ChatListItem();
			for (ChatListItem temp : chatListItemCollection)
			{
				if (temp.orderInList == 1)
					item1 = temp;
				else if (temp.orderInList == 2)
					item2 = temp;
			}
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat1"), View.VISIBLE);
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat2"), View.VISIBLE);
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat3"), View.VISIBLE);
			_multiBigNotifView.setViewVisibility(Resources.getResourseIdByName(context.getPackageName(), "id", "chat4"), View.VISIBLE);

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text1"), nbTotalChat
					+ " HelloPop Chats");
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title1"), nbTotalMsg
					+ " HelloPop Messages");
			_multiBigNotifView
					.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage1"), item1.timeOfMsg);
			_multiBigNotifView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "image1"),
					Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon72"));

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text2"), item1.contentText);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title2"), item1.contentTitle);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage2"),
					String.valueOf(item1.nbMsgInChat));

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "text3"), item2.contentText);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "title3"), item2.contentTitle);
			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "nbMessage3"),
					String.valueOf(item2.nbMsgInChat));

			_multiBigNotifView.setTextViewText(Resources.getResourseIdByName(context.getPackageName(), "id", "emit"), nbTotalChat + "");
			_multiBigNotifView.setImageViewResource(Resources.getResourseIdByName(context.getPackageName(), "id", "image4"),
					Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon36"));

			publishImage(context, _multiBigNotifView, "image2", item1.pictureUrl);
			publishImage(context, _multiBigNotifView, "image3", item2.pictureUrl);

		}
	}

	public void makeBigNotif(Context context, Intent intent)
	{
		addToChatList(intent);

		CharSequence tickerText = intent.getStringExtra("tickerText");

		Intent notificationIntent = null;
		PendingIntent contentIntent = null;
		notificationIntent = new Intent(context, NotificationActivity.class);
		notificationIntent.putExtra("params", LocalNotificationService.getFullJsonParams(intent));
		notificationIntent.putExtra("allclean", "true");
		contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		int nbTotalChat = getNbTotalChat();
		int nbTotalMsg = getNbTotalMsg();

		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		_notification = new NotificationCompat.Builder(context).setTicker(tickerText).setContentTitle(nbTotalMsg + " HelloPop Messages")
				.setContentText(nbTotalChat + " HelloPop Chats").setNumber(nbTotalChat)
				.setSmallIcon(Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon36"))
				.setLights(Color.BLUE, 500, 500).setContentIntent(contentIntent).setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
				.build();
		_notification.ledARGB = Color.BLUE;

		Vibrator v = (Vibrator) _mContext.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100); // vibrate 100 ms

		displayNotifFromChatList(context, intent);
	}

}
