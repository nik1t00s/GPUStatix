package com.gpustatix.utils;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GPUSettings {
    private String gpuVendor = "Unknown";
    private int coreClock = 0;
    private int memoryClock = 0;
    private int powerLimit = 0;
    private int tempLimit = 100;
    private int fanSpeed = 0;
    private int previousFanSpeed = 0;
    private boolean fanControlEnabled = false;
    private long lastFanSpeedChangeTime = 0;

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
                // Initialize fan speed based on current temperature
                initializeFanSpeed();
            } else {
                System.err.println("Failed to get NVML device handle. Error code: " + result);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize NVML: " + e.getMessage());
        }
    }
    
    /**
     * Initialize fan speed based on current temperature
     */
    private void initializeFanSpeed() {
        try {
            int currentTemp = getGpuTemperature();
            int initialFanSpeed;
            
            // Set initial fan speed based on temperature
            if (currentTemp <= 40) {
                initialFanSpeed = 30; // Cool - low fan speed
            } else if (currentTemp <= 60) {
                initialFanSpeed = 45; // Warm - moderate fan speed
            } else if (currentTemp <= 75) {
                initialFanSpeed = 65; // Hot - higher fan speed
            } else {
                initialFanSpeed = 80; // Very hot - high fan speed
            }
            
            // Gradually apply the initial fan speed
            setFanSpeedGradually(initialFanSpeed, true);
            
            System.out.println("Initialized fan speed to " + initialFanSpeed + "% based on temperature of " + currentTemp + "°C");
        } catch (Exception e) {
            System.err.println("Error initializing fan speed: " + e.getMessage());
            // Default fallback
            setFanSpeedGradually(40, true);
        }
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
                System.err.println("Error output: " + errorOutput);
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
            // Restore fan control to auto mode if we've modified it
            if (fanControlEnabled) {
                try {
                    // Get current temperature to set appropriate fan speed
                    int currentTemp = getGpuTemperature();
                    int safeFanSpeed = Math.max(40, Math.min(85, currentTemp - 10));
                    
                    // Set a safe fan speed before returning to auto
                    applyFanSpeed(safeFanSpeed);
                    Thread.sleep(500); // Brief pause
                    
                    // Return to auto fan control
                    String autoFanCommand = "nvidia-settings --assign [gpu:0]/GPUFanControlState=0";
                    executeCommand(autoFanCommand);
                    System.out.println("Restored automatic fan control");
                } catch (Exception e) {
                    System.err.println("Error restoring fan control: " + e.getMessage());
                }
            }
            
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

    /**
     * Sets the fan speed with validation
     * @param value Target fan speed percentage (0-100)
     */
    public void setFanSpeed(int value) {
        setFanSpeedGradually(value, false);
    }
    
    /**
     * Sets the fan speed with gradual transition
     * @param targetValue Target fan speed percentage (0-100)
     * @param isInitialSetting Whether this is the initial setting at startup
     */
    public void setFanSpeedGradually(int targetValue, boolean isInitialSetting) {
        // Validate input
        int validatedValue = Math.max(0, Math.min(100, targetValue));
        
        // Check if change is needed
        if (!isInitialSetting && Math.abs(validatedValue - fanSpeed) <= 3) {
            // Skip small changes to reduce system calls
            return;
        }
        
        // Rate limiting to prevent too frequent changes
        long currentTime = System.currentTimeMillis();
        if (!isInitialSetting && currentTime - lastFanSpeedChangeTime < 500) {
            // Less than 500ms since last change, skip this update
            return;
        }
        lastFanSpeedChangeTime = currentTime;
        
        // Enable fan control if needed
        if (!fanControlEnabled) {
            enableFanControl();
        }
        
        // If control couldn't be enabled, return
        if (!fanControlEnabled) {
            return;
        }
        
        // Get current fan speed if unknown
        if (fanSpeed == 0 && !isInitialSetting) {
            getFanSpeed();
        }
        
        // Calculate step size based on the difference
        int stepSize = 5; // Default step size
        if (Math.abs(validatedValue - fanSpeed) > 30) {
            stepSize = 10; // Larger steps for big changes
        } else if (Math.abs(validatedValue - fanSpeed) < 10) {
            stepSize = 3; // Smaller steps for small changes
        }
        
        // If this is the initial setting or the temperature is high, use more aggressive steps
        if (isInitialSetting || getGpuTemperature() > 80) {
            stepSize = Math.max(stepSize, 10);
        }
        
        // Apply fan speed change with multiple steps for smoother transition
        int currentFanSpeed = fanSpeed;
        int direction = (validatedValue > currentFanSpeed) ? 1 : -1;
        
        // For small changes, just set directly
        if (Math.abs(validatedValue - currentFanSpeed) <= stepSize || isInitialSetting) {
            applyFanSpeed(validatedValue);
            return;
        }
        
        // For larger changes, step through gradually
        while (Math.abs(validatedValue - currentFanSpeed) > stepSize) {
            currentFanSpeed += direction * stepSize;
            applyFanSpeed(currentFanSpeed);
            
            // Brief pause between steps for smoother transition
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Final setting to exact value
        if (currentFanSpeed != validatedValue) {
            applyFanSpeed(validatedValue);
        }
    }
    
    /**
     * Enables fan control
     * @return true if successful, false otherwise
     */
    private boolean enableFanControl() {
        String enableFanControlCommand = "nvidia-settings --assign [gpu:0]/GPUFanControlState=1";
        String enableFanControlResult = executeCommand(enableFanControlCommand);

        if (enableFanControlResult.contains("assigned value")) {
            System.out.println("Fan control enabled successfully.");
            fanControlEnabled = true;
            System.out.println("Number of fans detected: " + getNumberOfFans());
            System.out.println("DISPLAY: " + System.getenv("DISPLAY"));
            return true;
        } else {
            System.err.println("Failed to enable fan control via nvidia-settings. Output:");
            System.err.println(enableFanControlResult);
            fanControlEnabled = false;
            return false;
        }
    }
    
    /**
     * Applies fan speed setting directly to all fans
     * @param value Fan speed percentage (0-100)
     */
    private void applyFanSpeed(int value) {
        // Apply to all fans
        boolean success = true;
        for (int fan = 0; fan < getNumberOfFans(); fan++) {
            String command = "nvidia-settings -a '[fan:" + fan + "]/GPUTargetFanSpeed=" + value + "'";
            String result = executeCommand(command);

            if (result.contains("assigned value")) {
                System.out.println("Fan " + fan + " speed set to " + value + "%.");
            } else {
                System.err.println("Failed to set fan speed for fan " + fan + " via nvidia-settings. Output:");
                System.err.println(command);
                System.err.println(result.isEmpty() ? "<no output>" : result);
                success = false;
            }
        }
        
        if (success) {
            previousFanSpeed = fanSpeed;
            fanSpeed = value;
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

        // Get current GPU temperature
        int currentTemp = getGpuTemperature();
        if (currentTemp >= tempLimit) {
            System.out.println("Temperature has reached " + currentTemp + "°C. Taking corrective actions.");

            // Calculate appropriate fan speed based on temperature
            int tempExcess = currentTemp - tempLimit;
            int newFanSpeed;
            
            if (tempExcess <= 5) {
                // Slightly over limit: moderate increase
                newFanSpeed = fanSpeed + 10;
            } else if (tempExcess <= 10) {
                // Moderately over limit: larger increase
                newFanSpeed = fanSpeed + 20;
            } else {
                // Significantly over limit: aggressive increase
                newFanSpeed = Math.max(fanSpeed + 30, 90); // At least 90%
            }
            
            // Ensure fan speed stays within limits
            newFanSpeed = Math.min(100, newFanSpeed);
            
            // Apply fan speed change gradually
            setFanSpeedGradually(newFanSpeed, false);

            // Reduce clocks if temperature is still too high (over threshold + 3°C)
            if (currentTemp >= tempLimit + 3) {
                // Calculate clock reductions based on how far over temp limit
                int clockReduction = 30 + (tempExcess * 5); // Base 30MHz + 5MHz per degree over
                clockReduction = Math.min(clockReduction, 100); // Cap at 100MHz reduction
                
                int newCoreClock = coreClock - clockReduction;
                int newMemoryClock = memoryClock - clockReduction;
                
                if (newCoreClock < 0) {
                    newCoreClock = 0; // Minimum core clock
                }
                if (newMemoryClock < 0) {
                    newMemoryClock = 0; // Minimum memory clock
                }

                setCoreClock(newCoreClock);
                setMemoryClock(newMemoryClock);

                System.out.println("Core clock reduced to " + newCoreClock + " MHz.");
                System.out.println("Memory clock reduced to " + newMemoryClock + " MHz.");
            }
        } else if (currentTemp < tempLimit - 10 && fanSpeed > 50) {
            // Temperature is well below limit and fans are running fast - gradually reduce
            System.out.println("Temperature is well below limit (" + currentTemp + "°C). Reducing fan speed.");
            int newFanSpeed = fanSpeed - 5;
            setFanSpeedGradually(newFanSpeed, false);
        } else {
            System.out.println("Temperature is within safe limits (" + currentTemp + "°C). No action required.");
        }
    }


}

interface NVML extends Library {



    NVML INSTANCE = Native.load("libnvidia-ml.so", NVML.class);

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