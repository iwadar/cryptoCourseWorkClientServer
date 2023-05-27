package org.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

@RequiredArgsConstructor
public class ListenerActionDownload implements ActionListener {
    @NonNull
    String host;
    @NonNull
    int port;
    @NonNull
    Table table;
    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        int column = 0;
        int row = table.getSelectedRow();
        String fileName = table.getModel().getValueAt(row, column).toString();
        long sizeFile = Long.parseLong((table.getModel().getValueAt(row, column + 1).toString()));
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                table.startTask(host, port, "/home/dasha/data/fileFromServer/", fileName, sizeFile, row);
            }
        });
//        client.downloadFile("/home/dasha/data/fileFromServer/", fileName, sizeFile);

    }
}
