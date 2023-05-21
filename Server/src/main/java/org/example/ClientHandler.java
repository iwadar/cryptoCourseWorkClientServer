package org.example;

import org.example.camellia.Camellia;
import org.example.camellia.CamelliaKey;
import org.example.elgamal.ElgamalEncrypt;
import org.example.elgamal.ElgamalKey;
import org.example.mode.ECBMode;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.HelpFunction.*;
import static org.example.Server.addToList;
import static org.example.Server.getListFileWithSize;

public class ClientHandler implements Runnable{

    private final int UPLOAD = 127;
    private final int GET_FILES = 111;
    private final int DOWNLOAD = -128;
    private final int LENGTH_FILE_NAME = 256;
    private final int FILE_EXIST = -157;
    private final int OK = 200;
    private final int SERVER_ERROR = -300;

    private final int SIZE_BLOCK_CAMELLIA = 16;

    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writeBigInteger;
    private ObjectInputStream readerBigInteger;
    private Camellia symmetricalAlgo;
    private ECBMode symmetricalAlgoECB;

    private int numberClient;
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.writer = socket.getOutputStream();
            this.reader = socket.getInputStream();
            this.writeBigInteger = new ObjectOutputStream(socket.getOutputStream());
            this.readerBigInteger = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex)
        {
            closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
        }
    }

    private String keyExchange() {
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
        return camelliaSymmetricalKeyString;
    }

    @Override
    public void run() {
        String camelliaSymmetricalKeyString = keyExchange();
        CamelliaKey camelliaKey = new CamelliaKey();
        camelliaKey.generateKeys(camelliaSymmetricalKeyString);
        symmetricalAlgo = new Camellia(camelliaKey);
        symmetricalAlgoECB = new ECBMode(symmetricalAlgo);

        while (socket.isConnected()) {
            try {
                byte[] request = new byte[1];
                reader.read(request, 0, 1);
                if (request[0] == UPLOAD)
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
                    if ((status = uploadFile(new String(request), sizeFile)) > 0){
                        System.out.println("Request status: " + status + " [OK]");
                    }
                    else {
                        System.out.println("Request status: " + status + " [NOT OK]");
                    }
                } else if (request[0] == GET_FILES) {
                    sendListFiles();
                }
            } catch (IOException ex) {
                closeAll(socket, reader, writer, readerBigInteger, writeBigInteger);
                break;
            }
        }
    }

    private static String getFileExtension(String fileName) {
        int index = fileName.indexOf('.');
        return index == -1? null : fileName.substring(index);
    }

    private String createFileOnServer(String fileName) {
        String fullFileName = "/home/dasha/data/fileFromClients/";
        File file = new File(fullFileName + fileName);
        String fileNameWithoutExtension = fileName.replaceAll("\\.\\w+$", "");
        String extension = getFileExtension(fileName);
        int fileNo = 0;
        try {
            if (file.exists()) {
                while(file.exists()){
                    fileNo++;
                    file = new File(fullFileName + fileNameWithoutExtension + "("  + fileNo + ")" + extension);
                }
            } else {
                file.createNewFile();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
        return file.getPath();
    }

    private void deleteFile(String fullFileName) {
        File file = new File(fullFileName);
        if(file.delete()){
            System.out.println("[LOG] : " + fullFileName + " was deleted");
        } else System.out.println("[LOG] : " + fullFileName + " don't exist");
    }

    private int uploadFile(String fileName, long sizeFile) {
        String fullFileName = createFileOnServer(fileName);
        if ("".equals(fullFileName)) {
            return SERVER_ERROR;
        }
        System.out.println("[LOG] : CREATE E NEW FILE { " + fullFileName + " }");
        try(FileWriter writerToFile = new FileWriter(fullFileName, false))
        {
            byte[] encryptText = new byte[SIZE_BLOCK_CAMELLIA];
            int countByte = 0, read;
            while (countByte < sizeFile) {
                 if ((read = reader.read(encryptText)) == -1) {
                    deleteFile(fullFileName);
                    break;
                 }
                countByte += read;
                if (countByte == sizeFile) {
                    byte[] decryptText = deletePadding(symmetricalAlgoECB.decrypt(encryptText));
                    writerToFile.write(new String(decryptText));
                }
                else {
                    writerToFile.write(new String(symmetricalAlgoECB.decrypt(encryptText)));
                }
            }
            System.out.println("Read from client : " + countByte);
            addToList(fullFileName.substring(fullFileName.lastIndexOf('/') + 1, fullFileName.length() - 1), (long) sizeFile);
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return SERVER_ERROR;
        }
        return OK;
    }

    private void sendListFiles() {
        try {
            final OutputStream yourOutputStream = socket.getOutputStream(); // OutputStream where to send the map in case of network you get it from the Socket instance.
            final ObjectOutputStream mapOutputStream = new ObjectOutputStream(yourOutputStream);
            ConcurrentHashMap<String, Long> listFile = getListFileWithSize();
            mapOutputStream.writeObject(listFile);
            mapOutputStream.flush();
            mapOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
