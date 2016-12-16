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
    NSDictionary *userInfo = notification.request.content.userInfo;
    BOOL hasAlert = NO;
    if(userInfo != nil) {
        NSDictionary * apsData = userInfo[@"aps"];
        if(apsData != nil) {
            hasAlert = (apsData[@"alert"] != nil);
        }
    }
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
