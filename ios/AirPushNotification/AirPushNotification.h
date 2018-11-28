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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "FlashRuntimeExtensions.h"

@interface  AirPushNotification : NSObject <UIApplicationDelegate> {
    FREContext _context;
}

+ (AirPushNotification*)instance;
+ (NSString*)convertToJSonString:(NSDictionary*)dict;

- (BOOL) isInitialized;
- (void)trackRemoteNofiticationFromApp:(UIApplication*)app andUserInfo:(NSDictionary*)userInfo;
- (void)sendEvent:(NSString*)code;
- (void)sendLog:(NSString*)log;
- (void)sendEvent:(NSString*)code level:(NSString*)level;

@end

void AirPushNotificationContextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, uint32_t* numFunctionsToTest, const FRENamedFunction** functionsToSet);
void AirPushNotificationContextFinalizer(FREContext ctx);
void AirPushNotificationInitializer(void** extDataToSet, FREContextInitializer* ctxInitializerToSet, FREContextFinalizer* ctxFinalizerToSet);
void AirPushNotificationFinalizer(void *extData);

