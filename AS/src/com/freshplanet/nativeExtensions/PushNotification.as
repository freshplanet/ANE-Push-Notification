package com.freshplanet.nativeExtensions
{
	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;

    
    public class PushNotification extends EventDispatcher 
	{  
	
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
			trace('Push notification is'+(result ? ' ' : ' not ')+'supported');
			return result;
		}
		
		
		
		public static function getInstance() : PushNotification
		{
			return _instance ? _instance : new PushNotification();
		}
	
        /**
		 * Needs email Adress for Android Notifications.
		 * The email adress is the one the developer used to register to cd2m.
		 * @param emailAdress: adress email to use
		 */
		public function registerForPushNotification(emailAdress:String = null) : void
		{
			if (this.isPushNotificationSupported)
			{
				extCtx.call("registerPush", emailAdress);
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
		 * @param timestamp when the local notification should appear
		 */
		public function sendLocalNotification(message:String, timestamp:int):void
		{
			trace("[Push Notification]","sendLocalnotification");
			if (this.isPushNotificationSupported)
			{
				extCtx.call("sendLocalNotification", message, timestamp);
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
				
				if (e.code == "TOKEN_SUCCESS") 
				{
					trace( 'TOKEN_SUCCESS, token =', e.level );
					event = new PushNotificationEvent( PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT );
					event.token = e.level;
					this.dispatchEvent( event );
				}
				else if (e.code == "TOKEN_FAIL")
				{
					trace( 'TOKEN_FAIL, error =', e.level );
					event = new PushNotificationEvent( PushNotificationEvent.PERMISSION_REFUSED_EVENT );
					event.errorCode = "NativeCodeError";
					event.errorMessage = e.level;
					this.dispatchEvent( event );
				} else if (e.code == "COMING_FROM_NOTIFICATION")
				{
					trace( 'COMING_FROM_NOTIFICATION, message =', e.level );
					event = new PushNotificationEvent( PushNotificationEvent.COMING_FROM_NOTIFICATION_EVENT );
					
				} else
				{
					trace('unrecognized event '+e.code);
				}
			}
		}
		
	}
}