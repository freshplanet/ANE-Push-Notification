//
//  SPNotifCenterDelegate.h
//  AirPushNotification
//
//  Created by Peter Nicolai on 12/16/16.
//
//

#ifndef SPNotifCenterDelegate_h
#define SPNotifCenterDelegate_h

#import <UserNotifications/UserNotifications.h>

@interface SPNotifCenterDelegate : NSObject<UNUserNotificationCenterDelegate>

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler;

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
        didReceiveNotificationResponse:(UNNotificationResponse *)response
            withCompletionHandler:(void (^)(void))completionHandler;

@property(class) BOOL showWhenAppIsOpen;
+ (BOOL)showWhenAppIsOpen;
+ (void)setShowWhenAppIsOpen:(BOOL)show;

@end


#endif /* SPNotifCenterDelegate_h */




