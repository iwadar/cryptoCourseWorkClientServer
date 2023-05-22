package com.example.clientwithui.mode;

import com.example.clientwithui.camellia.Camellia;
import static com.example.clientwithui.HelpFunction.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ECBMode implements IModeCipher
{
    private Camellia symmetricalAlgorithm;
    private int processors;

    public ECBMode(Camellia symmetricalAlgorithm)
    {
        this.symmetricalAlgorithm = symmetricalAlgorithm;
        this.processors = Runtime.getRuntime().availableProcessors();
    }

    @Override
    public byte[] encrypt(byte[] notCipherText)
    {
        ExecutorService service = Executors.newFixedThreadPool(processors);
        List<Future<byte[]>> encryptedBlocksFutures = new LinkedList<>();

//        byte[] copyInputArrayWithPadding = padding(notCipherText, 16);
//
//        for (int i = 0; i < copyInputArrayWithPadding.length; i += 16)
//        {
//            byte[] block = getArray128(copyInputArrayWithPadding, i);
//            encryptedBlocksFutures.add(service.submit(() -> symmetricalAlgorithm.encrypt(block)));
//        }
//        service.shutdown();
//        return getArrayFromExecutors(encryptedBlocksFutures, copyInputArrayWithPadding.length);

        for (int i = 0; i < notCipherText.length; i += 16)
        {
            byte[] block = getArray128(notCipherText, i);
            encryptedBlocksFutures.add(service.submit(() -> symmetricalAlgorithm.encrypt(block)));
        }
        service.shutdown();
        return getArrayFromExecutors(encryptedBlocksFutures, notCipherText.length);

    }

    @Override
    public byte[] decrypt(byte[] cipherText)
    {
        ExecutorService service = Executors.newFixedThreadPool(processors);
        List<Future<byte[]>> encryptedBlocksFutures = new LinkedList<>();
        for (int i = 0; i <  cipherText.length; i += 16){
            byte[] block = getArray128(cipherText, i);
            encryptedBlocksFutures.add(service.submit(() -> symmetricalAlgorithm.decrypt(block)));
        }
        service.shutdown();
        cipherText = getArrayFromExecutors(encryptedBlocksFutures, cipherText.length);
//        cipherText = deletePadding(cipherText);
        return cipherText;
    }
    private static byte[] getArrayFromExecutors(List<Future<byte[]>> encryptedBlocksFutures, int lengthOfText)
    {
        byte[] result = new byte[lengthOfText];
        int i = 0;
        try
        {
            for (var futureBufToWrite : encryptedBlocksFutures)
            {
                byte[] encrypted = futureBufToWrite.get();
                System.arraycopy(encrypted, 0, result, i, 16);
                i += 16;
            }
        }
        catch (ExecutionException | InterruptedException e)
        {
            System.out.println("ERROR in getArrayFromExecutors()");
        }
        return result;
    }
    @Override
    public void reset() { }
}
