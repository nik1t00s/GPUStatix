package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;
import com.gpustatix.utils.SysInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;

/**
 * Enhanced GPU monitoring overlay with visual indicators and dynamic display options
 */
public class MonitoringOverlay extends JFrame {
    // UI Components
    private final JPanel contentPanel;
    private JLabel titleLabel;
    private JButton closeButton;
    private JButton minimizeButton;
    private JButton detailsButton;
    
    // Info labels
    private JLabel gpuNameLabel;
    private JLabel gpuTempLabel;
    private JLabel gpuFanLabel;
    private JLabel gpuUtilLabel;
    private JLabel gpuMemLabel;
    private JLabel coreClockLabel;
    private JLabel memClockLabel;
    private JLabel powerUsageLabel;
    
    // Progress bars for visual indicators
    private JProgressBar tempProgressBar;
    private JProgressBar fanProgressBar;
    private JProgressBar utilProgressBar;
    private JProgressBar memProgressBar;
    
    // State tracking
    private Point dragStart;
    private final GPUSettings gpuSettings;
    private Timer updateTimer;
    private boolean isDetailedView = true;
    private boolean isMinimized = false;
    private final int UPDATE_INTERVAL = 750; // Update interval in milliseconds
    
    // Dimensions
    private final int FULL_HEIGHT = 300;
    private final int COMPACT_HEIGHT = 180;
    private final int MINIMIZED_HEIGHT = 30;
    private final int WIDTH = 350;
    
    // Formatter for numbers
    private final DecimalFormat df = new DecimalFormat("#,###");

