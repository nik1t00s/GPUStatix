package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DashboardUI extends JFrame {
    private final GPUSettings gpuSettings;
    private MonitoringOverlay overlay;
    private final ExecutorService executor;
    private final ExecutorService uiUpdateExecutor;
    private volatile boolean isRunning = true;
    private volatile int pollingIntervalMs = 1000; // Default 1 second
    private final JTabbedPane tabbedPane;

    public DashboardUI(GPUSettings gpuSettings) {
        this.gpuSettings = gpuSettings;

        setTitle("GPUStatix");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed to DISPOSE_ON_CLOSE to handle cleanup
        setSize(760, 620);
        setMinimumSize(new Dimension(650, 520));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setResizable(true);
        setLayout(new BorderLayout());
        
        // Add window listener for resource cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        executor = Executors.newSingleThreadExecutor();
        uiUpdateExecutor = Executors.newSingleThreadExecutor();

        // Верхняя панель с названием видеокарты
        JLabel gpuLabel = new JLabel("GPU: " + gpuSettings.getGpuName(), SwingConstants.CENTER);
        gpuLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        gpuLabel.setForeground(StudioTheme.TEXT);
        gpuLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 20, 0));
        add(gpuLabel, BorderLayout.NORTH);
        
        // Create tabbed pane for different sections
        tabbedPane = new JTabbedPane();

        // Settings panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.setBackground(StudioTheme.BACKGROUND);
        
        // Controls panel for GPU settings
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(5, 1, 12, 12));
        controlsPanel.setBackground(StudioTheme.BACKGROUND);

        // Добавляем поля для ввода значений и отображения текущих значений
        controlsPanel.add(createValueField("Core Clock", gpuSettings.getCoreClock(), 500, 2000));
        controlsPanel.add(createValueField("Memory Clock", gpuSettings.getMemoryClock(), 1000, 8000));
        controlsPanel.add(createValueField("Power Limit", gpuSettings.getPowerLimit(), 50, 215));
        controlsPanel.add(createValueField("Temp Limit", gpuSettings.getTempLimit(), 50, 100));
        controlsPanel.add(createValueField("Fan Speed", gpuSettings.getFanSpeed(), 0, 100));
        
        // Add polling interval control
        JPanel pollingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pollingPanel.setBackground(StudioTheme.BACKGROUND);
        JLabel pollingLabel = new JLabel("Polling Interval (ms): ");
        pollingLabel.setForeground(Color.WHITE);
        JTextField pollingField = new JTextField(String.valueOf(pollingIntervalMs), 5);
        JButton applyButton = new JButton("Apply");
        
        applyButton.addActionListener(e -> {
            try {
                int newInterval = Integer.parseInt(pollingField.getText());
                if (newInterval >= 100 && newInterval <= 10000) {
                    pollingIntervalMs = newInterval;
                } else {
                    JOptionPane.showMessageDialog(this, "Interval must be between 100 and 10000 ms");
                    pollingField.setText(String.valueOf(pollingIntervalMs));
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid interval. Please enter a number.");
                pollingField.setText(String.valueOf(pollingIntervalMs));
            }
        });
        
        pollingPanel.add(pollingLabel);
        pollingPanel.add(pollingField);
        pollingPanel.add(applyButton);
        
        settingsPanel.add(controlsPanel, BorderLayout.CENTER);
        settingsPanel.add(pollingPanel, BorderLayout.SOUTH);
        
        // Add prominent button to launch fan curve window
        JPanel fanButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fanButtonPanel.setBackground(new Color(20, 20, 30));
        fanButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        JButton openFanCurveButton = new JButton("🌡️ Fan Curve Editor");
        openFanCurveButton.setToolTipText("Open fan curve control window (Alt+F)");
        openFanCurveButton.setFocusPainted(false);
        openFanCurveButton.setBackground(new Color(60, 60, 100));
        openFanCurveButton.setForeground(Color.WHITE);
        openFanCurveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        openFanCurveButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 150), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Add hover effect
        openFanCurveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                openFanCurveButton.setBackground(new Color(80, 80, 120));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                openFanCurveButton.setBackground(new Color(60, 60, 100));
            }
        });
        
        // Add action with feedback
        openFanCurveButton.addActionListener(e -> {
            openFanCurveButton.setEnabled(false);
            openFanCurveButton.setText("Opening...");
            
            // Use SwingUtilities to ensure button is updated before opening window
            SwingUtilities.invokeLater(() -> {
                try {
                    openFanCurveWindow();
                } finally {
                    // Restore button state
                    openFanCurveButton.setEnabled(true);
                    openFanCurveButton.setText("🌡️ Fan Curve Editor");
                }
            });
        });
        
        // Add keyboard shortcut (Alt+F)
        KeyStroke fanCurveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(fanCurveKeyStroke, "openFanCurve");
        getRootPane().getActionMap().put("openFanCurve", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFanCurveWindow();
            }
        });
        
        fanButtonPanel.add(openFanCurveButton);
        settingsPanel.add(fanButtonPanel, BorderLayout.NORTH);
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Settings", settingsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);

        // Нижняя панель с кнопкой для отображения оверлея
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(StudioTheme.BACKGROUND);

        JButton toggleOverlayButton = new JButton("Toggle Overlay");
        toggleOverlayButton.addActionListener(e -> toggleOverlay());
        buttonPanel.add(toggleOverlayButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setBackground(StudioTheme.BACKGROUND);
        getContentPane().setBackground(StudioTheme.BACKGROUND);
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
        currentValueLabel.setForeground(StudioTheme.ACCENT);

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
                    updateGpuSetting(label, newValue); // Обновляем значение через GPUSettings
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number.");
            }
        });

        panel.setBackground(StudioTheme.BACKGROUND);
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(currentValueLabel, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.EAST);

        return panel;
    }


    private void updateGpuSetting(String label, int value) {
        switch (label) {
            case "Core Clock" -> gpuSettings.setCoreClock(value);
            case "Memory Clock" -> gpuSettings.setMemoryClock(value);
            case "Power Limit" -> gpuSettings.setPowerLimitNVML(value);
            case "Fan Speed" -> gpuSettings.setFanSpeed(value);
            case "Temp Limit" -> gpuSettings.setTempLimit(value);
            default -> System.err.println("Unsupported setting: " + label);
        }
    }


    private void toggleOverlay() {
        if (overlay == null || !overlay.isVisible()) {
            overlay = new MonitoringOverlay();
            overlay.setVisible(true);
        } else {
            overlay.dispose();
            overlay = null;
        }
    }
    
    // Fan curve window reference
    private FanCurveWindow fanCurveWindow;
    
    /**
     * Opens the fan curve window
     */
    private void openFanCurveWindow() {
        try {
            if (fanCurveWindow == null || !fanCurveWindow.isDisplayable()) {
                // Create new window
                fanCurveWindow = new FanCurveWindow(gpuSettings);
                
                // Position window relative to main window
                try {
                    Point location = getLocationOnScreen();
                    fanCurveWindow.setLocation(
                        location.x + getWidth() + 10, 
                        location.y
                    );
                } catch (Exception e) {
                    // If positioning fails, use default location
                    fanCurveWindow.setLocationRelativeTo(this);
                }
                
                // Set up window lifecycle
                fanCurveWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        System.out.println("Fan curve window closed");
                        fanCurveWindow = null;
                    }
                });
                
                // Show window and start monitoring
                fanCurveWindow.setVisible(true);
                fanCurveWindow.startMonitoring();
                System.out.println("Fan curve window opened");
            } else {
                // Bring window to front if already open
                fanCurveWindow.toFront();
                fanCurveWindow.requestFocus();
                System.out.println("Fan curve window activated");
            }
        } catch (Exception e) {
            System.err.println("Error opening fan curve window: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error opening fan curve window: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            
            // Ensure window reference is cleared
            fanCurveWindow = null;
        }
    }
    
    /**
     * Starts the UI update loop with the configured polling interval
     */
    public void startUiUpdates() {
        uiUpdateExecutor.submit(() -> {
            while (isRunning) {
                try {
                    // Update UI with latest GPU values
                    SwingUtilities.invokeLater(this::updateUiValues);
                    
                    // Sleep for the configured polling interval
                    Thread.sleep(pollingIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error updating UI: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Updates UI elements with the latest GPU values
     */
    private void updateUiValues() {
        // Find the current panels in the settings tab
        JPanel settingsPanel = (JPanel) tabbedPane.getComponentAt(0);
        JPanel controlsPanel = (JPanel) settingsPanel.getComponent(0);
        
        // Update all GPU setting fields with latest values
        for (Component comp : controlsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Component[] components = panel.getComponents();
                
                if (components.length >= 2 && components[0] instanceof JLabel && components[1] instanceof JLabel) {
                    JLabel nameLabel = (JLabel) components[0];
                    JLabel valueLabel = (JLabel) components[1];
                    
                    String label = nameLabel.getText().replace(": ", "");
                    int value = 0;
                    
                    switch (label) {
                        case "Core Clock" -> value = gpuSettings.getCoreClock();
                        case "Memory Clock" -> value = gpuSettings.getMemoryClock();
                        case "Power Limit" -> value = gpuSettings.getPowerLimit();
                        case "Fan Speed" -> value = gpuSettings.getFanSpeed();
                        case "Temp Limit" -> value = gpuSettings.getTempLimit();
                    }
                    
                    valueLabel.setText(String.valueOf(value));
                }
            }
        }
    }
    
    /**
     * Performs proper shutdown of resources
     */
    public void shutdown() {
        isRunning = false;
        
        // Close overlay if open
        if (overlay != null && overlay.isVisible()) {
            overlay.dispose();
            overlay = null;
        }
        
        // Close fan curve window if open
        if (fanCurveWindow != null && fanCurveWindow.isDisplayable()) {
            try {
                // Stop monitoring before disposing
                fanCurveWindow.stopMonitoring();
                fanCurveWindow.dispose();
                System.out.println("Fan curve window cleaned up");
            } catch (Exception e) {
                System.err.println("Error closing fan curve window: " + e.getMessage());
            } finally {
                fanCurveWindow = null;
            }
        }
        
        // Shutdown executors gracefully
        executor.shutdown();
        uiUpdateExecutor.shutdown();
        
        try {
            // Wait for tasks to complete
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!uiUpdateExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                uiUpdateExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            uiUpdateExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Dashboard UI resources cleaned up");
    }
}
