package com.gpustatix.utils;

public class GPUSettings {
    private String gpuVendor;
    private int coreClock;
    private int memoryClock;
    private int powerLimit;
    private int tempLimit;
    private int fanSpeed;

    // Конструктор по умолчанию
    public GPUSettings() {
        this.gpuVendor = "Unknown";
        this.coreClock = 1000;
        this.memoryClock = 5000;
        this.powerLimit = 100;
        this.tempLimit = 80;
        this.fanSpeed = 50;
    }

    // Геттеры и сеттеры
    public String getGpuVendor() {
        return gpuVendor;
    }

    public void setGpuVendor(String gpuVendor) {
        this.gpuVendor = gpuVendor;
    }

    public int getCoreClock() {
        return coreClock;
    }

    public void setCoreClock(int coreClock) {
        this.coreClock = coreClock;
    }

    public int getMemoryClock() {
        return memoryClock;
    }

    public void setMemoryClock(int memoryClock) {
        this.memoryClock = memoryClock;
    }

    public int getPowerLimit() {
        return powerLimit;
    }

    public void setPowerLimit(int powerLimit) {
        this.powerLimit = powerLimit;
    }

    public int getTempLimit() {
        return tempLimit;
    }

    public void setTempLimit(int tempLimit) {
        this.tempLimit = tempLimit;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(int fanSpeed) {
        this.fanSpeed = fanSpeed;
    }
}