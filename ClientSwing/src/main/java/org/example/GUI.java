package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
    private static String[] columnName = {"Name file", "Size file", "Progress"};
    private static Object[][] dataServerTable;
    private static Object[][] dataClientTable;
    private static JScrollPane scrollPaneServer;
    private static JButton downloadButton;
    private static JButton uploadButton;
    private static JButton rebootButton;
    private static JButton infoButton;

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

    public static Object[][] getData(Client client) {

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
        rebootButton = new JButton("Refresh");
        infoButton = new JButton("Information");
        buttonGroup.add(downloadButton);
        buttonGroup.add(uploadButton);
        buttonGroup.add(rebootButton);
        buttonGroup.add(infoButton);
        buttonGroup.setBorder(new EmptyBorder(1, 1, 1, 1));

        downloadButton.addActionListener(new ListenerActionDownload(host, port, tableServer));
        uploadButton.addActionListener(new ListenerActionUpload(host, port, tableClient));
        rebootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try (Socket clientSocket = new Socket(host, port)) {
                    Client client = new Client(clientSocket);
                    dataServerTable = getData(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tableServer.updateTable(dataServerTable);
            }
        });

        infoButton.addActionListener(new ActionListener() {
            String msg = "This is an application for storing files on a remote server.\n" +
                    "\n" +
                    "To download a certain file, select the file and click \"Download\". Upon completion of the download, the file will be located in the \"User/Downloads/FileFromStorage\" folder.\n" +
                    "\n" +
                    "To upload a file to the server, click \"Upload\", select the file you want to upload.\n" +
                    "\n" +
                    "To view new files, press \"Refresh\".\n" +
                    "\n" +
                    "Author: Ivanchenko Daria\n" +
                    "Group: M8O-310B-20";
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(null, msg);
            }
        });

        frame.add(buttonGroup);
        frame.setVisible(true);
    }

}
