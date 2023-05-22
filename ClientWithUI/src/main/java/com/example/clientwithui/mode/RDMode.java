package com.example.clientwithui.mode;


import com.example.clientwithui.camellia.Camellia;

import static com.example.clientwithui.HelpFunction.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RDMode implements IModeCipher
{
    private Camellia symmetricalAlgorithm;
    private int processors;
    private byte[] initializationVector;
    BigInteger initial;
    BigInteger delta;

    public RDMode(Camellia symmetricalAlgorithm, byte[] IV)
    {
        this.processors = Runtime.getRuntime().availableProcessors();
        this.symmetricalAlgorithm = symmetricalAlgorithm;
        this.initializationVector = IV;
        this.initial = new BigInteger(IV);
        this.delta = new BigInteger(IV, IV.length / 2, IV.length / 2);
    }

    @Override
    public byte[] encrypt(byte[] notCipherText)
    {
        ExecutorService service = Executors.newFixedThreadPool(processors);
        List<Future<byte[]>> encryptedBlocksFutures = new LinkedList<>();
        byte[] copyInputArrayWithPadding = padding(notCipherText, 16);
        long shift = 1<<16;
        encryptedBlocksFutures.add(service.submit(() -> symmetricalAlgorithm.encrypt(ByteBuffer.allocate(16).put(initial.toByteArray()).array())));

        for (int i = 0; i < copyInputArrayWithPadding.length; i += 16)
        {
            byte[] initArray = initial.toByteArray();
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            byteBuffer.put(initArray);
            initArray = byteBuffer.array();
            for (int j = 0; j < 16; j++)
            {
                copyInputArrayWithPadding[i + j] ^= initArray[j];
            }
            byte[] block = getArray128(copyInputArrayWithPadding, i);
            encryptedBlocksFutures.add(service.submit(() -> symmetricalAlgorithm.encrypt(block)));
            initial = initial.add(delta).mod(BigInteger.valueOf(shift));
        }
        service.shutdown();
        return getArrayFromExecutors(encryptedBlocksFutures, copyInputArrayWithPadding.length);
    }

    @Override
    public byte[] decrypt(byte[] cipherText)
    {
        int index = 0;
        long shift = 1 << 8;
        ExecutorService service = Executors.newFixedThreadPool(processors);
        List<Future<byte[]>> decryptedBlocksFutures = new LinkedList<>();
        for (int i = 16; i < cipherText.length; i += 16) {
            byte[] block = getArray128(cipherText, i);
            decryptedBlocksFutures.add(service.submit(() -> symmetricalAlgorithm.decrypt(block)));
        }
        service.shutdown();

        byte[] resBytes = getArrayFromExecutors(decryptedBlocksFutures, cipherText.length);

        for (int i = 0; i < cipherText.length; i += 16)
        {
            byte[] initArray = initial.toByteArray();
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            byteBuffer.put(initArray);
            initArray = byteBuffer.array();
            for (int j = 0; j < 16; j++) {
                resBytes[index++] ^= initArray[j];
            }
            initial = initial.add(delta).mod(BigInteger.valueOf(shift));
        }
        resBytes = deletePadding(resBytes);
        return resBytes;
    }

    @Override
    public void reset()
    {
        this.initial = new BigInteger(this.initializationVector);
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
}
