#import "Rdservice.h"

/**
 * UIDAI RD (Registered Device) services are certified for Android and Windows
 * only — the capture flow depends on RD service apps resolved via Android
 * intents, which have no iOS equivalent. These implementations exist so the
 * module loads on iOS and resolves with a consistent FAILURE response instead
 * of crashing, letting cross-platform apps share one code path.
 */
static NSDictionary *RdserviceUnsupportedResponse(void)
{
  return @{
    @"status" : @"FAILURE",
    @"message" : @"UIDAI RD services are not available on iOS. Biometric capture is supported on Android only.",
  };
}

@implementation Rdservice

- (void)getDeviceInfo:(NSString *)deviceName
              resolve:(RCTPromiseResolveBlock)resolve
               reject:(RCTPromiseRejectBlock)reject
{
  resolve(RdserviceUnsupportedResponse());
}

- (void)getFingerPrint:(NSString *)deviceName
             pidOption:(NSString *)pidOption
               resolve:(RCTPromiseResolveBlock)resolve
                reject:(RCTPromiseRejectBlock)reject
{
  resolve(RdserviceUnsupportedResponse());
}

- (void)getIrisCapture:(NSString *)deviceName
             pidOption:(NSString *)pidOption
               resolve:(RCTPromiseResolveBlock)resolve
                reject:(RCTPromiseRejectBlock)reject
{
  resolve(RdserviceUnsupportedResponse());
}

- (void)getFaceCapture:(NSString *)deviceName
             pidOption:(NSString *)pidOption
               resolve:(RCTPromiseResolveBlock)resolve
                reject:(RCTPromiseRejectBlock)reject
{
  resolve(RdserviceUnsupportedResponse());
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativeRdserviceSpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"Rdservice";
}

@end
