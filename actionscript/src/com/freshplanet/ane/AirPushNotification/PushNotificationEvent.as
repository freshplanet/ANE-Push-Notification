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

	import flash.events.Event;

    /**
     *
     */
	public class PushNotificationEvent extends Event {

        /**
         *
         */
		public static const PERMISSION_GIVEN_WITH_TOKEN_EVENT : String = "permissiongivenwithtokenevent";

        /**
         *
         */
		public static const PERMISSION_REFUSED_EVENT : String = "permissionRefusedEvent";

        /**
         *
         */
		public static const COMING_FROM_NOTIFICATION_EVENT : String = "comingFromNotificationEvent";

        /**
         * App is launched (for the first time) when clicking on the push notification
         */
		public static const APP_STARTING_FROM_NOTIFICATION_EVENT : String = "startingFromNotificationEvent";

        /**
         * App was in background when clicking on the push notification
         */
		public static const APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT : String = "BroughtToForegroundFromNotificationEvent";

        /** App was in background and awoken by iOS when receiving the push notification (background fetch on iOS 7+) */
		public static const APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION_EVENT : String = "StartedInBackgroundFromNotificationEvent";

        /**
         * App was in foreground when receiving push notification
         */
		public static const NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT : String = "NotificationReceivedWhenInForegroundEvent";

        /**
         * Not available on earlier OS versions
         */
		public static const NOTIFICATION_SETTINGS_ENABLED: String = "notificationSettingsEnabled";
		public static const NOTIFICATION_SETTINGS_DISABLED: String = "notificationSettingsDisabled";
		
		public var token:String = null;
        public var errorCode:String = null;
        public var errorMessage:String = null;
        public var parameters:Object = null;

        public function PushNotificationEvent(type:String) {
            super(type, false, false);
        }
	}
}