// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api;

import org.xwalk.runtime.extension.XWalkExtensionContext;

import android.view.*;
import android.content.*;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceCapabilitiesDisplay {
    private String id;
    private String name;
    private boolean isPrimary;
    private boolean isInternal;
    private int dpiX;
    private int dpiY;
    private long width;
    private long height;
    private long availWidth;
    private long availHeight;
    private boolean isRegisteredListener;
    private XWalkExtensionContext context;
    private DeviceCapabilities mDeviceCapabilities;

    public DeviceCapabilitiesDisplay(DeviceCapabilities instance, XWalkExtensionContext mContext) {
        mDeviceCapabilities = instance;
        context = mContext;
        isRegisteredListener = false;
        registerListener();
    }

    public boolean GetDisplayInfo(Display display) {
        id = String.valueOf(display.getDisplayId());
        name = display.getName();
        if (id.equals(String.valueOf(display.DEFAULT_DISPLAY)))
            isPrimary = true;
        else
            isPrimary = false;
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        dpiX = (int)dm.xdpi;
        dpiY = (int)dm.ydpi;
        Point outSize = new Point();
        display.getRealSize(outSize);
        width = outSize.x;
        height = outSize.y;
        display.getSize(outSize);
        availWidth = outSize.x;
        availHeight = outSize.y;
        return true;
    }

    public JSONArray getInfo() {
        JSONArray outputArray = new JSONArray();

        DisplayManager dispMgr = (DisplayManager)(context.getContext().getSystemService(Context.DISPLAY_SERVICE));
        Display[] dispArr = dispMgr.getDisplays();

        for (Display disp : dispArr) {
            JSONObject displayObject = new JSONObject();
            if (GetDisplayInfo(disp)) {
                try {
                    displayObject.put("id", id);
                    displayObject.put("name", name);
                    displayObject.put("isPrimary", isPrimary);
                    displayObject.put("dpiX", dpiX);
                    displayObject.put("dpiY", dpiY);
                    displayObject.put("width", width);
                    displayObject.put("height", height);
                    displayObject.put("availWidth", availWidth);
                    displayObject.put("availHeight", availHeight);
                } catch (JSONException e) {
                    return outputArray;
                }

                outputArray.put(displayObject);
            }
        }

        return outputArray;
    }

    public void registerListener() {
        if(!isRegisteredListener) {
            DisplayManager dispMgr = (DisplayManager)(context.getContext().getSystemService(Context.DISPLAY_SERVICE));
            dispMgr.registerDisplayListener(mDisplayListener, null);

            isRegisteredListener = true;
        }
    }

    public void unregisterListener() {
        if(isRegisteredListener) {
            DisplayManager dispMgr = (DisplayManager)(context.getContext().getSystemService(Context.DISPLAY_SERVICE));
            dispMgr.unregisterDisplayListener(mDisplayListener);

            isRegisteredListener = false;
        }
    }

    private final DisplayListener mDisplayListener = new DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
            JSONObject displayObject = new JSONObject();
            try {
                displayObject.put("cmd", "connectDisplay");
                displayObject.put("eventName", "onconnect");
                displayObject.put("content", displayId);
            } catch (JSONException e){
                mDeviceCapabilities.postMessage("display listener error");
            }

            mDeviceCapabilities.postMessage(displayObject.toString());
        }

        @Override
        public void onDisplayChanged(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
            JSONObject displayObject = new JSONObject();
            try {
                displayObject.put("cmd", "disconnectDisplay");
                displayObject.put("eventName", "ondisconnect");
                displayObject.put("content", displayId);
            } catch (JSONException e){
                mDeviceCapabilities.postMessage("display listener error");
            }

            mDeviceCapabilities.postMessage(displayObject.toString());
        }
    };
}
