#import "com_pmovil_codenameone_nativeshare_NativeShareImpl.h"
#import "CodenameOne_GLViewController.h"
#import <Social/Social.h>
#import <Accounts/Accounts.h>

@implementation com_pmovil_codenameone_nativeshare_NativeShareImpl

int FACEBOOK = 1;
int TWITTER = 2;

-(void)show:(NSString*)text param1:(NSString*)image param2:(NSString*)mimeType param3:(int)services{
    SLComposeViewController *post;
    
    if ((FACEBOOK & services) != 0 && [SLComposeViewController isAvailableForServiceType:SLServiceTypeFacebook])
    {
        post = [SLComposeViewController
                   composeViewControllerForServiceType:SLServiceTypeFacebook];
    } else if ((TWITTER & services) != 0 && [SLComposeViewController isAvailableForServiceType:SLServiceTypeTwitter]) {
        post = [SLComposeViewController
                  composeViewControllerForServiceType:SLServiceTypeTwitter];
    } else {
        return;
    }
    
    [post setInitialText:text];
    if (image != nil) {
        if ([image hasPrefix:@"file:/"]) {
            image = [image substringFromIndex:6];
        }
        NSLog(@"Trying to share %@", image);
        [post addImage:[UIImage imageNamed:image]];
    }
    
    [[CodenameOne_GLViewController instance] presentViewController:post animated:YES completion:nil];
    
    [post setCompletionHandler:^(SLComposeViewControllerResult result) {
        
        
        switch (result) {
            case SLComposeViewControllerResultCancelled:
                NSLog(@"Post Canceled");
                break;
            case SLComposeViewControllerResultDone:
                NSLog(@"Post Sucessful");
                break;
                
            default:
                break;
        }
        
        [[CodenameOne_GLViewController instance] dismissViewControllerAnimated:YES completion:nil];
        
    }];
    
}

-(BOOL)isServiceSupported:(int)service{
    if ((FACEBOOK & service) != 0 ) {
        return [SLComposeViewController isAvailableForServiceType:SLServiceTypeFacebook];
    }
    if ((TWITTER & service) != 0 ) {
        return [SLComposeViewController isAvailableForServiceType:SLServiceTypeTwitter];
    }
    return NO;
}

-(BOOL)isSupported{
    return YES;
}

@end
