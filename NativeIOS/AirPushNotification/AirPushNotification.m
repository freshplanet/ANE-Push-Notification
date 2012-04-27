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

@end


FREContext myCtx = nil;

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
        FREDispatchStatusEventAsync(myCtx, (uint8_t*)"COMING_FROM_NOTIFICATION", (uint8_t*)[@"" UTF8String]); 
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
    
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
     (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
    return nil;
}

// sends local notification to the device.
DEFINE_ANE_FUNCTION(sendLocalNotification)
{
    // delete previously local notification
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    
    uint32_t string_length;
    const uint8_t *utf8_message;
    if (FREGetObjectAsUTF8(argv[0], &string_length, &utf8_message) != FRE_OK)
    {
        return nil;
    }

    NSString* message = [NSString stringWithUTF8String:(char*)utf8_message];
    
    uint32_t timestamp;
   if (FREGetObjectAsUint32(argv[1], &timestamp) != FRE_OK)
   {
       return nil;
   }
    
    
    NSDate *itemDate = [NSDate dateWithTimeIntervalSince1970:timestamp];
    
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif == nil)
        return NULL;
    localNotif.fireDate = itemDate;
    localNotif.timeZone = [NSTimeZone defaultTimeZone];
    
    localNotif.alertBody = message;
    localNotif.alertAction = @"View Details";
    
    localNotif.soundName = UILocalNotificationDefaultSoundName;    
    
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
    [localNotif release];
    return NULL;
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
         
         // get the info on the method we're going to override
         Method m1 = class_getInstanceMethod(objectClass, selectorToOverride1);
         Method m2 = class_getInstanceMethod(objectClass, selectorToOverride2);
         
         // add the method to the new class
         class_addMethod(modDelegate, selectorToOverride1, (IMP)didRegisterForRemoteNotificationsWithDeviceToken, method_getTypeEncoding(m1));
         
         class_addMethod(modDelegate, selectorToOverride2, (IMP)didFailToRegisterForRemoteNotificationsWithError, method_getTypeEncoding(m2));
         
         // register the new class with the runtime
         objc_registerClassPair(modDelegate);
     }
     // change the class of the object
     object_setClass(delegate, modDelegate);
    
    ///////// end of delegate injection / modification code
    
    // Register the links btwn AS3 and ObjC. (dont forget to modify the nbFuntionsToLink integer if you are adding/removing functions)
    NSInteger nbFuntionsToLink = 3;
    *numFunctionsToTest = nbFuntionsToLink;
    
    FRENamedFunction* func = (FRENamedFunction*) malloc(sizeof(FRENamedFunction) * nbFuntionsToLink);
    
    func[0].name = (const uint8_t*) "registerPush";
    func[0].functionData = NULL;
    func[0].function = &registerPush;
    
    func[1].name = (const uint8_t*) "setBadgeNb";
    func[1].functionData = NULL;
    func[1].function = &setBadgeNb;
    
    func[2].name = (const uint8_t*) "sendLocalNotification";
    func[1].functionData = NULL;
    func[2].function = &sendLocalNotification;
    
    *functionsToSet = func;
    
    myCtx = ctx;
}

// AirPushContextFinalizer()
//
// Set when the context extension is created.

void AirPushContextFinalizer(FREContext ctx) { 
    NSLog(@"Entering ContextFinalizer()");
    
    NSLog(@"Exiting ContextFinalizer()");	
}



// AirPushExtInitializer()
//
// The extension initializer is called the first time the ActionScript side of the extension
// calls ExtensionContext.createExtensionContext() for any context.

void AirPushExtInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet ) 
{
    
    NSLog(@"Entering ExtInitializer()");                    
    
	*extDataToSet = NULL;
	*ctxInitializerToSet = &AirPushContextInitializer; 
	*ctxFinalizerToSet = &AirPushContextFinalizer;
    
    NSLog(@"Exiting ExtInitializer()"); 
}