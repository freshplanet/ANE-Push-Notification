//
//  LocalNotificationScheduler.h
//  AirPushNotification
//
//  Created by Nigam Shah on 3/28/14.
//
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "FlashRuntimeExtensions.h"

@interface LocalNotificationScheduler : NSObject

//@property (atomic, retain) NSMutableArray *notifsInfoArr;

- (UILocalNotification*) createLocalNotif:(FREObject) notifArr;
- (void) scheduleLocalNotifs:(NSMutableArray*) notifs;

@end
