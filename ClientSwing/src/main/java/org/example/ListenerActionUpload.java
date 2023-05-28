package org.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
@RequiredArgsConstructor

public class ListenerActionUpload implements ActionListener {
    File fileName[] = new File[1];
    @NonNull
    String host;
    @NonNull
    int port;
    @NonNull
    Table table;
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setDialogTitle("Choose file for upload");
        if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileName[0] = jFileChooser.getSelectedFile();
            System.out.println("[LOG] : choose file " + fileName[0].getAbsoluteFile());
        }
        if (fileName[0] != null) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    table.startTaskUpload(host, port, fileName[0]);
                }
            });
        }
    }
}
