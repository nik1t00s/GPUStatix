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
        this.cpu = new Processor();
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
        StringBuilder frequencies = new StringBuilder();
        int cpuCount = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cpuCount; i++) {
            File file = new File("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine();
                    if (line != null) {
                        double frequency = Double.parseDouble(line) / 1000.0; // Конвертация в MHz
                        frequencies.append("CPU").append(i).append(": ").append(frequency).append(" MHz\n");
                    }
                }
            } else {
                frequencies.append("CPU").append(i).append(": N/A\n");
            }
        }
        return frequencies.toString();
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