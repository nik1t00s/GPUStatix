package com.gpustatix.utils;

public class GPUSettings {
    private final String gpuVendor;
    private int coreClock;
    private int memoryClock;
    private int powerLimit;
    private int tempLimit;
    private int fanSpeed;

    public GPUSettings(String gpuVendor, int coreClock, int memoryClock, int powerLimit, int tempLimit, int fanSpeed) {
        this.gpuVendor = gpuVendor;
        this.coreClock = coreClock;
        this.memoryClock = memoryClock;
        this.powerLimit = powerLimit;
        this.tempLimit = tempLimit;
        this.fanSpeed = fanSpeed;
    }

    public String getGpuVendor() {
        return gpuVendor;
    }

    public int getCoreClock() {
        return coreClock;
    }

    public int getMemoryClock() {
        return memoryClock;
    }

    public int getPowerLimit() {
        return powerLimit;
    }

    public int getTempLimit() {
        return tempLimit;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public void updateSetting(String setting, int value) {
        switch (setting) {
            case "Core Clock" -> coreClock = value;
            case "Memory Clock" -> memoryClock = value;
            case "Power Limit" -> powerLimit = value;
            case "Temp Limit" -> tempLimit = value;
            case "Fan Speed" -> fanSpeed = value;
        }
    }
}