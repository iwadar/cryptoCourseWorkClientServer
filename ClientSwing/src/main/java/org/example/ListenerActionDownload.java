package org.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Choose the file, please!");
            return;
        }
        String fileName = table.getModel().getValueAt(row, column).toString();
        long sizeFile = Long.parseLong((table.getModel().getValueAt(row, column + 1).toString()));
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                table.startTaskDownload(host, port, "/home/dasha/data/fileFromServer/", fileName, sizeFile, row);
            }
        });
    }
}
