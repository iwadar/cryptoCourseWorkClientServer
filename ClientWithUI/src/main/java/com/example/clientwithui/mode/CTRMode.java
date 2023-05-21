package com.example.clientwithui.mode;

import com.example.clientwithui.camellia.Camellia;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.clientwithui.HelpFunction.*;


public class CTRMode implements IModeCipher
{
    private Camellia symmetricalAlgorithm;
    private int processors;
    private byte[] initializationVector;
    private int halfLen;
    private int len;
    private int mod;
    private int ctr;

    public CTRMode(Camellia symmetricalAlgorithm, byte[] IV)
    {
        this.processors = Runtime.getRuntime().availableProcessors();
        this.symmetricalAlgorithm = symmetricalAlgorithm;
        this.initializationVector = IV;
        len = IV.length;
        halfLen = len / 2;
        mod = 1 << halfLen;
        ctr = 0;
    }

    @Override
    public byte[] encrypt(byte[] notCipherText)
    {
        ExecutorService service = Executors.newFixedThreadPool(processors);
        List<Future<byte[]>> encryptedBlocksFutures = new LinkedList<>();
        byte[] copyInputArrayWithPadding = padding(notCipherText, 16);

        for (int i = 0; i < copyInputArrayWithPadding.length; i += 16)
        {
            byte[] block = getArray128(copyInputArrayWithPadding, i);
            encryptedBlocksFutures.add(service.submit(() -> {
                int shift = ctr;
                byte[] encCtr = symmetricalAlgorithm.encrypt(shiftHalf(shift));
                for (int j = 0; j < 16; j++)
                {
                    block[j] ^= encCtr[j];
                }
                return block;
            }));
        }
        service.shutdown();
        return getArrayFromExecutors(encryptedBlocksFutures, copyInputArrayWithPadding.length);
    }

    @Override
    public byte[] decrypt(byte[] cipherText)
    {
        ExecutorService service = Executors.newFixedThreadPool(processors);
        List<Future<byte[]>> encryptedBlocksFutures = new LinkedList<>();
        for (int i = 0; i < cipherText.length; i += 16)
        {
            byte[] block = getArray128(cipherText, i);
            encryptedBlocksFutures.add(service.submit(() -> {
                int shift = ctr;
                byte[] encCtr = symmetricalAlgorithm.encrypt(shiftHalf(shift));
                for (int j = 0; j < 16; j++)
                {
                    block[j] ^= encCtr[j];
                }
                return block;
            }));
        }
        service.shutdown();
        cipherText = getArrayFromExecutors(encryptedBlocksFutures, cipherText.length);
        cipherText = deletePadding(cipherText);
        return cipherText;
    }

    private byte[] shiftHalf(int shift)
    {
        BigInteger half = new BigInteger(initializationVector, halfLen, halfLen);
        long longHalf = half.longValue();
        longHalf = (longHalf + shift) % mod;
        ByteBuffer buf = ByteBuffer.allocate(halfLen);
        buf.putLong(longHalf);
        int ind = halfLen;
        byte[] res = Arrays.copyOfRange(initializationVector, 0, len);
        for (var curByte : buf.array())
        {
            res[ind++] = curByte;
        }
        return res;
    }

    @Override
    public void reset()
    {
        ctr = 0;
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
