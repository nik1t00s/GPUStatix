package com.gpustatix.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        System.out.println(cpu);
        System.out.println("GPU" + "    " + "\n" +
                "MEM " + (gpu.getVRam()) + " GB"
        );
        System.out.println(ram);

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

    Processor cpu;

    public Processor() {
    }

    @Override
    public String toString() {
        try {
            return "CPU" +
                    "   " + getFreq() +
                    "   " + getTemp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFreq() throws IOException {
        double totalFreq = 0;
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("cpu MHz")) {
                    // Извлекаем значение частоты из строки
                    String[] parts = line.split(":\\s+");
                    if (parts.length > 1) {
                        totalFreq += Double.parseDouble(parts[1]);
                        count++;
                    }
                }
            }
        }

        // Рассчитываем среднюю частоту
        double avgFreq = (count > 0) ? totalFreq / count : 0;
        return avgFreq + " MHz";
    }

    public String getTemp(){
        if (sensor.getCpuTemperature() == 0){
            return "ERROR";
        }
        return Math.round(sensor.getCpuTemperature()) + " C";
    }
}

class RAM extends SysHardware{
    GlobalMemory RAM;
    public RAM(){
        this.RAM = hal.getMemory();
    }

    @Override
    public String toString() {
        return "RAM " + ((RAM.getTotal() / 1000000) - RAM.getAvailable() / 1000000) + " MB";
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