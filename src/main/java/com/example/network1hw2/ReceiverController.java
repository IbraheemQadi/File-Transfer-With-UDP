package com.example.network1hw2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;


public class ReceiverController {

    @FXML
    private Button btnChooseDest;

    @FXML
    private Label serverWaiting;

    @FXML
    private Button btnRunServer;

    @FXML
    private TextField destination;

    @FXML
    private Label lab1;

    @FXML
    private TextField port;

    //    --------------------------------------------------------------------
    String dest;

    private boolean checkFields() {
        return !destination.getText().isEmpty() && !port.getText().isEmpty();
    }

    @FXML
    void onChooseDestClick(ActionEvent event) {

        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Only directories can be choosed
        if (jfc.isMultiSelectionEnabled()) { // Only one directory at a time (no multiple selection)
            jfc.setMultiSelectionEnabled(false);
        }
        int r = jfc.showOpenDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) { // If a directory is choosed
            File selectedDirectory = jfc.getSelectedFile();

            dest = selectedDirectory.getAbsolutePath();
            destination.setText(dest);
        }


    }

    @FXML
    void onRunServerClick(ActionEvent event) {
        if (checkFields()) {
            btnRunServer.setDisable(true);
            serverWaiting.setVisible(true);
            int serverPort = Integer.parseInt(port.getText());

            Thread udpThread = new Thread(() -> {
                FileReceive fileReceive = new FileReceive();
                fileReceive.receiveFile(serverPort, dest);
                fileReceive = null;
                System.gc();
                btnRunServer.setDisable(false);
                serverWaiting.setVisible(false);
            });

            udpThread.start();


        } else {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Please enter valid inputs");
            alert.show();
        }


    }

}


//public class ReceiverController {
//    String path;
//
//    @FXML
//    private TextField PortNumber;
//
//    @FXML
//    private Button RunServer;
//
//    @FXML
//    private TextField TextPath;
//
//    @FXML
//    private Button but_UPdate;
//
//    @FXML
//    private Label lab1;
//
//    @FXML
//    private Label lab2;
//
//    @FXML
//    private TextArea list;
//
//    @FXML
//    private TextField text2;
//
//    @FXML
//    void RUNSERVER(ActionEvent event) {
//        lab2.setVisible(true);
//        lab1.setVisible(true);
//        TextPath.setVisible(true);
//        list.setVisible(true);
//        text2.setVisible(true);
//        but_UPdate.setVisible(true);
//        PortNumber.setVisible(true);
//        RunServer.setVisible(false);
//
//    }
//
//
//
//
//    @FXML
//
//    void onHelloButtonClick(ActionEvent event) {
//
//
//
//        JFileChooser fileChooser = new JFileChooser();
//
//        fileChooser.setCurrentDirectory(new File(".")); //sets current directory
//
//        int response = fileChooser.showOpenDialog(null); //select file to open
//        //int response = fileChooser.showSaveDialog(null); //select file to save
//
//        if (response == JFileChooser.APPROVE_OPTION) {
//            File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
//            path = file.getPath() ;
//            TextPath.setText(path);
//
//
//    }
//} }