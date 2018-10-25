#import "CcppSdkFlutterPlugin.h"
#import <ccpp_sdk_flutter/ccpp_sdk_flutter-Swift.h>

@implementation CcppSdkFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCcppSdkFlutterPlugin registerWithRegistrar:registrar];
}
@end
