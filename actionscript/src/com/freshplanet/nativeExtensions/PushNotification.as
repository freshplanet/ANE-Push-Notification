package com.freshplanet.nativeExtensions
{
	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;

    
    public class PushNotification extends EventDispatcher 
	{  
	
		public static const RECURRENCE_NONE:int   = 0;
		public static const RECURRENCE_DAILY:int  = 1;
		public static const RECURRENCE_WEEK:int   = 2;
		public static const RECURRENCE_MONTH:int  = 3;
		public static const RECURRENCE_YEAR:int   = 4;

		public static const DEFAULT_LOCAL_NOTIFICATION_ID:int = 0;
		
		private static var extCtx:ExtensionContext = null;
        
        private static var _instance:PushNotification;
        
		
        public function PushNotification()
		{
			if (!_instance)
			{
				if (this.isPushNotificationSupported)
				{
					
					extCtx = ExtensionContext.createExtensionContext("com.freshplanet.AirPushNotification", null);
				
					if (extCtx != null)
					{
						extCtx.addEventListener(StatusEvent.STATUS, onStatus);
					} else
					{
						trace('extCtx is null.');
					}
				}
				_instance = this;
			}
			else
			{
				throw Error( 'This is a singleton, use getInstance, do not call the constructor directly');
			}
		}
		
		
		public function get isPushNotificationSupported():Boolean
		{
			var result:Boolean = (Capabilities.manufacturer.search('iOS') > -1 || Capabilities.manufacturer.search('Android') > -1);
			return result;
		}

		/**
		 *  return true if notifs are enabled for this app in device settings
		 *  If iOS < 8 or android < 4.1 this isn't available, so will always return true.
		 */
		public function get notificationsEnabled():Boolean
		{
			if(!isPushNotificationSupported) {
				return false;
			}
			return extCtx.call("getNotificationsEnabled");
		}

		/**
		 * return true if OS permits sending user to settings (iOS 8, Android
		 */
		public function get canSendUserToSettings():Boolean
		{
			if(!isPushNotificationSupported) {
				return false;
			}
			return extCtx.call("getCanSendUserToSettings");
		}

		public function openDeviceNotificationSettings():void
		{
			if(isPushNotificationSupported) {
				extCtx.call("openDeviceNotificationSettings");
			}
		}
		
		public static function getInstance() : PushNotification
		{
			return _instance ? _instance : new PushNotification();
		}
	
        /**
		 * Needs Project id for Android Notifications.
		 * The project id is the one the developer used to register to gcm.
		 * @param projectId: project id to use
		 */
		public function registerForPushNotification(projectId:String = null) : void
		{
			if (this.isPushNotificationSupported)
			{
				extCtx.call("registerPush", projectId);
			}
		}
		
		public function setBadgeNumberValue(value:int):void
		{
			if (this.isPushNotificationSupported)
			{
				extCtx.call("setBadgeNb", value);
			}
		}
		
		
		/**
		 * Sends a local notification to the device.
		 * @param message the local notification text displayed
		 * @param timestamp when the local notification should appear (in sec)
		 * @param title (Android Only) Title of the local notification
		 * @param recurrenceType
		 * 
		 */
		public function sendLocalNotification(message:String, timestamp:int, title:String, recurrenceType:int = RECURRENCE_NONE,
											  notificationId:int = DEFAULT_LOCAL_NOTIFICATION_ID, deepLinkPath:String = null, androidLargeIconResourceId:String = null):void
		{
			if (this.isPushNotificationSupported)
			{
				if (notificationId == DEFAULT_LOCAL_NOTIFICATION_ID)
       			{
           			extCtx.call("sendLocalNotification", message, timestamp, title, recurrenceType);
         		} else
         		{
					if (Capabilities.manufacturer.search('Android') > -1)
					{
						extCtx.call("sendLocalNotification", message, timestamp, title, recurrenceType, notificationId, deepLinkPath, androidLargeIconResourceId);
					} else // iOS doesn't support null params
					{
						if(deepLinkPath === null) {
							extCtx.call("sendLocalNotification", message, timestamp, title, recurrenceType, notificationId);
						} else {
							extCtx.call("sendLocalNotification", message, timestamp, title, recurrenceType, notificationId, deepLinkPath);
						}
						
					}
         		}
			}
		}

		/**
      	* Not implemented on Android for now. 
      	* @param notificationId
      	* 
	  	*/
	    public function cancelLocalNotification(notificationId:int = DEFAULT_LOCAL_NOTIFICATION_ID):void
	    {
	       	if (this.isPushNotificationSupported)
	       	{
	         	if (notificationId == DEFAULT_LOCAL_NOTIFICATION_ID)
	         	{
	           		extCtx.call("cancelLocalNotification");
	         	} else
	         	{
	           		extCtx.call("cancelLocalNotification", notificationId);
	         	}
	       	}
	    }

		public function cancelAllLocalNotifications():void
		{
			if(this.isPushNotificationSupported)
			{
				extCtx.call("cancelAllLocalNotifications");
			}
		}
     		
		public function setIsAppInForeground(value:Boolean):void
		{
			if (this.isPushNotificationSupported)
			{
				extCtx.call("setIsAppInForeground", value);
			}
		}
		
		public function addListenerForStarterNotifications(listener:Function):void
		{
			if (this.isPushNotificationSupported)
			{
				this.addEventListener(PushNotificationEvent.APP_STARTING_FROM_NOTIFICATION_EVENT, listener);
				extCtx.call("fetchStarterNotification");
			}
		}

		public function storeTrackingNotifUrl(url:String):void
		{
			if (this.isPushNotificationSupported)
			{
				extCtx.call("storeNotifTrackingInfo", url);
			}
		}

		public function setShowWhileAppIsOpen(show:Boolean):void 
		{
			if(Capabilities.manufacturer.search('iOS') != -1) {
				extCtx.call("setShowWhileAppIsOpen", show);
			}
		}


        // onStatus()
        // Event handler for the event that the native implementation dispatches.
        //
        private function onStatus(e:StatusEvent):void 
		{
			if (this.isPushNotificationSupported)
			{

				var event : PushNotificationEvent;
				var data:String = e.level;
				switch (e.code)
				{
					case "TOKEN_SUCCESS":
						event = new PushNotificationEvent( PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT );
						event.token = e.level;
						break;
					case "TOKEN_FAIL":
						event = new PushNotificationEvent( PushNotificationEvent.PERMISSION_REFUSED_EVENT );
						event.errorCode = "NativeCodeError";
						event.errorMessage = e.level;
						break;
					case "COMING_FROM_NOTIFICATION":
						event = new PushNotificationEvent( PushNotificationEvent.COMING_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								event.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace("[PushNotification Error]", "cannot parse the params string", data);
							}
						}
						break;
					case "APP_STARTING_FROM_NOTIFICATION":
						event = new PushNotificationEvent( PushNotificationEvent.APP_STARTING_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								event.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace("[PushNotification Error]", "cannot parse the params string", data);
							}
						}
						break;
					case "APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION":
						event = new PushNotificationEvent( PushNotificationEvent.APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								event.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace("[PushNotification Error]", "cannot parse the params string", data);
							}
						}
						break;
					case "APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION":
						event = new PushNotificationEvent( PushNotificationEvent.APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								event.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace("[PushNotification Error]", "cannot parse the params string", data);
							}
						}
						break;
					case "NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND":
						event = new PushNotificationEvent( PushNotificationEvent.NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT );
						if (data != null)
						{
							try
							{
								event.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace("[PushNotification Error]", "cannot parse the params string", data);
							}
						}
						break;
					case "NOTIFICATION_SETTINGS_ENABLED":
						event = new PushNotificationEvent(PushNotificationEvent.NOTIFICATION_SETTINGS_ENABLED);
						break;
					case "NOTIFICATION_SETTINGS_DISABLED":
						event = new PushNotificationEvent(PushNotificationEvent.NOTIFICATION_SETTINGS_DISABLED);
						break;

					case "LOGGING":
						trace(e, e.level);
						break;
				}
				
				if (event != null)
				{
					this.dispatchEvent( event );
				}				
			}
		}
		
	}
}