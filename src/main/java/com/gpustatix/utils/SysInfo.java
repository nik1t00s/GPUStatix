package com.gpustatix.utils;

import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.*;

public class SysInfo {

    public static void displaySystemInfo() {
        Processor cpu = new Processor();
        RAM ram = new RAM();
        HardwareAbstractionLayer hal = SysHardware.getHal();
        List<GraphicsCard> graphicCards = hal.getGraphicsCards();
        GraphicsCard gpu = graphicCards.get(0);
        String[] VendorGPU = (gpu.getName().split(" "));
        System.out.println("\n" + cpu);
        System.out.println("\n" + gpu.getName());
        System.out.println("\n" + ram);
        GraphicsApp.launchGraphics();
    }
}

abstract class SysHardware {

    static SystemInfo si = new SystemInfo();
    static HardwareAbstractionLayer hal = si.getHardware();
    static Sensors sensor = hal.getSensors();

    public static HardwareAbstractionLayer getHal() {
        return hal;
    }
}

class Processor extends SysHardware {

    CentralProcessor cpu;

    public Processor() {
        this.cpu = hal.getProcessor();
    }

    public String getName() {
        return cpu.getProcessorIdentifier().getName().split(" CPU ")[0];
    }

    @Override
    public String toString() {
        return getName() +
                "   " + getLoad() +
                "   " + getFreq() +
                "   " + getTemp();
    }

    public String getTemp(){
        return Math.round(sensor.getCpuTemperature()) + " C";
    }

    public String getFreq() {
        return Math.round((float) cpu.getCurrentFreq()[cpu.getCurrentFreq().length - 1] / (10*10*10*10*10*10)) + "MHz";
    }

    public List<CentralProcessor.LogicalProcessor> getCores() {
        return cpu.getLogicalProcessors();
    }

    public int countCores() {
        return cpu.getLogicalProcessorCount();
    }

    public List<CentralProcessor.ProcessorCache> getCache() {
        return cpu.getProcessorCaches();
    }

    public String getLoad() {
        return Math.round(cpu.getProcessorCpuLoad(1000)[0]*100) + " %";
    }
}

class RAM extends SysHardware{
    GlobalMemory RAM;
    public RAM(){
        this.RAM = hal.getMemory();
    }

    @Override
    public String toString() {
        return "Total " + (RAM.getTotal() / 1000000) +
                " MB" + "   " +
                "Using " + ((RAM.getTotal() / 1000000) - RAM.getAvailable() / 1000000) + " MB";
    }
}

class GraphicsApp extends Application {
    @Override
    public void start(Stage stage) {
        // Создание осей для графика
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Value");

        // Создание линейного графика
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Framerate and Frametime Over Time");

        // Серия данных для framerate
        XYChart.Series<Number, Number> framerateSeries = new XYChart.Series<>();
        framerateSeries.setName("Framerate");

        // Серия данных для frametime
        XYChart.Series<Number, Number> frametimeSeries = new XYChart.Series<>();
        frametimeSeries.setName("Frametime");

        // Добавление данных в серии
        for (int time = 0; time < 100; time++) {
            double frameRate = 60 + Math.random() * 10;
            double frameTime = 16 + Math.random();

            framerateSeries.getData().add(new XYChart.Data<>(time, frameRate));
            frametimeSeries.getData().add(new XYChart.Data<>(time, frameTime));
        }

        // Добавление серий на график
        lineChart.getData().addAll(framerateSeries, frametimeSeries);

        // Создание сцены и добавление графика
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Performance Metrics");
        stage.show();
    }

    public static void launchGraphics() {
        launch();
    }
}