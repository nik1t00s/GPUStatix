package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import java.awt.*;

public class DashboardUI extends JFrame {
    private MonitoringOverlay overlay; // Ссылка на оверлей

    public DashboardUI(GPUSettings gpuSettings) {
        setTitle("GPUStatix Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLayout(new BorderLayout());

        // Верхняя панель
        JLabel gpuLabel = new JLabel("GPU: " + gpuSettings.getGpuVendor(), SwingConstants.CENTER);
        gpuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gpuLabel.setForeground(Color.WHITE);
        add(gpuLabel, BorderLayout.NORTH);

        // Центральная панель с графиками и ползунками
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(8, 1));
        centerPanel.setBackground(Color.BLACK);

        // Добавляем полоски загрузки
        centerPanel.add(createStatusBar("Core Clock: ", gpuSettings.getCoreClock()));
        centerPanel.add(createStatusBar("Memory Clock: ", gpuSettings.getMemoryClock()));
        centerPanel.add(createStatusBar("Temperature: ", 0));

        // Добавляем ползунки
        centerPanel.add(createSlider("Core Clock", 500, 2000, gpuSettings.getCoreClock()));
        centerPanel.add(createSlider("Memory Clock", 1000, 8000, gpuSettings.getMemoryClock()));
        centerPanel.add(createSlider("Power Limit", 50, 150, gpuSettings.getPowerLimit()));
        centerPanel.add(createSlider("Temp Limit", 50, 100, gpuSettings.getTempLimit()));
        centerPanel.add(createSlider("Fan Speed", 0, 100, gpuSettings.getFanSpeed()));

        add(centerPanel, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);

        // Кнопка для включения/выключения оверлея
        JButton toggleOverlayButton = new JButton("Toggle Overlay");
        toggleOverlayButton.addActionListener(e -> {
            if (overlay == null || !overlay.isVisible()) {
                // Создаем и показываем оверлей
                overlay = new MonitoringOverlay();
                overlay.setVisible(true);
            } else {
                // Закрываем оверлей
                overlay.dispose();
                overlay = null;
            }
        });

        JButton fanCurveButton = new JButton("Fan Curve");
        fanCurveButton.addActionListener(e -> System.out.println("Fan curve settings opened"));

        buttonPanel.add(toggleOverlayButton);
        buttonPanel.add(fanCurveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Стилизация
        setBackground(Color.BLACK);
        getContentPane().setBackground(Color.BLACK);
    }

    private JSlider createSlider(String label, int min, int max, int initial) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setForeground(Color.WHITE);
        sliderLabel.setHorizontalAlignment(JLabel.CENTER);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setBackground(Color.BLACK);
        slider.setForeground(Color.WHITE);
        slider.setMajorTickSpacing((max - min) / 5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        panel.setBackground(Color.BLACK);
        panel.add(sliderLabel, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);

        return slider;
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
    public static void main(String[] args) {
        GPUSettings gpuSettings = new GPUSettings();
        DashboardUI dashboard = new DashboardUI(gpuSettings);
        dashboard.setVisible(true);
    }
}