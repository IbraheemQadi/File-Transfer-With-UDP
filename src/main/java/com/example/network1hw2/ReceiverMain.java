package com.example.network1hw2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;


import javafx.application.Application;


public class ReceiverMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(ReceiverMain.class.getResource("receiver.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Receiver");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


}


