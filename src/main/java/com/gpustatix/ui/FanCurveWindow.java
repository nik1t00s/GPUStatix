package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Dedicated window for fan curve control with enhanced UI
 */
public class FanCurveWindow extends JFrame {
    private final FanCurveChart fanCurveChart;
    private final JLabel statusLabel = new JLabel(" ");
    private final GPUSettings gpuSettings;
    private File lastSaveLocation;
    private Timer statusTimer;
    private final String SETTINGS_FILE = "fan_curve_window.properties";
    private final Properties windowProps = new Properties();

    /**
     * Constructor
     * @param gpuSettings GPU settings to control fan speeds
     */
    public FanCurveWindow(GPUSettings gpuSettings) {
        this.gpuSettings = gpuSettings;
        
        // Window setup
        setTitle("GPU Fan Curve Control");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationByPlatform(true);
        
        // Create main panel with border padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(30, 30, 35));
        
        // Create fan curve chart
        fanCurveChart = new FanCurveChart(gpuSettings);
        fanCurveChart.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        
        // Create status bar
        JPanel statusPanel = createStatusPanel();
        
        // Add components to main panel
        mainPanel.add(fanCurveChart, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Add main panel and status bar to frame
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Create and set menu bar
        setJMenuBar(createMenuBar());
        
        // Load window position and size from properties
        loadWindowProperties();
        
        // Add window listeners
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowProperties();
                stopMonitoring();
            }
        });
        
        // Set up status message timer
        statusTimer = new Timer(5000, e -> clearStatus());
        statusTimer.setRepeats(false);
        
        // Display instructions
        showStatus("Double-click to add points, drag to move them, Delete key to remove selected point");
    }
    
    /**
     * Creates the control panel with preset buttons
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 10, 0));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.setBackground(new Color(30, 30, 35));
        
        // Create preset buttons
        JButton silentButton = createButton("Silent Preset", "Apply silent fan curve preset (lower speeds, quieter operation)");
        JButton balancedButton = createButton("Balanced Preset", "Apply balanced fan curve preset");
        JButton performanceButton = createButton("Performance Preset", "Apply performance fan curve preset (higher speeds, better cooling)");
        JButton applyButton = createButton("Apply Current Curve", "Apply the current fan curve");
        
        // Add action listeners
        silentButton.addActionListener(e -> {
            fanCurveChart.applySilentPreset();
            showStatus("Silent preset applied - fan noise will be minimized");
        });
        
        balancedButton.addActionListener(e -> {
            // Create and apply a balanced preset
            fanCurveChart.applyDefaultCurve();
            showStatus("Balanced preset applied - good combination of cooling and noise");
        });
        
        performanceButton.addActionListener(e -> {
            fanCurveChart.applyPerformancePreset();
            showStatus("Performance preset applied - maximum cooling prioritized");
        });
        
        applyButton.addActionListener(e -> {
            fanCurveChart.applyCurrentCurve();
            showStatus("Fan curve applied - custom settings now active");
        });
        
        panel.add(silentButton);
        panel.add(balancedButton);
        panel.add(performanceButton);
        panel.add(applyButton);
        
        return panel;
    }
    
    /**
     * Create a styled button with tooltip
     */
    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 60, 70));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return button;
    }
    
    /**
     * Creates the status panel at the bottom of the window
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(70, 70, 70)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panel.setBackground(new Color(40, 40, 45));
        
        // Set properties for the status label
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * Creates the menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem saveItem = new JMenuItem("Save Curve...", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveCurve());
        
        JMenuItem saveAsItem = new JMenuItem("Save Curve As...");
        saveAsItem.addActionListener(e -> saveCurveAs());
        
        JMenuItem loadItem = new JMenuItem("Load Curve...", KeyEvent.VK_L);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        loadItem.addActionListener(e -> loadCurve());
        
        JMenuItem exitItem = new JMenuItem("Close Window", KeyEvent.VK_X);
        exitItem.addActionListener(e -> dispose());
        
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Monitoring menu
        JMenu monitoringMenu = new JMenu("Monitoring");
        monitoringMenu.setMnemonic(KeyEvent.VK_M);
        
        JMenuItem startItem = new JMenuItem("Start Monitoring", KeyEvent.VK_S);
        startItem.addActionListener(e -> startMonitoring());
        
        JMenuItem stopItem = new JMenuItem("Stop Monitoring", KeyEvent.VK_T);
        stopItem.addActionListener(e -> stopMonitoring());
        
        monitoringMenu.add(startItem);
        monitoringMenu.add(stopItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem instructionsItem = new JMenuItem("Instructions", KeyEvent.VK_I);
        instructionsItem.addActionListener(e -> showInstructions());
        
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(instructionsItem);
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(monitoringMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Start monitoring GPU temperature and applying the fan curve
     */
    public void startMonitoring() {
        fanCurveChart.startMonitoring(1000);
        showStatus("Monitoring started - fan curve is now being actively applied");
    }
    
    /**
     * Stop monitoring GPU temperature
     */
    public void stopMonitoring() {
        fanCurveChart.stopMonitoring();
        showStatus("Monitoring stopped - fan control restored to default");
    }
    
    /**
     * Save the current fan curve to the last used location
     */
    private void saveCurve() {
        if (lastSaveLocation != null) {
            try {
                fanCurveChart.saveToFile(lastSaveLocation);
                showStatus("Fan curve saved to " + lastSaveLocation.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error saving fan curve: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            saveCurveAs();
        }
    }
    
    /**
     * Save the current fan curve to a user-selected location
     */
    private void saveCurveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Fan Curve");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Fan Curve Files (*.curve)", "curve"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Add .curve extension if not present
            if (!fileToSave.getName().toLowerCase().endsWith(".curve")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".curve");
            }
            
            try {
                fanCurveChart.saveToFile(fileToSave);
                lastSaveLocation = fileToSave;
                showStatus("Fan curve saved to " + fileToSave.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error saving fan curve: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Load a fan curve from a user-selected file
     */
    private void loadCurve() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Fan Curve");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Fan Curve Files (*.curve)", "curve"));
        
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            try {
                fanCurveChart.loadFromFile(fileToLoad);
                lastSaveLocation = fileToLoad;
                showStatus("Fan curve loaded from " + fileToLoad.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error loading fan curve: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Show instructions dialog
     */
    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
            "<html><body width='400'>" +
            "<h2>Fan Curve Editor Instructions</h2>" +
            "<p><b>Adding Points:</b> Double-click anywhere on the graph to add a new control point</p>" +
            "<p><b>Moving Points:</b> Click and drag any point to reposition it</p>" +
            "<p><b>Deleting Points:</b> Select a point and press the Delete key</p>" +
            "<p><b>Applying Changes:</b> Click 'Apply Current Curve' to activate your settings</p>" +
            "<p><b>Presets:</b> Use the preset buttons for quick configuration</p>" +
            "<p><b>Monitoring:</b> Start monitoring to automatically adjust fan speed based on temperature</p>" +
            "</body></html>",
            "Fan Curve Instructions",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show about dialog
     */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "<html><body width='350'>" +
            "<h2>GPU Fan Curve Control</h2>" +
            "<p>Version 1.0</p>" +
            "<p>A powerful tool for controlling GPU fan speeds based on temperature</p>" +
            "<p>Part of the GPUStatix application</p>" +
            "</body></html>",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Display a status message
     */
    public void showStatus(String message) {
        statusLabel.setText(message);
        
        // Reset the timer
        if (statusTimer.isRunning()) {
            statusTimer.stop();
        }
        statusTimer.start();
    }
    
    /**
     * Clear the status message
     */
    private void clearStatus() {
        statusLabel.setText(" ");
    }
    
    /**
     * Save window position and size to properties file
     */
    private void saveWindowProperties() {
        try {
            windowProps.setProperty("x", String.valueOf(getX()));
            windowProps.setProperty("y", String.valueOf(getY()));
            windowProps.setProperty("width", String.valueOf(getWidth()));
            windowProps.setProperty("height", String.valueOf(getHeight()));
            
            File propsFile = new File(SETTINGS_FILE);
            try (FileOutputStream out = new FileOutputStream(propsFile)) {
                windowProps.store(out, "Fan Curve Window Settings");
            }
        } catch (Exception e) {
            System.err.println("Error saving window properties: " + e.getMessage());
        }
    }
    
    /**
     * Load window position and size from properties file
     */
    private void loadWindowProperties() {
        try {
            File propsFile = new File(SETTINGS_FILE);
            if (propsFile.exists()) {
                try (FileInputStream in = new FileInputStream(propsFile)) {
                    windowProps.load(in);
                    
                    // Set position and size
                    int x = Integer.parseInt(windowProps.getProperty("x", "100"));
                    int y = Integer.parseInt(windowProps.getProperty("y", "100"));
                    int width = Integer.parseInt(windowProps.getProperty("width", "800"));
                    int height = Integer.parseInt(windowProps.getProperty("height", "600"));
                    
                    setBounds(x, y, width, height);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading window properties: " + e.getMessage());
            // Default size and center on screen
            setSize(800, 600);
            setLocationRelativeTo(null);
        }
    }
}

