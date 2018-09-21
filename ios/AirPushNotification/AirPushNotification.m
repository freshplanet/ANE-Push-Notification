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

#import "AirPushNotification.h"
#import <UIKit/UIApplication.h>
#import <UIKit/UIAlertView.h>
#import <UserNotifications/UserNotifications.h>
#import <objc/runtime.h>
#import <objc/message.h>
#import "StarterNotificationChecker.h"
#import "NotifCenterDelegate.h"
#import <MobileCoreServices/MobileCoreServices.h>

#define DEFINE_ANE_FUNCTION(fn) FREObject (fn)(FREContext context, void* functionData, uint32_t argc, FREObject argv[])
#define MAP_FUNCTION(fn, data) { (const uint8_t*)(#fn), (data), &(fn) }

NSString* const storedNotifTrackingUrl = @"storedNotifTrackingUrl";

@implementation AirPushNotification

static NotifCenterDelegate * _delegate = nil;


+ (void)load {
    // on iOS 10+, only use UNUserNotificationCenter to handle incoming notifications
    if ([UNUserNotificationCenter class]) {
        _delegate = [[NotifCenterDelegate alloc] init];
        [[UNUserNotificationCenter currentNotificationCenter] setDelegate:_delegate];
    }
}

+ (NSDictionary *) getAndClearDelegateStarterNotif {
    if(_delegate != nil) {
        NSDictionary * notif = _delegate.starterNotif;
        _delegate.starterNotif = nil;
        return notif;
    }
    return nil;
}


#pragma mark - init

+ (AirPushNotification*)instance {
    
    static AirPushNotification* _sharedInstance = nil;
    static dispatch_once_t oncePredicate;
    
    dispatch_once(&oncePredicate, ^{
        _sharedInstance = [[AirPushNotification alloc] init];
    });
    
    return _sharedInstance;
}

- (BOOL)isInitialized {
    return (_context != nil);
}

- (void)setupWithContext:(FREContext)extensionContext {
    _context = extensionContext;
}

#pragma mark - overriden notification event methods

- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken {}
- (void)application:(UIApplication*)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error {}
- (void)application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary*)userInfo {}
- (void)application:(UIApplication*)application didRegisterUserNotificationSettings:(UIUserNotificationSettings*)notificationSettings {}

#pragma mark - helpers

- (void)sendLog:(NSString*)log {
    [self sendEvent:@"log" level:log];
}

- (void)sendEvent:(NSString*)code {
    [self sendEvent:code level:@""];
}

- (void)sendEvent:(NSString*)code level:(NSString*)level {
    FREDispatchStatusEventAsync(_context, (const uint8_t*)[code UTF8String], (const uint8_t*)[level UTF8String]);
}

+ (NSString*)convertToJSonString:(NSDictionary*)dict {
    
    if (!dict)
        return @"{}";
    
    NSError* jsonError = nil;
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:dict options:0 error:&jsonError];
    
    if (jsonError != nil) {
        
        NSLog(@"[AirPushNotification] JSON stringify error: %@", jsonError.localizedDescription);
        return @"{}";
    }
    
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

#pragma mark - other

/**
 
 */
- (void)trackRemoteNofiticationFromApp:(UIApplication*)app andUserInfo:(NSDictionary*)userInfo {
    
    NSString* stringInfo = [AirPushNotification convertToJSonString:userInfo];
    
    if (app.applicationState == UIApplicationStateActive)
        [self sendEvent:@"NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND" level:stringInfo];
    else if (app.applicationState == UIApplicationStateInactive)
        [self sendEvent:@"APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION" level:stringInfo];
    else if (app.applicationState == UIApplicationStateBackground)
        [self sendEvent:@"APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION" level:stringInfo];
}

/**

 */
