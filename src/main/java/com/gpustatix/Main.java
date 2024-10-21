package com.gpustatix;

import com.gpustatix.utils.SysInfo;
import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class Main {

    public static void main(String[] args) {
        SysInfo sysInfo = new SysInfo();
        sysInfo.displaySystemInfo();
    }
}
