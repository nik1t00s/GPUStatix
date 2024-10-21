package com.gpustatix.utils;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

public class SysInfo {

    public static void displaySystemInfo() {
        SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();

        System.out.println("CPU: " + cpu);

        List<GraphicsCard> graphcisCards = hal.getGraphicsCards();

        int graphicsCardNumber = 1;
        for (GraphicsCard graphicsCard : graphcisCards) {
            System.out.println(
                "Graphics Card " +
                graphicsCardNumber +
                ":\n" +
                graphicsCard.getName()
            );
            graphicsCardNumber++;
        }
    }
}