+ (void)cancelAllLocalNotificationsWithId:(NSNumber*) notifId {
    
    [[UNUserNotificationCenter currentNotificationCenter] removePendingNotificationRequestsWithIdentifiers:@[[[NSString alloc] initWithFormat:@"%@", notifId]]];

    // leaving this in for migration purposes
    for (UILocalNotification* notif in [UIApplication sharedApplication].scheduledLocalNotifications) {

        if (notif.userInfo != nil) {

            if ([notif.userInfo objectForKey:[notifId stringValue]]) // also for migration
                [[UIApplication sharedApplication] cancelLocalNotification:notif];
            else if ([notif.userInfo objectForKey:@"notifId"]) { // the current way of storing notifId

                if ([[notif.userInfo objectForKey:@"notifId"] intValue] == [notifId intValue])
                    [[UIApplication sharedApplication] cancelLocalNotification:notif];
            }
        }
        else if ([notifId intValue] == 0) { // for migration purpose (all the notifications without userInfo will be removed)
            [[UIApplication sharedApplication] cancelLocalNotification:notif];
        }
    }
}

@end

#pragma mark - overrides

void didRegisterForRemoteNotificationsWithDeviceToken(id self, SEL _cmd, UIApplication* application, NSData* deviceToken) {
    
    NSString* tokenString = [NSString stringWithFormat:@"%@", deviceToken];
    [[AirPushNotification instance] sendEvent:@"TOKEN_SUCCESS" level:tokenString];
}

void didFailToRegisterForRemoteNotificationsWithError(id self, SEL _cmd, UIApplication* application, NSError* error) {
    
    NSString* tokenString = [NSString stringWithFormat:@"Failed to get token, error: %@", error];
    [[AirPushNotification instance] sendEvent:@"TOKEN_FAIL" level:tokenString];
}

void didReceiveRemoteNotification(id self, SEL _cmd, UIApplication* application, NSDictionary *userInfo) {
    
    NSString* stringInfo = [AirPushNotification convertToJSonString:userInfo];
    
    if (application.applicationState == UIApplicationStateActive) {
         [[AirPushNotification instance] sendEvent:@"NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND" level:stringInfo];
    } else if ([UNUserNotificationCenter class]) {
        return; // UNUserNotificationCenter will handle non-foreground notifs on iOS 10+
    } else if (application.applicationState == UIApplicationStateInactive) {
        [[AirPushNotification instance] sendEvent:@"APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION" level:stringInfo];
    } else if (application.applicationState == UIApplicationStateBackground) {
        [[AirPushNotification instance] sendEvent:@"APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION" level:stringInfo];
    }
}

void didRegisterUserNotificationSettings(id self, SEL _cmd, UIApplication* application, UIUserNotificationSettings* notificationSettings) {
    
    if (notificationSettings.types & UIUserNotificationTypeAlert) {
        
        [[AirPushNotification instance] sendEvent:@"NOTIFICATION_SETTINGS_ENABLED"];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else {
        [[AirPushNotification instance] sendEvent:@"NOTIFICATION_SETTINGS_DISABLED"];
    }
}

/**
    set the badge number (count around the app icon)
 */
DEFINE_ANE_FUNCTION(setBadgeNb) {
    
    int32_t value;
    if (FREGetObjectAsInt32(argv[0], &value) != FRE_OK)
        return nil;
    
    NSNumber* newBadgeValue = [NSNumber numberWithInt:value];
    UIApplication* application = [UIApplication sharedApplication];
    application.applicationIconBadgeNumber = [newBadgeValue integerValue];
    
    return NULL;
}


/**
    register the device for push notification.
 */
DEFINE_ANE_FUNCTION(registerPush) {
    
    if( [UNUserNotificationCenter class] ) {
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge)
                              completionHandler:^(BOOL granted, NSError * _Nullable error) {
                                  if( !error ){
                                      if(granted) {
                                          [[AirPushNotification instance] sendEvent:@"NOTIFICATION_SETTINGS_ENABLED"];
                                          //[[UIApplication sharedApplication] registerForRemoteNotifications];
                                      } else {
                                          [[AirPushNotification instance] sendEvent:@"NOTIFICATION_SETTINGS_DISABLED"];
                                      }
        
                                  } else {
                                      //todo maybe dispatch an error event here instead?
                                      [[AirPushNotification instance] sendEvent:@"NOTIFICATION_SETTINGS_DISABLED"];
                                  }  
                              }];
    }
    
    UIUserNotificationSettings* notificationSettings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert |
                                                        UIUserNotificationTypeBadge |
                                                        UIUserNotificationTypeSound
                                                        categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:notificationSettings];
    
    
    return NULL;
}

/**
 
 */
