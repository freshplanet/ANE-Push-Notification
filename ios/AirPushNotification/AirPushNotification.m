//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

#import "FlashRuntimeExtensions.h"
#import <UIKit/UIApplication.h>
#import <UIKit/UIAlertView.h>
#import "AirPushNotification.h"
#import <objc/runtime.h>
#import <objc/message.h>

#define DEFINE_ANE_FUNCTION(fn) FREObject (fn)(FREContext context, void* functionData, uint32_t argc, FREObject argv[])

@implementation AirPushNotification

//empty delegate functions, stubbed signature is so we can find this method in the delegate
//and override it with our custom implementation
- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken{}
// 
- (void)application:(UIApplication*)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error{}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo{}

- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings{}

+ (NSString*) convertToJSonString:(NSDictionary*)dict
{
    if(dict == nil) {
        return @"{}";
    }
    NSError *jsonError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:0 error:&jsonError];
    if (jsonError != nil) {
        NSLog(@"[AirPushNotification] JSON stringify error: %@", jsonError.localizedDescription);
        return @"{}";
    }
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+ (void) cancelAllLocalNotificationsWithId:(NSNumber*) notifId
{
    // we remove all notifications with the localNotificationId
    for (UILocalNotification* notif in [UIApplication sharedApplication].scheduledLocalNotifications)
    {
        if (notif.userInfo != nil)
        {
            if ([notif.userInfo objectForKey:[notifId stringValue]]) // also for migration
            {
                [[UIApplication sharedApplication] cancelLocalNotification:notif];
            } else if ([notif.userInfo objectForKey:@"notifId"]) { // the current way of storing notifId
                if([[notif.userInfo objectForKey:@"notifId"] intValue] == [notifId intValue]) {
                    [[UIApplication sharedApplication] cancelLocalNotification:notif];
                }
            }
        } else if ([notifId intValue] == 0) // for migration purpose (all the notifications without userInfo will be removed)
        {
            [[UIApplication sharedApplication] cancelLocalNotification:notif];
        }
    }
}

@end

FREContext myCtx = nil;

void didRegisterUserNotificationSettings(id self, SEL _cmd, UIApplication* application, UIUserNotificationSettings* notificationSettings)
{
    if(notificationSettings.types & UIUserNotificationTypeAlert) {
        FREDispatchStatusEventAsync(myCtx, (uint8_t*)"NOTIFICATION_SETTINGS_ENABLED", (uint8_t*)"");
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    } else {
        FREDispatchStatusEventAsync(myCtx, (uint8_t*)"NOTIFICATION_SETTINGS_DISABLED", (uint8_t*)"");
    }
    
}

//custom implementations of empty signatures above. Used for push notification delegate implementation.
void didRegisterForRemoteNotificationsWithDeviceToken(id self, SEL _cmd, UIApplication* application, NSData* deviceToken)
{
    NSString* tokenString = [NSString stringWithFormat:@"%@", deviceToken];
    NSLog(@"My token is: %@", deviceToken);

    if ( myCtx != nil )
    {
        FREDispatchStatusEventAsync(myCtx, (uint8_t*)"TOKEN_SUCCESS", (uint8_t*)[tokenString UTF8String]); 
    }
}

//custom implementations of empty signatures above. Used for push notification delegate implementation.
void didFailToRegisterForRemoteNotificationsWithError(id self, SEL _cmd, UIApplication* application, NSError* error)
{
    
    NSString* tokenString = [NSString stringWithFormat:@"Failed to get token, error: %@",error];
    
    if ( myCtx != nil )
    {
        FREDispatchStatusEventAsync(myCtx, (uint8_t*)"TOKEN_FAIL", (uint8_t*)[tokenString UTF8String]); 
    }
}

