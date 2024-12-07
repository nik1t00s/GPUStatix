package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardUI extends JFrame {
    private final GPUSettings gpuSettings;
    private MonitoringOverlay overlay;
    private final ExecutorService executor;

    public DashboardUI(GPUSettings gpuSettings) {
        this.gpuSettings = gpuSettings;

        setTitle("GPUStatix");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setResizable(false);
        setLayout(new BorderLayout());

        executor = Executors.newSingleThreadExecutor();

        // Верхняя панель с названием видеокарты
        JLabel gpuLabel = new JLabel("GPU: " + gpuSettings.getGpuName(), SwingConstants.CENTER);
        gpuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gpuLabel.setForeground(Color.WHITE);
        add(gpuLabel, BorderLayout.NORTH);

        // Центральная панель
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(5, 1)); // 8 строк для параметров
        centerPanel.setBackground(Color.BLACK);

        // Добавляем поля для ввода значений и отображения текущих значений
        centerPanel.add(createValueField("Core Clock", gpuSettings.getCoreClock(), 500, 2000));
        centerPanel.add(createValueField("Memory Clock", gpuSettings.getMemoryClock(), 1000, 8000));
        centerPanel.add(createValueField("Power Limit", gpuSettings.getPowerLimit(), 50, 150));
        centerPanel.add(createValueField("Temp Limit", gpuSettings.getTempLimit(), 50, 100));
        centerPanel.add(createValueField("Fan Speed", gpuSettings.getFanSpeed(), 0, 100));

        add(centerPanel, BorderLayout.CENTER);

        // Нижняя панель с кнопкой для отображения оверлея
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);

        JButton toggleOverlayButton = new JButton("Toggle Overlay");
        toggleOverlayButton.addActionListener(e -> toggleOverlay());
        buttonPanel.add(toggleOverlayButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setBackground(Color.BLACK);
        getContentPane().setBackground(Color.BLACK);
    }

    /**
     * Создаёт панель с меткой и текстовым полем для отображения и ввода значений
     *
     * @param label Название параметра
     * @param initialValue Начальное значение
     * @param min Минимальное значение (для валидации)
     * @param max Максимальное значение (для валидации)
     * @return панель с меткой и текстовым полем
     */
    private JPanel createValueField(String label, int initialValue, int min, int max) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(label + ": ");
        nameLabel.setForeground(Color.WHITE);

        // Поле с текущим значением
        JLabel currentValueLabel = new JLabel(String.valueOf(initialValue));
        currentValueLabel.setForeground(Color.GREEN);

        // Поле ввода для изменения значения
        JTextField inputField = new JTextField(4);
        inputField.setText(String.valueOf(initialValue));

        inputField.addActionListener(e -> {
            try {
                int newValue = Integer.parseInt(inputField.getText());
                if (newValue < min || newValue > max) {
                    JOptionPane.showMessageDialog(this, "Value must be between " + min + " and " + max);
                } else {
                    currentValueLabel.setText(String.valueOf(newValue));
                    gpuSettings.updateSetting(label, newValue);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number.");
            }
        });
        panel.setBackground(Color.BLACK);
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(currentValueLabel, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.EAST);

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