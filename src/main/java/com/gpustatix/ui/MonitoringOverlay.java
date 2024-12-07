package com.gpustatix.ui;

import com.gpustatix.utils.FrameRate;
import com.gpustatix.utils.SysInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoringOverlay extends JFrame {
    // private final GraphPanel frameRateGraph;
    // private final GraphPanel frameTimeGraph;
    private final JLabel systemInfoLabel;
    // private final JLabel fpsLabel;
    // private final FrameRate frameRateCalculator;

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

        // Метка для FPS
        //fpsLabel = new JLabel("FPS: 0");
        //fpsLabel.setForeground(Color.GREEN);
        //fpsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        //fpsLabel.setBounds(10, 70, 100, 20);
        //add(fpsLabel);

        // График FPS
        // frameRateGraph = new GraphPanel("Frame Rate (FPS)", new Color(0, 255, 0, 200));
        // frameRateGraph.setBounds(10, 100, 180, 70);
        // add(frameRateGraph);

        // График времени кадра
        // frameTimeGraph = new GraphPanel("Frame Time (ms)", new Color(255, 0, 0, 200));
        // frameTimeGraph.setBounds(200, 100, 180, 70);
        // add(frameTimeGraph);

        // Инициализация FPS-калькулятора
        // frameRateCalculator = new FrameRate();

        // Таймер для обновления данных
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // frameRateCalculator.frameRendered();

                // Обновляем FPS
                // double fps = frameRateCalculator.getCurrentFPS();
                // fpsLabel.setText(String.format("FPS: %.2f", fps));

                // Обновляем графики
                // double frameTime = (fps > 0) ? 1000.0 / fps : 0;
                // frameRateGraph.addGraphData(fps);
                // frameTimeGraph.addGraphData(frameTime);

                // Обновляем системную информацию
                systemInfoLabel.setText("<html>" + SysInfo.displaySystemInfo().replace("\n", "<br>") + "</html>");
            }
        });
        timer.start();
    }

    // Внутренний класс для панели графиков
    private static class GraphPanel extends JPanel {
        private final Queue<Double> dataQueue;
        private final String label;
        private final Color graphColor;
        private final int maxSize = 50;

        public GraphPanel(String label, Color graphColor) {
            this.label = label;
            this.graphColor = graphColor;
            this.dataQueue = new LinkedList<>();
            setOpaque(false);
        }

        public void addGraphData(double data) {
            if (dataQueue.size() >= maxSize) {
                dataQueue.poll();
            }
            dataQueue.add(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(graphColor);

            int width = getWidth();
            int height = getHeight();
            int prevX = 0, prevY = height;

            double maxValue = dataQueue.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            Double[] dataArray = dataQueue.toArray(new Double[0]);
            for (int i = 0; i < dataArray.length; i++) {
                int x = (i * width) / maxSize;
                int y = height - (int) ((dataArray[i] / maxValue) * height);
                if (i > 0) {
                    g2d.drawLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }

            // Текст с названием графика
            g2d.setColor(Color.WHITE);
            g2d.drawString(label, 10, 50);
        }
    }
}