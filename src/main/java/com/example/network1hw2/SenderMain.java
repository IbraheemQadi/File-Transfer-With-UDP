package com.example.network1hw2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;


import javafx.application.Application;


public class SenderMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(SenderMain.class.getResource("sender.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Sender");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }


}


