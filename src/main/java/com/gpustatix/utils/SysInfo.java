package com.gpustatix.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.badlogic.gdx.utils.TimeUtils;

public class SysInfo {

    private static final FrameRate frameRate = new FrameRate();

    public static void displaySystemInfo() {
        Processor cpu = new Processor();
        RAM ram = new RAM();
        System.out.println(cpu);
        System.out.println("GPU" + "    " + "\n" +
                "MEM " + " GB"
        );
        System.out.println(ram);

        frameRate.update();
        System.out.println(frameRate.getFrameRate() + " FPS");
    }
}

abstract class SysHardware {

    String line;

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
        return Math.round(avgFreq) + " MHz";
    }

    public String getTemp(){
        return " C";
    }
}

class RAM extends SysHardware{
    RAM ram;
    public String getUsedRAM() throws IOException{
        long totalMemory = 0;
        long freeMemory = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo")){
            while ((line = br.readLine()) != null) {
                if (line.startsWith("MemTotal:")){
                    String[] parts = line.split(":\\s+");
                    totalMemory += (long) Double.parseDouble(parts[1]);
                }
                else if (line.startsWith("MemAvailable:")){
                    String[] parts = line.split(":\\s+");
                    freeMemory += (long) Double.parseDouble(parts[1]);
                }
            }
        }
        return Math.round((totalMemory - freeMemory) / 1024) + " MB";
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