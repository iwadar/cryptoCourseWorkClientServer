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

public class ClientHandler implements Runnable{
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private ObjectOutputStream writerObject;
    private ObjectInputStream readerObject;
    private Camellia symmetricalAlgo;
    private ECBMode symmetricalAlgoECB;
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
        CamelliaKey camelliaKey = new CamelliaKey();
        camelliaKey.generateKeys(camelliaSymmetricalKeyString);
        symmetricalAlgo = new Camellia(camelliaKey);
        symmetricalAlgoECB = new ECBMode(symmetricalAlgo);

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
                    }
                    else {
                        System.out.println("Request status: " + status + " [NOT OK]");
                        writer.write(Functional.SERVER_ERROR);
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

//    private static String getFileExtension(String fileName) {
//        int index = fileName.indexOf('.');
//        return index == -1? null : fileName.substring(index);
//    }

//    private String createFileOnServer(String fileName) {
//        String fullFileName = "/home/dasha/data/fileFromClients/";
//        File file = new File(fullFileName + fileName);
//        String fileNameWithoutExtension = fileName.replaceAll("\\.\\w+$", "");
//        String extension = getFileExtension(fileName);
//        int fileNo = 0;
//        try {
//            while (!file.createNewFile()){
//                fileNo++;
//                file = new File(fullFileName + fileNameWithoutExtension + "("  + fileNo + ")" + extension);
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return "";
//        }
//        return file.getPath();
//    }

//    private void deleteFile(String fullFileName) {
//        File file = new File(fullFileName);
//        if(file.delete()){
//            System.out.println("[LOG] : " + fullFileName + " was deleted");
//        } else System.out.println("[LOG] : " + fullFileName + " don't exist");
//    }

    private int uploadFile(String fileName, long sizeFile) {
        String fullFileName = Functional.createFileOnServer(fileName);
        if ("".equals(fullFileName)) {
            return Functional.SERVER_ERROR;
        }
        System.out.println("[LOG] : CREATE NEW FILE { " + fullFileName + " }");
        try (OutputStream writerToFile = new BufferedOutputStream(new FileOutputStream(fullFileName)))
        {
            byte[] encryptText = new byte[Functional.SIZE_BLOCK_READ];
            int countByte = 0, read;
            while (countByte < sizeFile) {
                if ((read = reader.read(encryptText)) == -1) {
                    Functional.deleteFile(fullFileName);
                    break;
                }
                countByte += read;
                if (countByte == sizeFile) {
                    for (int i = 0; i < read - Functional.SIZE_BLOCK_CAMELLIA; i += Functional.SIZE_BLOCK_CAMELLIA) {
                        writerToFile.write(symmetricalAlgoECB.decrypt(getArray128(encryptText, i)));
                    }
                    byte[] decryptText = deletePadding(symmetricalAlgoECB.decrypt(getArray128(encryptText, read - Functional.SIZE_BLOCK_CAMELLIA)));
                    writerToFile.write(decryptText);
                }
                else {
                    for (int i = 0; i < encryptText.length; i += Functional.SIZE_BLOCK_CAMELLIA) {
                        writerToFile.write(symmetricalAlgoECB.decrypt(getArray128(encryptText, i)));
                    }
                }
            }
            System.out.println("Read from client : " + countByte);
            listFileWithSize.put(fullFileName.substring(fullFileName.lastIndexOf('/') + 1, fullFileName.length() - 1), sizeFile);
        }
        catch(IOException ex){
            ex.printStackTrace();
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