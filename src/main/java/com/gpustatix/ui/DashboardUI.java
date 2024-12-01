package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardUI extends JFrame {
    private MonitoringOverlay overlay;
    private final ExecutorService executor;

    public DashboardUI(GPUSettings gpuSettings) {
        setTitle("GPUStatix Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setResizable(false); // Отключаем возможность развернуть
        setLayout(new BorderLayout());

        executor = Executors.newSingleThreadExecutor();

        // Верхняя панель
        JLabel gpuLabel = new JLabel("GPU: " + gpuSettings.getGpuName(), SwingConstants.CENTER);
        gpuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gpuLabel.setForeground(Color.WHITE);
        add(gpuLabel, BorderLayout.NORTH);

        // Центральная панель
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(8, 1));
        centerPanel.setBackground(Color.BLACK);

        // Добавляем статус-бары
        centerPanel.add(createStatusBar("Core Clock: ", gpuSettings.getCoreClock()));
        centerPanel.add(createStatusBar("Memory Clock: ", gpuSettings.getMemoryClock()));
        centerPanel.add(createStatusBar("Temperature: ", gpuSettings.getGpuTemperature()));

        // Добавляем слайдеры
        centerPanel.add(createSlider("Core Clock", 500, 2000, gpuSettings));
        centerPanel.add(createSlider("Memory Clock", 1000, 8000, gpuSettings));
        centerPanel.add(createSlider("Power Limit", 50, 150, gpuSettings));
        centerPanel.add(createSlider("Temp Limit", 50, 100, gpuSettings));
        centerPanel.add(createSlider("Fan Speed", 0, 100, gpuSettings));

        add(centerPanel, BorderLayout.CENTER);

        // Нижняя панель
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);

        JButton toggleOverlayButton = new JButton("Toggle Overlay");
        toggleOverlayButton.addActionListener(e -> toggleOverlay());

        buttonPanel.add(toggleOverlayButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setBackground(Color.BLACK);
        getContentPane().setBackground(Color.BLACK);
    }

    private JPanel createStatusBar(String label, int value) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel(label);
        statusLabel.setForeground(Color.WHITE);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(value);
        progressBar.setStringPainted(true);

        panel.setBackground(Color.BLACK);
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSlider(String label, int min, int max, GPUSettings gpuSettings) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setForeground(Color.WHITE);

        // Получаем значение, проверяем его и корректируем при необходимости
        int initialValue = gpuSettings.getCoreClock();
        if (initialValue < min) {
            initialValue = min;
        } else if (initialValue > max) {
            initialValue = max;
        }

        JSlider slider = new JSlider(min, max, initialValue);
        slider.setBackground(Color.BLACK);
        slider.addChangeListener(e -> gpuSettings.updateSetting(label, slider.getValue()));

        JTextField valueField = new JTextField(String.valueOf(initialValue), 4);
        valueField.addActionListener(e -> {
            try {
                int value = Integer.parseInt(valueField.getText());
                if (value < min) value = min;
                if (value > max) value = max;
                slider.setValue(value);
            } catch (NumberFormatException ex) {
                // Игнорируем некорректный ввод
            }
        });
        panel.add(sliderLabel, BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueField, BorderLayout.EAST);
        panel.setBackground(Color.BLACK);

        return panel;
    }

    private void toggleOverlay() {
        if (overlay == null || !overlay.isVisible()) {
            executor.submit(() -> {
                overlay = new MonitoringOverlay();
                overlay.setVisible(true);
            });
        } else {
            overlay.dispose();
            overlay = null;
        }
    }
}