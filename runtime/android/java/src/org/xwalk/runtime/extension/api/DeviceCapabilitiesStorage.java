// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api;

import org.xwalk.runtime.extension.XWalkExtensionContext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.StatFs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DeviceCapabilitiesStorage {
    private DeviceCapabilities mDeviceCapabilities;
    private XWalkExtensionContext mExtensionContext;

    private long[] mInternalInfo = new long[2];
    private long[] mSDCardInfo = new long[2];
    private boolean mIsRegisterListener = false;

    private IntentFilter mIntentFilter = new IntentFilter();
    private final BroadcastReceiver mSDCardListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            JSONObject sdCardObject = new JSONObject();
            try {
                if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                    sdCardObject.put("cmd", "attachStorage");
                    sdCardObject.put("eventName", "onattach");
                    sdCardObject.put("content", "hello");
                } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                    sdCardObject.put("cmd", "detachStorage");
                    sdCardObject.put("eventName", "ondetach");
                    sdCardObject.put("content", "nice");
                }
                mDeviceCapabilities.postMessage(sdCardObject.toString());
            } catch (JSONException e) {
                mDeviceCapabilities.postMessage("listen error");
            }
        }
    };

    public DeviceCapabilitiesStorage(DeviceCapabilities instance) {
        mDeviceCapabilities = instance;
        registerIntentFilter();
    }

    public void setExtensionContext(XWalkExtensionContext context) {
        mExtensionContext = context;
    }

    public JSONArray getInfo() {
        JSONArray outputArray = new JSONArray();
        if (getStorageInfo()) {
            try {
                JSONObject sdCardObject = new JSONObject();
                sdCardObject.put("id", 1);
                sdCardObject.put("name", "SDCard");
                if (Environment.isExternalStorageRemovable())
                    sdCardObject.put("type", "removable");
                else
                    sdCardObject.put("type", "fixed");
                sdCardObject.put("capacity", mSDCardInfo[0]);

                JSONObject internalObject = new JSONObject();
                internalObject.put("id", 2);
                internalObject.put("name", "Internal");
                internalObject.put("type", "fixed");
                internalObject.put("capacity", mInternalInfo[0]);

                outputArray.put(sdCardObject);
                outputArray.put(internalObject);
            } catch (JSONException e) {
                return outputArray;
            }
        }
        return outputArray;
    }

    public void registerListener() {
        if (!mIsRegisterListener) {
            mExtensionContext.getActivity().registerReceiver(mSDCardListener,
                mIntentFilter);
            mIsRegisterListener = true;
        }
    }

    public void unregisterListener() {
        if (mIsRegisterListener)
            mExtensionContext.getActivity().unregisterReceiver(mSDCardListener);
        mIsRegisterListener = false;
    }

    private boolean getStorageInfo() {
        File internalDir = Environment.getDataDirectory();
        StatFs internalStatFs = new StatFs(internalDir.getPath());
        long bSizeInternal = internalStatFs.getBlockSize();

        mInternalInfo[0] = bSizeInternal * internalStatFs.getBlockCount() / 1024;
        mInternalInfo[1] =
            bSizeInternal * internalStatFs.getAvailableBlocks() / 1024;

        String externalStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
            File sdCardDir = Environment.getExternalStorageDirectory();
            StatFs sdCardStatfs = new StatFs(sdCardDir.getPath());
            long bSizeSDCard = sdCardStatfs.getBlockSize();

            mSDCardInfo[0] = bSizeSDCard * sdCardStatfs.getBlockCount() / 1024;
            mSDCardInfo[1] = bSizeSDCard * sdCardStatfs.getAvailableBlocks() / 1024;
        }

        return true;
    }

    private void registerIntentFilter() {
        mIntentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mIntentFilter.addDataScheme("file");
    }
}
