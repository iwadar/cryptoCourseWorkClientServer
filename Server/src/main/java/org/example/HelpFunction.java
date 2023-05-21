package org.example;

import java.util.Random;
import java.util.stream.Collectors;

public class HelpFunction {
    public static byte[] getArray128(byte[] initArray, int startIndex)
    {
        byte[] copy = new byte[16];
        System.arraycopy(initArray, startIndex, copy, 0, 16);
        return copy;
    }

    public static long[] getLongFrom128Byte(byte[] initArray)
    {
        byte[] bla = new byte[8];
        byte[] bla1 = new byte[8];
        System.arraycopy(initArray, 0, bla, 0, 8);
        System.arraycopy(initArray, 8, bla1, 0, 8);
        long[] res = {bytesToLong(bla), bytesToLong(bla1)};
        return res;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static byte[] deletePadding(byte[] input)
    {
        int paddingLength = input[input.length - 1];
        byte[] tmp = new byte[input.length - paddingLength];
        System.arraycopy(input, 0, tmp, 0, tmp.length);
        return tmp;
    }

    public static byte[] XORByteArray(byte[] first, byte[] second)
    {
        if (first.length != second.length)
        {
            System.out.println("ERROR XORByteArray(byte[] first, byte[] second): Array have to be same length");
            return new byte[0];
        }
        byte[] result = new byte[first.length];
        for (int i = 0; i < first.length; i++)
        {
            result[i] = (byte) (first[i] ^ second[i]);
        }
        return result;
    }

    public static byte[] twoLongToOneByteArray(long[] array)
    {
        byte[] result = new byte[array.length * Long.BYTES];
        System.arraycopy(longToBytes(array[0]), 0, result, 0, 8);
        System.arraycopy(longToBytes(array[1]), 0, result, 8, 8);
        return result;
    }

    public static String generateRandomString(int size)
    {
        String symbols = "abcdefghijklmnopqrstuvwxyz123456789";
        return new Random().ints(size, 0, symbols.length())
                .mapToObj(symbols::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static byte[] padding(byte[] notCipherText, int sizeBlock)
    {
        int lengthPadding = sizeBlock - notCipherText.length % sizeBlock;
        byte[] copyInputArrayWithPadding = new byte[notCipherText.length + lengthPadding];
        System.arraycopy(notCipherText, 0, copyInputArrayWithPadding, 0, notCipherText.length);
        for (int i = 0; i < lengthPadding; i++)
        {
            copyInputArrayWithPadding[notCipherText.length + i] = (byte)lengthPadding;
        }
        return copyInputArrayWithPadding;
    }
}
