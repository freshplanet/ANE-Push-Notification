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

#define DEFINE_ANE_FUNCTION(fn) FREObject (fn)(FREContext context, void* functionData, uint32_t argc, FREObject argv[])
#define MAP_FUNCTION(fn, data) { (const uint8_t*)(#fn), (data), &(fn) }

NSString* const storedNotifTrackingUrl = @"storedNotifTrackingUrl";

@implementation AirPushNotification

#pragma mark - init

+ (AirPushNotification*)instance {
    
    static AirPushNotification* _sharedInstance = nil;
    static dispatch_once_t oncePredicate;
    
    dispatch_once(&oncePredicate, ^{
        _sharedInstance = [[AirPushNotification alloc] init];
    });
    
    return _sharedInstance;
}

- (void)setupWithContext:(FREContext)extensionContext {
    
    _context = extensionContext;
    
    id delegate = [[UIApplication sharedApplication] delegate];
    Class delegateClass = [delegate class];
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        
        [self swizzleInstanceMethodsFromClass:delegateClass
                               originSelector:@selector(application:didRegisterForRemoteNotificationsWithDeviceToken:)
                             swizzledSelector:@selector(xxx_application:didRegisterForRemoteNotificationsWithDeviceToken:)];
        
        [self swizzleInstanceMethodsFromClass:delegateClass
                               originSelector:@selector(application:didReceiveRemoteNotification:)
                             swizzledSelector:@selector(xxx_application:didReceiveRemoteNotification:)];
        
        [self swizzleInstanceMethodsFromClass:delegateClass
                               originSelector:@selector(application:didReceiveRemoteNotification:)
                             swizzledSelector:@selector(xxx_application:didReceiveRemoteNotification:)];
        
        [self swizzleInstanceMethodsFromClass:delegateClass
                               originSelector:@selector(application:didRegisterUserNotificationSettings:)
                             swizzledSelector:@selector(xxx_application:didRegisterUserNotificationSettings:)];
    });
}

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

- (void)swizzleInstanceMethodsFromClass:(Class)originClass originSelector:(SEL)originSelector swizzledSelector:(SEL)swizzledSelector {
    
    Class swizzleClass = [self class];
    
    Method originalMethod = class_getInstanceMethod(originClass, originSelector);
    Method swizzledMethod = class_getInstanceMethod(swizzleClass, swizzledSelector);
    
    BOOL didAddMethod = class_addMethod(originClass,
                                        originSelector,
                                        method_getImplementation(swizzledMethod),
                                        method_getTypeEncoding(swizzledMethod));
    
    if (didAddMethod) {
        
        class_replaceMethod(originClass,
                            swizzledSelector,
                            method_getImplementation(originalMethod),
                            method_getTypeEncoding(originalMethod));
    }
    else {
        method_exchangeImplementations(originalMethod, swizzledMethod);
    }
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

#pragma mark - notification registration events

- (void)xxx_application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken {
    
    NSString* tokenString = [NSString stringWithFormat:@"%@", deviceToken];
    [self sendEvent:@"TOKEN_SUCCESS" level:tokenString];
}

- (void)xxx_application:(UIApplication*)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error {
    
    NSString* tokenString = [NSString stringWithFormat:@"Failed to get token, error: %@", error];
    [self sendEvent:@"TOKEN_FAIL" level:tokenString];
}

- (void)xxx_application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary*)userInfo {
    
    NSString* stringInfo = [AirPushNotification convertToJSonString:userInfo];
    
    if (application.applicationState == UIApplicationStateActive)
        [self sendEvent:@"NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND" level:stringInfo];
    else if (application.applicationState == UIApplicationStateInactive)
        [self sendEvent:@"APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION" level:stringInfo];
    else if (application.applicationState == UIApplicationStateBackground)
        [self sendEvent:@"APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION" level:stringInfo];
}

- (void)xxx_application:(UIApplication*)application didRegisterUserNotificationSettings:(UIUserNotificationSettings*)notificationSettings {

    if (notificationSettings.types & UIUserNotificationTypeAlert) {
        
        [self sendEvent:@"NOTIFICATION_SETTINGS_ENABLED"];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else {
        [self sendEvent:@"NOTIFICATION_SETTINGS_DISABLED"];
    }
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
    
    // we remove all notifications with the localNotificationId
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
   
    // recurrence
    uint32_t recurrence = 0;
    if (argc >= 4)
        FREGetObjectAsUint32(argv[3], &recurrence);
    
    // local notif id: 0 is default
    uint32_t localNotificationId = 0;
    if (argc >= 5 && FREGetObjectAsUint32(argv[4], &localNotificationId) != FRE_OK)
        localNotificationId = 0;

    NSNumber* localNotifIdNumber = [NSNumber numberWithInt:localNotificationId];
    [AirPushNotification cancelAllLocalNotificationsWithId:localNotifIdNumber];
    
    NSDate* itemDate = [NSDate dateWithTimeIntervalSince1970:timestamp];
    UILocalNotification* localNotif = [[UILocalNotification alloc] init];
    
    if (localNotif == nil)
        return NULL;
    
    localNotif.fireDate = itemDate;
    localNotif.timeZone = [NSTimeZone defaultTimeZone];
    
    localNotif.alertBody = message;
    localNotif.alertAction = @"View Details";
    localNotif.soundName = UILocalNotificationDefaultSoundName;
    
    if (argc == 6) { // optional path for deep linking
        
        uint32_t path_len;
        const uint8_t *path_utf8;
        if (FREGetObjectAsUTF8(argv[5], &path_len, &path_utf8) == FRE_OK) {
            
            NSString* pathString = [NSString stringWithUTF8String:(char*)path_utf8];
            localNotif.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:localNotifIdNumber, @"notifId", pathString, @"path", nil];
        }
        else {
            localNotif.userInfo = [NSDictionary dictionaryWithObject:localNotifIdNumber forKey:@"notifId"];
        }
        
    }
    else {
        localNotif.userInfo = [NSDictionary dictionaryWithObject:localNotifIdNumber forKey:@"notifId"];
    }
    
    if (recurrence > 0) {
        
        if (recurrence == 1)
            localNotif.repeatInterval = NSDayCalendarUnit;
        else if (recurrence == 2)
            localNotif.repeatInterval = NSWeekCalendarUnit;
        else if (recurrence == 3)
            localNotif.repeatInterval = NSMonthCalendarUnit;
        else if (recurrence == 4)
            localNotif.repeatInterval = NSYearCalendarUnit;
    }
    
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
//    [localNotif release];
    
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
    
//    if ([UNUserNotificationCenter class]) // ios 10
//        [[UNUserNotificationCenter currentNotificationCenter] removeAllPendingNotificationRequests];
//    else
//        [[UIApplication sharedApplication] cancelAllLocalNotifications];
    
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
    
    FRENamedFunction functions[] = {
        MAP_FUNCTION(registerPush, NULL),
        MAP_FUNCTION(setBadgeNb, NULL),
        MAP_FUNCTION(sendLocalNotification, NULL),
        MAP_FUNCTION(setIsAppInForeground, NULL),
        MAP_FUNCTION(fetchStarterNotification, NULL),
        MAP_FUNCTION(cancelLocalNotification, NULL),
        MAP_FUNCTION(cancelAllLocalNotifications, NULL),
        MAP_FUNCTION(getCanSendUserToSettings, NULL),
        MAP_FUNCTION(getNotificationsEnabled, NULL),
        MAP_FUNCTION(openDeviceNotificationSettings, NULL)
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