//custom implementations of empty signatures above. Used for push notification delegate implementation.
void didReceiveRemoteNotification(id self, SEL _cmd, UIApplication* application,NSDictionary *userInfo)
{
    if ( myCtx != nil )
    {
    NSString *stringInfo = [AirPushNotification convertToJSonString:userInfo];
        if (application.applicationState == UIApplicationStateActive)
        {
            FREDispatchStatusEventAsync(myCtx, (uint8_t*)"NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND", (uint8_t*)[stringInfo UTF8String]);
        }
        else if (application.applicationState == UIApplicationStateInactive)
        {
            FREDispatchStatusEventAsync(myCtx, (uint8_t*)"APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION", (uint8_t*)[stringInfo UTF8String]);
        }
        else if (application.applicationState == UIApplicationStateBackground)
        {
            FREDispatchStatusEventAsync(myCtx, (uint8_t*)"APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION", (uint8_t*)[stringInfo UTF8String]);
        }
    }
}




// set the badge number (count around the app icon)
DEFINE_ANE_FUNCTION(setBadgeNb)
{
    int32_t value;
    if (FREGetObjectAsInt32(argv[0], &value) != FRE_OK)
    {
        return nil;
    }
    
    NSNumber *newBadgeValue = [NSNumber numberWithInt:value]; 
    
    UIApplication *uiapplication = [UIApplication sharedApplication];
    uiapplication.applicationIconBadgeNumber = [newBadgeValue integerValue];
    return nil;
}


// register the device for push notification.
DEFINE_ANE_FUNCTION(registerPush)
{
    // iOS8+ selector 
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
         UIUserNotificationSettings* notificationSettings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:notificationSettings];
    } else { // iOS7 or less
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
         (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
    }
    return nil;
}

// register the device for push notification.
DEFINE_ANE_FUNCTION(setIsAppInForeground)
{    
    return nil;
}

// register the device for push notification.
DEFINE_ANE_FUNCTION(fetchStarterNotification)
{
    BOOL appStartedWithNotification = [StarterNotificationChecker applicationStartedWithNotification];
    if(appStartedWithNotification)
    {
        NSDictionary *launchOptions = [StarterNotificationChecker getStarterNotification];
        NSString *stringInfo = [AirPushNotification convertToJSonString:launchOptions];
        FREDispatchStatusEventAsync(myCtx, (uint8_t*)"APP_STARTING_FROM_NOTIFICATION", (uint8_t*)[stringInfo UTF8String]);
    }
    return nil;
}



// sends local notification to the device.
DEFINE_ANE_FUNCTION(sendLocalNotification)
{

    uint32_t string_length;
    const uint8_t *utf8_message;
    // message
    if (FREGetObjectAsUTF8(argv[0], &string_length, &utf8_message) != FRE_OK)
    {
        return nil;
    }

    NSString* message = [NSString stringWithUTF8String:(char*)utf8_message];
    
    // timestamp
    uint32_t timestamp;
   if (FREGetObjectAsUint32(argv[1], &timestamp) != FRE_OK)
   {
       return nil;
   }
   
    // recurrence
    uint32_t recurrence = 0;
    if (argc >= 4 )
    {
        FREGetObjectAsUint32(argv[3], &recurrence);
    }
    
    // sound name : UILocalNotificationDefaultSoundName is default
    NSString* soundName = UILocalNotificationDefaultSoundName;
    if(argc >= 5)
    {
        const uint8_t *utf8_soundName;
        if (FREGetObjectAsUTF8(argv[4], &string_length, &utf8_soundName) == FRE_OK)
        {
            soundName = [NSString stringWithUTF8String:(char*)utf8_soundName];
        }
    }
    
    // local notif id: 0 is default
    uint32_t localNotificationId = 0;
    if (argc >= 6)
    {
        if (FREGetObjectAsUint32(argv[5], &localNotificationId) != FRE_OK)
        {
            localNotificationId = 0;
        }
    }
    
    
    NSNumber *localNotifIdNumber =[NSNumber numberWithInt:localNotificationId];
        
    [AirPushNotification cancelAllLocalNotificationsWithId:localNotifIdNumber];
    
    NSDate *itemDate = [NSDate dateWithTimeIntervalSince1970:timestamp];
    
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif == nil)
        return NULL;
    localNotif.fireDate = itemDate;
    localNotif.timeZone = [NSTimeZone defaultTimeZone];

    localNotif.alertBody = message;
    localNotif.alertAction = @"View Details";
    localNotif.soundName = soundName;
    
    if (argc == 7) // optional path for deep linking
    {
        uint32_t path_len;
        const uint8_t *path_utf8;
        if (FREGetObjectAsUTF8(argv[6], &path_len, &path_utf8) == FRE_OK) {
            NSString* pathString = [NSString stringWithUTF8String:(char*)path_utf8];
            localNotif.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:localNotifIdNumber, @"notifId", pathString, @"path", nil];
        } else {
            localNotif.userInfo = [NSDictionary dictionaryWithObject:localNotifIdNumber forKey:@"notifId"];
        }
        
    } else {
        localNotif.userInfo = [NSDictionary dictionaryWithObject:localNotifIdNumber forKey:@"notifId"];
    }
    
    

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
    
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
    [localNotif release];
    return NULL;
}

