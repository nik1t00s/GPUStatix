package com.gpustatix.utils;

import java.util.List;

import oshi.SystemInfo;
import oshi.hardware.*;

public class SysInfo {

    public static void displaySystemInfo() {
        Processor cpu = new Processor();
        RAM ram = new RAM();
        HardwareAbstractionLayer hal = SysHardware.getHal();
        List<GraphicsCard> graphicCards = hal.getGraphicsCards();
        GraphicsCard gpu = graphicCards.get(0);
        String[] VendorGPU = (gpu.getName().split(" "));
        System.out.println("\n" + cpu);
        System.out.println("\n" + gpu.getName());
        System.out.println("\n" + ram);
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
        return cpu.getProcessorIdentifier().getName().split(" CPU ")[0];
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

class RAM extends SysHardware{
    GlobalMemory RAM;
    public RAM(){
        this.RAM = hal.getMemory();
    }

    @Override
    public String toString() {
        return "Total " + (RAM.getTotal() / 1000000) +
                " MB" + "   " +
                "Using " + ((RAM.getTotal() / 1000000) - RAM.getAvailable() / 1000000) + " MB";
    }
}

class GraphicsApp{
}
