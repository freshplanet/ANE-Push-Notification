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


#import "StarterNotificationChecker.h"
#import "SPNotifCenterDelegate.h"

#import <UserNotifications/UserNotifications.h>

static BOOL _startedWithNotification = NO;
static NSDictionary *_notification = nil;

@implementation StarterNotificationChecker

// hack, this is called before UIApplicationDidFinishLaunching
+ (void)load
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(createStarterNotificationChecker:)
                                                 name:@"UIApplicationDidFinishLaunchingNotification" object:nil];
    
    if([UNUserNotificationCenter class]) {
        [[UNUserNotificationCenter currentNotificationCenter] setDelegate: [[SPNotifCenterDelegate alloc] init]];
    }
    
}

+ (void)createStarterNotificationChecker:(NSNotification *)notification
{
    NSDictionary *launchOptions = [notification userInfo] ;
    
    // This code will be called immediately after application:didFinishLaunchingWithOptions:.
    NSDictionary *remoteNotification = [launchOptions objectForKey: UIApplicationLaunchOptionsRemoteNotificationKey];
    if (notification)
    {
        _startedWithNotification = YES;
        _notification = remoteNotification;
    }
    else
    {
        UILocalNotification *localNotification = [launchOptions objectForKey: UIApplicationLaunchOptionsLocalNotificationKey];
        if(localNotification) {
            _notification = localNotification.userInfo;
            _startedWithNotification = YES;
        } else {
          _startedWithNotification = NO;
        }
    }
}

+(BOOL) applicationStartedWithNotification
{
    return _startedWithNotification;
}

+(NSDictionary*) getStarterNotification
{
    return _notification;
}

+ (void) deleteStarterNotification
{
    _notification = nil;
}

@end