// sends local notification to the device.
DEFINE_ANE_FUNCTION(cancelLocalNotification)
{
    uint32_t localNotificationId;
    if (argc == 1)
    {
        if (FREGetObjectAsUint32(argv[0], &localNotificationId) != FRE_OK)
        {
            return nil;
        }
    } else
    {
        localNotificationId = 0;
    }
    
    [AirPushNotification cancelAllLocalNotificationsWithId:[NSNumber numberWithInt:localNotificationId]];
    return nil;

}

DEFINE_ANE_FUNCTION(cancelAllLocalNotifications)
{
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    return nil;
}

DEFINE_ANE_FUNCTION(getCanSendUserToSettings)
{
    FREObject canSendObj = nil;
    // if this selector is available the other feature is as well
    uint32_t canSend = [[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)] ? 1 : 0;
    FRENewObjectFromBool(canSend, &canSendObj);
    return canSendObj;
}

//TODO implement this for ios8 and return true for ios7
DEFINE_ANE_FUNCTION(getNotificationsEnabled)
{
    BOOL enabled = true;
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(currentUserNotificationSettings)]){
        UIUserNotificationSettings *notifSettings = [[UIApplication sharedApplication] currentUserNotificationSettings];
        enabled = (notifSettings.types & UIUserNotificationTypeAlert);
    }
    
    FREObject notifsEnabled = nil;
    FRENewObjectFromBool(enabled, &notifsEnabled);
    return notifsEnabled;
}

DEFINE_ANE_FUNCTION(openDeviceSettings)
{
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
    return nil;
}

