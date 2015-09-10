/*
 * The MIT License
 *
 * Copyright 2015 Pmovil LTDA.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.pmovil.codenameone.nativeshare;

import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.services.ImageDownloadService;
import com.codename1.system.NativeLookup;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import org.bouncycastle.crypto.digests.MD5Digest;

/**
 * Allows native sharing text and image directly to Facebook or Twitter
 * 
 * Curretly supported in Android and iOS
 *
 * @author Fabricio
 */
public class Share {
    public static final int FACEBOOK = 1;
    public static final int TWITTER = 2;
    private static NativeShare peer;
    
    private Share() {
    }
    
    public static Share getInstance() throws RuntimeException {
        if (peer == null) {
            peer = (NativeShare)NativeLookup.create(NativeShare.class);
            if ( peer == null ) {
                throw new RuntimeException("Native share is not implemented yet in this platform.");
            }
        }
        if ( !peer.isSupported() ){
            throw new RuntimeException("Native share is not supported in this platform.");
        }
        Share dialog = new Share();
        return dialog;
    }
    
    public void show(final String text, String image, final String mimeType, final int services) {
        show(text, image, mimeType, services, null);
    }
    
    public void show(final String text, String image, final String mimeType, final int services, final ActionListener callback) {
        if (image != null && image.toLowerCase().startsWith("http")) {
            try {
                final String basename = image.substring(image.lastIndexOf("/"));
                String extension;
                if (basename.indexOf('.') >= 0) {
                    extension = basename.substring(basename.lastIndexOf("."));
                } else if (mimeType != null) {
                    extension = "." + mimeType.substring(mimeType.lastIndexOf("/") + 1);
                } else {
                    extension = ".jpg";
                }
                final String file = FileSystemStorage.getInstance().getAppHomePath() + "share_" + md5(image) + extension;
                Log.p("Fetching " + image + " to " + file);
                ImageDownloadService.createImageToFileSystem(image, new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        Log.p(file + " fetched");
                        peer.show(text, file, mimeType != null ? mimeType : "image/jpg" , services);
                        if (callback != null) {
                            callback.actionPerformed(new ActionEvent(file));
                        }
                        // delete after share ?
                    }
                }, file);
            } catch (IndexOutOfBoundsException ex) {
                if (callback != null) {
                    callback.actionPerformed(new ActionEvent("failed"));
                }
            }
        } else {
            peer.show(text, image, mimeType, services);
            if (callback != null) {
                callback.actionPerformed(new ActionEvent(image));
            }
        }
    }
    
    public boolean isServiceSupported(int service) {
        return peer.isServiceSupported(service);
    }
    
    private static String md5(String text) {
        MD5Digest digest = new MD5Digest();
        byte[] data = text.getBytes();
        digest.update(data, 0, data.length);
        byte[] md5 = new byte[digest.getDigestSize()];
        digest.doFinal(md5, 0);
        return bytesToHex(md5);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}