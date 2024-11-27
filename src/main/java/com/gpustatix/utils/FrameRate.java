package com.gpustatix.utils;

public class FrameRate {
    private long lastFrameTime;
    private int frames;
    private double currentFPS;

    public FrameRate() {
        lastFrameTime = System.nanoTime();
        frames = 0;
        currentFPS = 0;
    }

    public void frameRendered() {
        frames++;
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - lastFrameTime;

        if (deltaTime >= 1_000_000_000) { // Один цикл = 1 секунда
            currentFPS = frames / (deltaTime / 1_000_000_000.0);
            frames = 0;
            lastFrameTime = currentTime;
        }
    }

    public double getCurrentFPS() {
        return currentFPS;
    }
}