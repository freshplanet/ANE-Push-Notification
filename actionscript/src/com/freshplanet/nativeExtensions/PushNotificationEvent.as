package com.freshplanet.nativeExtensions
{
	import flash.events.Event;
	
	public class PushNotificationEvent extends Event
	{
		
		public static const PERMISSION_GIVEN_WITH_TOKEN_EVENT : String = "permissiongivenwithtokenevent";
		public static const PERMISSION_REFUSED_EVENT : String = "permissionRefusedEvent";

		public static const COMING_FROM_NOTIFICATION_EVENT : String = "comingFromNotificationEvent";
		/** App is launched (for the first time) when clicking on the push notification*/
		public static const APP_STARTING_FROM_NOTIFICATION_EVENT : String = "startingFromNotificationEvent";
		/** App was in background when clicking on the push notification*/
		public static const APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT : String = "BroughtToForegroundFromNotificationEvent";
		/** App was in foreground when receiving push notification*/
		public static const NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT : String = "NotificationReceivedWhenInForegroundEvent";
		
		public var token : String;
		public var errorCode : String;
		public var errorMessage : String;
		public var parameters:Object;
		
		public function PushNotificationEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}