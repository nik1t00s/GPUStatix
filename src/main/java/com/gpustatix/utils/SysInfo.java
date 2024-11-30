package com.gpustatix.utils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SysInfo {

    static String line;

    public static boolean checkIntegrated() {
        try {
            Process process = new ProcessBuilder("lspci").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Ищем строку с упоминанием "VGA compatible controller" или "3D controller"
                if (line.toLowerCase().contains("vga compatible controller") || line.toLowerCase().contains("3d controller")) {
                    // Проверяем наличие "Intel" в строке
                    if (line.toLowerCase().contains("intel")) {
                        return true; // Найден интегрированный графический адаптер Intel
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false; // Нет интегрированного графического адаптера Intel
    }

    public static String displaySystemInfo() {
        GPUSettings settings = GPUSettings.getInstance();

        System.out.println("GPU Temperature: " + settings.getGpuTemperature() + "C");
        System.out.println("GPU Memory Usage: " + settings.getGpuMemoryUsage() + "MiB");
        System.out.println("GPU Utilization: " + settings.getGpuUtilization() + "%");
        System.out.println("Core Clock: " + settings.getCoreClock() + " MHz");
        System.out.println("Memory Clock: " + settings.getMemoryClock() + " MHz");
        System.out.println("Power Limit: " + settings.getPowerLimit() + " W");
        System.out.println("Fan Speed: " + settings.getFanSpeed() + " RPM");
        Processor cpu = new Processor();
        RAM ram = new RAM();
        StringBuilder info = new StringBuilder();
        info.append(cpu).append("\n");
        if (checkIntegrated()){
            info.append("INTEGRATED\n");
        }
        else{
            info.append("GPU" + "    " + settings.getGpuTemperature()  + "    " + settings.getGpuUtilization() + "  " + settings.getFanSpeed() + "\n" +
                    "MEM " + settings.getGpuMemoryUsage() + " MB" + "\n"
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
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("vendor_id")) {
                    String[] parts = line.split(":\\s+");
                    if (parts.length > 1) {
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

    public String getTemperature() {
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

    public String getLoad() {
        try {
            long idleTime1 = 0, totalTime1 = 0, idleTime2 = 0, totalTime2 = 0;

            // Чтение первого состояния
            String[] firstStat = getCpuStat();
            idleTime1 = Long.parseLong(firstStat[4]);
            totalTime1 = calculateTotal(firstStat);

            // Задержка для измерения изменений (1 секунда)
            Thread.sleep(1000);

            // Чтение второго состояния
            String[] secondStat = getCpuStat();
            idleTime2 = Long.parseLong(secondStat[4]);
            totalTime2 = calculateTotal(secondStat);

            // Вычисление загрузки
            long deltaIdle = idleTime2 - idleTime1;
            long deltaTotal = totalTime2 - totalTime1;
            double cpuLoad = 100.0 * (1.0 - ((double) deltaIdle / deltaTotal));

            return Math.round(cpuLoad) + "%";
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка: не удалось получить загрузку CPU";
        }
    }

    private String[] getCpuStat() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/stat"))) {
            String line = br.readLine(); // Первая строка содержит общую статистику CPU
            return line.split("\\s+");
        }
    }

    private long calculateTotal(String[] stat) {
        long total = 0;
        for (int i = 1; i < stat.length; i++) { // Пропускаем "cpu" в начале строки
            total += Long.parseLong(stat[i]);
        }
        return total;
    }

    public String getV() {
        String voltageDir = "/sys/class/hwmon/";
        double voltageMillivolts = -1;

        try {
            java.io.File hwmonDir = new java.io.File(voltageDir);
            if (hwmonDir.exists() && hwmonDir.isDirectory()) {
                for (java.io.File hwmon : hwmonDir.listFiles()) {
                    if (hwmon.isDirectory()) {
                        for (java.io.File file : hwmon.listFiles()) {
                            if (file.getName().endsWith("_label")) {
                                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                                    String label = br.readLine().trim();
                                    if (label.equalsIgnoreCase("Vcore")) { // Проверка метки
                                        String inputFile = file.getAbsolutePath().replace("_label", "_input");
                                        try (BufferedReader inputReader = new BufferedReader(new FileReader(inputFile))) {
                                            voltageMillivolts = Double.parseDouble(inputReader.readLine().trim()) / 1000.0;
                                            return String.format("C%.2fV", voltageMillivolts);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            double v = 0.0;
            try {
                Process process = new ProcessBuilder("sensors").start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("in1:")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 1) {
                            v += Double.parseDouble(parts[1]);
                            return v + "V";
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                throw new RuntimeException(e);
            }
            return "CPU Voltage not available";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading voltage";
        }
    }
}

class RAM {
    public String getUsedRAM() throws IOException {
        double totalRAM = 0;
        double freeRAM = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    totalRAM = Double.parseDouble(line.split("\\s+")[1]);
                } else if (line.startsWith("MemAvailable:")) {
                    freeRAM = Double.parseDouble(line.split("\\s+")[1]);
                }

                // Если уже получили необходимую информацию, можем прервать чтение
                if (totalRAM > 0 && freeRAM > 0) {
                    break;
                }
            }
        }

        // Используемая RAM = Всего RAM - Доступно RAM
        double usedRAM = totalRAM - freeRAM;

        // Возвращаем значение в виде строки с указанием единиц измерения в МБ
        return String.format("%.2f MB", usedRAM / 1024);
    }

    @Override
    public String toString() {
        try {
            return "RAM: " + getUsedRAM();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}