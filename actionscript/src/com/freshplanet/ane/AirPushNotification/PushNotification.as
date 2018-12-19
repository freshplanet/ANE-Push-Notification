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
package com.freshplanet.ane.AirPushNotification {

	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;

    /**
     *
     */
    public class PushNotification extends EventDispatcher {

        // --------------------------------------------------------------------------------------//
        //																						 //
        // 									   PUBLIC API										 //
        // 																						 //
        // --------------------------------------------------------------------------------------//

		public static const RECURRENCE_NONE:int = 0;
		public static const RECURRENCE_DAILY:int = 1;
		public static const RECURRENCE_WEEK:int = 2;
		public static const RECURRENCE_MONTH:int = 3;
		public static const RECURRENCE_YEAR:int = 4;

		public static const DEFAULT_LOCAL_NOTIFICATION_ID:int = 0;

        /**
         * If <code>true</code>, logs will be displayed at the ActionScript level.
         */
        public static var logEnabled:Boolean = false;

        /**
         *
         */
        public static function get isSupported():Boolean {
            return _isIOS() || _isAndroid();
        }

        /**
         *
         */
        public static function get instance():PushNotification {
            return _instance ? _instance : new PushNotification();
        }

		/**
		 *  return true if notifs are enabled for this app in device settings
		 *  If iOS < 8 or android < 4.1 this isn't available, so will always return true.
		 */
		public function getNotificationsEnabled():void {
            
			if (!isSupported)
				return;

			_context.call("getNotificationsEnabled");
		}

		/**
		 * Check if notificationChannel is enabled in Android notification settings
		 * @param channelId
		 * @return
		 */
		public function isNotificationChannelEnabled(channelId:String):Boolean {

			if (!isSupported)
				return false;

			if(_isIOS())
				return true;


			return _context.call("checkNotificationChannelEnabled", channelId);
		}

		/**
		 * return true if OS permits sending user to settings (iOS 8, Android
		 */
		public function get canSendUserToSettings():Boolean {

			if (!isSupported)
				return false;

			return _context.call("getCanSendUserToSettings");
		}

        /**
         *
         */
		public function openDeviceNotificationSettings(channelId:String = null):void {

			if(!channelId)
				channelId = "";

			if (isSupported)
                _context.call("openDeviceNotificationSettings", channelId);
		}

        /**
		 * Needs Project id for Android Notifications.
		 * The project id is the one the developer used to register to gcm.
		 * @param projectId: project id to use
		 * @param iOSNotificationCategories: notification categories used on iOS (PushNotificationCategoryiOS)
		 */
		public function registerForPushNotification(projectId:String = null, iOSNotificationCategories:Array = null):void {

			if(!isSupported)
				return;

			if (_isAndroid())
                _context.call("registerPush", projectId);
			else if(_isIOS())
				_context.call("registerPush", iOSNotificationCategories != null ? JSON.stringify(iOSNotificationCategories) : null);
		}

        /**
         *
         * @param value
         */
		public function setBadgeNumberValue(value:int):void {

			if (isSupported)
                _context.call("setBadgeNb", value);
		}
		
		/**
		 * Sends a local notification to the device.
		 * @param message   the local notification text displayed
		 * @param timestamp when the local notification should appear (in sec)
		 * @param title     (Android Only) Title of the local notification
		 * @param recurrenceType
         * @param notificationId
         * @param deepLinkPath
         * @param iconPath
         * @param groupId  used to group notifications together
         * @param categoryId  used to display custom summaries on iOS
         * @param makeSquare  used to display square icons on Android, will default to round
		 */
		public function sendLocalNotification(message:String,
                                              timestamp:int,
                                              title:String,
                                              recurrenceType:int = RECURRENCE_NONE,
											  notificationId:int = DEFAULT_LOCAL_NOTIFICATION_ID,
                                              deepLinkPath:String = null,
                                              iconPath:String = null,
                                              groupId:String = null,
											  categoryId:String = null,
											  makeSquare:Boolean = false
		):void {

			if (!isSupported)
				return;

			if(!iconPath)
				iconPath = "";

			if(!deepLinkPath)
				deepLinkPath = "";

			if(!groupId)
				groupId = "";

			if(!categoryId)
				categoryId = "";

			_context.call("sendLocalNotification", message, timestamp, title, recurrenceType, notificationId, deepLinkPath, iconPath, groupId, categoryId, makeSquare);

		}

		/**
      	* Not implemented on Android for now. 
      	* @param notificationId
	  	*/
	    public function cancelLocalNotification(notificationId:int = DEFAULT_LOCAL_NOTIFICATION_ID):void {

	       	if (isSupported) {

	         	if (notificationId == DEFAULT_LOCAL_NOTIFICATION_ID)
                    _context.call("cancelLocalNotification");
	         	else
                    _context.call("cancelLocalNotification", notificationId);
	       	}
	    }

        /**
         *
         */
		public function cancelAllLocalNotifications():void {

			if (isSupported)
                _context.call("cancelAllLocalNotifications");
		}

        /**
         *
         * @param value
         */
		public function setIsAppInForeground(value:Boolean, appGroupId:String):void {

			if (isSupported)
                _context.call("setIsAppInForeground", value, appGroupId);
		}

        /**
         *
         * @param listener
         */
		public function addListenerForStarterNotifications(listener:Function):void {

			if (isSupported) {

				this.addEventListener(PushNotificationEvent.APP_STARTING_FROM_NOTIFICATION_EVENT, listener);
                _context.call("fetchStarterNotification");
			}
		}

		public function checkAppNotificationSettingsRequest():void {

			if (_isIOS()) {
				_context.call("checkAppNotificationSettingsRequest");
			}
		}



        public function storeTrackingNotifUrl(url:String, appGroupId:String):void {

            if (isSupported)
                _context.call("storeNotifTrackingInfo", url, appGroupId);
        }

		/**
		 * Create Android notification channel
		 * @param channelId
		 * @param channelName
		 * @param importance
		 * @param enableLights
		 * @param enableVibration
		 */
		public function createAndroidNotificationChannel(channelId:String, channelName:String, importance:int, enableLights:Boolean, enableVibration:Boolean):void {

			if (_isAndroid())
				_context.call("createNotificationChannel", channelId, channelName, importance, enableLights, enableVibration);
		}




		// --------------------------------------------------------------------------------------//
        //																						 //
        // 									 	PRIVATE API										 //
        // 																						 //
        // --------------------------------------------------------------------------------------//

        private static const EXTENSION_ID:String = "com.freshplanet.ane.AirPushNotification";
        private static var _instance:PushNotification = null;
        private var _context:ExtensionContext = null;

        /**
         * "private" singleton constructor
         */
        public function PushNotification() {

            super();

            if (_instance)
                throw Error("This is a singleton, use getInstance(), do not call the constructor directly.");

            _instance = this;

            _context = ExtensionContext.createExtensionContext(EXTENSION_ID, null);

            if (!_context)
                _log("ERROR", "Extension context is null. Please check if extension.xml is setup correctly.");
            else
                _context.addEventListener(StatusEvent.STATUS, _onStatus);
        }

        /**
         *
         * @param strings
         */
        private function _log(...strings):void {

            if (logEnabled) {

                strings.unshift(EXTENSION_ID);
                trace.apply(null, strings);
            }
        }

        /**
         *
         * @return  true if iOS
         */
        private static function _isIOS():Boolean {
			return Capabilities.manufacturer.indexOf("iOS") > -1 && Capabilities.os.indexOf("x86_64") < 0 && Capabilities.os.indexOf("i386") < 0;
        }

        /**
         *
         * @return  true if Android
         */
        private static function _isAndroid():Boolean {
            return Capabilities.manufacturer.indexOf("Android") > -1;
        }

        /**
         *
         * @param e
         */
        private function _onStatus(e:StatusEvent):void {

            var event:PushNotificationEvent = null;
            var data:String = e.level;

            switch (e.code) {

                case "TOKEN_SUCCESS":
                    event = new PushNotificationEvent(PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT);
                    event.token = e.level;
                    break;

                case "TOKEN_FAIL":
                    event = new PushNotificationEvent(PushNotificationEvent.PERMISSION_REFUSED_EVENT);
                    event.errorCode = "NativeCodeError";
                    event.errorMessage = e.level;
                    break;

                case "COMING_FROM_NOTIFICATION":
                    event = new PushNotificationEvent(PushNotificationEvent.COMING_FROM_NOTIFICATION_EVENT);

                    if (data != null) {

                        try {
                            event.parameters = JSON.parse(data);
                        }
                        catch (error:Error) {
                            trace("[PushNotification Error]", "cannot parse the params string", data);
                        }
                    }
                    break;

                case "APP_STARTING_FROM_NOTIFICATION":

                    event = new PushNotificationEvent(PushNotificationEvent.APP_STARTING_FROM_NOTIFICATION_EVENT);

                    if (data != null) {

                        try {
                            event.parameters = JSON.parse(data);
                        }
                        catch (error:Error) {
                            trace("[PushNotification Error]", "cannot parse the params string", data);
                        }
                    }
                    break;

                case "APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION":

                    event = new PushNotificationEvent(PushNotificationEvent.APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT);

                    if (data != null) {

                        try {
                            event.parameters = JSON.parse(data);
                        }
                        catch (error:Error) {
                            trace("[PushNotification Error]", "cannot parse the params string", data);
                        }
                    }
                    break;

                case "APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION":

                    event = new PushNotificationEvent(PushNotificationEvent.APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION_EVENT);

                    if (data != null) {

                        try {
                            event.parameters = JSON.parse(data);
                        }
                        catch (error:Error) {
                            trace("[PushNotification Error]", "cannot parse the params string", data);
                        }
                    }
                    break;

                case "NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND":

                    event = new PushNotificationEvent(PushNotificationEvent.NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT);

                    if (data != null) {

                        try {
                            event.parameters = JSON.parse(data);
                        }
                        catch (error:Error) {
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

				case "GET_NOTIFICATIONS_ENABLED_RESULT":
					event = new PushNotificationEvent(PushNotificationEvent.GET_NOTIFICATIONS_ENABLED_RESULT, e.level == "true");
					break;

				case "OPEN_APP_NOTIFICATION_SETTINGS":
					event = new PushNotificationEvent(PushNotificationEvent.OPEN_APP_NOTIFICATION_SETTINGS);
					break;

                case "LOGGING":
                    trace(e, e.level);
                    break;
            }

            if (event != null)
                this.dispatchEvent(event);
        }
		
	}
}