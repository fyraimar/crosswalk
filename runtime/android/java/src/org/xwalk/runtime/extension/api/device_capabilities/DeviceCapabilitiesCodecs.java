
// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.device_capabilities;

import android.util.Log;

import java.io.RandomAccessFile;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.runtime.extension.XWalkExtensionContext;

import org.chromium.base.JNINamespace;

@JNINamespace("xwalk::extensions")
public class DeviceCapabilitiesCodecs {
    private static final String TAG = "DeviceCapabilitiesCodecs";

    private DeviceCapabilities mDeviceCapabilities;

    int echo = 0;

    public DeviceCapabilitiesCodecs(DeviceCapabilities instance,
                                 XWalkExtensionContext context) {
        mDeviceCapabilities = instance;
    }

    public JSONObject getInfo() {
        JSONObject out = new JSONObject();
        echo = nativeGetString();
        try {
            out.put("echo", echo);
        } catch (JSONException e) {
            return mDeviceCapabilities.setErrorMessage(e.toString());
        }

        return out;
    }

    private native int nativeGetString();
}
