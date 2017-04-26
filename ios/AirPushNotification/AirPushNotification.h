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

#ifndef AirPushNotification_AirPushNotification_h
#define AirPushNotification_AirPushNotification_h
#endif

#import <UIKit/UIKit.h>
#import "StarterNotificationChecker.h"

@interface  AirPushNotification : NSObject <UIApplicationDelegate>

+(NSString*) convertToJSonString:(NSDictionary*)dict;

@end

FREObject setBadgeNb(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject registerPush(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject sendLocalNotification(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject cancelLocalNotification(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject cancelAllLocalNotifications(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject setIsAppInForeground(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject fetchStarterNotification(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject getCanSendUserToSettings(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject getNotificationsEnabled(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);
FREObject openDeviceSettings(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);

void AirPushContextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, uint32_t* numFunctionsToTest, const FRENamedFunction** functionsToSet);
void AirPushContextFinalizer(FREContext ctx);
void AirPushExtInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet );
void AirPushExtFinalizer(void *extData);

void didRegisterUserNotificationSettings(id self, SEL _cmd, UIApplication* application, UIUserNotificationSettings* notificationSettings);
void didRegisterForRemoteNotificationsWithDeviceToken(id self, SEL _cmd, UIApplication* application, NSData* deviceToken);
void didFailToRegisterForRemoteNotificationsWithError(id self, SEL _cmd, UIApplication* application, NSError* error);
void didReceiveRemoteNotification(id self, SEL _cmd, UIApplication* application,NSDictionary *userInfo);
