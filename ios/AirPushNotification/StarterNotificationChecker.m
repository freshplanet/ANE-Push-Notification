//
//  StarterNotificationChecker.m
//  AirPushNotification
//
//  Created by Thibaut on 13/12/12.
//
//

#import "StarterNotificationChecker.h"

static BOOL _startedWithNotification = NO;
static NSDictionary *_notification = nil;

@implementation StarterNotificationChecker

// hack, this is called before UIApplicationDidFinishLaunching
+ (void)load
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(createStarterNotificationChecker:)
                                                 name:@"UIApplicationDidFinishLaunchingNotification" object:nil];
    
}

+ (void)createStarterNotificationChecker:(NSNotification *)notification
{
    NSDictionary *launchOptions = [notification userInfo] ;
    
    // This code will be called immediately after application:didFinishLaunchingWithOptions:.
    NSDictionary *remoteNotification = [launchOptions objectForKey: @"UIApplicationLaunchOptionsRemoteNotificationKey"];
    if (notification)
    {
        _startedWithNotification = YES;
        _notification = remoteNotification;
    }
    else
    {
        _startedWithNotification = NO;
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
