package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.camellia.Camellia;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;
import org.example.mode.ModeCipher;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable{

    private final String pathToStorage = "/home/dasha/data/fileFromClients/";
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writerObject;
    private ObjectInputStream readerObject;
    private D_Encryption symmetricalAlgo;
    private ObjectMapper objectMapper;
    private ConcurrentHashMap<String, Long> listFileWithSize;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Long> listFileWithSize) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            this.writerObject = new ObjectOutputStream(socket.getOutputStream());
            this.readerObject = new ObjectInputStream(socket.getInputStream());
            this.listFileWithSize = listFileWithSize;
            this.objectMapper = new ObjectMapper();
        } catch (IOException ex) {
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
    }

    private String[] keyExchange() {
        ElgamalKey k = new ElgamalKey();
        k.generateKey();
        ElgamalEncrypt elgamalCipher = new ElgamalEncrypt(k);
        StringBuilder camelliaSymmetricalKeyString = new StringBuilder();
        byte[] initVector = new byte[Functional.SIZE_BLOCK_CAMELLIA];
        try {
            BigInteger[] publicKey = {k.getPublicKey().getP(), k.getPublicKey().getG(), k.getPublicKey().getY()};
            writerObject.writeObject(publicKey);
            writerObject.flush();
            BigInteger[] encryptSymmetricalKey = (BigInteger[]) readerObject.readObject();

            reader.read(initVector);
            encryptSymmetricalKey = elgamalCipher.decrypt(encryptSymmetricalKey);

            for (var number: encryptSymmetricalKey) {
                camelliaSymmetricalKeyString.append(new String(number.toByteArray()));
            }
        } catch (IOException | ClassNotFoundException ex) {
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
        return new String[]{camelliaSymmetricalKeyString.toString(), new String(initVector)};
    }

    @Override
    public void run() {
        String[] cipherKeys = keyExchange();
        symmetricalAlgo = new D_Encryption(new Camellia(cipherKeys[0]), ModeCipher.CFB, cipherKeys[1]);
        while (socket.isConnected()) {
            try {
                int sizeRequest = reader.read();
                byte[] requestByte = new byte[sizeRequest];
                reader.read(requestByte);
                Request request = objectMapper.readValue(requestByte, Request.class);
                if (request.getRequestCode() == Functional.UPLOAD)
                {
                    System.out.println("Request : upload file to server");
                    int status = uploadFile(request.getFileName(), request.getSizeFileToSend());
                    System.out.println("Request status: " + status + ((status == Functional.OK) ? " [OK]" : " [NOT OK]"));
                    writer.write(status);
                    writer.flush();
                }
                else if (request.getRequestCode() == Functional.DOWNLOAD) {
                    System.out.println("Request : download file to client");
                    int status = downloadFile(request.getFileName(), request.getSizeFileToSend());
                    System.out.println("Request status: " + status + ((status == Functional.OK) ? " [OK]" : " [NOT OK]"));
                }
                else if (request.getRequestCode() == Functional.GET_FILES) {
                    System.out.println("Request : get list of files");
                    sendListFiles();
                }
            } catch (IOException | NegativeArraySizeException ex) {
                closeAll(socket, reader, writer, readerObject, writerObject);
                break;
            }
        }
        System.out.println("[LOG] : client has disconnected\n");
    }

    private int uploadFile(String fileName, long sizeFile) {
        String fullFileName = Functional.createFileOnCompute(pathToStorage, fileName);
        if ("".equals(fullFileName)) {
            return Functional.SERVER_ERROR;
        }
        System.out.println("[LOG] : CREATE NEW FILE { " + fullFileName + " }");
        try {
            if (sizeFile != 0) {
                long sizeUploadFiles = Functional.uploadFile(fullFileName, sizeFile, symmetricalAlgo, reader);
                System.out.println("Read from client : " + sizeUploadFiles);
            }
            listFileWithSize.put(fullFileName.substring(fullFileName.lastIndexOf('/') + 1), new File(fullFileName).length());
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return Functional.SERVER_ERROR;
        }
        return Functional.OK;
    }

    byte[] pojoToJsonString(Response response) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(response).getBytes();
    }

    private void sendStartInfo(int codeOperation, String fileName, long sizeFile, int status) {
        Response response = new Response(codeOperation, fileName, sizeFile, status);
        try {
            byte[] responseInBytes = pojoToJsonString(response);
            writer.write(responseInBytes.length);
            writer.write(responseInBytes);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private int downloadFile(String fileName, long sizeFile) {
        File file = new File(pathToStorage + fileName);
        if (!file.exists() || file.length() != sizeFile) {
            sendStartInfo(Functional.DOWNLOAD, fileName, sizeFile, Functional.FILE_IS_NOT_EXIST);
            return Functional.FILE_IS_NOT_EXIST;
        }
        try {
            sendStartInfo(Functional.DOWNLOAD, fileName, sizeFile, Functional.OK);
            if (sizeFile != 0) {
                long countWrite = Functional.downloadFile(pathToStorage + fileName, symmetricalAlgo, writer);
                System.out.println("[LOG] : SEND (bytes) : " + countWrite);
            }
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