// AirPushContextInitializer()
//
// The context initializer is called when the runtime creates the extension context instance.
void AirPushContextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, 
                        uint32_t* numFunctionsToTest, const FRENamedFunction** functionsToSet) 
{
    
    //injects our modified delegate functions into the sharedApplication delegate
    
     id delegate = [[UIApplication sharedApplication] delegate];
     
     Class objectClass = object_getClass(delegate);
     
     NSString *newClassName = [NSString stringWithFormat:@"Custom_%@", NSStringFromClass(objectClass)];
     Class modDelegate = NSClassFromString(newClassName);
     if (modDelegate == nil) {
         // this class doesn't exist; create it
         // allocate a new class
         modDelegate = objc_allocateClassPair(objectClass, [newClassName UTF8String], 0);
         
         SEL selectorToOverride1 = @selector(application:didRegisterForRemoteNotificationsWithDeviceToken:);
         
         SEL selectorToOverride2 = @selector(application:didFailToRegisterForRemoteNotificationsWithError:);
         
         SEL selectorToOverride3 = @selector(application:didReceiveRemoteNotification:);
         
         // get the info on the method we're going to override
         Method m1 = class_getInstanceMethod(objectClass, selectorToOverride1);
         Method m2 = class_getInstanceMethod(objectClass, selectorToOverride2);
         Method m3 = class_getInstanceMethod(objectClass, selectorToOverride3);
         
         // add the method to the new class
         class_addMethod(modDelegate, selectorToOverride1, (IMP)didRegisterForRemoteNotificationsWithDeviceToken, method_getTypeEncoding(m1));
         class_addMethod(modDelegate, selectorToOverride2, (IMP)didFailToRegisterForRemoteNotificationsWithError, method_getTypeEncoding(m2));
         class_addMethod(modDelegate, selectorToOverride3, (IMP)didReceiveRemoteNotification, method_getTypeEncoding(m3));
         
         if ([[UIApplication sharedApplication] respondsToSelector:@selector(currentUserNotificationSettings)]){
             SEL selectorToOverride4 = @selector(application:didRegisterUserNotificationSettings:);
             Method m4 = class_getInstanceMethod(objectClass, selectorToOverride4);
             class_addMethod(modDelegate, selectorToOverride4, (IMP)didRegisterUserNotificationSettings, method_getTypeEncoding(m4));
         }

         // register the new class with the runtime
         objc_registerClassPair(modDelegate);
     }
     // change the class of the object
     object_setClass(delegate, modDelegate);
    
    ///////// end of delegate injection / modification code
    
    // Register the links btwn AS3 and ObjC. (dont forget to modify the nbFuntionsToLink integer if you are adding/removing functions)
    NSInteger nbFuntionsToLink = 10;
    *numFunctionsToTest = nbFuntionsToLink;
    
    FRENamedFunction* func = (FRENamedFunction*) malloc(sizeof(FRENamedFunction) * nbFuntionsToLink);
    
    func[0].name = (const uint8_t*) "registerPush";
    func[0].functionData = NULL;
    func[0].function = &registerPush;
    
    func[1].name = (const uint8_t*) "setBadgeNb";
    func[1].functionData = NULL;
    func[1].function = &setBadgeNb;
    
    func[2].name = (const uint8_t*) "sendLocalNotification";
    func[2].functionData = NULL;
    func[2].function = &sendLocalNotification;
    
    func[3].name = (const uint8_t*) "setIsAppInForeground";
    func[3].functionData = NULL;
    func[3].function = &setIsAppInForeground;

    func[4].name = (const uint8_t*) "fetchStarterNotification";
    func[4].functionData = NULL;
    func[4].function = &fetchStarterNotification;

    func[5].name = (const uint8_t*) "cancelLocalNotification";
    func[5].functionData = NULL;
    func[5].function = &cancelLocalNotification;
    
    func[6].name = (const uint8_t*) "cancelAllLocalNotifications";
    func[6].functionData = NULL;
    func[6].function = &cancelAllLocalNotifications;
    
    func[7].name = (const uint8_t*) "getCanSendUserToSettings";
    func[7].functionData = NULL;
    func[7].function = &getCanSendUserToSettings;
    
    func[8].name = (const uint8_t*) "getNotificationsEnabled";
    func[8].functionData = NULL;
    func[8].function = &getNotificationsEnabled;
    
    func[9].name = (const uint8_t*) "openDeviceNotificationSettings";
    func[9].functionData = NULL;
    func[9].function = &openDeviceSettings;

    
    *functionsToSet = func;
    
    myCtx = ctx;
}

// AirPushContextFinalizer()
//
// Set when the context extension is created.

void AirPushContextFinalizer(FREContext ctx) { 
}



// AirPushExtInitializer()
//
// The extension initializer is called the first time the ActionScript side of the extension
// calls ExtensionContext.createExtensionContext() for any context.

void AirPushExtInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet ) 
{
    *extDataToSet = NULL;
    *ctxInitializerToSet = &AirPushContextInitializer; 
    *ctxFinalizerToSet = &AirPushContextFinalizer;
}

void AirPushExtFinalizer(void *extData) { }
