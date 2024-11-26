package com.gpustatix.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SysInfo {

    static String line;

    public static String getResolution() {
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

    public static boolean checkIntegrated() {
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

    public static String displaySystemInfo() {
        Processor cpu = new Processor();
        RAM ram = new RAM();
        StringBuilder info = new StringBuilder();
        info.append(cpu).append("\n");
        if (checkIntegrated()){
           info.append("INTEGRATED\n");
        }
        else{
            info.append("GPU" + "    " + "\n" +
                    "MEM " + " MB"
            );
        }
        info.append(ram);
        // info.append(getResolution());
        return info.toString();
    }
}

class Processor {

    String line;

    @Override
    public String toString() {
        try {
            return "CPU" +
                    "   " + getTemperature() +
                    "   " + getLoad() +
                    "   " + getFreq() +
                    "   " + getV();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getVendor() {
        String vendor = "";
        try(BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = br.readLine()) != null){
                if (line.startsWith("vendor_id")){
                    String[] parts = line.split(":\\s+");
                    if (parts.length > 1){
                        vendor = parts[1].trim();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vendor.equals("AuthenticAMD") ? "AMD" : vendor.equals("GenuineIntel") ? "Intel" : "Unknown";
    }

    public String getFreq() throws IOException {
        double totalFreq = 0;
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            while ((line = br.readLine()) != null) {
                if (line.startsWith("cpu MHz")) {
                    String[] parts = line.trim().split(":\\s+");
                    if (parts.length > 1) {
                        totalFreq += Double.parseDouble(parts[1]);
                        count++;
                    }
                }
            }
        }
        double avgFreq = (count > 0) ? totalFreq / count : 0;
        return Math.round(avgFreq) + "MHz";
    }

    public String getTemperature(){
        StringBuilder temperatureInfo = new StringBuilder();
        String cpuVendor = getVendor();
        try {
            Process process = new ProcessBuilder("sensors").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                if (line.contains("Tctl:") && cpuVendor.equals("AMD")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        temperatureInfo.append(parts[1].trim());
                    }
                } else if (line.contains("Package id 0:") && cpuVendor.equals("Intel")) {
                    String[] parts = line.trim().split(":\\s+");
                    if (parts.length > 1) {
                        String[] linepartts = parts[1].trim().split(" ");
                        temperatureInfo.append(linepartts[0].trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return temperatureInfo.toString();
    }
    public String getLoad(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("top", "-bn1");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            double cpuLoad = -1.0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("%Cpu(s):")) {
                    String[] tokens = line.replace(",", "").split("\\s+");
                    cpuLoad = 100.0 - Double.parseDouble(tokens[3]);
                    break;
                }
            }
            reader.close();

            if (cpuLoad >= 0.0){
                return String.format("%.2f%%", cpuLoad);
            }
            else{
                return "Ошибка: не удалось получить загрузку CPU";
            }
        } catch (IOException e) {
           e.printStackTrace();
           return "Ошибка при выполнении команды top.";
        }
    }
    public String getV(){
        double v = 0.0;
        try{
            Process process = new ProcessBuilder("sensors").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null){
                if (line.startsWith("in1:")){
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1){
                        v += Double.parseDouble(parts[1]);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return v + "V";
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