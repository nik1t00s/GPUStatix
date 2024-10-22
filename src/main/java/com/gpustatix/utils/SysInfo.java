package com.gpustatix.utils;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

public class SysInfo {

    public static void displaySystemInfo() {
        Processor cpu = new Processor();
        HardwareAbstractionLayer hal = SysHardware.getHal();

        System.out.println("\n" + cpu.getName());
    }
}

abstract class SysHardware {

    static SystemInfo si = new SystemInfo();
    static HardwareAbstractionLayer hal = si.getHardware();

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

    public long[] getFreq() {
        return cpu.getCurrentFreq();
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

    public double[] getLoad() {
        return cpu.getProcessorCpuLoad(0);
    }
}
