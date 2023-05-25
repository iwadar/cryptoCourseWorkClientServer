package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private ServerSocket serverSocket;
    private static ConcurrentHashMap<String, Long> listFileWithSize;

    public Server(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
        this.listFileWithSize = new ConcurrentHashMap<>();
    }

//    public static ConcurrentHashMap<String, Long> getListFileWithSize() {
//        return listFileWithSize;
//    }

//    public static void addToList(String fileName, Long size) {
//        listFileWithSize.put(fileName, size);
//    }
    public void startServer() {
        final File folder = new File("/home/dasha/data/fileFromClients");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                listFileWithSize.put(fileEntry.getName(), fileEntry.length());
            }
        }
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                ClientHandler clientHandler = new ClientHandler(socket, listFileWithSize);

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

        try (ServerSocket serverSocket = new ServerSocket(8081))
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
