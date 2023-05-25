package org.example;

import org.example.camellia.*;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;
import org.example.mode.ECBMode;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.HelpFunction.*;

public class Client {
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writerObject;
    private ObjectInputStream readerObject;
    private Camellia symmetricalAlgo;
    private ECBMode symmetricalAlgoECB;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            // сгенерили ключ
            this.readerObject = new ObjectInputStream(socket.getInputStream());
            this.writerObject = new ObjectOutputStream(socket.getOutputStream());
            BigInteger[] publicKey = (BigInteger[]) readerObject.readObject();

            // приняли публичный ключ, создали экземпляр ключа для шифрования, создали объект класса Эль Шамаля, чтобы шифрануть симметричный ключ
            String camelliaSecretKeyString = generateRandomString(32);
            ElgamalKey elgamalPublicKey = new ElgamalKey(publicKey[0], publicKey[1], publicKey[2]);
            ElgamalEncrypt elgamalEncrypt = new ElgamalEncrypt(elgamalPublicKey);
            var decryptElgamalKey = elgamalEncrypt.encrypt(camelliaSecretKeyString.getBytes());
            writerObject.writeObject(decryptElgamalKey);
            writerObject.flush();
            CamelliaKey camelliaKey = new CamelliaKey();
            camelliaKey.generateKeys(camelliaSecretKeyString);
            symmetricalAlgo = new Camellia(camelliaKey);
            symmetricalAlgoECB = new ECBMode(symmetricalAlgo);
        } catch (IOException | ClassNotFoundException ex)
        {
            closeAll(socket, reader, writer, readerObject, writerObject);
        }
    }

    private void sendStartInformation(String fullFileName, int state) {
        File file = new File(fullFileName);
        String fileName = file.getName();
        byte[] fileNameInBytes = fileName.getBytes();
        try {
            writer.write(state);
            writer.write(fileNameInBytes.length);
            writer.write(fileNameInBytes);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        if (state == Functional.UPLOAD) {
            long fileSize = file.length() + (Functional.SIZE_BLOCK_CAMELLIA - file.length() % Functional.SIZE_BLOCK_CAMELLIA);
            try {
                writer.write(longToBytes(fileSize));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendFile(String fullFileName) {
        try {
            if (socket.isConnected()) {
                sendStartInformation(fullFileName, Functional.UPLOAD);
                byte[] data = new byte[Functional.SIZE_BLOCK_READ];
                int read, countRead = 0;
                try (FileInputStream readerFromFile = new FileInputStream(fullFileName))
                {
                    while ((read = readerFromFile.read(data)) != -1) {
                        if (read < Functional.SIZE_BLOCK_READ)
                        {
                            int fullBlock = (read / Functional.SIZE_BLOCK_CAMELLIA) * Functional.SIZE_BLOCK_CAMELLIA;
                            for (int i = 0; i < fullBlock; i += Functional.SIZE_BLOCK_CAMELLIA) {
                                System.arraycopy(symmetricalAlgoECB.encrypt(getArray128(data, i)), 0, data, i, Functional.SIZE_BLOCK_CAMELLIA);
                            }
                            byte[] newData = getArray128(data, fullBlock);
                            padding(newData, Functional.SIZE_BLOCK_CAMELLIA, read - fullBlock);
                            countRead += fullBlock + newData.length;
                            System.arraycopy(symmetricalAlgoECB.encrypt(newData), 0, data, fullBlock, Functional.SIZE_BLOCK_CAMELLIA);
                            writer.write(Arrays.copyOfRange(data, 0, fullBlock + newData.length));
                            writer.flush();
                        }
                        else {
                            for (int i = 0; i < data.length; i += Functional.SIZE_BLOCK_CAMELLIA) {
                                System.arraycopy(symmetricalAlgoECB.encrypt(getArray128(data, i)), 0, data, i, Functional.SIZE_BLOCK_CAMELLIA);
                            }
                            writer.write(data);
                            writer.flush();
                            countRead += read;
                        }
                    }
                    System.out.println("[LOG] : SEND (byte) : " + countRead);
                }
                catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
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

    public void downloadFile(String directoryToLoad, String fileName) {
        sendStartInformation(fileName, Functional.DOWNLOAD);
//        try {
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }

    public ConcurrentHashMap getListFile() {
        try {
            writer.write(Functional.GET_FILES);
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
                Socket clientSocket = new Socket("127.0.0.1", 8081)
        ) {
            Client c = new Client(clientSocket);
            c.sendFile("/home/dasha/Pictures/face.jpg");
//            System.out.println("-----------------------------------------------");
//            Thread.sleep(6000);
//            c.getListFile().forEach((key, value) -> System.out.println(key + " " + value));
//            System.out.println("-----------------------------------------------");

//            c.sendFile("/home/dasha/data/fileFromClients/bla.txt");
//            System.out.println("-----------------------------------------------");
//            c.getListFile().forEach((key, value) -> System.out.println(key + " " + value));
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