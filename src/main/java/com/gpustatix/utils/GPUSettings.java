package com.gpustatix.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

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

    public GPUSettings() {
        // Инициализация значений по умолчанию
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

    public int getCoreClock() {
        if (coreClock == 0 && isCommandAvailable("nvidia-settings")) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUCoreClockFreq");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    coreClock = Integer.parseInt(parts[1].trim().replace(" MHz", ""));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing core clock");
            }
        }
        return coreClock;
    }

    public int getMemoryClock() {
        if (memoryClock == 0 && isCommandAvailable("nvidia-settings")) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUMemoryTransferRate");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    memoryClock = Integer.parseInt(parts[1].trim().replace(" MHz", ""));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing memory clock");
            }
        }
        return memoryClock;
    }

    public int getPowerLimit() {
        if (powerLimit == 0 && isCommandAvailable("nvidia-settings")) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUCurrentPowerLimit");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    powerLimit = Integer.parseInt(parts[1].trim().replace(" W", ""));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing power limit");
            }
        }
        return powerLimit;
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

    public int getFanSpeed() {
        if (fanSpeed == 0 && isCommandAvailable("nvidia-settings")) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUFanSpeed");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    fanSpeed = Integer.parseInt(parts[1].trim().replace(" RPM", ""));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing fan speed");
            }
        }
        return fanSpeed;
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