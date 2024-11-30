package com.gpustatix;

import com.gpustatix.ui.DashboardUI;
import com.gpustatix.utils.GPUSettings;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Создаем объект GPUSettings
            GPUSettings gpuSettings = new GPUSettings();

            // Передаем его в конструктор DashboardUI
            DashboardUI dashboard = new DashboardUI(gpuSettings);
            dashboard.setVisible(true);
        });
    }
}