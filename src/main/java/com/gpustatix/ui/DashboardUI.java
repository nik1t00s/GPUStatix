package com.gpustatix.ui;

import com.gpustatix.utils.GPUSettings;

import javax.swing.*;
import java.awt.*;

public class DashboardUI extends JFrame {
    public DashboardUI(GPUSettings gpuSettings){
        setTitle("GPUStatix Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,600);
        setLayout(new BorderLayout());

        JLabel gpuLabel = new JLabel("GPU : ", SwingConstants.CENTER);
        gpuLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        gpuLabel.setHorizontalAlignment(JLabel.CENTER);
        add(gpuLabel, BorderLayout.NORTH);

        JPanel slidersPanel = new JPanel();
        slidersPanel.setLayout(new GridLayout(6,1));
        add(slidersPanel, BorderLayout.CENTER);

        //  здесь будут ползунки

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save Profile");
        JButton settingsButton = new JButton("Settings");
        JButton resetButton = new JButton("Reset");

        buttonPanel.add(saveButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(resetButton);

        add(buttonPanel,BorderLayout.SOUTH);

    }
    private JSlider createSlider(String label, int min, int max, int initial){
        JPanel panel = new JPanel(new BorderLayout());
        JLabel sliderLabel = new JLabel(label);
        sliderLabel.setHorizontalAlignment(JLabel.CENTER);
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        panel.add(sliderLabel, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        return slider;
    }

    public static void main(String[] args) {
        GPUSettings gpuSettings = new GPUSettings();
        DashboardUI dashboard = new DashboardUI(gpuSettings);
        dashboard.setVisible(true);
    }
}
