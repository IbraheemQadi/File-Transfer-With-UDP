package com.example.network1hw2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;


public class SenderController {

    @FXML
    private Button btnChooseFile;

    @FXML
    private Button btnSendFile;

    @FXML
    private TextField filePath;

    @FXML
    private TextField serverIP;

    @FXML
    private TextField serverPort;

    @FXML
    private Label fileSize;

    @FXML
    private Label resendPackets;

    @FXML
    private Label sentPacktes;

    @FXML
    private Label fileReceived;


    //    --------------------------------------------------------------------
    File file;

    private boolean checkFields() {
        return !filePath.getText().isEmpty() && !serverIP.getText().isEmpty() && !serverPort.getText().isEmpty();
    }

    @FXML
    void onChooseFileClick(ActionEvent event) {

        JFileChooser jfc = new JFileChooser(); // Choosing the file to send
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY); // Only files can be choosed (not directories)
        if (jfc.isMultiSelectionEnabled()) { // Only one file at a time (no multiple selection)
            jfc.setMultiSelectionEnabled(false);
        }
        int r = jfc.showOpenDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) { // If a file is choosed
            file = jfc.getSelectedFile();
            filePath.setText(file.getName());
            fileSize.setText(" " + file.length() / 1000 + " KB");
            fileReceived.setText("No");
        }

        sentPacktes.setText("");
        resendPackets.setText("");
    }

    @FXML
    void onSendFileClick(ActionEvent event) {

        if (checkFields()) {
            try {
                String host = serverIP.getText();
                int port = Integer.parseInt(serverPort.getText());
                AtomicInteger countSend = new AtomicInteger();
                AtomicInteger countReSend = new AtomicInteger();
                AtomicInteger sendRatio = new AtomicInteger();

                Thread udpThread = new Thread(() -> {
                    FileSend fileSend = new FileSend();
                    fileSend.sendFile(host, port, file);
                    countSend.set(fileSend.countSendPackets);
                    countReSend.set(fileSend.countResendPackets);
                    sendRatio.set((fileSend.sendRatio));
                    fileSend = null;
                    System.gc();

                });

                udpThread.start();
                udpThread.join();
                if (sendRatio.get() == 100) {
                    sentPacktes.setText("" + countSend.get());
                    resendPackets.setText("" + countReSend.get());
                    fileReceived.setText("Yes");
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        } else {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Please enter valid inputs");
            alert.show();
        }


    }

}
