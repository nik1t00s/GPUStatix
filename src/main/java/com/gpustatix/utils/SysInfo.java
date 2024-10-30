package com.gpustatix.utils;

import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;

public class SysInfo {

    public static void displaySystemInfo() {
        Processor cpu = new Processor();
        HardwareAbstractionLayer hal = SysHardware.getHal();
        List<GraphicsCard> graphicCards = hal.getGraphicsCards();
        VideoCard gpu = new VideoCard(graphicCards.get(0));
        System.out.println("\n" + cpu);
        System.out.println("\n" + gpu.getName());
    }
}

abstract class SysHardware {

    static SystemInfo si = new SystemInfo();
    static HardwareAbstractionLayer hal = si.getHardware();
    static Sensors sensor = hal.getSensors();

    public static HardwareAbstractionLayer getHal() {
        return hal;
    }
}

class Processor extends SysHardware {

    CentralProcessor cpu;

    public Processor() {
        this.cpu = hal.getProcessor();
    }

    public String getName() {
        String result = "";
        String[] allStrings = cpu.toString().split("\n");
        result += allStrings[0];
        return result;
    }

    @Override
    public String toString() {
        return getName() +
                "   " + getLoad() +
                "   " + getFreq() +
                "   " + getTemp();
    }

    public String getTemp(){
        return Math.round(sensor.getCpuTemperature()) + " C";
    }

    public String getFreq() {
        return Math.round((float) cpu.getCurrentFreq()[cpu.getCurrentFreq().length - 1] / (10*10*10*10*10*10)) + "MHz";
    }

    public List<CentralProcessor.LogicalProcessor> getCores() {
        return cpu.getLogicalProcessors();
    }

    public int countCores() {
        return cpu.getLogicalProcessorCount();
    }

    public List<CentralProcessor.ProcessorCache> getCache() {
        return cpu.getProcessorCaches();
    }

    public String getLoad() {
        return Math.round(cpu.getProcessorCpuLoad(1000)[0]*100) + " %";
    }
}

class VideoCard implements GraphicsCard {

    private final GraphicsCard videoCard;

    public VideoCard(GraphicsCard videoCard) {
        this.videoCard = videoCard;
    }

    @Override
    public String getDeviceId() {
        return videoCard.getDeviceId();
    }

    @Override
    public String getName() {
        return videoCard.getName();
    }

    @Override
    public String getVendor() {
        return videoCard.getVendor();
    }

    @Override
    public long getVRam() {
        return videoCard.getVRam();
    }

    @Override
    public String getVersionInfo() {
        return videoCard.getVersionInfo();
    }
}
