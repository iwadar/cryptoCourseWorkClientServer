package com.example.clientwithui;

import com.example.clientwithui.camellia.Camellia;
import com.example.clientwithui.camellia.CamelliaKey;
import com.example.clientwithui.elgamal.ElgamalEncrypt;
import com.example.clientwithui.elgamal.ElgamalKey;
import com.example.clientwithui.mode.ECBMode;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

import static com.example.clientwithui.HelpFunction.*;


public class Client {
    private final int UPLOAD = 127;
    private final int DOWNLOAD = -128;
    private final int LENGTH_FILE_NAME = 256;
    private final int FILE_EXIST = -157;
    private final int OK = 200;
    private final int SIZE_BLOCK_CAMELLIA = 16;
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writeBigInteger;
    private ObjectInputStream readerBigInteger;
    private Camellia symmetricalAlgo;
    private ECBMode symmetricalAlgoECB;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            // сгенерили ключ
            String camelliaSecretKeyString = generateRandomString(32);
            // здесь можно сделать что необходимо в первую очередь
            this.readerBigInteger = new ObjectInputStream(socket.getInputStream());
            this.writeBigInteger = new ObjectOutputStream(socket.getOutputStream());
            BigInteger[] publicKey = (BigInteger[]) readerBigInteger.readObject();

            // приняли публичный ключ, создали экземпляр ключа для шифрования, создали объект класса Эль Шамаля, чтобы шифрануть симметричный ключ
            ElgamalKey elgamalPublicKey = new ElgamalKey(publicKey[0], publicKey[1], publicKey[2]);
            ElgamalEncrypt elgamalEncrypt = new ElgamalEncrypt(elgamalPublicKey);
            var decryptElgamalKey = elgamalEncrypt.encrypt(camelliaSecretKeyString.getBytes());
            writeBigInteger.writeObject(decryptElgamalKey);
            writeBigInteger.flush();
            CamelliaKey camelliaKey = new CamelliaKey();
            camelliaKey.generateKeys(camelliaSecretKeyString);
            symmetricalAlgo = new Camellia(camelliaKey);
            symmetricalAlgoECB = new ECBMode(symmetricalAlgo);
        } catch (IOException | ClassNotFoundException ex)
        {
            closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
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
        if (state == UPLOAD) {
            long fileSize = file.length() + (SIZE_BLOCK_CAMELLIA - file.length() % SIZE_BLOCK_CAMELLIA);
            try {
                writer.write(longToBytes(fileSize));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendFile(String fullFileName) {
        try {
            while (socket.isConnected()) {
                sendStartInformation(fullFileName, UPLOAD);
                byte[] data = new byte[SIZE_BLOCK_CAMELLIA];
                int read, countRead = 0;
                try(FileInputStream readerFromFile = new FileInputStream(fullFileName))
                {
                    while ((read = readerFromFile.read(data)) != -1) {
                        if (read < SIZE_BLOCK_CAMELLIA)
                        {
                            padding(data, SIZE_BLOCK_CAMELLIA, read);
                        }
                        writer.write(symmetricalAlgoECB.encrypt(data));
                        countRead += SIZE_BLOCK_CAMELLIA;
                    }
                    System.out.println("[LOG] : SEND (byte) : " + countRead);
                    break;
                }
                catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
                writer.flush();
                break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
        }
    }

    public void downloadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    try {
                        byte[] buffer = reader.readAllBytes();
                    } catch (IOException e) {
                        e.printStackTrace();
                        closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
                    }
                }
            }
        }).start();
    }

    public void closeAll(Socket socket, InputStream reader, OutputStream writer, ObjectInputStream readerBigInteger, ObjectOutputStream writeBigInteger) {
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
            if (readerBigInteger != null) {
                readerBigInteger.close();
            }
            if (writeBigInteger != null) {
                writeBigInteger.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        try (
//                Socket clientSocket = new Socket("127.0.0.1", 8081)
//        ) {
//            Client c = new Client(clientSocket);
//            c.sendFile("/home/dasha/data/fileFromClients/bla.txt");
//
//        }catch (IOException ex)
//        {
//            ex.printStackTrace();
//        }
//
//    }
}