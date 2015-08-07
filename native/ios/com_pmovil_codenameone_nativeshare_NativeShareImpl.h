#import <Foundation/Foundation.h>

@interface com_pmovil_codenameone_nativeshare_NativeShareImpl : NSObject {
}

-(BOOL)isServiceSupported:(int)service;
-(void)show:(NSString*)text param1:(NSString*)image param2:(NSString*)mimeType param3:(int)services;
-(BOOL)isSupported;
@end
