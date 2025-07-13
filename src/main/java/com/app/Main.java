package com.app;

import com.view.MainView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainView view = new MainView(primaryStage);
        Scene scene = new Scene((Parent) view.getRoot(), 1000, 600); // Cast added here
        primaryStage.setTitle("Property File Comparator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
