package com.gpustatix;

import com.gpustatix.ui.DashboardUI;
import com.gpustatix.utils.SysInfo;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++){
           SysInfo.displaySystemInfo();
        }
    }
}
