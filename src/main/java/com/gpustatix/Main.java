package com.gpustatix;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class Main {

    public static void main(String[] args) {
        SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        CentralProcessor cpu = hal.getProcessor();

        System.out.println("Operating System: " + os);
        System.out.println("CPU: " + cpu);
    }
}
