package com.gpustatix.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.utils.TimeUtils;

public class SysInfo {

    private static final FrameRate frameRate = new FrameRate();

    public static void displaySystemInfo() throws IOException {
        // Processor cpu = new Processor();
        RAM ram = new RAM();
        InfoAboutPC info = new InfoAboutPC();
        // System.out.println(cpu);
        if (info.checkIntegrated()){
           System.out.println("INTEGRATED");
        }
        else{
            System.out.println("GPU" + "    " + "\n" +
                    "MEM " + " GB"
            );
        }
        System.out.println(ram);
        System.out.println(info);
        frameRate.update();
        System.out.println(frameRate.getFrameRate() + " FPS");
    }
}

class InfoAboutPC{
    String line;
    InfoAboutPC() throws IOException {
    }
    public String getResolution() throws IOException{
        StringBuilder resolution = new StringBuilder();
        try{
            Process process = new ProcessBuilder("neofetch", "--off").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null){
                if (line.contains("Resolution")){
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        resolution.append(parts[1].trim());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resolution.toString();
    }

    @Override
    public String toString() {
        try {
            return "Resolution" + "  " + getResolution() + "\n";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkIntegrated() throws IOException{
        String vendor = "";
        try{
            Process process = new ProcessBuilder("neofetch", "--off").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null){
                if (line.contains("GPU")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        vendor += parts[1].trim();
                    }
                    break;
                }
            }
            if (vendor.equals("Intel")){
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}

class Processor {

    Processor cpu;

    String line;

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
                    String[] parts = line.split(":\\s+");
                    if (parts.length > 1) {
                        totalFreq += Double.parseDouble(parts[1]);
                        count++;
                    }
                }
            }
        }
        double avgFreq = (count > 0) ? totalFreq / count : 0;
        return Math.round(avgFreq) + " MHz";
    }

    public String getTemp(){
        Sensors sensors = new Sensors();
        return sensors.getTemperature() + " C";
    }
}

class RAM{
    String line;
    public String getUsedRAM() throws IOException{
        StringBuilder usedRAM = new StringBuilder();
        try{
            Process process = new ProcessBuilder("neofetch", "--off").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null){
                if (line.contains("Memory")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        usedRAM.append(parts[1].trim());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return usedRAM.toString();
    }

    @Override
    public String toString() {
        try {
            return "RAM" + "    " + getUsedRAM();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

class Sensors{
    String line;
    public String getTemperature(){
        StringBuilder temperatureInfo = new StringBuilder();
        try{
            Process process = new ProcessBuilder("sensors").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null){
                temperatureInfo.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return temperatureInfo.toString();
    }
}