#import <React/RCTBridgeModule.h>
#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(SmartconfigSwjava, NSObject)

RCT_EXTERN_METHOD(stop)

RCT_EXTERN_METHOD(start: (NSString *)ssid bssid:(NSString *)bssid password:(NSString *)password timeScan:(NSInteger)timeScan
                errorCallback:(RCTResponseSenderBlock *)errorCallback )

@end
