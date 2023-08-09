package com.example.network1hw2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FileReceive {

    void receiveFile(int port, String fileDest) {
        System.out.println("Ready to receive!");
        createFile(port, fileDest); // Creating the file
    }

    private static void createFile(int port, String serverRoute) {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            // To receive the file name
            byte[] receiveFileName = new byte[1024];
            DatagramPacket receiveFileNamePacket = new DatagramPacket(receiveFileName, receiveFileName.length);
            socket.receive(receiveFileNamePacket);
            System.out.println("Receiving file name");
            byte[] data = receiveFileNamePacket.getData(); // Reading the name in bytes
            String fileName = new String(data, 0, receiveFileNamePacket.getLength()); //  name => string

            // Create the file and the output stream
            System.out.println("Creating file");
            File file = new File(serverRoute + "\\" + fileName);
            FileOutputStream outToFile = new FileOutputStream(file);

            receiveFile(outToFile, socket); // Receiving the file
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void receiveFile(FileOutputStream outToFile, DatagramSocket socket) throws IOException {
        System.out.println("Receiving file");
        boolean endOfFile; // Have we reached end of file
        int sequenceNumber = 0; // Order of sequences
        int foundLast = 0; // The last sequence found

        while (true) {
            byte[] message = new byte[1024]; // received datagram
            byte[] fileByteArray = new byte[1021]; //  data to be writen to the file

            // Receive packet and retrieve the data
            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            socket.receive(receivedPacket);
            message = receivedPacket.getData(); // Data to be written to the file

            // Port and IP for sender
            InetAddress address = receivedPacket.getAddress();
            int port = receivedPacket.getPort();

            // Retrieve sequence number
            sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
            // Check if we reached last datagram (end of file)
            endOfFile = (message[2] & 0xff) == 1;

            // If sequence number is the last seen + 1, then it is correct
            // We get the data from the message and write the ack that it has been received correctly
            if (sequenceNumber == (foundLast + 1)) {

                // set the last sequence number to be the one we just received
                foundLast = sequenceNumber;

                // Retrieve data from message
                System.arraycopy(message, 3, fileByteArray, 0, 1021);

                // Write the retrieved data to the file and print received data sequence number
                outToFile.write(fileByteArray);
                System.out.println("Received: Sequence number:" + foundLast);

                // Send acknowledgement
                sendAck(foundLast, socket, address, port);
            } else {
                System.out.println("Expected sequence number: " + (foundLast + 1) + " but received " + sequenceNumber + ". DISCARDING");
                // Re send the acknowledgement
                sendAck(foundLast, socket, address, port);
            }
            // Check for last datagram
            if (endOfFile) {
                outToFile.close();
                break;
            }

        }
    }

    private static void sendAck(int foundLast, DatagramSocket socket, InetAddress address, int port) throws IOException {
        // send acknowledgement
        byte[] ackPacket = new byte[2];
        ackPacket[0] = (byte) (foundLast >> 8);
        ackPacket[1] = (byte) (foundLast);
        // the datagram packet to be sent
        DatagramPacket acknowledgement = new DatagramPacket(ackPacket, ackPacket.length, address, port);
        socket.send(acknowledgement);
        System.out.println("Sent ack: Sequence Number = " + foundLast);
    }
}
