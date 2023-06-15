package org.example;

import java.io.*;
import java.util.Arrays;

import static org.example.HelpFunction.*;

public class Functional {

    public static final int UPLOAD = 127;
    public static final int GET_FILES = 111;
    public static final int DOWNLOAD = -128;
    public static final int OK = 200;
    public static final int OK_FILE_IS_EMPTY = 201;
    public static final int SERVER_ERROR = 300;
    public static final int FILE_IS_NOT_EXIST = 305;

    public static final int SIZE_BLOCK_CAMELLIA = 16;
    public static final int SIZE_BLOCK_READ = 2048;


    public static String getFileExtension(String fileName) {
        int index = fileName.indexOf('.');
        return index == -1 ? null : fileName.substring(index);
    }

    public static String createFileOnCompute(String fullFileName, String fileName) {
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

    public static long uploadFile(String fullFileName, long sizeFile, D_Encryption symmetricalAlgo, InputStream reader) throws IOException {
        long countByte = 0;
        int read;
        try (OutputStream writerToFile = new BufferedOutputStream(new FileOutputStream(fullFileName))) {
            byte[] encryptText = new byte[Functional.SIZE_BLOCK_READ];
            while (countByte < sizeFile) {
                if ((read = reader.read(encryptText)) == -1) {
                    Functional.deleteFile(fullFileName);
                    break;
                }
                countByte += read;
                if (countByte == sizeFile) {
                    for (int i = 0; i < read - Functional.SIZE_BLOCK_CAMELLIA; i += Functional.SIZE_BLOCK_CAMELLIA) {
                        writerToFile.write(symmetricalAlgo.decrypt(getArray128(encryptText, i)));
                    }
                    byte[] decryptText = deletePadding(symmetricalAlgo.decrypt(getArray128(encryptText, read - Functional.SIZE_BLOCK_CAMELLIA)));
                    writerToFile.write(decryptText);
                } else {
                    for (int i = 0; i < encryptText.length; i += Functional.SIZE_BLOCK_CAMELLIA) {
                        writerToFile.write(symmetricalAlgo.decrypt(getArray128(encryptText, i)));
                    }
                }
            }
        } catch (IOException ex) {
            throw ex;
        }
        return countByte;
    }


    public static long downloadFile(String fullFileName, D_Encryption symmetricalAlgo, OutputStream writer) throws IOException {
        byte[] data = new byte[Functional.SIZE_BLOCK_READ];
        long countRead = 0;
        int read;
        try (FileInputStream readerFromFile = new FileInputStream(fullFileName))
        {
            while ((read = readerFromFile.read(data)) != -1) {
                if (read < Functional.SIZE_BLOCK_READ)
                {
                    int fullBlock = (read / Functional.SIZE_BLOCK_CAMELLIA) * Functional.SIZE_BLOCK_CAMELLIA;
                    for (int i = 0; i < fullBlock; i += Functional.SIZE_BLOCK_CAMELLIA) {
                        byte[] encr = symmetricalAlgo.encrypt(getArray128(data, i));
                        System.arraycopy(encr, 0, data, i, Functional.SIZE_BLOCK_CAMELLIA);
                    }
                    byte[] newData = getArray128(data, fullBlock);
                    padding(newData, Functional.SIZE_BLOCK_CAMELLIA, read - fullBlock);
                    countRead += fullBlock + newData.length;
                    byte[] bla1 = symmetricalAlgo.encrypt(newData);
                    System.arraycopy(bla1, 0, data, fullBlock, Functional.SIZE_BLOCK_CAMELLIA);
                    data = Arrays.copyOfRange(data, 0, fullBlock + newData.length);
                    writer.write(data);
                    writer.flush();
                }
                else {
                    for (int i = 0; i < data.length; i += Functional.SIZE_BLOCK_CAMELLIA) {
                        byte[] bla = symmetricalAlgo.encrypt(getArray128(data, i));
                        System.arraycopy(bla, 0, data, i, Functional.SIZE_BLOCK_CAMELLIA);
                    }
                    writer.write(data);
                    writer.flush();
                    countRead += read;
                }
            }
        }
        catch(IOException ex){
            throw ex;
        }
        return countRead;
    }


}