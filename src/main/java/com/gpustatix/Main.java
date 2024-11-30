package com.gpustatix;

import com.gpustatix.ui.DashboardUI;
import com.gpustatix.utils.GPUSettings;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GPUSettings gpuSettings = new GPUSettings("NVIDIA", 1500, 7000, 100, 80, 50);
            DashboardUI dashboard = new DashboardUI(gpuSettings);
            dashboard.setVisible(true);
        });
    }
}