package com.example.network1hw2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSend {

    int countSendPackets = 0;
    int countResendPackets = 0;
    double bytesSend = 0;
    double fileSize = 0;
    int sendRatio = 0;

    void sendFile(String host, int port, File file) {
        try {
            String fileName = file.getName();

            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);

            byte[] fileNameBytes = fileName.getBytes(); // File name => bytes
            DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, address, port); // File name packet
            socket.send(fileStatPacket); // Sending  the file name

            fileSize = file.length();

            byte[] fileByteArray = readFileToByteArray(file); // File => array of bytes
            sendFile(socket, fileByteArray, address, port);


        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

    private void sendFile(DatagramSocket socket, byte[] fileByteArray, InetAddress address, int port) throws IOException {
        System.out.println("Sending file");
        int sequenceNumber = 0;
        boolean endOfFile;
        int ackSequence = 0; // TODO : ack from the server => To see if the datagram was received correctly

        for (int i = 0; i < fileByteArray.length; i = i + 1021) {
            sequenceNumber += 1;
            countSendPackets += 1;

            // Create message
            byte[] message = new byte[1024]; // First two bytes of the data are for control (datagram integrity and order)
            // TODO: unsigned 16 bit for seq number , so this application can not send files for size more than 66.9 MB
            message[0] = (byte) (sequenceNumber >> 8);
            message[1] = (byte) (sequenceNumber);

            if ((i + 1021) >= fileByteArray.length) { // Have we reached the end of file?
                endOfFile = true;
                message[2] = (byte) (1); // We reached the end of the file (we are now sending the last packet)
            } else {
                endOfFile = false;
                message[2] = (byte) (0); // We haven't reached the end of the file, still sending datagrams
            }

            if (!endOfFile) {
                System.arraycopy(fileByteArray, i, message, 3, 1021);
                bytesSend += 1021;
            } else { // If it is the last datagram
                System.arraycopy(fileByteArray, i, message, 3, fileByteArray.length - i); // TODO: to get the rest of file, becouse the packet now is less than 1021 bit
                bytesSend += fileByteArray.length - i;
            }

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port); // The data to be sent
            socket.send(sendPacket); // Sending the data
            System.out.println("Sent: Sequence number = " + sequenceNumber);
            sendRatio = (int) ((bytesSend / fileSize) * 100);
            System.out.println("Send :" + sendRatio + "%");

            boolean ackRec; // Was the packet received?

            // TODO: Timer & seq number & resending packets
            while (true) {
                byte[] ack = new byte[2]; // Create another packet for datagram ackknowledgement
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try {
                    socket.setSoTimeout(100); // TODO: Timer =>  Waiting for the server to send the ack
                    socket.receive(ackpack);
                    ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // sequence number
                    ackRec = true; // We received the ack
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timed out waiting for ack");
                    ackRec = false; // We did not receive an ack
                }

                // If the package was received correctly next packet can be sent
                if ((ackSequence == sequenceNumber) && (ackRec)) {
                    System.out.println("Ack received: Sequence Number = " + ackSequence);
                    break;
                } // Packet was not received, so we resend it
                else {
                    countResendPackets += 1;
                    socket.send(sendPacket);
                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
                }
            }
        }
    }

    byte[] readFileToByteArray(File file) {
        // convert the file to byte array using the input stream.
        FileInputStream fileInputStream = null;
        fileSize = file.length();
        System.out.println("file size : " + (int) file.length() / 1000 + " KB");
        byte[] bArray = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bArray);
            fileInputStream.close();

        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
        return bArray;
    }

}