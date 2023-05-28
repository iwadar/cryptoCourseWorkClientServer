package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.camellia.*;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;
import org.example.mode.ModeCipher;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.HelpFunction.*;

public class Client {
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writerObject;
    private ObjectInputStream readerObject;
    private D_Encryption symmetricalAlgo;
    private ObjectMapper objectMapper;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            this.readerObject = new ObjectInputStream(socket.getInputStream());
            this.writerObject = new ObjectOutputStream(socket.getOutputStream());
            this.objectMapper = new ObjectMapper();

            BigInteger[] publicKey = (BigInteger[]) readerObject.readObject();
            // приняли публичный ключ, создали экземпляр ключа для шифрования, создали объект класса Эль Шамаля, чтобы шифрануть симметричный ключ
            String camelliaSecretKeyString = generateRandomString(32);
            String initializationVector = generateRandomString(Functional.SIZE_BLOCK_CAMELLIA);

            ElgamalKey elgamalPublicKey = new ElgamalKey(publicKey[0], publicKey[1], publicKey[2]);
            ElgamalEncrypt elgamalEncrypt = new ElgamalEncrypt(elgamalPublicKey);
            var decryptElgamalKey = elgamalEncrypt.encrypt(camelliaSecretKeyString.getBytes());
            writerObject.writeObject(decryptElgamalKey);
            writerObject.flush();

            writer.write(initializationVector.getBytes());
            writer.flush();
            symmetricalAlgo = new D_Encryption(new Camellia(camelliaSecretKeyString), ModeCipher.ECB, camelliaSecretKeyString);
        } catch (IOException | ClassNotFoundException ex)
        {
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
    }
    byte[] pojoToJsonString(Request request) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(request).getBytes();
    }

    private void sendStartInformation(String fullFileName, int state, long fileSize) {
        File file = new File(fullFileName);
        String fileName = file.getName();
        if (state == Functional.UPLOAD) {
            fileSize = file.length() + (Functional.SIZE_BLOCK_CAMELLIA - file.length() % Functional.SIZE_BLOCK_CAMELLIA);
        }
        Request request = new Request(state, fileName, fileSize);
        try {
            byte[] requestInBytes = pojoToJsonString(request);
            writer.write(requestInBytes.length);
            writer.write(requestInBytes);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendFile(String fullFileName) {
        try {
            if (socket.isConnected()) {
                sendStartInformation(fullFileName, Functional.UPLOAD, 0);
                long countRead = Functional.downloadFile(fullFileName, symmetricalAlgo, writer);
                System.out.println("[LOG] : SEND (bytes) : " + countRead);

                if (reader.read() == Functional.OK) {
                    System.out.println("File downloads");
                }
                else {
                    System.out.println("File DON'T downloads");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
    }

    public void downloadFile(String directoryToLoad, String fileName, long getFileSize) {
        String fullFileName = Functional.createFileOnCompute(directoryToLoad, fileName);
        if ("".equals(fullFileName)) {
            return;
        }
        System.out.println("[LOG] : CREATE NEW FILE { " + fullFileName + " }");
        try {
            sendStartInformation(fileName, Functional.DOWNLOAD, getFileSize);
            int sizeResponse = reader.read();
            byte[] responseByte = new byte[sizeResponse];
            reader.read(responseByte);
            Response response = objectMapper.readValue(responseByte, Response.class);
            if (response.getStatus() >= Functional.SERVER_ERROR) {
                System.out.println("Server error: " + response.getStatus() + " [NOT OK]");
                return;
            }
            long sizeFile = getFileSize + (Functional.SIZE_BLOCK_CAMELLIA - getFileSize % Functional.SIZE_BLOCK_CAMELLIA);
            long sizeUploadFiles = Functional.uploadFile(fullFileName, sizeFile, symmetricalAlgo, reader);
            System.out.println("Read from server : " + sizeUploadFiles);
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    public ConcurrentHashMap getListFile() {
        try {
            Request request = new Request(Functional.GET_FILES, "", 0);
            byte[] requestInBytes = pojoToJsonString(request);
            writer.write(requestInBytes.length);
            writer.write(requestInBytes);
            writer.flush();
            return (ConcurrentHashMap) readerObject.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
        return new ConcurrentHashMap();
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

    public static void main(String[] args) {
        try (
                Socket clientSocket = new Socket("127.0.0.1", 8080)
        ) {
            Client c = new Client(clientSocket);
            c.sendFile("/home/dasha/Pictures/face.jpg");
//            System.out.println("-----------------------------------------------");
////            Thread.sleep(6000);
//            c.getListFile().forEach((key, value) -> System.out.println(key + " " + value));
//            System.out.println("-----------------------------------------------");
//
//            c.sendFile("/home/dasha/data/fileFromClients/bla.txt");
//            System.out.println("-----------------------------------------------");
//            c.getListFile().forEach((key, value) -> System.out.println(key + " " + value));
            c.downloadFile("/home/dasha/data/fileFromServer/", "bla.txt", 10031);

//            c.downloadFile("bla.txt");
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

    }
}