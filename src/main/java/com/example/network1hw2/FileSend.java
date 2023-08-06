package com.example.network1hw2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import javax.swing.JFileChooser;

/**
 * @author Absalom Herrera
 */
public class FileSend {

//    void ready(int port, String host) {
//
//        System.out.println("Choosing file to send");
//        try {
//            DatagramSocket socket = new DatagramSocket();
//            InetAddress address = InetAddress.getByName(host);
//            String fileName;
//
//            JFileChooser jfc = new JFileChooser(); // Choosing the file to send
//            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY); // Only files can be choosed (not directories)
//            if (jfc.isMultiSelectionEnabled()) { // Only one file at a time (no multiple selection)
//                jfc.setMultiSelectionEnabled(false);
//            }
//
//            int r = jfc.showOpenDialog(null);
//            if (r == JFileChooser.APPROVE_OPTION) { // If a file is choosed
//                File file = jfc.getSelectedFile();
//                fileName = file.getName();
//                byte[] fileNameBytes = fileName.getBytes(); // File name as bytes to send it
//                DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, address, port); // File name packet
//                socket.send(fileStatPacket); // Sending the packet with the file name
//
//                byte[] fileByteArray = readFileToByteArray(file); // Array of bytes the file is made of
//                sendFile(socket, fileByteArray, address, port); // Entering the method to send the actual file
//            }
//            socket.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            System.exit(1);
//        }
//    }

    void sendFile(String host, int port, File file) {
        try {
            String fileName = file.getName();

            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);

            byte[] fileNameBytes = fileName.getBytes(); // File name as bytes to send it
            DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, address, port); // File name packet
            socket.send(fileStatPacket); // Sending the packet with the file name

            byte[] fileByteArray = readFileToByteArray(file); // Array of bytes the file is made of
            sendFile(socket, fileByteArray, address, port); // Entering the method to send the actual file


        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

    private void sendFile(DatagramSocket socket, byte[] fileByteArray, InetAddress address, int port) throws IOException {
        System.out.println("Sending file");
        int sequenceNumber = 0; // For order
        boolean endOfFile; // To see if we got to the end of the file
        int ackSequence = 0; // TODO : ack from the server => To see if the datagram was received correctly

        for (int i = 0; i < fileByteArray.length; i = i + 1021) {
            sequenceNumber += 1;

            // Create message

            byte[] message = new byte[1024]; // First two bytes of the data are for control (datagram integrity and order)
            // TODO: unsigned 16 bit for seq number , so this application can not send files for size more than 66.9 MB
            message[0] = (byte) (sequenceNumber >> 8); // TODO: why is there a shift by 8 ?
            message[1] = (byte) (sequenceNumber);

            if ((i + 1021) >= fileByteArray.length) { // Have we reached the end of file?
                endOfFile = true;
                message[2] = (byte) (1); // We reached the end of the file (last datagram to be send) (we are now sending the last packet)
            } else {
                endOfFile = false;
                message[2] = (byte) (0); // We haven't reached the end of the file, still sending datagrams
            }

            if (!endOfFile) {
                System.arraycopy(fileByteArray, i, message, 3, 1021);
            } else { // If it is the last datagram
                System.arraycopy(fileByteArray, i, message, 3, fileByteArray.length - i); // TODO: to get the rest of file, becouse the packet now is less than 1021 bit
            }

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port); // The data to be sent
            socket.send(sendPacket); // Sending the data
            System.out.println("Sent: Sequence number = " + sequenceNumber);

            boolean ackRec; // Was the datagram received?

            // TODO: Timer & seq number & resending packets
            while (true) {
                byte[] ack = new byte[2]; // Create another packet for datagram ackknowledgement
                DatagramPacket ackpack = new DatagramPacket(ack, ack.length);

                try {
                    socket.setSoTimeout(50); // TODO: Timer =>  Waiting for the server to send the ack
                    socket.receive(ackpack);
                    ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff); // Figuring the sequence number
                    ackRec = true; // We received the ack
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timed out waiting for ack");
                    ackRec = false; // We did not receive an ack
                }

                // If the package was received correctly next packet can be sent
                if ((ackSequence == sequenceNumber) && (ackRec)) {
                    System.out.println("Ack received: Sequence Number = " + ackSequence);
                    break;
                } // Package was not received, so we resend it
                else {
                    socket.send(sendPacket);
                    System.out.println("Resending: Sequence Number = " + sequenceNumber);
                }
            }
        }
    }

    static byte[] readFileToByteArray(File file) {
        FileInputStream fileInputStream = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
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

//    public static void main(String[] args) {
//        int port = 6789;
//        String host = "127.0.0.1"; // local host
//        FileSend fs = new FileSend();
//        fs.ready(port, host);
//    }
}