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

#import "NotifCenterDelegate.h"
#import "AirPushNotification.h"

@interface NotifCenterDelegate()
@end

@implementation NotifCenterDelegate

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    NSDictionary * notifDict = response.notification.request.content.userInfo;
    
    if ([[AirPushNotification instance] isInitialized]) {
        // we can dispatch an event
        [[AirPushNotification instance] trackRemoteNofiticationFromApp:[UIApplication sharedApplication] andUserInfo:notifDict];
    } else {
        // wait for it to be fetched
        self.starterNotif = notifDict;
    }

    [self performTracking:response.notification.request.content withCompletionHander:completionHandler];

}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler
{
    
    if ([[AirPushNotification instance] isInitialized]) {
        [[AirPushNotification instance] trackRemoteNofiticationFromApp:[UIApplication sharedApplication] andUserInfo:notification.request.content.userInfo];
    }
   void (^simpleBlock)(void) = ^{
      completionHandler(UNNotificationPresentationOptionNone);
    };
    
    [self performTracking:notification.request.content withCompletionHander:simpleBlock];

}

-(void)userNotificationCenter:(UNUserNotificationCenter *)center openSettingsForNotification:(UNNotification *)notification {
    
    if ([[AirPushNotification instance] isInitialized]) {
        // we can dispatch an event
        self.pendingOpenAppNotificationSettings = false;
        [[AirPushNotification instance] sendEvent:@"OPEN_APP_NOTIFICATION_SETTINGS"];
    } else {
        // wait for ane initialized
        self.pendingOpenAppNotificationSettings = true;
    }
}

- (void) performTracking:(UNNotificationContent *)notificationContent withCompletionHander:(void (^)(void))completionHandler {
    
    NSDictionary *userInfo = notificationContent.userInfo;
    NSString* notifTrackingURL = [[NSUserDefaults standardUserDefaults] stringForKey:storedNotifTrackingUrl];
    
    if (!notifTrackingURL || !userInfo)
        completionHandler();
    else {
        
        NSString* link = nil;
        
        if ([userInfo objectForKey:@"type"] != NULL)
            link = [@"/?source_type=notif&source_ref=" stringByAppendingString:[userInfo objectForKey:@"type"]];
        else {
            
            completionHandler();
            return;
        }
        
        if ([userInfo objectForKey:@"sender"] != NULL)
            link = [[link stringByAppendingString:@"&source_userId="] stringByAppendingString:[userInfo objectForKey:@"sender"]];
        
//        NSString *appState = nil;
//        UIApplication *app = [UIApplication sharedApplication];
//        if (app.applicationState == UIApplicationStateActive)
//            appState = @"open";
//        else if (app.applicationState == UIApplicationStateInactive)
//            appState = @"closed";
//        else if (app.applicationState == UIApplicationStateBackground)
//            appState = @"background";
//
//        link = [[link stringByAppendingString:@"&appState="] stringByAppendingString:appState];
//
//        if(notificationContent.categoryIdentifier)
//            link = [[link stringByAppendingString:@"&categoryId="] stringByAppendingString:notificationContent.categoryIdentifier];
//
//        if ([userInfo objectForKey:@"trackingId"] != NULL)
//            link = [[link stringByAppendingString:@"&trackingId="] stringByAppendingString:[userInfo objectForKey:@"trackingId"]];
        
        
        // we url-encode the link (with special cases for '&' and '=')
        link = [link stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLHostAllowedCharacterSet]];
        link = [link stringByReplacingOccurrencesOfString:@"&" withString:@"%26"];
        link = [link stringByReplacingOccurrencesOfString:@"=" withString:@"%3D"];
        
        link = [@"&link=" stringByAppendingString:link];
        
        notifTrackingURL = [notifTrackingURL stringByAppendingString:link];
        
        NSURL* url = [[NSURL alloc] initWithString:notifTrackingURL];
        
        if (!url)
            completionHandler();
        else {
            
            [[AirPushNotification instance] sendLog:[@"MATEO performing tracking " stringByAppendingString:url.absoluteString]];
            NSURLSessionDataTask* task = [[NSURLSession sharedSession] dataTaskWithURL:url
                                                                     completionHandler:^(NSData *data, NSURLResponse *response,NSError *error) {
                                                                         completionHandler();
                                                                         
                                                                     }];
            
            [task resume];
        }
    }
}

@end
