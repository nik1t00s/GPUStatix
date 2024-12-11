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
    private int tempLimit = 100;
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
            int result = NVML.INSTANCE.nvmlDeviceGetHandleByIndex(0, deviceRef);
            if (result == NVML.NVML_SUCCESS) {
                device = deviceRef.getValue();
            } else {
                System.err.println("Failed to get NVML device handle. Error code: " + result);
            }
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
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Command failed with exit code " + exitCode);
                System.err.println("Error output: " + errorOutput.toString());
                return errorOutput.toString().trim(); // Возвращаем вывод ошибки
            }

            return output.toString().trim();

        } catch (IOException | InterruptedException e) {
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

    public void setCoreClock(int value) {
        String command = "sudo nvidia-settings -a '[gpu:0]/GPUGraphicsClockOffset[3]=" + value + "'";
        String result = executeCommand(command);

        if (result.contains("Attribute 'GPUGraphicsClockOffset'")) {
            System.out.println("Core clock set to " + value + " MHz.");
            coreClock = value;
        } else {
            System.err.println("Failed to set core clock via nvidia-settings. Output:");
            System.err.println(command);
            System.err.println(result.isEmpty() ? "<no output>" : result);
        }
    }

    public void setMemoryClock(int value) {
        String command = "sudo nvidia-settings -a '[gpu:0]/GPUMemoryTransferRateOffset[3]=" + value + "'";
        String result = executeCommand(command);

        if (result.contains("Attribute 'GPUMemoryTransferRateOffset'")) {
            System.out.println("Memory clock set to " + value + " MHz.");
            memoryClock = value;
        } else {
            System.err.println("Failed to set memory clock via nvidia-settings. Output:");
            System.err.println(command);
            System.err.println(result.isEmpty() ? "<no output>" : result);
        }
    }

    public void setPowerLimitNVML(int value) {
        try {
            int result = NVML.INSTANCE.nvmlDeviceSetPowerManagementLimit(device, value * 1000);
            if (result != NVML.NVML_SUCCESS) {
                System.err.println("Failed to set power limit via NVML. Error code: " + result);
            } else {
                powerLimit = value;
            }
        } catch (Exception e) {
            System.err.println("Failed to set power limit via NVML: " + e.getMessage());
        }
    }

    public void setFanSpeed(int value) {
        // Включаем ручное управление вентиляторами
        String enableFanControlCommand = "nvidia-settings --assign [gpu:0]/GPUFanControlState=1";
        String enableFanControlResult = executeCommand(enableFanControlCommand);

        if (enableFanControlResult.contains("assigned value")) {
            System.out.println("Fan control enabled successfully.");
        } else {
            System.err.println("Failed to enable fan control via nvidia-settings. Output:");
            System.err.println(enableFanControlResult);
            return;
        }

        System.out.println("Number of fans detected: " + getNumberOfFans());

        System.out.println("DISPLAY: " + System.getenv("DISPLAY"));

        // Устанавливаем скорость для всех вентиляторов
        for (int fan = 0; fan < getNumberOfFans(); fan++) {
            String command = "nvidia-settings -a '[fan:" + fan + "]/GPUTargetFanSpeed=" + value + "'";
            String result = executeCommand(command);

            if (result.contains("assigned value")) {
                System.out.println("Fan " + fan + " speed set to " + value + "%.");
            } else {
                System.err.println("Failed to set fan speed for fan " + fan + " via nvidia-settings. Output:");
                System.err.println(command);
                System.err.println(result.isEmpty() ? "<no output>" : result);
            }
        }
    }


    public int getNumberOfFans() {
        String command = "nvidia-settings -q fans";
        String result = executeCommand(command);

        if (result.isEmpty()) {
            System.err.println("Failed to query fans via nvidia-settings");
            return 0;
        }

        // Поиск количества вентиляторов в выводе
        int fanCount = 0;
        for (String line : result.split("\n")) {
            if (line.contains("[fan:")) {
                fanCount++;
            }
        }

        return fanCount;
    }

    public void setTempLimit(int newTempLimit) {
        tempLimit = newTempLimit;
        System.out.println("Setting temperature limit to " + tempLimit + "°C.");

        // Проверяем текущую температуру GPU
        int currentTemp = getGpuTemperature();
        if (currentTemp >= tempLimit) {
            System.out.println("Temperature has reached " + currentTemp + "°C. Taking corrective actions.");

            // Увеличиваем скорость вентиляторов
            int newFanSpeed = fanSpeed + 20; // Увеличиваем скорость вентиляторов на 20% (можно настроить)
            if (newFanSpeed > 100) {
                newFanSpeed = 100; // Ограничиваем на 100% (максимальная скорость)
            }
            setFanSpeed(newFanSpeed);

            // Уменьшаем частоты (core и memory clocks)
            int newCoreClock = coreClock - 50; // Уменьшаем частоту ядра на 50 МГц (можно настроить)
            int newMemoryClock = memoryClock - 50; // Уменьшаем частоту памяти на 50 МГц (можно настроить)

            if (newCoreClock < 0) {
                newCoreClock = 0; // Минимальная частота ядра
            }
            if (newMemoryClock < 0) {
                newMemoryClock = 0; // Минимальная частота памяти
            }

            setCoreClock(newCoreClock);
            setMemoryClock(newMemoryClock);

            System.out.println("Core clock reduced to " + newCoreClock + " MHz.");
            System.out.println("Memory clock reduced to " + newMemoryClock + " MHz.");
        } else {
            System.out.println("Temperature is within safe limits (" + currentTemp + "°C). No action required.");
        }
    }

    public void updateSetting(String setting, int value) {
        switch (setting) {
            case "Core Clock" -> coreClock = value;
            case "Memory Clock" -> memoryClock = value;
            case "Power Limit" -> powerLimit = value;
            case "Temp Limit" -> tempLimit = value;
            case "Fan Speed" -> fanSpeed = value;
            case "GPU Temperature" -> gpuTemperature = value;
            case "GPU Memory Usage" -> gpuMemoryUsage = value;
            case "GPU Utilization" -> gpuUtilization = value + "%";
            default -> System.err.println("Unknown setting: " + setting);
        }
    }
}

interface NVML extends Library {
    NVML INSTANCE = Native.load("libnvidia-ml.so.560.35.03", NVML.class);

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

    int nvmlDeviceSetApplicationsClocks(Pointer device, int clockType, int frequency);

    int nvmlDeviceSetPowerManagementLimit(Pointer device, int limit);
}