DEFINE_ANE_FUNCTION(setIsAppInForeground) {
    
    return NULL;
}

/**
 
 */
DEFINE_ANE_FUNCTION(fetchStarterNotification) {
    
    if ([UNUserNotificationCenter class]) {
        NSDictionary * receivedNotif = [AirPushNotification getAndClearDelegateStarterNotif];
        if (receivedNotif != nil) {
            NSString* receivedNotifString = [AirPushNotification convertToJSonString:receivedNotif];
            FREDispatchStatusEventAsync(context, (uint8_t*)"APP_STARTING_FROM_NOTIFICATION", (uint8_t*)[receivedNotifString UTF8String]);
        }
        
        return NULL;
    }
    
    BOOL appStartedWithNotification = [StarterNotificationChecker applicationStartedWithNotification];
    
    if (appStartedWithNotification) {
        
        NSDictionary* launchOptions = [StarterNotificationChecker getStarterNotification];
        NSString* stringInfo = [AirPushNotification convertToJSonString:launchOptions];
        FREDispatchStatusEventAsync(context, (uint8_t*)"APP_STARTING_FROM_NOTIFICATION", (uint8_t*)[stringInfo UTF8String]);
    }
    
    return NULL;
}


/**
    sends local notification to the device.
 */
DEFINE_ANE_FUNCTION(sendLocalNotification) {
    
    // message
    uint32_t string_length;
    const uint8_t* utf8_message;
    if (FREGetObjectAsUTF8(argv[0], &string_length, &utf8_message) != FRE_OK)
        return NULL;

    NSString* message = [NSString stringWithUTF8String:(char*)utf8_message];
    
    // timestamp
    uint32_t timestamp;
    if (FREGetObjectAsUint32(argv[1], &timestamp) != FRE_OK)
        return NULL;
   
    // title
    const uint8_t* utf8_title;
    NSString* title = nil;
    if (FREGetObjectAsUTF8(argv[2], &string_length, &utf8_title) == FRE_OK) {
        title = [NSString stringWithUTF8String:(char*)utf8_title];
    }
    
    // recurrence
    uint32_t recurrence = 0;
    FREGetObjectAsUint32(argv[3], &recurrence);
    
    // local notif id: 0 is default
    uint32_t localNotificationId = 0;
    if (FREGetObjectAsUint32(argv[4], &localNotificationId) != FRE_OK)
        localNotificationId = 0;

    NSNumber* localNotifIdNumber = [NSNumber numberWithInt:localNotificationId];
    [AirPushNotification cancelAllLocalNotificationsWithId:localNotifIdNumber];
    
    NSDate* itemDate = [NSDate dateWithTimeIntervalSince1970:timestamp];
    
    UNMutableNotificationContent *content = [UNMutableNotificationContent new];
    
    uint32_t path_len;
    const uint8_t *path_utf8;
    if (FREGetObjectAsUTF8(argv[5], &path_len, &path_utf8) == FRE_OK) {
        
        NSString* pathString = [NSString stringWithUTF8String:(char*)path_utf8];
        if(pathString.length > 0){
            content.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:localNotifIdNumber, @"notifId", pathString, @"path", nil];
        }
        else {
            content.userInfo = [NSDictionary dictionaryWithObject:localNotifIdNumber forKey:@"notifId"];
        }
        
    }
    else {
        content.userInfo = [NSDictionary dictionaryWithObject:localNotifIdNumber forKey:@"notifId"];
    }
    
    unsigned units = NSCalendarUnitDay | NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond;
    BOOL repeat = false;
    
    if (recurrence > 0) {
        
        if (recurrence == 1)
            units = units | NSCalendarUnitDay;
        else if (recurrence == 2)
            units = units | NSCalendarUnitWeekday;
        else if (recurrence == 3)
            units = units | NSCalendarUnitMonth;
        else if (recurrence == 4)
            units = units | NSCalendarUnitYear;
        
        repeat = true;
    }
    
    
    NSCalendar *calendar = [NSCalendar currentCalendar];
    calendar.timeZone = [NSTimeZone defaultTimeZone];
    NSDateComponents *components = [calendar components:units fromDate:itemDate];
    UNCalendarNotificationTrigger *trigger = [UNCalendarNotificationTrigger triggerWithDateMatchingComponents:components repeats:repeat];
    
    uint32_t iconURL_len;
    const uint8_t *iconURL_utf8;
    if (FREGetObjectAsUTF8(argv[6], &iconURL_len, &iconURL_utf8) == FRE_OK) {
        
        NSString* iconPath = [NSString stringWithUTF8String:(char*)iconURL_utf8];
        UIImage *img = [UIImage imageNamed:iconPath];
        NSURL *temporaryFileLocation = [[NSURL fileURLWithPath:NSTemporaryDirectory() isDirectory:YES] URLByAppendingPathComponent:@"notification_icon"];
        NSError *error;
        [UIImagePNGRepresentation(img) writeToFile:temporaryFileLocation.path options:0 error:&error];
        if (error)
        {
            NSLog(@"Error while creating temp file: %@", error);
        }
        UNNotificationAttachment *icon = [UNNotificationAttachment attachmentWithIdentifier:@"thumbnail" URL:temporaryFileLocation
                                                                                    options:@{UNNotificationAttachmentOptionsTypeHintKey: (NSString*)kUTTypePNG }
                                                                                      error:&error];
        if (icon)
        {
            content.attachments = @[icon];
        }
    }
    
    uint32_t groupId_len;
    const uint8_t *groupId_utf8;
    NSString* groupId = nil;
    if (FREGetObjectAsUTF8(argv[7], &groupId_len, &groupId_utf8) == FRE_OK) {
        groupId = [NSString stringWithUTF8String:(char*)groupId_utf8];
    }
    
    content.body = message;
    if(title != nil) {
        content.title = title;
    }
    content.sound = [UNNotificationSound defaultSound];
    content.threadIdentifier = groupId;
    
    UNNotificationRequest *request = [UNNotificationRequest requestWithIdentifier:[[NSString alloc] initWithFormat:@"%@", localNotifIdNumber] content:content trigger:trigger];
    [UNUserNotificationCenter.currentNotificationCenter addNotificationRequest:request withCompletionHandler:nil];
    
    return NULL;
}

