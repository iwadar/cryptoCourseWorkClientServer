package org.example;

import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
//        try (Socket clientSocket = new Socket("127.0.0.1", 8080)) {
//            Client c = new Client(clientSocket);
//            System.out.println("main : " + (c.socket.isClosed() ? "close":"not close"));
            GUI app = new GUI();
//            app.setVisible(true);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }
}