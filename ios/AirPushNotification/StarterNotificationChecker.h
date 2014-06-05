//
//  StarterNotificationChecker.h
//  AirPushNotification
//
//  Created by Thibaut on 13/12/12.
//
//

#import <UIKit/UIKit.h>

@interface StarterNotificationChecker : NSObject
+ (void)load;

+ (void) createStarterNotificationChecker:(NSNotification*) notification;

+ (BOOL) applicationStartedWithNotification;

+ (NSDictionary*) getStarterNotification;

+ (void) deleteStarterNotification;

@end
