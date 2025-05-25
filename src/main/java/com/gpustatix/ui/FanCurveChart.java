package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Interactive fan curve chart component that allows users to create
 * and edit temperature-based fan curves for GPU cooling.
 */
public class FanCurveChart extends JPanel {
    // Constants for chart dimensions and settings
    private static final int PADDING = 70;
    private static final int POINT_SIZE = 12;
    private static final int GRID_LINES = 16;
    private static final int MIN_TEMP = 20;
    private static final int MAX_TEMP = 100;
    private static final int MIN_FAN = 0;
    private static final int MAX_FAN = 100;
    private static final int SNAP_THRESHOLD = 5; // Snap points within 5 units of grid lines
    
    // Current curve points (temperature -> fan speed)
    private final List<Point> curvePoints = new ArrayList<>();
    
    // Selected point for dragging
    private Point selectedPoint = null;
    
    // Preset curves
    private final List<Point> silentPreset = new ArrayList<>();
    private final List<Point> performancePreset = new ArrayList<>();
    
    // GPU settings reference
    private final GPUSettings gpuSettings;
    
    // Current temperature and fan speed for monitoring
    private int currentTemp = 0;
    private int currentFan = 0;
    
    // Executor for background monitoring
    private ScheduledExecutorService monitoringExecutor;
    private boolean isMonitoring = false;
    
    /**
     * Point class to store temperature and fan speed pairs
     */
    public static class Point implements Comparable<Point> {
        private int temperature;
        private int fanSpeed;
        
        public Point(int temperature, int fanSpeed) {
            this.temperature = temperature;
            this.fanSpeed = fanSpeed;
        }
        
        public int getTemperature() {
            return temperature;
        }
        
        public void setTemperature(int temperature) {
            this.temperature = temperature;
        }
        
        public int getFanSpeed() {
            return fanSpeed;
        }
        
        public void setFanSpeed(int fanSpeed) {
            this.fanSpeed = fanSpeed;
        }
        
        @Override
        public int compareTo(Point other) {
            return Integer.compare(this.temperature, other.temperature);
        }
        
        @Override
        public String toString() {
            return temperature + "°C=" + fanSpeed + "%";
        }
    }
    
    /**
     * Constructor for FanCurveChart
     * @param gpuSettings GPU settings instance to control fan speed
     */
    public FanCurveChart(GPUSettings gpuSettings) {
        this.gpuSettings = gpuSettings;
        
        // Set a much larger size for better precision
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(600, 400));
        setBackground(Color.BLACK);
        
        // Initialize with default curve
        initializeDefaultCurve();
        initializePresets();
        
