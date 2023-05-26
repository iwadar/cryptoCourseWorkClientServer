package org.example;

import org.example.camellia.Camellia;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;
import org.example.mode.ModeCipher;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.HelpFunction.*;

public class ClientHandler implements Runnable{

    private String pathToStorage = "/home/dasha/data/fileFromClients/";
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writerObject;
    private ObjectInputStream readerObject;
    private D_Encryption symmetricalAlgo;
    private ConcurrentHashMap<String, Long> listFileWithSize;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Long> listFileWithSize) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            this.writerObject = new ObjectOutputStream(socket.getOutputStream());
            this.readerObject = new ObjectInputStream(socket.getInputStream());
            this.listFileWithSize = listFileWithSize;
        } catch (IOException ex)
        {
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
    }

    private String keyExchange() {
        ElgamalKey k = new ElgamalKey();
        k.generateKey();
        ElgamalEncrypt elgamalCipher = new ElgamalEncrypt(k);
        StringBuilder camelliaSymmetricalKeyString = new StringBuilder();
        try {
            BigInteger[] publicKey = {k.getPublicKey().getP(), k.getPublicKey().getG(), k.getPublicKey().getY()};
            writerObject.writeObject(publicKey);
            writerObject.flush();
            BigInteger[] encryptSymmetricalKey = (BigInteger[]) readerObject.readObject();

            encryptSymmetricalKey = elgamalCipher.decrypt(encryptSymmetricalKey);

            for (var number: encryptSymmetricalKey) {
                camelliaSymmetricalKeyString.append(new String(number.toByteArray()));
            }
        } catch (IOException | ClassNotFoundException ex) {
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
        return camelliaSymmetricalKeyString.toString();
    }

    @Override
    public void run() {
        String camelliaSymmetricalKeyString = keyExchange();
        // TODO: передавать вектор инициализации!!!!!!!!!!
        symmetricalAlgo = new D_Encryption(new Camellia(camelliaSymmetricalKeyString), ModeCipher.ECB, camelliaSymmetricalKeyString);
        while (socket.isConnected()) {
            try {
                byte[] request = new byte[1];
                reader.read(request, 0, 1);
                if (request[0] == Functional.UPLOAD)
                {
                    System.out.println("Request : upload file to server");
                    // получили размер имени, а потом имя файла
                    int lengthFileName = reader.read();
                    request = new byte[lengthFileName];
                    reader.read(request, 0, lengthFileName);

                    // получили размер файла
                    byte[] fileSizeBuf = new byte[8];
                    reader.read(fileSizeBuf, 0, 8);
                    long sizeFile = bytesToLong(fileSizeBuf);
                    int status;
                    if ((status = uploadFile(new String(request), sizeFile)) == Functional.OK){
                        System.out.println("Request status: " + status + " [OK]");
                        writer.write(Functional.OK);
                        writer.flush();
                    }
                    else {
                        System.out.println("Request status: " + status + " [NOT OK]");
                        writer.write(Functional.SERVER_ERROR);
                        writer.flush();
                    }
                } else if (request[0] == Functional.DOWNLOAD) {
                    System.out.println("Request : download file to client");
                    // получили размер имени, а потом имя файла
                    int lengthFileName = reader.read();
                    request = new byte[lengthFileName];
                    reader.read(request, 0, lengthFileName);
                    int status;
                    if ((status = downloadFile(new String(request))) == Functional.OK){
                        System.out.println("Request status: " + status + " [OK]");
                    }
                    else {
                        System.out.println("Request status: " + status + " [NOT OK]");
                        writer.write(status);
                        writer.flush();
                    }
                } else if (request[0] == Functional.GET_FILES) {
                    System.out.println("Request : get list of files");
                    sendListFiles();
                }
            } catch (IOException ex) {
                closeAll(socket, reader, writer, readerObject, writerObject);
                break;
            }
        }
    }

    private int uploadFile(String fileName, long sizeFile) {
        String fullFileName = Functional.createFileOnCompute(pathToStorage, fileName);
        if ("".equals(fullFileName)) {
            return Functional.SERVER_ERROR;
        }
        System.out.println("[LOG] : CREATE NEW FILE { " + fullFileName + " }");
        try {
            long sizeUploadFiles = Functional.uploadFile(fullFileName, sizeFile, symmetricalAlgo, reader);
            System.out.println("Read from client : " + sizeUploadFiles);
            listFileWithSize.put(fullFileName.substring(fullFileName.lastIndexOf('/') + 1, fullFileName.length() - 1), sizeFile);
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return Functional.SERVER_ERROR;
        }
        return Functional.OK;
    }

    private int downloadFile(String fileName) {
        File file = new File(pathToStorage + fileName);
        if (!file.exists()) {
            return Functional.FILE_IS_NOT_EXIST;
        }
        try {
            long countWrite = Functional.downloadFile(pathToStorage + fileName, symmetricalAlgo, writer);
            System.out.println("[LOG] : SEND (bytes) : " + countWrite);
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
            return Functional.SERVER_ERROR;
        }
        return Functional.OK;
    }

    private void sendListFiles() {
        try {
            writerObject.reset();
            listFileWithSize.forEach((key, value) -> System.out.println(key + " " + value));
            writerObject.writeObject(listFileWithSize);
            writerObject.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void closeAll(Socket socket, InputStream reader, OutputStream writer, ObjectInputStream readerObject, ObjectOutputStream writerObject) {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (readerObject != null) {
                readerObject.close();
            }
            if (writerObject != null) {
                writerObject.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}