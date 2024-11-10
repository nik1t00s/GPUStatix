package com.gpustatix.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("GPUStatix Dashboard");

        VBox root = new VBox();

        Scene scene = new Scene(root, 800,600);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args){
        launch(args);
    }
}
