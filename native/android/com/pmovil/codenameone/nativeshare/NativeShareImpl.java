package com.pmovil.codenameone.nativeshare;

import java.util.ArrayList;
import java.util.List;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import com.codename1.impl.android.AndroidNativeUtil;
import com.codename1.impl.android.CodenameOneActivity;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeShareImpl {

    private static final int FACEBOOK = 1;
    private static final int TWITTER = 2;

    public void show(String text, String image, String mimeType, int services) {
        final CodenameOneActivity activity = (CodenameOneActivity) AndroidNativeUtil.getActivity();
        List<String> packageNames = new ArrayList<String>();
        if ((services & FACEBOOK) != 0) {
            packageNames.add("com.facebook.katana");
        } // in android we allow both facebook and twitter
        if ((services & TWITTER) != 0) {
            packageNames.add("com.twitter.android");
        }
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        if (image == null) {
            shareIntent.setType("text/plain");
        } else {
            shareIntent.setType(mimeType);
        }
        List<Intent> targetedShareIntents = new ArrayList<Intent>();
        List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(shareIntent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                if (image == null) {
                    targetedShare.setType("text/plain");
                    if (packageNames.contains(info.activityInfo.packageName.toLowerCase())) {
                        targetedShare.putExtra(android.content.Intent.EXTRA_TEXT, text);
                        targetedShare.setPackage(info.activityInfo.packageName.toLowerCase());
                        targetedShare.setComponent(new ComponentName(info.activityInfo.packageName.toLowerCase(), info.activityInfo.name));
                        if (!targetedShareIntents.contains(targetedShare)) {
                            targetedShareIntents.add(targetedShare);
                        }
                    }
                } else {
                    targetedShare.setType(mimeType);
                    if (packageNames.contains(info.activityInfo.packageName.toLowerCase())) {
                        targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(fixAttachmentPath(image)));
                        targetedShare.putExtra(Intent.EXTRA_TEXT, text);
                        targetedShare.setPackage(info.activityInfo.packageName.toLowerCase());
                        targetedShare.setComponent(new ComponentName(info.activityInfo.packageName.toLowerCase(), info.activityInfo.name));
                        if (!targetedShareIntents.contains(targetedShare)) {
                            targetedShareIntents.add(targetedShare);
                        }
                    }
                }
            }
            if (!targetedShareIntents.isEmpty()) {
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Share with...");
                if (!targetedShareIntents.isEmpty()) {
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                }
                activity.startActivity(chooserIntent);
            }
        }
    }

    public boolean isServiceSupported(int service) {
        final CodenameOneActivity activity = (CodenameOneActivity) AndroidNativeUtil.getActivity();
        List<String> packageNames = new ArrayList<String>();
        if ((service & FACEBOOK) != 0) {
            packageNames.add("com.facebook.katana");
        } else if ((service & TWITTER) != 0) {
            packageNames.add("com.twitter.android");
        }
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(shareIntent, 0);
        for (ResolveInfo info : resInfo) {
            if (packageNames.contains(info.activityInfo.packageName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSupported() {
        return true;
    }

    private String fixAttachmentPath(String attachment) {
        if (!(attachment.indexOf(FileSystemStorage.getInstance().getAppHomePath()) < 0)) {
            FileSystemStorage fs = FileSystemStorage.getInstance();
            final char sep = fs.getFileSystemSeparator();
            String fileName = attachment.substring(attachment.lastIndexOf(sep) + 1);
            String[] roots = FileSystemStorage.getInstance().getRoots();
            // iOS doesn't have an SD card
            String root = roots[0];
            for (String root1 : roots) {
                //media_rw is a protected system lib
                if (FileSystemStorage.getInstance().getRootType(root1) == FileSystemStorage.ROOT_TYPE_SDCARD && root1.indexOf("media_rw") < 0) {
                    root = root1;
                    break;
                }
            }
            //might happen if only the media_rw is of type ROOT_TYPE_SDCARD
            if(!(root.indexOf("media_rw") < 0)){
                //try again without checking the root type
                for (String root1 : roots) {
                    //media_rw is a protected system lib
                    if (root1.indexOf("media_rw") < 0) {
                        root = root1;
                        break;
                    }
                }            
            }
            
            String fileUri = root + sep + "tmp" + sep + fileName;
            FileSystemStorage.getInstance().mkdir(root + sep + "tmp");
            try {
                InputStream is = FileSystemStorage.getInstance().openInputStream(attachment);
                OutputStream os = FileSystemStorage.getInstance().openOutputStream(fileUri);
                byte [] buf = new byte[1024];
                int len;
                while((len = is.read(buf)) > -1){
                    os.write(buf, 0, len);
                }
                is.close();
                os.close();
            } catch (IOException ex) {
                Log.p("Native share failed: " + ex.getMessage());
            }

            attachment = fileUri;
        }
        if (attachment.indexOf(":") < 0) {
            attachment = "file://" + attachment;
        }
        return attachment;
    }
}
