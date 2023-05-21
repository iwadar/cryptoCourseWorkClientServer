package com.example.clientwithui;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    private Hyperlink hpDownload;
    @FXML
    private Hyperlink hpUpload;
    @FXML
    private Hyperlink hpReboot;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button btCancel;

    private Client client;


    @FXML
    void actionDownload() {

    }
    @FXML
    void actionUpload() {

    }
    @FXML
    void actionReboot() {

    }
    @FXML
    void actionCancel() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try (
                Socket clientSocket = new Socket("127.0.0.1", 8081)
        ) {
            Client c = new Client(clientSocket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}