package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Server {

    private ServerSocket serverSocket;
    private int countClient = 0;

    public Server(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                countClient++;
                System.out.println("A new client has connected! Number client: " + countClient);
                ClientHandler clientHandler = new ClientHandler(socket, countClient);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void closeServerSocket()
    {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(8080))
        {
            System.out.println("Server started!");
            Server server = new Server(serverSocket);
            server.startServer();
//            while (true) {
//                try {
//                    Socket socket = serverSocket.accept();
//                    BufferedWriter writer = new BufferedWriter(
//                            new OutputStreamWriter(
//                                    socket.getOutputStream()));
//                    BufferedReader reader = new BufferedReader(
//                            new InputStreamReader(
//                                    socket.getInputStream()));
//                    new Thread(() -> {
//                        System.out.println("Client connected!");
//                        try {
//                            String response = Optional.ofNullable(reader.readLine()).orElse("");
//                            System.out.println("Request: " + response);
//                            writer.write("Hello from KOROL AND SHUT! PANKI HOOOOOOOOOOOOOOOY!" + response);
//                            writer.newLine();
//                            writer.flush();
//                            socket.close();
//                            writer.close();
//                            reader.close();
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    }).start();
//
//                } catch (Exception ex)
//                {
//                    ex.printStackTrace();
//                }
//            }
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);

        }
    }
}
