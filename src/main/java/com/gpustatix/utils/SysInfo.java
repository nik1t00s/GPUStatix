package com.gpustatix.utils;

import java.util.List;

import oshi.SystemInfo;
import oshi.hardware.*;
import com.badlogic.gdx.utils.TimeUtils;

public class SysInfo {

    private static final FrameRate frameRate = new FrameRate();

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

        frameRate.update();
        System.out.println(frameRate.getFrameRate() + " FPS");
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

class FrameRate {
    private long lastTimeCounted;
    private float sinceChange;
    private float frameRate;
    private int framesCount;

    public FrameRate() {
        lastTimeCounted = TimeUtils.millis();
        sinceChange = 0;
        frameRate = 0;
        framesCount = 0;
    }

    public void update() {
        framesCount++;
        long delta = TimeUtils.timeSinceMillis(lastTimeCounted);
        lastTimeCounted = TimeUtils.millis();

        sinceChange += delta;
        if (sinceChange >= 1000) {
            sinceChange = 0;
            frameRate = framesCount; // количество кадров за секунду
            framesCount = 0; // сбрасываем счетчик кадров
        }
    }

    public float getFrameRate() {
        return frameRate;
    }
}