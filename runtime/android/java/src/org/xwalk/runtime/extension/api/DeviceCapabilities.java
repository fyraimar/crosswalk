// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api;

import org.xwalk.runtime.extension.XWalkExtension;
import org.xwalk.runtime.extension.XWalkExtensionContext;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceCapabilities extends XWalkExtension {
    public static final String NAME = "navigator.system";
    public static final String JS_API_PATH = "js_api/device_capabilities.js";
    private static final String TAG = "DeviceCapabilities";

    private DeviceCapabilitiesCPU mCPU = new DeviceCapabilitiesCPU();
    private DeviceCapabilitiesDisplay mDisplay;
    private DeviceCapabilitiesMemory mMemory = new DeviceCapabilitiesMemory();
    private DeviceCapabilitiesStorage mStorage = new DeviceCapabilitiesStorage(this);

    public DeviceCapabilities(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);
        mStorage.setExtensionContext(context);
        mDisplay = new DeviceCapabilitiesDisplay(this, context);
    }

    private void handleMessage(String message) {
        try {
            JSONObject jsonInput = new JSONObject(message);
            String cmd = jsonInput.getString("cmd");
            if (cmd.equals("addEventListener")) {
                String eventName = jsonInput.getString("eventName");
                handleAddEventListener(eventName);
            } else {
                String replyID = jsonInput.getString("_reply_id");
                handleGetDeviceInfo(replyID, cmd);
            }
        } catch (JSONException e) {
            this.postMessage("error");
        }
    }

    private void handleGetDeviceInfo(String replyID, String cmd) {
        try {
            JSONObject jsonOutput = new JSONObject();
            if (cmd.equals("getCPUInfo"))
                jsonOutput = mCPU.getInfo();
            else if (cmd.equals("getDisplayInfo"))
                jsonOutput.put("displays", mDisplay.getInfo());
            else if (cmd.equals("getMemoryInfo"))
                jsonOutput = mMemory.getInfo();
            else if (cmd.equals("getStorageInfo"))
                jsonOutput.put("storages", mStorage.getInfo());
            jsonOutput.put("_reply_id", replyID);
            this.postMessage(jsonOutput.toString());
        } catch (JSONException e) {
            this.postMessage("error");
        }
    }

    private void handleAddEventListener(String eventName) {
        if (eventName.equals("onattach") || eventName.equals("ondetach"))
            mStorage.registerListener();
        //if (eventName.equals("onconnect"))
    }

    @Override
    public void onMessage(String message) {
        if (!message.isEmpty())
            handleMessage(message);
    }

    @Override
    public void onDestroy() {
        mStorage.unregisterListener();
        mDisplay.unregisterListener();
    }
}
