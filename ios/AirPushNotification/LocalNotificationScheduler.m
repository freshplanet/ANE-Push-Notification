//
//  LocalNotificationScheduler.m
//  AirPushNotification
//
//  Created by Nigam Shah on 3/28/14.
//
//

#import "LocalNotificationScheduler.h"
#import "AirPushNotification.h"

@implementation LocalNotificationScheduler

- (void) scheduleLocalNotifs:(NSMutableArray*) notifs
{
	[UIApplication sharedApplication].scheduledLocalNotifications = notifs;
}

- (UILocalNotification*) createLocalNotif:(FREObject) notifArr
{
	uint32_t notifArrCount;
	FREGetArrayLength(notifArr, &notifArrCount);
	
	uint32_t string_length;
    const uint8_t *utf8_message;
	
	// message
	FREObject obj0;
	FREGetArrayElementAt(notifArr, 0, &obj0);
	
	if (FREGetObjectAsUTF8(obj0, &string_length, &utf8_message) != FRE_OK)
	{
		return nil;
	}
	
	NSString* message = [NSString stringWithUTF8String:(char*)utf8_message];
	
	// timestamp
	FREObject obj1;
	FREGetArrayElementAt(notifArr, 1, &obj1);
	
	uint32_t timestamp;
	if (FREGetObjectAsUint32(obj1, &timestamp) != FRE_OK)
	{
		return nil;
	}
	
	
	// NOTE: notifArr[2] is "title" which is Android only
	
	// recurrence
	FREObject obj3;
	FREGetArrayElementAt(notifArr, 0, &obj3);
	
	uint32_t recurrence = 0;
	if (notifArrCount >= 4 )
	{
		FREGetObjectAsUint32(obj3, &recurrence);
	}
	
	// local notif id: 0 is default
	FREObject obj4;
	FREGetArrayElementAt(notifArr, 0, &obj4);
	
	uint32_t localNotificationId = 0;
	if (notifArrCount >= 5)
	{
		if (FREGetObjectAsUint32(obj4, &localNotificationId) != FRE_OK)
		{
			localNotificationId = 0;
		}
	}
	
	// local notif content id: 0 is default
	uint32_t localNotificationContentId = 0;
	FREObject obj5;
	FREGetArrayElementAt(notifArr, 0, &obj5);
	
	if (notifArrCount >= 6)
	{
		if (FREGetObjectAsUint32(obj5, &localNotificationContentId) != FRE_OK)
		{
			localNotificationContentId = 0;
		}
	}
	
	// timezone name
	FREObject obj6;
	FREGetArrayElementAt(notifArr, 0, &obj6);
	
	if (notifArrCount >= 7) {
		if (FREGetObjectAsUTF8(obj6, &string_length, &utf8_message) != FRE_OK)
		{
			return nil;
		}
		
	}
	
	NSString* timezoneName = [NSString stringWithUTF8String:(char*)utf8_message];
	NSNumber *localNotifIdNumber = [NSNumber numberWithInt:localNotificationId];
    NSNumber *localNotifContentIdNumber =[NSNumber numberWithInt:localNotificationContentId];
    NSDate *itemDate = [NSDate dateWithTimeIntervalSince1970:timestamp];
	
	//====================
	
    
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
	
    if (localNotif == nil) return nil;
	
    localNotif.fireDate = itemDate;
    localNotif.timeZone = [NSTimeZone timeZoneWithName:timezoneName];
    localNotif.alertBody = message;
    localNotif.alertAction = @"View Details";
    localNotif.soundName = UILocalNotificationDefaultSoundName;
    
	localNotif.userInfo =
	@{	[localNotifIdNumber stringValue] : @"",
		@"contentId" : [localNotifContentIdNumber stringValue]
		};
	
	
    if (recurrence > 0)
    {
        if (recurrence == 1)
        {
            localNotif.repeatInterval = NSDayCalendarUnit;
        } else if (recurrence == 2)
        {
            localNotif.repeatInterval = NSWeekCalendarUnit;
        } else if (recurrence == 3)
        {
            localNotif.repeatInterval = NSMonthCalendarUnit;
        } else if (recurrence == 4)
        {
            localNotif.repeatInterval = NSYearCalendarUnit;
        }
        
    }
    
    return localNotif;
}

@end
