Air Native Extension for Push Notifications (iOS + Android)
======================================

This is an [Air native extension](http://www.adobe.com/devnet/air/native-extensions-for-air.html) for sending push notifications on iOS and Android. It has been developed by [FreshPlanet](http://freshplanet.com) and is used in the game [SongPop](http://songpop.fm).


Push Notifications
---------

On iOS devices, this ANE uses Apple [Push Notification Services](https://developer.apple.com/library/ios/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingWIthAPS/CommunicatingWIthAPS.html). On Android devices, it uses [Google Cloud Messaging (GCM)](http://developer.android.com/guide/google/gcm/index.html).


Installation
---------

If your app is iOS-only, you can directly use the binary located in the *bin* folder (AirPushNotification.ane). You should add it to your application project's Build Path and make sure to package it with your app (more information [here](http://help.adobe.com/en_US/air/build/WS597e5dadb9cc1e0253f7d2fc1311b491071-8000.html)).

If your app supports Android, you need to compile the ANE with your own assets (status bar icon etc...). To do that, use the ant build script located in the *build* folder (build.xml):

```xml
cd /path/to/the/ane/build
mv example.build.config build.config
# edit the build.config file to provide your machine-specific paths
ant
```

You should also update your manifest with:

	```
		<!-- Only this application can receive the messages and registration result -->
		<permission android:name="INSERT.APP.ID.HERE.permission.C2D_MESSAGE" android:protectionLevel="signature" />
		<uses-permission android:name="INSERT.APP.ID.HERE.permission.C2D_MESSAGE" />
		
		<!-- This app has permission to register and receive message -->
		<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
		
		<application>

			<activity android:name="com.freshplanet.nativeExtensions.NotificationActivity" android:theme="@android:style/Theme.Translucent.NoTitleBar.Fullscreen" />
			
			<receiver android:name="com.freshplanet.nativeExtensions.C2DMBroadcastReceiver"
				android:permission="com.google.android.c2dm.permission.SEND">
				
				<!-- Receive the actual message -->
				<intent-filter>
					<action android:name="com.google.android.c2dm.intent.RECEIVE" />
					<category android:name="INSERT.APP.ID.HERE" />
				</intent-filter>
				
				<!-- Receive the registration id -->
				<intent-filter>
					<action android:name="com.google.android.c2dm.intent.REGISTRATION" />
					<category android:name="INSERT.APP.ID.HERE" />
				</intent-filter>
			</receiver>
			
			<!-- Local notification -->
			<service android:name="com.freshplanet.nativeExtensions.LocalNotificationService"/>
			<receiver android:name="com.freshplanet.nativeExtensions.LocalBroadcastReceiver" android:process=":remote"></receiver>

		</application>
	```

and replace INSERT.APP.ID.HERE by your application id (<id> tag in your manifest).


Usage
-----

```actionscript

// Register the device for a push notifications
// GOOGLE_PROJECT_ID is necessary only on Android and is your project's ID on GCM.
// On iOS, you can call the method with no parameter.
PushNotification.getInstance().registerForPushNotification(GOOGLE_PROJECT_ID);

// Register for events
PushNotification.getInstance().addEventListener(PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT, onPushNotificationToken);
PushNotification.getInstance().addEventListener(PushNotificationEvent.NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT, onNotificationReceivedInForeground);
PushNotification.getInstance().addEventListener( PushNotificationEvent.APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT, onNotificationReceivedInBackground);
PushNotification.getInstance().addListenerForStarterNotifications(onNotificationReceivedStartingTheApp);

// Handle events
function onPushNotificationToken(event:PushNotificationEvent):void
{
	trace("My push token is: " + event.token);
}
// other event handlers also receive a PushNotificationEvent

```

Packaging final app for Android
-------------------------------

When you build your final APK for Android, you need to update (patch) the AIRSDK you're using.
First, make sure you download the latest build-tools (from the Android SDK manager).
Then, patch your AIR SDK with the following command:
`cp pathtoyourANDROIDSDK/build-tools/22.0.1/lib/dx.jar pathtoyourAIRSDK/lib/android/bin/dx.jar`


Notes:
* included binary has been compiled for 64-bit iOS support

Authors
------

This ANE has been written by [Thibaut Crenn](https://github.com/titi-us). It belongs to [FreshPlanet Inc.](http://freshplanet.com) and is distributed under the [Apache Licence, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


Join the FreshPlanet team - GAME DEVELOPMENT in NYC
------

We are expanding our mobile engineering teams.

FreshPlanet is a NYC based mobile game development firm and we are looking for senior engineers to lead the development initiatives for one or more of our games/apps. We work in small teams (6-9) who have total product control.  These teams consist of mobile engineers, UI/UX designers and product experts.


Please contact Tom Cassidy (tcassidy@freshplanet.com) or apply at http://freshplanet.com/jobs/