/**
 
 */
DEFINE_ANE_FUNCTION(cancelLocalNotification) {
    
    uint32_t localNotificationId;
    
    if (argc != 1)
        localNotificationId = 0;
    else if (FREGetObjectAsUint32(argv[0], &localNotificationId) != FRE_OK)
        return nil;
    
    [AirPushNotification cancelAllLocalNotificationsWithId:[NSNumber numberWithInt:localNotificationId]];
    
    return NULL;
}

/**
 
 */
DEFINE_ANE_FUNCTION(cancelAllLocalNotifications) {
    

    [[UNUserNotificationCenter currentNotificationCenter] removeAllPendingNotificationRequests];
    // leaving this in for migration purposes
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    
    return NULL;
}

/**
 
 */
DEFINE_ANE_FUNCTION(getCanSendUserToSettings) {
    
    FREObject canSendObj = nil;
    FRENewObjectFromBool(1, &canSendObj);
    return canSendObj;
}

/**
 
 */
DEFINE_ANE_FUNCTION(getNotificationsEnabled) {
    
    UIUserNotificationSettings* notifSettings = [[UIApplication sharedApplication] currentUserNotificationSettings];
    BOOL enabled = (notifSettings.types & UIUserNotificationTypeAlert);
    
    FREObject notifsEnabled = nil;
    FRENewObjectFromBool(enabled, &notifsEnabled);
    return notifsEnabled;
}

/**
    when receiving a notification, AS3 might be not available, so we store all the info we need here.
 */
DEFINE_ANE_FUNCTION(storeNotifTrackingInfo) {
    
    uint32_t string_length;
    const uint8_t *utf8_message;
    if (FREGetObjectAsUTF8(argv[0], &string_length, &utf8_message) != FRE_OK)
        return nil;

    NSString* url = [NSString stringWithUTF8String:(char*)utf8_message];
    
    [[NSUserDefaults standardUserDefaults] setObject:url forKey:storedNotifTrackingUrl];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    return nil;
}

/**
 
 */
