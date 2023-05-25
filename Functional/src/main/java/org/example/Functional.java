package org.example;

import java.io.*;

public class Functional {

    public static final int UPLOAD = 127;
    public static final int GET_FILES = 111;
    public static final int DOWNLOAD = -128;
    public static final int OK = 200;
    public static final int SERVER_ERROR = 300;
    public static final int SIZE_BLOCK_CAMELLIA = 16;
    public static final int SIZE_BLOCK_READ = 2048;


    public static String getFileExtension(String fileName) {
        int index = fileName.indexOf('.');
        return index == -1 ? null : fileName.substring(index);
    }

    public static String createFileOnServer(String fileName) {
        String fullFileName = "/home/dasha/data/fileFromClients/";
        File file = new File(fullFileName + fileName);
        String fileNameWithoutExtension = fileName.replaceAll("\\.\\w+$", "");
        String extension = getFileExtension(fileName);
        int fileNo = 0;
        try {
            while (!file.createNewFile()) {
                fileNo++;
                file = new File(fullFileName + fileNameWithoutExtension + "(" + fileNo + ")" + extension);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
        return file.getPath();
    }

    public static void deleteFile(String fullFileName) {
        File file = new File(fullFileName);
        if (file.delete()) {
            System.out.println("[LOG] : " + fullFileName + " was deleted");
        } else System.out.println("[LOG] : " + fullFileName + " don't exist");
    }
}