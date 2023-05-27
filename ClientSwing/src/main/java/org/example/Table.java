package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class Table {

    private String[] columnNames = {"Name file", "Size file", "Progress"};
    private Object[][] data = {};
    private DefaultTableModel model;

    private JTable table;

    public JComponent makeUI(JPanel p) {
        TableColumn column = table.getColumnModel().getColumn(columnNames.length - 1);
        column.setCellRenderer(new ProgressRenderer());
        p.add(new JScrollPane(table));
        return p;
    }

    public void startTask(String host, int port, String directory, String fileName, long sizeFile, int row) {
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try (Socket clientSocket = new Socket(host, port)){
                    Client client = new Client(clientSocket);
                    client.downloadFile(directory, fileName, sizeFile);
                    publish(100);
                    return 100;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return -1;
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


//    public void startTask(String str) {
//        final int key = model.getRowCount();
//        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
//            private int sleepDummy = new Random().nextInt(100) + 1;
//            private int lengthOfTask = 100;

//            @Override
//            protected Integer doInBackground() {

//                int current = 0;
//                while (current < lengthOfTask && !isCancelled()) {
//                    if (!table.isDisplayable()) {
//                        break;
//                    }
//                    if (key == 2 && current > 60) { //Error Test
//                        cancel(true);
//                        publish(-1);
//                        return -1;
//                    }
//                    current++;
//                    try {
//                        Thread.sleep(sleepDummy);
//                    } catch (InterruptedException ie) {
//                        break;
//                    }
//                    publish(100 * current / lengthOfTask);
//                }
//                return sleepDummy * lengthOfTask;
//            }
//
//            @Override
//            protected void process(java.util.List<Integer> c) {
////                model.setValueAt(c.get(c.size() - 1), key, columnNames.length - 1);
//            }
//
//            @Override
//            protected void done() {
//                String text;
//                int i = -1;
//                if (isCancelled()) {
//                    text = "Cancelled";
//                } else {
//                    try {
//                        i = get();
//                        text = (i >= 0) ? "Done" : "Disposed";
//                    } catch (Exception ignore) {
//                        ignore.printStackTrace();
//                        text = ignore.getMessage();
//                    }
//                }
//                System.out.println(key + ":" + text + "(" + i + "ms)");
//            }
//        };
//        model.addRow(new Object[]{str, 0});
//        worker.execute();
//    }

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
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }
    public TableModel getModel() {
        return table.getModel();
    }
}


class ProgressRenderer extends DefaultTableCellRenderer {

    private final JProgressBar b = new JProgressBar(0, 100);

    public ProgressRenderer() {
        super();
        setOpaque(true);
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
