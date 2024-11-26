package com.gpustatix.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

class FrameRate{
    private long lastTime;
    private int frameCount;
    private float fps;

    public FrameRate(){
        lastTime = System.nanoTime();
        frameCount = 0;
        fps = 0;
    }
    public void update(){
        frameCount++;
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - lastTime;

        if (elapsedTime >= 1_000_000_000){
            fps = frameCount;
            frameCount = 0;
            lastTime = currentTime;
        }
    }
    public float getFPS(){
        return fps;
    }
}

public class MonitoringOverlay extends JFrame {
    private final GraphPanel graphPanel;
    private static final FrameRate fps = new FrameRate();

    public MonitoringOverlay(){
        setTitle("Framerate and Frametime Overlay");
        setSize(800,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setOpacity(0.85f);
        setAlwaysOnTop(true);
        graphPanel = new GraphPanel();
        add(graphPanel);
        Timer timer = new Timer(1000 / 60, new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               fps.update();
               double frameRate = fps.getFPS();
               double frameTime = 1000.0 / frameRate;
               graphPanel.addGraphData(frameRate, frameTime);
           }
        });
    }
}

class GraphPanel extends JPanel {
    private final Queue<Double> frameRates;
    private final Queue<Double> frameTimes;
    private final int maxSize = 100;

    public GraphPanel() {
        this.frameRates = new LinkedList<>();
        this.frameTimes = new LinkedList<>();
        setBackground(new Color(0,0,0,0));
    }
    public void addGraphData(double frameRate, double frameTime){
        if (frameRates.size() >= maxSize){
            frameRates.poll();
        }
        if (frameTimes.size() >= maxSize){
            frameTimes.poll();
        }
        frameRates.add(frameRate);
        frameTimes.add(frameTime);
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(new Color(0,255,0,200));
        drawGraph(g, frameRates, "Frame Rate (FPS)", getWidth() / 4);

        g.setColor(new Color(255,0,0,200));
        drawGraph(g, frameTimes, "Frame Time (ms)", getWidth() / 4);
    }
    private void drawGraph(Graphics g, Queue<Double> data, String label, int offsetX){
        int width = getWidth() / 2;
        int height = getHeight() - 50;
        int prevX = 0;
        int prevY = -1;
        double maxValue = data.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        for (int i = 0; i < data.size(); i++){
            double value = data.toArray(new Double[0])[i];
            int x = (i * width) / maxSize + offsetX;
            int y = (int) ((1 - value / maxValue) * height);
            if (prevY != -1){
                g.drawLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }
        g.setColor(Color.WHITE);
        g.drawString(label, (width / 2) + offsetX - 50, 15);
    }
}