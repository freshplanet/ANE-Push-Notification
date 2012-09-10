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


Usage
-----

In order to register the device for a push notifications, you should use the following method:

    PushNotification.getInstance().registerForPushNotification(GOOGLE_PROJECT_ID);

The argument *GOOGLE_PROJECT_ID* is necessary only on Android and is your project's ID on [GCM](http://developer.android.com/guide/google/gcm/index.html).

On iOS, once the user has given his permission, the following event is triggered:

    PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT


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