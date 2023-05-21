package org.example;

import org.example.camellia.*;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

import static org.example.HelpFunction.*;

public class Client {
    private final int UPLOAD = 127;
    private final int DOWNLOAD = -128;
    private final int LENGTH_FILE_NAME = 256;
    private final int FILE_EXIST = -157;
    private final int OK = 200;
    private final int SIZE_BLOCK_CAMELLIA = 128;
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writeBigInteger;
    private ObjectInputStream readerBigInteger;
    private Camellia symmetricalAlgo;

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
        } catch (IOException | ClassNotFoundException ex)
        {
            closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
        }
    }

    public void sendFile() {
        try {
            while (socket.isConnected()) {
                String fullFileName = "/home/dasha/data/bla";
                File file = new File(fullFileName);
                String fileName = file.getName();
                byte[] fileNameInBytes = fileName.getBytes();
                byte[] request = new byte[fileNameInBytes.length];
                System.arraycopy(fileNameInBytes, 0, request, 0, fileNameInBytes.length);
                writer.write(UPLOAD);
                writer.write(fileNameInBytes.length);
                writer.write(request);
                try(FileInputStream readerFromFile = new FileInputStream(fullFileName))
                {
                    byte[] data = new byte[SIZE_BLOCK_CAMELLIA];
                    int read;
                    int countRead = 0;
                    while ((read = readerFromFile.read(data)) != -1) {
                        byte[] encrypt = symmetricalAlgo.encrypt(data);
                        // И отправляем в сокет
                        writer.write(encrypt);
                        countRead += read;
                    }
                    System.out.println("Read : " + countRead);
                    break;
                }
                catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
                writer.flush();
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

    public static void main(String[] args) {
        try (
                Socket clientSocket = new Socket("127.0.0.1", 8080)
        ) {
            Client c = new Client(clientSocket);
            c.sendFile();

        }catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }
}