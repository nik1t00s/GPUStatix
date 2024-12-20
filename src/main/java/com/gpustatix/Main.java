package com.gpustatix;

import com.gpustatix.ui.DashboardUI;
import com.gpustatix.utils.GPUSettings;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("jna.library.path", "/usr/lib/x86_64-linux-gnu");
        SwingUtilities.invokeLater(() -> {
            // GPUSettings теперь инициализируется без параметров, данные будут получаться через геттеры
            GPUSettings gpuSettings = new GPUSettings ();
            DashboardUI dashboard = new DashboardUI(gpuSettings);
            dashboard.setVisible(true);
        });
    }
}