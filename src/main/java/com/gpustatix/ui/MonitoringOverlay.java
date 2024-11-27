package com.gpustatix.ui;

import com.gpustatix.utils.FrameRate;
import com.gpustatix.utils.SysInfo;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoringOverlay extends JFrame {
    private final GraphPanel frameRateGraph;
    private final GraphPanel frameTimeGraph;
    private final JLabel systemInfoLabel;
    private final JLabel fpsLabel;
    private final FrameRate frameRateCalculator;

    public MonitoringOverlay() {
        setTitle("Framerate and Frametime Overlay");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setOpacity(0.85f);
        setAlwaysOnTop(true);

        // Получение разрешения экрана
        Dimension resolution = SysInfo.getResolution();
        setSize(resolution);
        setLayout(null);

        // Информация о системе
        systemInfoLabel = new JLabel("<html>" + SysInfo.displaySystemInfo().replace("\n", "<br>") + "</html>");
        systemInfoLabel.setForeground(Color.WHITE);
        systemInfoLabel.setBounds(10, 10, resolution.width / 3, resolution.height / 4);
        add(systemInfoLabel);

        // Метка для FPS
        fpsLabel = new JLabel("FPS: 0");
        fpsLabel.setForeground(Color.GREEN);
        fpsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        fpsLabel.setBounds(10, resolution.height / 4, resolution.width / 3, 20);
        add(fpsLabel);

        // График FPS
        frameRateGraph = new GraphPanel("Frame Rate (FPS)", new Color(0, 255, 0, 200));
        frameRateGraph.setBounds(10, resolution.height / 4 + 30, resolution.width / 3, resolution.height / 3);
        add(frameRateGraph);

        // График времени кадра
        frameTimeGraph = new GraphPanel("Frame Time (ms)", new Color(255, 0, 0, 200));
        frameTimeGraph.setBounds(10, resolution.height / 4 + resolution.height / 3 + 40, resolution.width / 3, resolution.height / 3);
        add(frameTimeGraph);

        // Инициализация FPS-калькулятора
        frameRateCalculator = new FrameRate();

        // Таймер для обновления данных
        Timer timer = new Timer(1000 / 60, e -> {
            frameRateCalculator.frameRendered();

            double fps = frameRateCalculator.getCurrentFPS();
            fpsLabel.setText(String.format("FPS: %.2f", fps));

            double frameTime = (fps > 0) ? 1000.0 / fps : 0;
            frameRateGraph.addGraphData(fps);
            frameTimeGraph.addGraphData(frameTime);
        });
        timer.start();
    }
}
class GraphPanel extends JPanel {
    private final Queue<Double> dataQueue;
    private final String label;
    private final Color graphColor;
    private final int maxSize = 100;

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

        // Рисуем график
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

        // Добавляем текст с меткой графика
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, 10, 20);
    }
}