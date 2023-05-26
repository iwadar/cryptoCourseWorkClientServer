package com.example.clientwithui;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

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
    @FXML
    private TableView table;
    private TableColumn<Map.Entry<String, Long>, String> column1;
    private TableColumn<Map.Entry<String, Long>, Long> column2;
//    @FXML
//    private TableColumn tableNameFile;
//    @FXML
//    private TableColumn tableSizeFile;

    private Client client;

    @FXML
    void actionDownload() {

    }
    @FXML
    void actionUpload() {

    }
    @FXML
    void actionReboot() {
        setDataInTable();
    }
    @FXML
    void actionCancel() {

    }

    private void setDataInTable() {
        ConcurrentHashMap<String, Long> listFiles = client.getListFile();
        listFiles.forEach((key, value) -> System.out.println(key + " " + value));
        if (listFiles == null) {
            table.setPlaceholder(new Label("Files not uploaded yet"));
            return;
        }
        ObservableList<Map.Entry<String, Long>> items = FXCollections.observableArrayList(listFiles.entrySet());

        table.setItems(items);
        table.getColumns().setAll(column1, column2);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 8080);
            client = new Client(clientSocket);

            column1 = new TableColumn<>("File name");
            column1.setPrefWidth(710);
            column1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, Long>, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, Long>, String> p) {
                    // this callback returns property for just one cell, you can't use a loop here
                    // for first column we use key
                    return new SimpleObjectProperty<String>(p.getValue().getKey());
                }
            });

            column2 = new TableColumn<>("File size (bytes)");
            column2.setPrefWidth(120);
            column2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, Long>, Long>, ObservableValue<Long>>() {

                @Override
                public ObservableValue<Long> call(TableColumn.CellDataFeatures<Map.Entry<String, Long>, Long> p) {
                    // for second column we use value
                    return new SimpleObjectProperty<Long>(p.getValue().getValue());
                }
            });
            setDataInTable();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}