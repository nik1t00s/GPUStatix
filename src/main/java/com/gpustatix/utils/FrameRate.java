package com.gpustatix.utils;

public class FrameRate {
    private long lastTime;
    private int frames;

    public FrameRate() {
        lastTime = System.nanoTime();
    }

    public void frameRendered() {
        frames++;
    }

    public double getCurrentFPS() {
        long currentTime = System.nanoTime();
        long elapsed = currentTime - lastTime;

        if (elapsed > 1_000_000_000) {
            lastTime = currentTime;
            double fps = frames / (elapsed / 1_000_000_000.0);
            frames = 0;
            return fps;
        }
        return 0;
    }
}