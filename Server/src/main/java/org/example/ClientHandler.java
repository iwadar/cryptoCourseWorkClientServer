package org.example;

import org.example.camellia.Camellia;
import org.example.camellia.CamelliaKey;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Optional;

public class ClientHandler implements Runnable{

    private final int UPLOAD = 127;

    private final int DOWNLOAD = -128;
    private final int LENGTH_FILE_NAME = 256;
    private final int FILE_EXIST = -157;
    private final int OK = 200;
    private final int SERVER_ERROR = -300;

    private final int SIZE_BLOCK_CAMELLIA = 128;

    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writeBigInteger;
    private ObjectInputStream readerBigInteger;
    private Camellia symmetricalAlgo;

    private int numberClient;
    public ClientHandler(Socket socket, int numberClient) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            this.writeBigInteger = new ObjectOutputStream(socket.getOutputStream());
            this.readerBigInteger = new ObjectInputStream(socket.getInputStream());
            this.numberClient = numberClient;
            // здесь можно сделать что необходимо в первую очередь
        } catch (IOException ex)
        {
            closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
        }
    }

    @Override
    public void run() {
        ElgamalKey k = new ElgamalKey();
        k.generateKey();
        ElgamalEncrypt elgamalCipher = new ElgamalEncrypt(k);
        String camelliaSymmetricalKeyString = "";
        try {
            BigInteger[] publicKey = {k.getPublicKey().getP(), k.getPublicKey().getG(), k.getPublicKey().getY()};
            writeBigInteger.writeObject(publicKey);
            writeBigInteger.flush();
            BigInteger[] encryptSymmetricalKey = (BigInteger[]) readerBigInteger.readObject();

            encryptSymmetricalKey = elgamalCipher.decrypt(encryptSymmetricalKey);

            for (var number: encryptSymmetricalKey) {
                camelliaSymmetricalKeyString += new String(number.toByteArray());
            }
        } catch (IOException | ClassNotFoundException ex) {
            closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
        }
        CamelliaKey camelliaKey = new CamelliaKey();
        camelliaKey.generateKeys(camelliaSymmetricalKeyString);
        symmetricalAlgo = new Camellia(camelliaKey);

        while (socket.isConnected()) {
            try {
                byte[] request = new byte[1];
                reader.read(request, 0, 1);
                if (request[0] == UPLOAD)
                {
                    int lengthFileName = reader.read();
                    request = new byte[lengthFileName];
                    System.out.println("Request : upload file to server");
                    reader.read(request, 0, lengthFileName);
                    int status;
                    if ((status = uploadFile(new String(request))) > 0){
                        System.out.println("Request status: 200 [OK]");
                    }
                    else {
                        System.out.println("Request status: " + status + " [NOT OK]");

                    }
                }
            } catch (IOException ex) {
                closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
                break;
            }
        }
    }

    private int uploadFile(String fileName) {
        String fullFileName = "/home/dasha/data/fileFromClients/" + this.numberClient + "/" + fileName;
        File file = new File(fullFileName);
        if (file.exists()) {
            return FILE_EXIST;
        }
        System.out.println(fullFileName);
        try(FileWriter writerToFile = new FileWriter(fullFileName, false))
        {
            byte[] encryptText = new byte[SIZE_BLOCK_CAMELLIA];
            int countByte = 0;
            int readByte;
            while ((readByte = reader.read(encryptText)) > 0) {
                writerToFile.write(new String(symmetricalAlgo.decrypt(encryptText)));
                countByte += readByte;
            }
            System.out.println("Read from client : " + countByte);
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return SERVER_ERROR;
        }
        return OK;
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
}
