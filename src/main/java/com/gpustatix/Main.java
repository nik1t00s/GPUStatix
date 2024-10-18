package com.gpustatix;

import java.util.List;
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

        List<GraphicsCard> graphcisCards = hal.getGraphicsCards();

        int graphicsCardNumber = 1;
        for (GraphicsCard graphicsCard : graphcisCards) {
            System.out.println(
                "Graphics Card " +
                graphicsCardNumber +
                ":\n" +
                format(graphicsCard)
            );
            graphicsCardNumber++;
        }
    }

    private static String format(GraphicsCard graphicsCard) {
        double memoryAsGB = graphicsCard.getVRam() / 1073741824;
        String VideoCard = "";

        VideoCard += " Graphics Card Name:" + graphicsCard.getName() + "\n";
        VideoCard += " Graphics Card VRAM: " + memoryAsGB + " GB\n";
        return VideoCard;
    }
}
