package com.gpustatix.utils;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GPUSettings {
    private static GPUSettings instance;
    private String gpuVendor = "Unknown";
    private int coreClock = 0;
    private int memoryClock = 0;
    private int powerLimit = 0;
    private int tempLimit = 0;
    private int fanSpeed = 0;

    private int gpuTemperature = 0;
    private String gpuName = "Unknown";
    private int gpuMemoryUsage = 0;
    private String gpuUtilization = "Unknown";
    private Pointer device;

    public GPUSettings() {
        try {
            NVML.INSTANCE.nvmlInit();
            PointerByReference deviceRef = new PointerByReference();
            NVML.INSTANCE.nvmlDeviceGetHandleByIndex(0, deviceRef);
            device = deviceRef.getValue();
        } catch (Exception e) {
            System.err.println("Failed to initialize NVML: " + e.getMessage());
        }
    }

    public static GPUSettings getInstance() {
        if (instance == null) {
            instance = new GPUSettings();
        }
        return instance;
    }

    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ""; // Возвращаем пустую строку, если команда завершилась с ошибкой
            }
            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            // Логирование ошибки
            System.err.println("Error executing command: " + command);
            e.printStackTrace();
            return ""; // Возвращаем пустую строку в случае ошибки
        }
    }

    private boolean isCommandAvailable(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "command -v " + command});
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public String getGpuVendor() {
        if (gpuVendor.equals("Unknown") && isCommandAvailable("nvidia-settings")) {
            String result = executeCommand("nvidia-settings -q gpus | grep 'GPU' | head -n 1");
            gpuVendor = result.contains("NVIDIA") ? "NVIDIA" : "Unknown";
        }
        return gpuVendor;
    }

    public String getGpuName() {
        if (gpuName.equals("Unknown") && isCommandAvailable("nvidia-smi")) {
            String result = executeCommand("nvidia-smi --query-gpu=name --format=csv,noheader,nounits");
            gpuName = result.isEmpty() ? "Unknown" : result.trim();
        }
        if (gpuName.equals("Unknown") && isCommandAvailable("glxinfo")){
            String result = executeCommand("glxinfo");
            if (!result.isEmpty()) {
                String[] lines = result.split("\n");
                for (String line : lines) {
                    // Ищем строку с названием GPU
                    if (line.toLowerCase().contains("device:") || line.toLowerCase().contains("renderer string:")) {
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            return parts[1].trim();
                        }
                    }
                }
            }
            gpuName = "Unknown";
        }
        return gpuName;
    }

    public int getTempLimit() {
        if (tempLimit == 0 && isCommandAvailable("nvidia-settings")) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUGraphicsTemp");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    tempLimit = Integer.parseInt(parts[1].trim().replace(" C", ""));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing temperature limit");
            }
        }
        return tempLimit;
    }

    public int getGpuTemperature() {
        if (gpuTemperature == 0 && isCommandAvailable("nvidia-smi")) {
            String result = executeCommand("nvidia-smi --query-gpu=temperature.gpu --format=csv,noheader,nounits");
            gpuTemperature = result.isEmpty() ? 0 : Integer.parseInt(result.trim());
        }
        return gpuTemperature;
    }

    public int getGpuMemoryUsage() {
        if (gpuMemoryUsage == 0 && isCommandAvailable("nvidia-smi")) {
            String result = executeCommand("nvidia-smi --query-gpu=memory.used --format=csv,noheader,nounits");
            gpuMemoryUsage = result.isEmpty() ? 0 : Integer.parseInt(result.trim());
        }
        return gpuMemoryUsage;
    }

    public String getGpuUtilization() {
        if (gpuUtilization.equals("Unknown") && isCommandAvailable("nvidia-smi")) {
            String result = executeCommand("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");
            gpuUtilization = result.isEmpty() ? "Unknown" : result.trim();
        }
        return gpuUtilization;
    }

    public int getCoreClock() {
        try {
            IntByReference clockRef = new IntByReference();
            NVML.INSTANCE.nvmlDeviceGetClock(device, NVML.NVML_CLOCK_GRAPHICS, NVML.NVML_CLOCK_ID_CURRENT, clockRef);
            coreClock = clockRef.getValue();
        } catch (Exception e) {
            System.err.println("Failed to get core clock: " + e.getMessage());
        }
        return coreClock;
    }

    public int getMemoryClock() {
        try {
            IntByReference clockRef = new IntByReference();
            NVML.INSTANCE.nvmlDeviceGetClock(device, NVML.NVML_CLOCK_MEM, NVML.NVML_CLOCK_ID_CURRENT, clockRef);
            memoryClock = clockRef.getValue();
        } catch (Exception e) {
            System.err.println("Failed to get memory clock: " + e.getMessage());
        }
        return memoryClock;
    }

    public int getPowerLimit() {
        try {
            IntByReference powerRef = new IntByReference();
            NVML.INSTANCE.nvmlDeviceGetPowerManagementLimit(device, powerRef);
            powerLimit = powerRef.getValue() / 1000; // Ватты
        } catch (Exception e) {
            System.err.println("Failed to get power limit: " + e.getMessage());
        }
        return powerLimit;
    }

    public int getFanSpeed() {
        try {
            IntByReference fanSpeedRef = new IntByReference();
            NVML.INSTANCE.nvmlDeviceGetFanSpeed(device, fanSpeedRef);
            fanSpeed = fanSpeedRef.getValue();
        } catch (Exception e) {
            System.err.println("Failed to get fan speed: " + e.getMessage());
        }
        return fanSpeed;
    }

    public void shutdown() {
        try {
            NVML.INSTANCE.nvmlShutdown();
        } catch (Exception e) {
            System.err.println("Failed to shutdown NVML: " + e.getMessage());
        }
    }

    public void updateSetting(String setting, int value) {
        switch (setting) {
            case "Core Clock":
                coreClock = value;
                break;
            case "Memory Clock":
                memoryClock = value;
                break;
            case "Power Limit":
                powerLimit = value;
                break;
            case "Temp Limit":
                tempLimit = value;
                break;
            case "Fan Speed":
                fanSpeed = value;
                break;
            default:
                System.err.println("Unknown setting: " + setting);
                break;
        }
    }
}

interface NVML extends Library {
    NVML INSTANCE = Native.load("nvidia-ml", NVML.class);

    int NVML_SUCCESS = 0;
    int NVML_TEMPERATURE_GPU = 0;
    int NVML_CLOCK_GRAPHICS = 0;
    int NVML_CLOCK_MEM = 1;
    int NVML_CLOCK_ID_CURRENT = 0;
    int NVML_DEVICE_NAME_BUFFER_SIZE = 64;

    int nvmlInit();

    int nvmlShutdown();

    int nvmlDeviceGetHandleByIndex(int index, PointerByReference device);

    int nvmlDeviceGetName(Pointer device, byte[] name, int length);

    int nvmlDeviceGetTemperature(Pointer device, int sensorType, IntByReference temp);

    int nvmlDeviceGetClock(Pointer device, int clockType, int clockId, IntByReference clock);

    int nvmlDeviceGetPowerManagementLimit(Pointer device, IntByReference power);

    int nvmlDeviceGetFanSpeed(Pointer device, IntByReference fanSpeed);
}