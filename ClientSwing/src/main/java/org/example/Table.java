package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static org.example.HelpFunction.*;

public class Table {

    private String[] columnNames = {"Name file", "Size file (bytes)", "Progress"};
    private Object[][] data;
    private DefaultTableModel model;

    private JTable table;

    public JComponent makeUI(JPanel p) {
        TableColumn column = table.getColumnModel().getColumn(columnNames.length - 1);
        column.setCellRenderer(new ProgressRenderer());
        p.add(new JScrollPane(table));
        return p;
    }

    public Table(Object[][] data) {
        this.data = data;
        this.model = new DefaultTableModel(data, columnNames) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.table = new JTable(model);
        this.table.setSelectionBackground(Color.ORANGE);
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }
    public TableModel getModel() {
        return table.getModel();
    }

    public void updateTable(Object[][] newData) {
        this.model.setRowCount(0);
        this.data = newData;
        for (int i = 0; i < newData.length; i++) {
            this.model.addRow(newData[i]);
        }
        TableColumn column = this.table.getColumnModel().getColumn(columnNames.length - 1);
        column.setCellRenderer(new ProgressRenderer());
        table.revalidate();
    }

    public void startTaskDownload(String host, int port, String directoryToLoad, String fileName, long getFileSize, int row) {
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try (Socket clientSocket = new Socket(host, port)) {
                    Client client = new Client(clientSocket);
                    InputStream reader = client.getReader();
                    D_Encryption symmetricalAlgo = client.getSymmetricalAlgo();
                    ObjectMapper objectMapper = client.getObjectMapper();

                    String fullFileName = Functional.createFileOnCompute(directoryToLoad, fileName);
                    if ("".equals(fullFileName)) {
                        publish(-1);
                        return -1;
                    }
                    System.out.println("[LOG] : CREATE NEW FILE { " + fullFileName + " }");
                    client.sendStartInformation(fileName, Functional.DOWNLOAD, getFileSize);
                    int sizeResponse = reader.read();
                    System.out.println("sizeResponce:" + sizeResponse);
                    byte[] responseByte = new byte[sizeResponse];
                    reader.read(responseByte);
                    Response response = objectMapper.readValue(responseByte, Response.class);
                    if (response.getStatus() >= Functional.SERVER_ERROR) {
                        System.out.println("Server error: " + response.getStatus() + " [NOT OK]");
                        publish(-1);
                        JOptionPane.showMessageDialog(null, "Oooops, this file is not on the server or the server has crashed :'(" );
                        Functional.deleteFile(fullFileName);
                        return -1;
                    }
                    long sizeFile = getFileSize + (Functional.SIZE_BLOCK_CAMELLIA - getFileSize % Functional.SIZE_BLOCK_CAMELLIA);
                    System.out.println("getFileSize: " + getFileSize);
                    System.out.println("sizeFile " + sizeFile);
                    long countByte = 0;//, countWrite = 0, prev = 0;
                    int read = 0;
                    int totalBytesRead = 0;
                    try (OutputStream writerToFile = new BufferedOutputStream(new FileOutputStream(fullFileName))) {
                        byte[] encryptText = new byte[Functional.SIZE_BLOCK_READ];

                        while (countByte < sizeFile) {
                            while(totalBytesRead < Functional.SIZE_BLOCK_READ && countByte != sizeFile) {
                                read = reader.read(encryptText, totalBytesRead, Functional.SIZE_BLOCK_READ - totalBytesRead);
                                if (read == -1) {
                                    Functional.deleteFile(fullFileName);
                                    publish(-1);
                                    JOptionPane.showMessageDialog(null, "Oooops, the server has crashed :'(" );
                                    break;
                                }
                                totalBytesRead += read;
                                countByte += read;
                            }
//                            countByte += totalBytesRead;
//                            if ((read = reader.read(encryptText, 0, Functional.SIZE_BLOCK_READ)) == -1) {
//                                Functional.deleteFile(fullFileName);
//                                publish(-1);
//                                JOptionPane.showMessageDialog(null, "Oooops, the server has crashed :'(" );
//                                break;
//                            }
//                            if (totalBytesRead != 2048) {
//                                System.out.println("NOT 2048? else" + read);
//                            }
                            read = totalBytesRead;
                            totalBytesRead = 0;
                            publish((int) (countByte * 100 / sizeFile));
//                            countByte += read;
                            if (countByte == sizeFile) {
                                System.out.println("countByte = " + countByte);
                                System.out.println("read = "+ read);
                                for (int i = 0; i < read - Functional.SIZE_BLOCK_CAMELLIA; i += Functional.SIZE_BLOCK_CAMELLIA) {
                                    byte[] bla = symmetricalAlgo.decrypt(getArray128(encryptText, i));
//                                    countWrite += bla.length;
//                                    writerToFile.write(symmetricalAlgo.decrypt(getArray128(encryptText, i)));
                                    writerToFile.write(bla);
                                }
//                                System.out.println("before padding: " + (countWrite));
                                byte[] bla = symmetricalAlgo.decrypt(getArray128(encryptText, read - Functional.SIZE_BLOCK_CAMELLIA));
//                                byte[] decryptText = deletePadding(symmetricalAlgo.decrypt(getArray128(encryptText, read - Functional.SIZE_BLOCK_CAMELLIA)));
                                byte[] decryptText = deletePadding(bla);
                                countWrite += decryptText.length;
                                System.out.println("after padding: " + (countWrite - prev));
                                System.out.println("len decrypt text = " + decryptText.length);
                                writerToFile.write(decryptText);
                            } else {
                                for (int i = 0; i < encryptText.length; i += Functional.SIZE_BLOCK_CAMELLIA) {
                                    byte[] bla = symmetricalAlgo.decrypt(getArray128(encryptText, i));
                                    System.arraycopy(bla, 0, encryptText, i, Functional.SIZE_BLOCK_CAMELLIA);
//                                    writerToFile.write(bla);
                                    countWrite += bla.length;
                                }
                                writerToFile.write(encryptText);
                                writerToFile.flush();
                            }
//                            System.out.println("countByte = " + countByte + ": " + (((countWrite - prev) == 2048 )? "" : "ERRRRROR" + countWrite ));
                            prev = countWrite;
                        }
                    }
                    publish(100);
                    System.out.println("Write to file " + countWrite);
                    System.out.println("Read from server : " + countByte);
                } catch (IOException e) {
                    e.printStackTrace();
                    publish(-1);
                    return -1;
                }
                return 100;
            }
            @Override
            protected void process(java.util.List<Integer> chunks) {
                model.setValueAt(chunks.get(chunks.size() - 1), row, columnNames.length - 1);
            }
            @Override
            protected void done() {
                String text;
                int i = -1;
                if (isCancelled()) {
                    text = "Cancelled";
                } else {
                    try {
                        i = get();
                        text = (i >= 0) ? "Done" : "Disposed";
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                        text = ignore.getMessage();
                    }
                }
                System.out.println(row + ":" + text + "(" + i + "ms)");
            }
        };
        worker.execute();
    }

    public void startTaskUpload(String host, int port, File fileName) {
        model.addRow(new Object[]{fileName.getName(), fileName.length(), 0});
        final int row = model.getRowCount();
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try (Socket clientSocket = new Socket(host, port)) {
                    Client client = new Client(clientSocket);
                    OutputStream writer = client.getWriter();
                    InputStream reader = client.getReader();
                    D_Encryption symmetricalAlgo = client.getSymmetricalAlgo();

                    client.sendStartInformation(fileName.getAbsolutePath(), Functional.UPLOAD, 0);

                    byte[] data = new byte[Functional.SIZE_BLOCK_READ];
                    long countRead = 0, sizeFile = fileName.length();
                    int read;
                    try (FileInputStream readerFromFile = new FileInputStream(fileName.getAbsolutePath()))
                    {
                        while ((read = readerFromFile.read(data)) != -1) {
                            publish((int) (countRead * 100 / sizeFile));
                            if (read < Functional.SIZE_BLOCK_READ)
                            {
                                int fullBlock = (read / Functional.SIZE_BLOCK_CAMELLIA) * Functional.SIZE_BLOCK_CAMELLIA;
                                for (int i = 0; i < fullBlock; i += Functional.SIZE_BLOCK_CAMELLIA) {
                                    System.arraycopy(symmetricalAlgo.encrypt(getArray128(data, i)), 0, data, i, Functional.SIZE_BLOCK_CAMELLIA);
                                }
                                byte[] newData = getArray128(data, fullBlock);
                                padding(newData, Functional.SIZE_BLOCK_CAMELLIA, read - fullBlock);
                                countRead += fullBlock + newData.length;
                                System.arraycopy(symmetricalAlgo.encrypt(newData), 0, data, fullBlock, Functional.SIZE_BLOCK_CAMELLIA);
                                writer.write(Arrays.copyOfRange(data, 0, fullBlock + newData.length));
                                writer.flush();
                            }
                            else {
                                for (int i = 0; i < data.length; i += Functional.SIZE_BLOCK_CAMELLIA) {
                                    System.arraycopy(symmetricalAlgo.encrypt(getArray128(data, i)), 0, data, i, Functional.SIZE_BLOCK_CAMELLIA);
                                }
                                writer.write(data);
                                writer.flush();
                                countRead += read;
                            }
                        }
                    }
                    System.out.println("[LOG] : SEND (bytes) : " + countRead);
                    if (reader.read() == Functional.OK) {
                        System.out.println("File downloads");
                        publish(100);
                    }
                    else {
                        System.out.println("File DON'T downloads");
                        publish(-1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    publish(-1);
                    return -1;
                }
                return 100;
            }
            @Override
            protected void process(java.util.List<Integer> chunks) {
                model.setValueAt(chunks.get(chunks.size() - 1), row - 1, columnNames.length - 1);
            }
            @Override
            protected void done() {
                String text;
                int i = -1;
                if (isCancelled()) {
                    text = "Cancelled";
                } else {
                    try {
                        i = get();
                        text = (i >= 0) ? "Done" : "Disposed";
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                        text = ignore.getMessage();
                    }
                }
                System.out.println(row + ":" + text + "(" + i + "ms)");
            }
        };
        worker.execute();
    }
}

class ProgressRenderer extends DefaultTableCellRenderer {

    private final JProgressBar b = new JProgressBar(0, 100);

    public ProgressRenderer() {
        super();
        setOpaque(true);
        b.setForeground(Color.GREEN);
        b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Integer i = (Integer) value;
        String text = "Completed";
        if (i < 0) {
            text = "Error";
        } else if (i < 100) {
            b.setValue(i);
            return b;
        }
        super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
        return this;
    }

}