        // Add mouse listeners for interactivity
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedPoint != null) {
                    selectedPoint = null;
                    repaint();
                    applyCurrentCurve();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                lastMousePosition = e.getPoint();
                repaint();
            }
        });
        
        // Add key listener for deleting points
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedPoint != null) {
                    curvePoints.remove(selectedPoint);
                    selectedPoint = null;
                    repaint();
                    applyCurrentCurve();
                }
            }
        });
    }
    
    /**
     * Initialize default fan curve
     */
    private void initializeDefaultCurve() {
        curvePoints.clear();
        // Simple curve: low speed at low temps, high speed at high temps
        curvePoints.add(new Point(30, 20));  // 30°C -> 20% fan speed
        curvePoints.add(new Point(50, 40));  // 50°C -> 40% fan speed
        curvePoints.add(new Point(70, 70));  // 70°C -> 70% fan speed
        curvePoints.add(new Point(85, 100)); // 85°C -> 100% fan speed
        Collections.sort(curvePoints);
    }
    
    /**
     * Apply the default curve (alias for initializeDefaultCurve for external use)
     */
    public void applyDefaultCurve() {
        initializeDefaultCurve();
        repaint();
        applyCurrentCurve();
    }
    
    /**
     * Initialize preset fan curves
     */
    private void initializePresets() {
        // Silent preset - emphasizes quiet operation
        silentPreset.clear();
        silentPreset.add(new Point(30, 0));   // 30°C -> 0% (off)
        silentPreset.add(new Point(50, 20));  // 50°C -> 20%
        silentPreset.add(new Point(65, 40));  // 65°C -> 40%
        silentPreset.add(new Point(75, 60));  // 75°C -> 60%
        silentPreset.add(new Point(85, 100)); // 85°C -> 100%
        
        // Performance preset - emphasizes cooling
        performancePreset.clear();
        performancePreset.add(new Point(30, 30));  // 30°C -> 30%
        performancePreset.add(new Point(45, 50));  // 45°C -> 50%
        performancePreset.add(new Point(60, 70));  // 60°C -> 70%
        performancePreset.add(new Point(70, 90));  // 70°C -> 90%
        performancePreset.add(new Point(80, 100)); // 80°C -> 100%
    }
    
    /**
     * Apply the silent preset
     */
    public void applySilentPreset() {
        curvePoints.clear();
        curvePoints.addAll(silentPreset);
        repaint();
        applyCurrentCurve();
    }
    
    /**
     * Apply the performance preset
     */
    public void applyPerformancePreset() {
        curvePoints.clear();
        curvePoints.addAll(performancePreset);
        repaint();
        applyCurrentCurve();
    }
    
    /**
     * Apply the current fan curve to the GPU
     */
    public void applyCurrentCurve() {
        if (isMonitoring) {
            int temp = gpuSettings.getGpuTemperature();
            int fan = calculateFanSpeedForTemperature(temp);
            if (fan >= 0) {
                gpuSettings.setFanSpeed(fan);
            }
        }
    }
    
    /**
     * Start monitoring GPU temperature and applying fan curve
     */
    public void startMonitoring(int intervalMs) {
        if (isMonitoring) {
            stopMonitoring();
        }
        
        isMonitoring = true;
        monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
        monitoringExecutor.scheduleAtFixedRate(this::updateMonitoring, 0, intervalMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stop monitoring GPU temperature
     */
    public void stopMonitoring() {
        isMonitoring = false;
        if (monitoringExecutor != null) {
            try {
                // First attempt graceful shutdown
                monitoringExecutor.shutdown();
                
                // Wait for tasks to complete
                if (!monitoringExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    // Force shutdown if tasks haven't completed
                    monitoringExecutor.shutdownNow();
                    
                    // Wait again to ensure termination
                    if (!monitoringExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                        System.err.println("Warning: Monitoring executor did not terminate properly");
                    }
                }
            } catch (InterruptedException e) {
                // Force shutdown on interruption
                monitoringExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                System.err.println("Monitoring shutdown was interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error during monitoring shutdown: " + e.getMessage());
                // Ensure executor is terminated
                monitoringExecutor.shutdownNow();
            } finally {
                // Ensure fans return to a safe state
                try {
                    // Restore default fan control
                    System.out.println("Restoring default fan control settings");
                    // If we have a reasonable temperature, calculate a safe fan speed
                    if (currentTemp > 0 && currentTemp < 100) {
                        int safeFanSpeed = Math.max(30, currentTemp - 40); // Simple calculation: at least 30%, more at higher temps
                        gpuSettings.setFanSpeed(safeFanSpeed);
                    } else {
                        // Use a safe default if temperature reading is questionable
                        gpuSettings.setFanSpeed(50);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to restore default fan control: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Update current temperature and fan speed readings
     */
    private void updateMonitoring() {
        try {
            // Get current GPU temperature
            currentTemp = gpuSettings.getGpuTemperature();
            
            // Safety check for temperature readings
            if (currentTemp <= 0 || currentTemp > 120) {
                System.err.println("Warning: Unusual temperature reading: " + currentTemp + "°C");
                // Use the last known good temperature if available, otherwise use a safe default
                if (currentTemp <= 0) {
                    currentTemp = currentTemp > 0 ? currentTemp : 50; // Default to 50°C if no valid reading
                } else {
                    // Temperature is unusually high, might be a sensor error
                    System.err.println("Potentially dangerous temperature reading! Defaulting to max fan speed.");
                    gpuSettings.setFanSpeed(100); // Set fans to 100% for safety
                    SwingUtilities.invokeLater(this::repaint);
                    return;
                }
            }
            
            // Determine fan speed based on current curve
            currentFan = calculateFanSpeedForTemperature(currentTemp);
            
            // Apply fan speed with error checking
            if (currentFan >= 0) {
                try {
                    gpuSettings.setFanSpeed(currentFan);
                } catch (Exception e) {
                    System.err.println("Failed to set fan speed: " + e.getMessage());
                    // Try again with a safe default
                    try {
                        gpuSettings.setFanSpeed(70); // Set a safe default fan speed
                    } catch (Exception ex) {
                        System.err.println("Critical: Failed to set default fan speed: " + ex.getMessage());
                    }
                }
            }
            
            // Update UI on EDT
            SwingUtilities.invokeLater(this::repaint);
        } catch (Exception e) {
            System.err.println("Error in monitoring: " + e.getMessage());
            e.printStackTrace();
            
            // Try to ensure fans are running in case of error
            try {
                gpuSettings.setFanSpeed(70); // Set a safe default in case of errors
            } catch (Exception ex) {
                // Critical error handling - log but don't rethrow
                System.err.println("Critical error setting fan speed during error recovery: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Calculate fan speed for a given temperature based on the current curve
     */
    private int calculateFanSpeedForTemperature(int temperature) {
        if (curvePoints.isEmpty()) {
            return -1;
        }
        
        // If temperature is below the lowest point on the curve
        if (temperature <= curvePoints.get(0).getTemperature()) {
            return curvePoints.get(0).getFanSpeed();
        }
        
        // If temperature is above the highest point on the curve
        if (temperature >= curvePoints.get(curvePoints.size() - 1).getTemperature()) {
            return curvePoints.get(curvePoints.size() - 1).getFanSpeed();
        }
        
        // Find the two points that temperature falls between
        for (int i = 0; i < curvePoints.size() - 1; i++) {
            Point p1 = curvePoints.get(i);
            Point p2 = curvePoints.get(i + 1);
            
            if (temperature >= p1.getTemperature() && temperature <= p2.getTemperature()) {
                // Linear interpolation
                double ratio = (double)(temperature - p1.getTemperature()) / 
                               (p2.getTemperature() - p1.getTemperature());
                
                return (int)(p1.getFanSpeed() + ratio * (p2.getFanSpeed() - p1.getFanSpeed()));
            }
        }
        
        return -1;
    }
    
    /**
     * Handle mouse press events for selecting points
     */
    private void handleMousePressed(MouseEvent e) {
        Point2D mousePoint = e.getPoint();
        selectedPoint = findNearestPoint(mousePoint);
        repaint();
        requestFocusInWindow();
    }
    
    /**
     * Handle mouse drag events for moving points
     */
    private void handleMouseDragged(MouseEvent e) {
        if (selectedPoint != null) {
            Point2D chartPoint = screenToChartCoordinates(e.getPoint());
            
            // Constrain to valid temperature and fan speed ranges
            int rawTemp = (int)chartPoint.getX();
            int rawFan = (int)chartPoint.getY();
            
            // Apply grid snapping for better precision
            int snapTemp = snapToGrid(rawTemp, MIN_TEMP, MAX_TEMP);
            int snapFan = snapToGrid(rawFan, MIN_FAN, MAX_FAN);
            
            // Ensure values stay within valid ranges
            int temp = Math.max(MIN_TEMP, Math.min(MAX_TEMP, snapTemp));
            int fan = Math.max(MIN_FAN, Math.min(MAX_FAN, snapFan));
            
            // Check if we need to prevent overlapping points (prevent duplicate temperatures)
            boolean canUpdate = true;
            for (Point p : curvePoints) {
                if (p != selectedPoint && p.getTemperature() == temp) {
                    canUpdate = false;
                    break;
                }
            }
            
            if (canUpdate) {
                // Update the selected point
                selectedPoint.setTemperature(temp);
                selectedPoint.setFanSpeed(fan);
                
                // Sort points to maintain temperature order
                Collections.sort(curvePoints);
            }
            
            repaint();
        }
    }
    
    /**
     * Snap a value to the nearest grid line if it's close enough
     */
    private int snapToGrid(int value, int min, int max) {
        // Calculate the interval between grid lines
        int range = max - min;
        int interval = range / GRID_LINES;
        
        // Find the nearest grid line
        int gridLine = min + (((value - min) + interval / 2) / interval) * interval;
        
        // If close enough, snap to the grid line
        if (Math.abs(value - gridLine) <= SNAP_THRESHOLD) {
            return gridLine;
        }
        
        return value;
    }
    
    /**
     * Handle mouse click events for adding points
     */
    private void handleMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // Double-click to add a new point
            Point2D chartPoint = screenToChartCoordinates(e.getPoint());
            
            // Apply grid snapping for better precision
            int rawTemp = (int)chartPoint.getX();
            int rawFan = (int)chartPoint.getY();
            
            int snapTemp = snapToGrid(rawTemp, MIN_TEMP, MAX_TEMP);
            int snapFan = snapToGrid(rawFan, MIN_FAN, MAX_FAN);
            
            // Constrain to valid ranges
            int temp = Math.max(MIN_TEMP, Math.min(MAX_TEMP, snapTemp));
            int fan = Math.max(MIN_FAN, Math.min(MAX_FAN, snapFan));
            
            // Check if a point with this temperature already exists
            boolean pointExists = false;
            for (Point p : curvePoints) {
                if (p.getTemperature() == temp) {
                    pointExists = true;
                    // Update the existing point instead
                    p.setFanSpeed(fan);
                    selectedPoint = p;
                    break;
                }
            }
            
            if (!pointExists) {
                // Create and add the new point
                Point newPoint = new Point(temp, fan);
                curvePoints.add(newPoint);
                selectedPoint = newPoint;
                Collections.sort(curvePoints);
            }
            
            repaint();
            applyCurrentCurve();
        }
    }
    
    /**
     * Find the nearest point to the given mouse position
     */
    private Point findNearestPoint(Point2D mousePoint) {
        double minDistance = Double.MAX_VALUE;
        Point nearestPoint = null;
        
        for (Point p : curvePoints) {
            Point2D screenPoint = chartToScreenCoordinates(new Point2D.Double(p.getTemperature(), p.getFanSpeed()));
            double distance = mousePoint.distance(screenPoint);
            
            // Increase selection radius for easier point selection
            if (distance < minDistance && distance < POINT_SIZE * 3) {
                minDistance = distance;
                nearestPoint = p;
            }
        }
        
        return nearestPoint;
    }
    
    /**
     * Convert screen coordinates to chart coordinates
     */
    private Point2D screenToChartCoordinates(Point2D screenPoint) {
        double chartWidth = getWidth() - 2 * PADDING;
        double chartHeight = getHeight() - 2 * PADDING;
        
        double x = MIN_TEMP + (screenPoint.getX() - PADDING) / chartWidth * (MAX_TEMP - MIN_TEMP);
        double y = MAX_FAN - (screenPoint.getY() - PADDING) / chartHeight * (MAX_FAN - MIN_FAN);
        
        return new Point2D.Double(x, y);
    }
    
    /**
     * Convert chart coordinates to screen coordinates
     */
    private Point2D chartToScreenCoordinates(Point2D chartPoint) {
        double chartWidth = getWidth() - 2 * PADDING;
        double chartHeight = getHeight() - 2 * PADDING;
        
        double x = PADDING + (chartPoint.getX() - MIN_TEMP) / (MAX_TEMP - MIN_TEMP) * chartWidth;
        double y = PADDING + (MAX_FAN - chartPoint.getY()) / (MAX_FAN - MIN_FAN) * chartHeight;
        
        return new Point2D.Double(x, y);
    }
    
    /**
     * Save the current fan curve to a file
     */
    public void saveToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Point p : curvePoints) {
                writer.write(p.getTemperature() + "," + p.getFanSpeed());
                writer.newLine();
            }
        }
    }
    
    /**
     * Load a fan curve from a file
     */
    public void loadFromFile(File file) throws IOException {
        List<Point> loadedPoints = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        int temp = Integer.parseInt(parts[0]);
                        int fan = Integer.parseInt(parts[1]);
                        loadedPoints.add(new Point(temp, fan));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid line in fan curve file: " + line);
                    }
                }
            }
        }
        
        if (!loadedPoints.isEmpty()) {
            curvePoints.clear();
            curvePoints.addAll(loadedPoints);
            Collections.sort(curvePoints);
            repaint();
            applyCurrentCurve();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill background
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw chart area
        g2d.setColor(Color.BLACK);
        g2d.fillRect(PADDING, PADDING, getWidth() - 2 * PADDING, getHeight() - 2 * PADDING);
        
        // Draw grid
        drawGrid(g2d);
        
        // Draw axes and labels
        drawAxes(g2d);
        
        // Draw the fan curve
        drawCurve(g2d);
        
        // Draw current temperature and fan speed indicators
        drawCurrentStatus(g2d);
    }
    
    /**
     * Draw the grid lines
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(0.5f));
        
        double chartWidth = getWidth() - 2 * PADDING;
        double chartHeight = getHeight() - 2 * PADDING;
        
        // Vertical grid lines (temperature)
        for (int i = 0; i <= GRID_LINES; i++) {
            double x = PADDING + (chartWidth / GRID_LINES) * i;
            g2d.draw(new Line2D.Double(x, PADDING, x, getHeight() - PADDING));
        }
        
        // Horizontal grid lines (fan speed)
        for (int i = 0; i <= GRID_LINES; i++) {
            double y = PADDING + (chartHeight / GRID_LINES) * i;
            g2d.draw(new Line2D.Double(PADDING, y, getWidth() - PADDING, y));
        }
    }
    
    /**
     * Draw the axes and labels
     */
    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.0f));
        
        // X-axis (temperature)
        g2d.draw(new Line2D.Double(PADDING, getHeight() - PADDING, getWidth() - PADDING, getHeight() - PADDING));
        
        // Y-axis (fan speed)
        g2d.draw(new Line2D.Double(PADDING, PADDING, PADDING, getHeight() - PADDING));
        
        // X-axis labels (temperature)
        double chartWidth = getWidth() - 2 * PADDING;
        for (int i = 0; i <= GRID_LINES; i++) {
            int temp = MIN_TEMP + ((MAX_TEMP - MIN_TEMP) / GRID_LINES) * i;
            double x = PADDING + (chartWidth / GRID_LINES) * i;
            g2d.drawString(String.valueOf(temp) + "°C", (float) x - 10, getHeight() - PADDING + 15);
        }
        
        // Y-axis labels (fan speed)
        double chartHeight = getHeight() - 2 * PADDING;
        for (int i = 0; i <= GRID_LINES; i++) {
            int fan = MAX_FAN - ((MAX_FAN - MIN_FAN) / GRID_LINES) * i;
            double y = PADDING + (chartHeight / GRID_LINES) * i;
            g2d.drawString(String.valueOf(fan) + "%", PADDING - 35, (float) y + 5);
        }
        
        // Axis titles
        Font titleFont = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(titleFont);
        g2d.drawString("Temperature (°C)", getWidth() / 2 - 60, getHeight() - 10);
        
        // Rotate for Y-axis label
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Fan Speed (%)", -getHeight() / 2 - 40, 20);
        g2d.rotate(Math.PI / 2); // Restore rotation
        
        // Add grid value indicators for better precision
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(new Color(200, 200, 200, 150));
    }
    
    /**
     * Draw the fan curve and control points
     */
    private void drawCurve(Graphics2D g2d) {
        if (curvePoints.isEmpty()) {
            return;
        }
        
        // Sort points by temperature
        Collections.sort(curvePoints);
        
        // Draw connecting lines
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(2.0f));
        
        for (int i = 0; i < curvePoints.size() - 1; i++) {
            Point p1 = curvePoints.get(i);
            Point p2 = curvePoints.get(i + 1);
            
            Point2D screenP1 = chartToScreenCoordinates(new Point2D.Double(p1.getTemperature(), p1.getFanSpeed()));
            Point2D screenP2 = chartToScreenCoordinates(new Point2D.Double(p2.getTemperature(), p2.getFanSpeed()));
            
            g2d.draw(new Line2D.Double(screenP1, screenP2));
        }
        
        // Draw points
        for (Point p : curvePoints) {
            Point2D screenPoint = chartToScreenCoordinates(new Point2D.Double(p.getTemperature(), p.getFanSpeed()));
            
            if (p == selectedPoint) {
                // Selected point - yellow with larger size
                g2d.setColor(Color.YELLOW);
                g2d.fill(new Ellipse2D.Double(screenPoint.getX() - POINT_SIZE, screenPoint.getY() - POINT_SIZE, 
                                             POINT_SIZE * 2, POINT_SIZE * 2));
                
                // Draw border for better visibility
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.draw(new Ellipse2D.Double(screenPoint.getX() - POINT_SIZE, screenPoint.getY() - POINT_SIZE, 
                                             POINT_SIZE * 2, POINT_SIZE * 2));
                
                // Show precise value for selected point
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString(p.toString(), (float) screenPoint.getX() + 15, (float) screenPoint.getY() - 15);
            } else {
                // Regular point - white with smaller size
                g2d.setColor(Color.WHITE);
                g2d.fill(new Ellipse2D.Double(screenPoint.getX() - POINT_SIZE / 2, screenPoint.getY() - POINT_SIZE / 2, 
                                             POINT_SIZE, POINT_SIZE));
                
                // Draw border for better visibility
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.draw(new Ellipse2D.Double(screenPoint.getX() - POINT_SIZE / 2, screenPoint.getY() - POINT_SIZE / 2, 
                                             POINT_SIZE, POINT_SIZE));
                
                // Show small tooltip for regular points on hover
                if (mouseIsNear(screenPoint)) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString(p.toString(), (float) screenPoint.getX() + 10, (float) screenPoint.getY() - 10);
                }
            }
        }
    }
    
    // Track mouse position for hover effects
    private Point2D lastMousePosition = new Point2D.Double(0, 0);
    
    /**
     * Check if mouse is near a point
     */
    private boolean mouseIsNear(Point2D point) {
        if (lastMousePosition == null) return false;
        return lastMousePosition.distance(point) < POINT_SIZE * 2;
    }
    
    /**
     * Draw current temperature and fan speed indicators
     */
    private void drawCurrentStatus(Graphics2D g2d) {
        if (!isMonitoring) {
            return;
        }
        
        // Draw current temperature vertical line
        Point2D tempPoint1 = chartToScreenCoordinates(new Point2D.Double(currentTemp, MIN_FAN));
        Point2D tempPoint2 = chartToScreenCoordinates(new Point2D.Double(currentTemp, MAX_FAN));
        
        g2d.setColor(new Color(255, 150, 150, 150));
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2d.draw(new Line2D.Double(tempPoint1, tempPoint2));
        
        // Draw current fan speed horizontal line
        Point2D fanPoint1 = chartToScreenCoordinates(new Point2D.Double(MIN_TEMP, currentFan));
        Point2D fanPoint2 = chartToScreenCoordinates(new Point2D.Double(MAX_TEMP, currentFan));
        
        g2d.setColor(new Color(150, 150, 255, 150));
        g2d.draw(new Line2D.Double(fanPoint1, fanPoint2));
        
        // Draw intersection point
        Point2D currentPoint = chartToScreenCoordinates(new Point2D.Double(currentTemp, currentFan));
        g2d.setColor(Color.RED);
        g2d.fill(new Ellipse2D.Double(currentPoint.getX() - POINT_SIZE, currentPoint.getY() - POINT_SIZE, 
                                     POINT_SIZE * 2, POINT_SIZE * 2));
        
        // Display current values
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Current: " + currentTemp + "°C / " + currentFan + "%", 
                      getWidth() - 150, 20);
    }
}

