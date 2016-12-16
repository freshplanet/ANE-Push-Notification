//
//  SPNotifCenterDelegate.m
//  AirPushNotification
//
//  Created by Peter Nicolai on 12/16/16.
//
//

#import <Foundation/Foundation.h>
#import "SPNotifCenterDelegate.h"

@implementation SPNotifCenterDelegate

BOOL _showWhenAppIsOpen = NO;

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler
{
    BOOL hasAlert = [notification.request.content.body length] > 1;
    if(_showWhenAppIsOpen && hasAlert) {
        completionHandler(UNNotificationPresentationOptionAlert);
    }
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
        didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    
}

+ (BOOL)showWhenAppIsOpen { return _showWhenAppIsOpen; }
+ (void)setShowWhenAppIsOpen:(BOOL)show { _showWhenAppIsOpen = show; }

@end