DEFINE_ANE_FUNCTION(openDeviceNotificationSettings) {
    
    UIApplication* app = [UIApplication sharedApplication];
    NSURL* appSettingsURL = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    
    if ([app respondsToSelector:@selector(openURL:options:completionHandler:)]) {  // ios 10
        
        [app openURL:appSettingsURL options:@{} completionHandler:^(BOOL success) {
            // nothing
        }];
    }
    else {
        [app openURL:appSettingsURL];
    }
    
    return NULL;
}

void AirPushNotificationContextInitializer(void* extData,
                                           const uint8_t* ctxType,
                                           FREContext ctx,
                                           uint32_t* numFunctionsToTest,
                                           const FRENamedFunction** functionsToSet) {
    
    [[AirPushNotification instance] setupWithContext:ctx];
    
    id delegate = [[UIApplication sharedApplication] delegate];
    Class objectClass = object_getClass(delegate);
    
    NSString* newClassName = [NSString stringWithFormat:@"Custom_%@", NSStringFromClass(objectClass)];
    Class modDelegate = NSClassFromString(newClassName);
    
    if (modDelegate == nil) {
        
        // this class doesn't exist; create it
        // allocate a new class
        modDelegate = objc_allocateClassPair(objectClass, [newClassName UTF8String], 0);
        
        SEL selectorToOverride1 = @selector(application:didRegisterForRemoteNotificationsWithDeviceToken:);
        SEL selectorToOverride2 = @selector(application:didFailToRegisterForRemoteNotificationsWithError:);
        SEL selectorToOverride3 = @selector(application:didReceiveRemoteNotification:);
        SEL selectorToOverride4 = @selector(application:didRegisterUserNotificationSettings:);
        
        // get the info on the method we're going to override
        Method m1 = class_getInstanceMethod(objectClass, selectorToOverride1);
        Method m2 = class_getInstanceMethod(objectClass, selectorToOverride2);
        Method m3 = class_getInstanceMethod(objectClass, selectorToOverride3);
        Method m4 = class_getInstanceMethod(objectClass, selectorToOverride4);
        
        // add the method to the new class
        class_addMethod(modDelegate, selectorToOverride1, (IMP)didRegisterForRemoteNotificationsWithDeviceToken, method_getTypeEncoding(m1));
        class_addMethod(modDelegate, selectorToOverride2, (IMP)didFailToRegisterForRemoteNotificationsWithError, method_getTypeEncoding(m2));
        class_addMethod(modDelegate, selectorToOverride3, (IMP)didReceiveRemoteNotification, method_getTypeEncoding(m3));
        class_addMethod(modDelegate, selectorToOverride4, (IMP)didRegisterUserNotificationSettings, method_getTypeEncoding(m4));
        
        // register the new class with the runtime
        objc_registerClassPair(modDelegate);
    }
    
    // change the class of the object
    object_setClass(delegate, modDelegate);
    
    static FRENamedFunction functions[] = {
        MAP_FUNCTION(registerPush, NULL),
        MAP_FUNCTION(setBadgeNb, NULL),
        MAP_FUNCTION(sendLocalNotification, NULL),
        MAP_FUNCTION(setIsAppInForeground, NULL),
        MAP_FUNCTION(fetchStarterNotification, NULL),
        MAP_FUNCTION(cancelLocalNotification, NULL),
        MAP_FUNCTION(cancelAllLocalNotifications, NULL),
        MAP_FUNCTION(getCanSendUserToSettings, NULL),
        MAP_FUNCTION(getNotificationsEnabled, NULL),
        MAP_FUNCTION(openDeviceNotificationSettings, NULL),
        MAP_FUNCTION(storeNotifTrackingInfo, NULL)
    };
    
    *numFunctionsToTest = sizeof(functions) / sizeof(FRENamedFunction);
    *functionsToSet = functions;
    
}

void AirPushNotificationContextFinalizer(FREContext ctx) {
    
    // nothing
}

void AirPushNotificationInitializer(void** extDataToSet,
                                    FREContextInitializer* ctxInitializerToSet,
                                    FREContextFinalizer* ctxFinalizerToSet) {
    
    *extDataToSet = NULL;
    *ctxInitializerToSet = &AirPushNotificationContextInitializer;
    *ctxFinalizerToSet = &AirPushNotificationContextFinalizer;
}

void AirPushNotificationFinalizer(void *extData) {
    
    // nothing
}


