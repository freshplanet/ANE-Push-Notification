Air Native Extension for Push Notifications (iOS + Android)
======================================

This is an [Air native extension](http://www.adobe.com/devnet/air/native-extensions-for-air.html) for sending push notifications on iOS and Android. It has been developed by [FreshPlanet](http://freshplanet.com) and is used in the game [SongPop](http://songpop.fm).


Push Notifications
---------

On iOS devices, this ANE uses Apple [Push Notification Services](https://developer.apple.com/library/ios/#documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingWIthAPS/CommunicatingWIthAPS.html). On Android devices, it uses [Google Cloud Messaging (GCM)](http://developer.android.com/guide/google/gcm/index.html).


Installation
---------

The ANE binary (AirPushNotification.ane) is located in the *bin* folder. You should add it to your application project's Build Path and make sure to package it with your app (more information [here](http://help.adobe.com/en_US/air/build/WS597e5dadb9cc1e0253f7d2fc1311b491071-8000.html)).

On Android, you might want to update the content of the *android/res* folder with your own assets.
You should also update your android manifest:

	```
		<!-- Only this application can receive the messages and registration result -->
		<permission android:name="INSERT.APP.ID.HERE.permission.C2D_MESSAGE" android:protectionLevel="signature" />
		<uses-permission android:name="INSERT.APP.ID.HERE.permission.C2D_MESSAGE" />
		<!-- This app has permission to register and receive message -->
		<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
		
		
		<!-- The following lines have to placed inside the <application> tag-->
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
	```

and replace INSERT.APP.ID.HERE by your application id (<id> tag in your manifest).

Usage
-----

In order to register the device for a push notifications, you should use the following method:

    PushNotification.getInstance().registerForPushNotification(GOOGLE_PROJECT_ID);

The argument *GOOGLE_PROJECT_ID* is necessary only on Android and is your project's ID on [GCM](http://developer.android.com/guide/google/gcm/index.html). On iOS, you can call the method with no parameter.

Once the user has given his permission, the following event is triggered:

    PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT


Sample Code for remote notifications events:
	PushNotification.getInstance().addEventListener( PushNotificationEvent.NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT, onNotificationReceivedInForeground);
	PushNotification.getInstance().addEventListener( PushNotificationEvent.APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT, onNotificationReceivedInBackground);
	PushNotification.getInstance().addListenerForStarterNotifications(onNotificationReceivedStartingTheApp);

where event listeners will receive a PushNotificationEvent as argument.

Build script
---------

Should you need to edit the extension source code and/or recompile it, you will find an ant build script (build.xml) in the *build* folder:

    cd /path/to/the/ane/build
    mv example.build.config build.config
    #edit the build.config file to provide your machine-specific paths
    ant


Authors
------

This ANE has been written by [Thibaut Crenn](https://github.com/titi-us). It belongs to [FreshPlanet Inc.](http://freshplanet.com) and is distributed under the [Apache Licence, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).