package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class GUI extends JFrame {

    private static final String host = "127.0.0.1";
    private static final int port = 8080;
    private String[] columnName = {"Name file", "Size file", "Progress"};
    private static Object[][] dataServerTable;
    private static Object[][] dataClientTable;
    private static JScrollPane scrollPaneServer;
    private static JButton downloadButton;
    private static JButton uploadButton;

    private static JButton rebootButton;
    private static JFrame frame;
    private static Table tableServer;
    private static Table tableClient;

    public GUI() {
        super("Storage mem about potatoes");
        Container container = this.getContentPane();
        container.setLayout(new GridLayout(2, 3, 2, 2));
        try (Socket clientSocket = new Socket(host, port)) {
            Client client = new Client(clientSocket);
            dataServerTable = getData(client);
            dataClientTable = new Object[][]{};
            createAndShowGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object[][] getData(Client client) {

        ConcurrentHashMap dataFile = client.getListFile();
        Object[][] dataServerTable = new Object[dataFile.size()][columnName.length];
        int i = 0;
        for (var key : dataFile.keySet()) {
            dataServerTable[i++] = new Object[]{key, dataFile.get(key), 0};
        }
        return dataServerTable;
    }

    public static void createAndShowGUI() {
        frame = new JFrame();
        frame.setSize(520, 540);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel fileFromServer = new JPanel();
        JPanel fileFromClient = new JPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("File from server", fileFromServer);
        tabbedPane.add("Upload file", fileFromClient);

        tableServer = new Table(dataServerTable);
        tableClient = new Table(dataClientTable);
        tableServer.makeUI(fileFromServer);
        tableClient.makeUI(fileFromClient);
        frame.add(tabbedPane);

        JPanel buttonGroup = new JPanel();
        downloadButton = new JButton("Download");
        uploadButton = new JButton("Upload");
        rebootButton = new JButton("Reboot storage");
        buttonGroup.add(downloadButton);
        buttonGroup.add(uploadButton);
        buttonGroup.add(rebootButton);
        buttonGroup.setBorder(new EmptyBorder(1, 1, 1, 1));

        downloadButton.addActionListener(new ListenerActionDownload(host, port, tableServer));

//        System.out.println("createAndShow : " + (client.socket.isClosed() ? "close":"not close"));
//        downloadButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                System.out.println("action : " + (client.socket.isClosed() ? "close":"not close"));
//            }
//
//        });


//        uploadButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                JFileChooser jFileChooser = new JFileChooser();
//                jFileChooser.setDialogTitle("Choose file for upload");
//                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                    listUpload.add(jFileChooser.getSelectedFile());
//                    System.out.println("[LOG] : choose file " + jFileChooser.getSelectedFile().getAbsoluteFile());
//                }
//            }
//        });
        frame.add(buttonGroup);
        frame.setVisible(true);
    }

}