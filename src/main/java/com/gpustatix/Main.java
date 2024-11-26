package com.gpustatix;

import com.gpustatix.ui.MonitoringOverlay;
import com.gpustatix.utils.SysInfo;
import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            MonitoringOverlay overlay = new MonitoringOverlay();
            overlay.setVisible(true);
        });
        for (int i = 0; i < 5; i++){
            System.out.println(SysInfo.displaySystemInfo());
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
