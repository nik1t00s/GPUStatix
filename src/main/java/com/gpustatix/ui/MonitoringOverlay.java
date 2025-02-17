package com.gpustatix.ui;

import com.gpustatix.utils.SysInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MonitoringOverlay extends JFrame {
    private final JLabel systemInfoLabel;

    public MonitoringOverlay() {
        setTitle("Framerate and Frametime Overlay");
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0)); // Прозрачный фон
        setOpacity(0.85f); // Полупрозрачность

        // Устанавливаем размер и позицию оверлея
        setSize(400, 90);
        setLocation(10, 10);
        setLayout(null);

        // Метка для системной информации
        systemInfoLabel = new JLabel("<html>" + SysInfo.displaySystemInfo().replace("\n", "<br>") + "</html>");
        systemInfoLabel.setForeground(Color.WHITE);
        systemInfoLabel.setBounds(10, 10, 380, 60);
        add(systemInfoLabel);

        // Таймер для обновления данных
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обновляем системную информацию
                systemInfoLabel.setText("<html>" + SysInfo.displaySystemInfo().replace("\n", "<br>") + "</html>");
            }
        });
        timer.start();
    }
}