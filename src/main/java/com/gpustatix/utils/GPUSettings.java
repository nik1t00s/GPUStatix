package com.gpustatix.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GPUSettings {
    private static GPUSettings instance;
    private String gpuVendor;
    private int coreClock;
    private int memoryClock;
    private int powerLimit;
    private int tempLimit;
    private int fanSpeed;

    // Добавленные поля для данных nvidia-smi
    private int gpuTemperature;
    private String gpuName;
    private int gpuMemoryUsage;
    private String gpuUtilization;

    public GPUSettings() {
        // Инициализация значений через утилиту nvidia-settings
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
            process.waitFor();
            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getGpuVendor() {
        if (gpuVendor == null) {
            // Используем nvidia-settings для получения информации о видеокарте
            String result = executeCommand("nvidia-settings -q gpus | grep 'GPU' | head -n 1");
            gpuVendor = result.contains("NVIDIA") ? "NVIDIA" : "Unknown";
        }
        return gpuVendor;
    }

    public String getGpuName() {
        if (gpuName == null) {
            String result = executeCommand("nvidia-smi --query-gpu=name --format=csv,noheader,nounits");
            gpuName = result.trim();
        }
        return gpuName;
    }

    public int getCoreClock() {
        if (coreClock == 0) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUCoreClockFreq");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    coreClock = Integer.parseInt(parts[1].trim().replace(" MHz", ""));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return coreClock;
    }

    public int getMemoryClock() {
        if (memoryClock == 0) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUMemoryTransferRate");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    memoryClock = Integer.parseInt(parts[1].trim().replace(" MHz", ""));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return memoryClock;
    }

    public int getPowerLimit() {
        if (powerLimit == 0) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUCurrentPowerLimit");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    powerLimit = Integer.parseInt(parts[1].trim().replace(" W", ""));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return powerLimit;
    }

    public int getTempLimit() {
        if (tempLimit == 0) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUGraphicsTemp");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    tempLimit = Integer.parseInt(parts[1].trim().replace(" C", ""));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return tempLimit;
    }
    public int getFanSpeed() {
        if (fanSpeed == 0) {
            String result = executeCommand("nvidia-settings -q [gpu:0]/GPUFanSpeed");
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    fanSpeed = Integer.parseInt(parts[1].trim().replace(" RPM", ""));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return fanSpeed;
    }

    // Методы для извлечения данных nvidia-smi

    public int getGpuTemperature() {
        if (gpuTemperature == 0) {
            String result = executeCommand("nvidia-smi --query-gpu=temperature.gpu --format=csv,noheader,nounits");
            gpuTemperature = Integer.parseInt(result.trim());
        }
        return gpuTemperature;
    }

    public int getGpuMemoryUsage() {
        if (gpuMemoryUsage == 0) {
            String result = executeCommand("nvidia-smi --query-gpu=memory.used --format=csv,noheader,nounits");
            gpuMemoryUsage = Integer.parseInt(result.trim());
        }
        return gpuMemoryUsage;
    }

    public String getGpuUtilization() {
        if (gpuUtilization == null) {
            String result = executeCommand("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");
            gpuUtilization = result.trim();
        }
        return gpuUtilization;
    }

    // Метод для обновления настроек GPU
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