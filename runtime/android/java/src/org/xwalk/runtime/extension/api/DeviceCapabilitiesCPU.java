// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceCapabilitiesCPU {
    public static final int SYSTEM_INFO_STAT_FILE_SIZE = 1024;
    public static final String SYSTEM_INFO_CPU_DIR = "/sys/devices/system/cpu/";
    public static final String SYSTEM_INFO_CPUINFO_FILE = "/proc/cpuinfo";
    public static final String SYSTEM_INFO_STAT_FILE = "/proc/stat";

    private double cpuLoad = 0.0;
    private int coreNum = 0;
    private long oldCPUTotal = 0;
    private long oldCPUUsed = 0;
    private String cpuArch = "";

    private FileReader mFileReader;
    private FileInputStream mFileInputStream;
    private InputStreamReader mInputStreamReader;
    private BufferedReader mBufferedReader;

    public DeviceCapabilitiesCPU() {
        getCPULoad();
    }

    private boolean getCPUCoreNumber() {
        class CoreFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName()))
                    return true;
                return false;
            }
        }
        File cpuInfoDir = new File(SYSTEM_INFO_CPU_DIR);
        File[] coreFiles = cpuInfoDir.listFiles(new CoreFilter());
        coreNum = coreFiles.length;
        return true;
    }

    private boolean getCPUArch() {
        try {
            mFileReader = new FileReader(SYSTEM_INFO_CPUINFO_FILE);
            mBufferedReader = new BufferedReader(mFileReader);

            cpuArch = "";
            String readLine = "";
            while ((readLine = mBufferedReader.readLine()) != null) {
                String[] lineArray = readLine.split(":\\s+", 2);
                if (lineArray[0].contains("model name")) {
                    cpuArch = lineArray[1];
                    break;
                }
            }
            if (cpuArch.isEmpty())
                cpuArch = "Unknown";
        } catch (IOException e) {
            cpuArch = "Unknown";
            return false;
        }
        return true;
    }

    private boolean getCPULoad() {
        try {
            mFileInputStream = new FileInputStream(SYSTEM_INFO_STAT_FILE);
            mInputStreamReader = new InputStreamReader(mFileInputStream);
            mBufferedReader =
                new BufferedReader(mInputStreamReader, SYSTEM_INFO_STAT_FILE_SIZE);
            String[] cpuInfoArray = mBufferedReader.readLine().split(" ");
            mBufferedReader.close();

            long curCPUUsed = Long.parseLong(cpuInfoArray[2])
                              + Long.parseLong(cpuInfoArray[3])
                              + Long.parseLong(cpuInfoArray[4]);
            long curCPUTotal = curCPUUsed
                               + Long.parseLong(cpuInfoArray[5])
                               + Long.parseLong(cpuInfoArray[6])
                               + Long.parseLong(cpuInfoArray[7])
                               + Long.parseLong(cpuInfoArray[8]);
            cpuLoad = (double) (curCPUUsed - oldCPUUsed)
                               / (curCPUTotal- oldCPUTotal);
            oldCPUUsed = curCPUUsed;
            oldCPUTotal = curCPUTotal;
        } catch (IOException e) {
            cpuLoad = 0.0;
            return false;
        }
        return true;
    }

    public JSONObject getInfo() {
        JSONObject outputObject = new JSONObject();
        if (getCPUCoreNumber() && getCPUArch() && getCPULoad()) {
            try {
                outputObject.put("cpuNum", coreNum);
                outputObject.put("cpuArch", cpuArch);
                outputObject.put("cpuLoad", cpuLoad);
            } catch (JSONException e) {
                return outputObject;
            }
        }
        return outputObject;
    }
}