    public MonitoringOverlay() {
        this.gpuSettings = new GPUSettings();
        
        setTitle("GPU Monitoring Overlay");
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setOpacity(0.92f); // Slightly less transparent for better readability
        
        // Create main panel with dark background and rounded border
        JPanel wrapperPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(15, 15, 20, 240));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g);
            }
        };
        wrapperPanel.setOpaque(false);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Create title panel with controls
        JPanel titlePanel = createTitlePanel();
        wrapperPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Create content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        // Initialize GPU information labels and progress bars
        initializeComponents();
        
        // Add basic stats to content panel
        addBasicStats();
        
        // Add detailed stats
        JPanel detailedPanel = createDetailedPanel();
        contentPanel.add(detailedPanel);
        
        // Set up window
        wrapperPanel.add(contentPanel, BorderLayout.CENTER);
        add(wrapperPanel);
        setSize(WIDTH, FULL_HEIGHT);
        setLocation(20, 20);
        
        // Set the shape of the window to match the rounded panel
        setShape(new RoundRectangle2D.Double(0, 0, WIDTH, FULL_HEIGHT, 15, 15));
        
        // Add global keyboard shortcuts
        addKeyboardShortcuts();
        
        // Set up timer for updating GPU information
        updateTimer = new Timer(UPDATE_INTERVAL, e -> updateGPUInfo());
        
        // Update info immediately
        updateGPUInfo();
    }
    
    /**
     * Creates the title panel with window controls
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(5, 10, 5, 5));
        
        // Title label
        titleLabel = new JLabel("GPU Monitor");
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Control buttons panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);
        
        // Minimize button
        minimizeButton = createControlButton("−", "Minimize");
        minimizeButton.addActionListener(e -> toggleMinimize());
        
        // Details toggle button
        detailsButton = createControlButton("⋯", "Toggle Details");
        detailsButton.addActionListener(e -> toggleDetailedView());
        
        // Close button
        closeButton = createControlButton("×", "Close");
        closeButton.addActionListener(e -> dispose());
        
        controlPanel.add(minimizeButton);
        controlPanel.add(detailsButton);
        controlPanel.add(closeButton);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(controlPanel, BorderLayout.EAST);
        
        // Make title bar draggable
        setupDraggable(titlePanel);
        
        return titlePanel;
    }
    
    /**
     * Creates a control button with hover effects
     */
    private JButton createControlButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(new Color(180, 180, 180));
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(25, 20));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (text.equals("×")) {
                    button.setForeground(new Color(255, 100, 100));
                } else {
                    button.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(new Color(180, 180, 180));
            }
        });
        
        return button;
    }
    
    /**
     * Initialize all components used in the overlay
     */
    private void initializeComponents() {
        // Basic info labels
        gpuNameLabel = createInfoLabel("GPU: ", "");
        gpuTempLabel = createInfoLabel("Temperature: ", "0°C");
        gpuFanLabel = createInfoLabel("Fan Speed: ", "0%");
        gpuUtilLabel = createInfoLabel("Utilization: ", "0%");
        gpuMemLabel = createInfoLabel("Memory: ", "0 MB");
        
        // Detailed info labels
        coreClockLabel = createInfoLabel("Core Clock: ", "0 MHz");
        memClockLabel = createInfoLabel("Memory Clock: ", "0 MHz");
        powerUsageLabel = createInfoLabel("Power Usage: ", "0W");
        
        // Progress bars
        tempProgressBar = createProgressBar(Color.RED);
        fanProgressBar = createProgressBar(Color.CYAN);
        utilProgressBar = createProgressBar(Color.GREEN);
        memProgressBar = createProgressBar(Color.ORANGE);
    }
    
    /**
     * Create a styled progress bar for visual indicators
     */
    private JProgressBar createProgressBar(Color barColor) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(false);
        bar.setBackground(new Color(30, 30, 30));
        bar.setForeground(barColor);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(WIDTH - 40, 6));
        bar.setMaximumSize(new Dimension(WIDTH - 40, 6));
        return bar;
    }
    
    /**
     * Add the basic stats to the content panel
     */
    private void addBasicStats() {
        contentPanel.add(createPaddedPanel(gpuNameLabel));
        
        JPanel tempPanel = new JPanel(new BorderLayout(5, 2));
        tempPanel.setOpaque(false);
        tempPanel.add(createInfoPanel("Temperature: ", gpuTempLabel), BorderLayout.NORTH);
        tempPanel.add(tempProgressBar, BorderLayout.SOUTH);
        contentPanel.add(tempPanel);
        
        JPanel fanPanel = new JPanel(new BorderLayout(5, 2));
        fanPanel.setOpaque(false);
        fanPanel.add(createInfoPanel("Fan Speed: ", gpuFanLabel), BorderLayout.NORTH);
        fanPanel.add(fanProgressBar, BorderLayout.SOUTH);
        contentPanel.add(fanPanel);
        
        JPanel utilPanel = new JPanel(new BorderLayout(5, 2));
        utilPanel.setOpaque(false);
        utilPanel.add(createInfoPanel("Utilization: ", gpuUtilLabel), BorderLayout.NORTH);
        utilPanel.add(utilProgressBar, BorderLayout.SOUTH);
        contentPanel.add(utilPanel);
        
        JPanel memPanel = new JPanel(new BorderLayout(5, 2));
        memPanel.setOpaque(false);
        memPanel.add(createInfoPanel("Memory: ", gpuMemLabel), BorderLayout.NORTH);
        memPanel.add(memProgressBar, BorderLayout.SOUTH);
        contentPanel.add(memPanel);
    }
    
    /**
     * Create the detailed stats panel
     */
    private JPanel createDetailedPanel() {
        JPanel detailedPanel = new JPanel();
        detailedPanel.setLayout(new BoxLayout(detailedPanel, BoxLayout.Y_AXIS));
        detailedPanel.setOpaque(false);
        detailedPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(80, 80, 80));
        separator.setBackground(new Color(40, 40, 40));
        detailedPanel.add(separator);
        
        // Add detailed info
        detailedPanel.add(Box.createVerticalStrut(10));
        detailedPanel.add(createPaddedPanel(coreClockLabel));
        detailedPanel.add(createPaddedPanel(memClockLabel));
        detailedPanel.add(createPaddedPanel(powerUsageLabel));
        
        return detailedPanel;
    }
    
    /**
     * Create a padded panel for consistent spacing
     */
    private JPanel createPaddedPanel(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        panel.add(label, BorderLayout.WEST);
        return panel;
    }
    
    /**
     * Create an info panel with label and value
     */
    private JPanel createInfoPanel(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(180, 180, 180));
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create an info label with standard styling
     */
    private JLabel createInfoLabel(String prefix, String initialValue) {
        JLabel label = new JLabel(prefix + initialValue);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }
    
    /**
     * Update all GPU information with visual indicators
     */
    private void updateGPUInfo() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Update GPU name
                String gpuName = gpuSettings.getGpuName();
                if (gpuName.length() > 30) {
                    gpuName = gpuName.substring(0, 27) + "...";
                }
                gpuNameLabel.setText("GPU: " + gpuName);
                
                // Update temperature with color indicator
                int temp = gpuSettings.getGpuTemperature();
                gpuTempLabel.setText(temp + "°C");
                tempProgressBar.setValue(temp);
                
                // Color-code temperature based on value
                if (temp >= 80) {
                    gpuTempLabel.setForeground(new Color(255, 100, 100));
                    tempProgressBar.setForeground(new Color(255, 50, 50));
                } else if (temp >= 70) {
                    gpuTempLabel.setForeground(new Color(255, 180, 50));
                    tempProgressBar.setForeground(new Color(255, 180, 50));
                } else {
                    gpuTempLabel.setForeground(Color.WHITE);
                    tempProgressBar.setForeground(new Color(50, 255, 50));
                }
                
                // Update fan speed
                int fanSpeed = gpuSettings.getFanSpeed();
                gpuFanLabel.setText(fanSpeed + "%");
                fanProgressBar.setValue(fanSpeed);
                
                // Update utilization
                String utilString = gpuSettings.getGpuUtilization();
                int util = 0;
                try {
                    util = Integer.parseInt(utilString.replace("%", "").trim());
                } catch (NumberFormatException e) {
                    // Default to 0 if parsing fails
                }
                gpuUtilLabel.setText(utilString);
                utilProgressBar.setValue(util);
                
                // Color-code utilization based on value
                if (util >= 90) {
                    utilProgressBar.setForeground(new Color(255, 100, 100));
                } else if (util >= 70) {
                    utilProgressBar.setForeground(new Color(255, 180, 50));
                } else {
                    utilProgressBar.setForeground(new Color(50, 255, 50));
                }
                
                // Update memory usage
                int memUsage = gpuSettings.getGpuMemoryUsage();
                gpuMemLabel.setText(df.format(memUsage) + " MB");
                
                // Estimate memory percentage (assuming 8GB card - adjust if needed)
                int memPercent = Math.min(100, (int)(memUsage / 80.0));
                memProgressBar.setValue(memPercent);
                
                // Update clock speeds and power
                int coreClock = gpuSettings.getCoreClock();
                int memoryClock = gpuSettings.getMemoryClock();
                int powerLimit = gpuSettings.getPowerLimit();
                
                coreClockLabel.setText("Core Clock: " + coreClock + " MHz");
                memClockLabel.setText("Memory Clock: " + memoryClock + " MHz");
                powerUsageLabel.setText("Power Usage: " + powerLimit + "W");
                
            } catch (Exception e) {
                System.err.println("Error updating GPU info: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void setupDraggable(Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                SwingUtilities.convertPointToScreen(dragStart, component);
                dragStart.x -= getLocation().x;
                dragStart.y -= getLocation().y;
            }
        });
        
        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getPoint();
                SwingUtilities.convertPointToScreen(current, component);
                setLocation(current.x - dragStart.x, current.y - dragStart.y);
            }
        });
    }
    
    /**
     * Toggle between detailed and compact views
     */
    private void toggleDetailedView() {
        isDetailedView = !isDetailedView;
        
        if (isDetailedView) {
            detailsButton.setText("⋯");
            detailsButton.setToolTipText("Hide Details");
            setSize(WIDTH, FULL_HEIGHT);
        } else {
            detailsButton.setText("⋯");
            detailsButton.setToolTipText("Show Details");
            setSize(WIDTH, COMPACT_HEIGHT);
        }
        
        // Components after the basic stats are only visible in detailed view
        for (int i = 5; i < contentPanel.getComponentCount(); i++) {
            contentPanel.getComponent(i).setVisible(isDetailedView);
        }
        
        // Update window shape to match new size
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
    }
    
    /**
     * Toggle between minimized and normal states
     */
    private void toggleMinimize() {
        isMinimized = !isMinimized;
        
        if (isMinimized) {
            minimizeButton.setText("□");
            minimizeButton.setToolTipText("Restore");
            setSize(WIDTH, MINIMIZED_HEIGHT);
            
            // Hide all components except title bar
            for (Component comp : contentPanel.getComponents()) {
                comp.setVisible(false);
            }
        } else {
            minimizeButton.setText("−");
            minimizeButton.setToolTipText("Minimize");
            
            if (isDetailedView) {
                setSize(WIDTH, FULL_HEIGHT);
                
                // Show all components
                for (Component comp : contentPanel.getComponents()) {
                    comp.setVisible(true);
                }
            } else {
                setSize(WIDTH, COMPACT_HEIGHT);
                
                // Show only basic components
                for (int i = 0; i < contentPanel.getComponentCount(); i++) {
                    contentPanel.getComponent(i).setVisible(i < 5);
                }
            }
        }
        
        // Update window shape to match new size
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
    }
    
    /**
     * Add keyboard shortcuts for quick hide/show
     */
    private void addKeyboardShortcuts() {
        // Create a key stroke for Alt+G
        KeyStroke hideKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_DOWN_MASK);
        
        // Register an action to hide/show the overlay
        Action hideAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMinimize();
            }
        };
        
        // Add the action to the root pane
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(hideKeyStroke, "hideAction");
        getRootPane().getActionMap().put("hideAction", hideAction);
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateTimer.start();
        } else {
            updateTimer.stop();
        }
    }
    
    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
        
        // Clear all references to help garbage collection
        for (Component comp : contentPanel.getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).removeAll();
            }
        }
        contentPanel.removeAll();
        
        // Remove all listeners
        for (MouseListener ml : getMouseListeners()) {
            removeMouseListener(ml);
        }
        for (MouseMotionListener mml : getMouseMotionListeners()) {
            removeMouseMotionListener(mml);
        }
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }
        
        // Release GPU-related resources
        // Note: We don't shutdown the GPUSettings since other components might be using it
        // That should be handled at application shutdown
        
        System.out.println("Overlay resources cleaned up");
        super.dispose();
    }
}
