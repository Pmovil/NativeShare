/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pmovil.codenameone.nativeshare;

import com.codename1.system.NativeInterface;

/**
 *
 * @author fabricio
 */
public interface NativeShare extends NativeInterface {

    public void show(String text, String image, String mimeType, int services);
    public boolean isServiceSupported(int service);
}
